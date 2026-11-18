package api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.AvqApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Product;
import domain.Staff;
import domain.StaffRole;
import org.springframework.mock.web.MockMultipartFile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import testsupport.Fixtures;

/** Giai đoạn 5: phân quyền JWT (BR03, BR15), lỗi dạng JSON {code, message}, luồng HTTP đặt hàng + thanh toán giả lập. */
@SpringBootTest(classes = AvqApplication.class, properties = { "app.scheduling.enabled=false", "app.upload-dir=target/test-uploads" })
@AutoConfigureMockMvc
@Transactional
class ApiSecurityTest {
	@Autowired MockMvc mvc;
	@Autowired ObjectMapper json;
	@Autowired EntityManager em;
	@Autowired PasswordEncoder encoder;
	Fixtures fx;

	@BeforeEach
	void setUp() {
		fx = new Fixtures(em);
	}

	private String register() throws Exception {
		return register("api-" + fx.tag() + "@avq.test");
	}

	private String register(String email) throws Exception {
		String body = "{\"fullName\":\"Khách API\",\"email\":\"" + email + "\",\"password\":\"matkhau123\"}";
		String res = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk()).andExpect(jsonPath("$.role").value("CUSTOMER"))
				.andReturn().getResponse().getContentAsString();
		return "Bearer " + json.readTree(res).get("token").asText();
	}

	private String staffToken() throws Exception {
		return staffToken(StaffRole.STAFF);
	}

	private String staffToken(StaffRole role) throws Exception {
		Staff s = fx.staff();
		s.setRole(role);
		s.setPasswordHash(encoder.encode("nhanvien123"));
		em.flush();
		String res = mvc.perform(post("/api/auth/staff/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"identifier\":\"" + s.getUsername() + "\",\"password\":\"nhanvien123\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.role").value(role.name()))
				.andReturn().getResponse().getContentAsString();
		return "Bearer " + json.readTree(res).get("token").asText();
	}

	@Test
	void br03_xemSanPhamKhongCanDangNhap_gioHangThiCan() throws Exception {
		mvc.perform(get("/api/products")).andExpect(status().isOk());
		mvc.perform(get("/api/categories")).andExpect(status().isOk());
		mvc.perform(get("/api/cart"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
				.andExpect(jsonPath("$.message").value("Vui lòng đăng nhập để tiếp tục"));
	}

	@Test
	void br15_tachQuyenKhachVaNhanVien() throws Exception {
		String customer = register();
		String staff = staffToken();
		mvc.perform(get("/api/cart").header("Authorization", customer)).andExpect(status().isOk());
		mvc.perform(get("/api/admin/orders").header("Authorization", customer))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		mvc.perform(get("/api/admin/orders").header("Authorization", staff)).andExpect(status().isOk());
		mvc.perform(get("/api/cart").header("Authorization", staff)).andExpect(status().isForbidden());
		mvc.perform(get("/api/cart").header("Authorization", "Bearer token-gia")).andExpect(status().isUnauthorized());
	}

	@Test
	void loiTraVeJson_codeVaMessage() throws Exception {
		mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fullName\":\"A\",\"email\":\"x@y.z\",\"password\":\"123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value("Mật khẩu tối thiểu 8 ký tự"));
		String customer = register();
		mvc.perform(post("/api/cart/items").header("Authorization", customer).param("qty", "1"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VARIANT_REQUIRED"));
		mvc.perform(get("/api/products/999999999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
	}

	@Test
	void luongHttp_datHangOnline_thanhToanGiaLap_nhanVienGiaoHang() throws Exception {
		Product p = fx.product(fx.category(), "Áo API", 250_000, 5);
		em.flush();
		String customer = register();
		String staff = staffToken();
		Long variantId = fx.variant(p).getId();

		mvc.perform(post("/api/cart/items").header("Authorization", customer)
						.param("variantId", variantId.toString()).param("qty", "2"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.totalQuantity").value(2));
		mvc.perform(get("/api/orders/checkout").header("Authorization", customer))
				.andExpect(status().isOk()).andExpect(jsonPath("$.shippingFee").value(0)).andExpect(jsonPath("$.total").value(500000));

		String placed = mvc.perform(post("/api/orders").header("Authorization", customer).contentType(MediaType.APPLICATION_JSON)
						.content("{\"receiverName\":\"Lê C\",\"receiverPhone\":\"0987654321\",\"shippingAddress\":\"Đà Nẵng\",\"paymentMethod\":\"ONLINE\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CHO_THANH_TOAN"))
				.andReturn().getResponse().getContentAsString();
		JsonNode order = json.readTree(placed);
		String code = order.get("orderCode").asText();

		mvc.perform(post("/api/payments/mock/" + code).header("Authorization", customer).param("success", "true"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.RspCode").value("00"));
		mvc.perform(get("/api/orders/" + code).header("Authorization", customer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CHO_XAC_NHAN"))
				.andExpect(jsonPath("$.paymentStatus").value("THANH_CONG"));

		mvc.perform(put("/api/admin/orders/" + code + "/status").header("Authorization", staff).param("to", "DANG_GIAO"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DANG_GIAO"))
				.andExpect(jsonPath("$.trackingCode").exists());
		mvc.perform(post("/api/orders/" + code + "/cancel").header("Authorization", customer))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CANCEL_NOT_ALLOWED"));
	}

	/** BR15: khách không giả lập thanh toán được đơn của người khác; đơn không còn chờ thanh toán thì bị từ chối. */
	@Test
	void thanhToanGiaLap_chiChuDon_vaChiDonChoThanhToan() throws Exception {
		Product p = fx.product(fx.category(), "Áo chủ đơn", 250_000, 5);
		em.flush();
		String owner = register();
		String other = register("khac-" + fx.tag() + "@avq.test");
		Long variantId = fx.variant(p).getId();
		mvc.perform(post("/api/cart/items").header("Authorization", owner)
						.param("variantId", variantId.toString()).param("qty", "1"))
				.andExpect(status().isOk());
		String placed = mvc.perform(post("/api/orders").header("Authorization", owner).contentType(MediaType.APPLICATION_JSON)
						.content("{\"receiverName\":\"Lê C\",\"receiverPhone\":\"0987654321\",\"shippingAddress\":\"Huế\",\"paymentMethod\":\"ONLINE\"}"))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String code = json.readTree(placed).get("orderCode").asText();

		mvc.perform(post("/api/payments/mock/" + code).header("Authorization", other).param("success", "true"))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
		mvc.perform(get("/api/orders/" + code).header("Authorization", owner))
				.andExpect(jsonPath("$.status").value("CHO_THANH_TOAN"));

		mvc.perform(post("/api/orders/" + code + "/cancel").header("Authorization", owner)).andExpect(status().isOk());
		mvc.perform(post("/api/payments/mock/" + code).header("Authorization", owner).param("success", "true"))
				.andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ORDER_NOT_PENDING"));
	}

	/** Bổ sung 17/11: quản lý nhân viên chỉ dành cho chủ cửa hàng (BR02). */
	@Test
	void quanLyNhanVien_chiChuCuaHang() throws Exception {
		String staff = staffToken();
		String owner = staffToken(StaffRole.OWNER);
		mvc.perform(get("/api/admin/staff").header("Authorization", staff))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		mvc.perform(get("/api/admin/staff").header("Authorization", owner)).andExpect(status().isOk());
		mvc.perform(post("/api/admin/staff").header("Authorization", owner).contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"nvapi" + fx.tag() + "\",\"fullName\":\"NV API\",\"password\":\"matkhau123\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.role").value("STAFF"));
		mvc.perform(post("/api/admin/staff").header("Authorization", owner).contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"Có Dấu\",\"fullName\":\"x\",\"password\":\"123\"}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		// chủ cửa hàng vẫn có mọi quyền nhân viên
		mvc.perform(get("/api/admin/orders").header("Authorization", owner)).andExpect(status().isOk());
	}

	/** Bổ sung 17/11: tải ảnh sản phẩm – nhân viên được, khách không; file giả dạng ảnh bị từ chối. */
	@Test
	void taiAnhSanPham() throws Exception {
		byte[] png = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };
		String staff = staffToken();
		String res = mvc.perform(multipart("/api/admin/products/images").file(new MockMultipartFile("file", "a.png", "image/png", png))
						.header("Authorization", staff))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		org.assertj.core.api.Assertions.assertThat(res).startsWith("/uploads/").endsWith(".png");
		mvc.perform(get(res)).andExpect(status().isOk()).andExpect(content().bytes(png));
		mvc.perform(multipart("/api/admin/products/images").file(new MockMultipartFile("file", "x.png", "image/png", "hello".getBytes()))
						.header("Authorization", staff))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_IMAGE"));
		mvc.perform(multipart("/api/admin/products/images").file(new MockMultipartFile("file", "a.png", "image/png", png))
						.header("Authorization", register()))
				.andExpect(status().isForbidden());
	}
}
