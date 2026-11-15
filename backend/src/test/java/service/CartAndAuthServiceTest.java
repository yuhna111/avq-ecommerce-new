package service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import api.dto.CartDto;
import api.dto.RegisterRequest;
import api.dto.TokenDto;
import domain.Customer;
import domain.Product;
import domain.ProductVariant;
import infra.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** DT-02 (giỏ hàng) và BR01/BR16 (tài khoản) trên CSDL thật. */
class CartAndAuthServiceTest extends ServiceTestBase {
	@Autowired CartService cartService;
	@Autowired AuthService authService;
	@Autowired JwtTokenProvider jwt;

	// ---------- DT-02 ----------
	@Test
	void dt02_R2_chuaChonBienThe() {
		Customer c = fx.customer();
		assertThatThrownBy(() -> cartService.addToCart(c.getId(), null, 1))
				.hasFieldOrPropertyWithValue("code", "VARIANT_REQUIRED")
				.hasMessageContaining("chọn size và màu");
	}

	@Test
	void dt02_R3_vuotTonKhoHoacQua10() {
		Customer c = fx.customer();
		Product few = fx.product(fx.category(), "Áo còn 3", 200_000, 3);
		Product many = fx.product(fx.category(), "Áo còn 50", 200_000, 50);
		em.flush();
		assertThatThrownBy(() -> cartService.addToCart(c.getId(), fx.variant(few).getId(), 4))
				.hasFieldOrPropertyWithValue("code", "OUT_OF_STOCK")
				.hasMessageContaining("Chỉ còn 3 sản phẩm size M màu Đen");
		assertThatThrownBy(() -> cartService.addToCart(c.getId(), fx.variant(many).getId(), 11))
				.hasFieldOrPropertyWithValue("code", "QTY_LIMIT");
	}

	@Test
	void dt02_R4_themDongMoi_R5_congDon_vaR3KhiCongDonVuotTon() {
		Customer c = fx.customer();
		Product p = fx.product(fx.category(), "Áo cộng dồn", 150_000, 5);
		ProductVariant v = fx.variant(p);
		em.flush();

		CartDto r4 = cartService.addToCart(c.getId(), v.getId(), 2);
		assertThat(r4.items()).hasSize(1);
		assertThat(r4.items().get(0).quantity()).isEqualTo(2);

		CartDto r5 = cartService.addToCart(c.getId(), v.getId(), 3);
		assertThat(r5.items()).hasSize(1); // BR07: vẫn một dòng
		assertThat(r5.items().get(0).quantity()).isEqualTo(5);
		assertThat(r5.subtotal()).isEqualByComparingTo("750000");

		assertThatThrownBy(() -> cartService.addToCart(c.getId(), v.getId(), 1))
				.hasFieldOrPropertyWithValue("code", "OUT_OF_STOCK")
				.hasMessageContaining("trong giỏ đã có 5");
	}

	@Test
	void doiSoLuongVaXoaDong() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo sửa SL", 100_000, 8));
		em.flush();
		cartService.addToCart(c.getId(), v.getId(), 1);
		assertThat(cartService.changeQty(c.getId(), v.getId(), 7).items().get(0).quantity()).isEqualTo(7);
		assertThatThrownBy(() -> cartService.changeQty(c.getId(), v.getId(), 9)).hasFieldOrPropertyWithValue("code", "OUT_OF_STOCK");
		assertThat(cartService.removeItem(c.getId(), v.getId()).items()).isEmpty();
	}

	// ---------- BR01 / BR16 ----------
	@Test
	void br01_dangKyBangEmailHoacSdt_khongTrung() {
		String email = "dk-" + fx.tag() + "@avq.test";
		TokenDto t = authService.register(new RegisterRequest("Nguyễn Văn A", email, null, "matkhau123"));
		assertThat(jwt.parse(t.token()).role()).isEqualTo("CUSTOMER");
		assertThat(t.expiresInMinutes()).isEqualTo(30);

		assertThatThrownBy(() -> authService.register(new RegisterRequest("B", email.toUpperCase(), null, "matkhau123")))
				.hasFieldOrPropertyWithValue("code", "ACCOUNT_EXISTS")
				.hasMessage("Email/SĐT đã được đăng ký, vui lòng đăng nhập");
		assertThatThrownBy(() -> authService.register(new RegisterRequest("C", null, null, "matkhau123")))
				.hasFieldOrPropertyWithValue("code", "IDENTIFIER_REQUIRED");
	}

	@Test
	void br16_dangNhapSai5Lan_khoa_dungMatKhauVanBiChan() {
		String phone = "0977" + fx.tag().replaceAll("[^0-9]", "1").substring(0, 6);
		authService.register(new RegisterRequest("Khách khóa", null, phone, "dungmatkhau"));
		for (int i = 1; i <= 4; i++) {
			assertThatThrownBy(() -> authService.loginCustomer(phone, "saimatkhau"))
					.hasFieldOrPropertyWithValue("code", "BAD_CREDENTIALS")
					.hasMessage("Email/SĐT hoặc mật khẩu không đúng");
		}
		assertThatThrownBy(() -> authService.loginCustomer(phone, "saimatkhau"))
				.hasFieldOrPropertyWithValue("code", "ACCOUNT_LOCKED");
		assertThatThrownBy(() -> authService.loginCustomer(phone, "dungmatkhau"))
				.hasFieldOrPropertyWithValue("code", "ACCOUNT_LOCKED");
	}

	@Test
	void br16_dangNhapDung_resetBoDem() {
		String email = "rs-" + fx.tag() + "@avq.test";
		authService.register(new RegisterRequest("Khách reset", email, null, "dungmatkhau"));
		for (int i = 0; i < 3; i++) {
			assertThatThrownBy(() -> authService.loginCustomer(email, "sai")).isInstanceOf(BusinessException.class);
		}
		assertThat(authService.loginCustomer(email, "dungmatkhau").token()).isNotBlank();
		flushClear();
		assertThat(em.createQuery("select c.failedLoginCount from Customer c where c.email = :e", Integer.class)
				.setParameter("e", email).getSingleResult()).isZero();
	}
}
