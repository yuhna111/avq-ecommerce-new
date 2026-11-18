package service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import api.dto.OrderDetailDto;
import api.dto.OrderResultDto;
import api.dto.PlaceOrderRequest;
import domain.Customer;
import domain.Order;
import domain.OrderStatus;
import domain.OrderStatusHistory;
import domain.Payment;
import domain.PaymentMethod;
import domain.PaymentStatus;
import domain.Product;
import domain.ProductVariant;
import domain.Staff;
import infra.MockPaymentGatewayClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import repository.OrderRepository;
import repository.OrderStatusHistoryRepository;
import repository.PaymentRepository;

/** BR08–BR14, DT-03/DT-04, DT-05, BR12 trên CSDL thật (mỗi test rollback). */
class OrderAndPaymentServiceTest extends ServiceTestBase {
	@Autowired OrderService orderService;
	@Autowired CartService cartService;
	@Autowired PaymentService paymentService;
	@Autowired MockPaymentGatewayClient mockGateway;
	@Autowired OrderRepository orderRepository;
	@Autowired PaymentRepository paymentRepository;
	@Autowired OrderStatusHistoryRepository historyRepository;

	private PlaceOrderRequest req(PaymentMethod m) {
		return new PlaceOrderRequest("Trần Thị B", "0912345678", "12 Nguyễn Trãi, Hà Nội", m);
	}

	private int stock(ProductVariant v) {
		em.flush();
		return em.createQuery("select v.stockQty from ProductVariant v where v.id = :id", Integer.class)
				.setParameter("id", v.getId()).getSingleResult();
	}

	@Test
	void br10_cod_choXacNhan_truKho_chotGia_lichSuTaoDon_xoaGio() {
		Customer c = fx.customer();
		Product p = fx.product(fx.category(), "Áo COD", 200_000, 10);
		ProductVariant v = fx.variant(p);
		em.flush();
		cartService.addToCart(c.getId(), v.getId(), 2);

		OrderResultDto r = orderService.place(c.getId(), req(PaymentMethod.COD));

		assertThat(r.status()).isEqualTo(OrderStatus.CHO_XAC_NHAN);
		assertThat(r.paymentUrl()).isNull();
		assertThat(r.total()).isEqualByComparingTo("430000"); // 400.000 + 30.000 (DT-01)
		assertThat(stock(v)).isEqualTo(8); // BR11
		assertThat(cartService.getCart(c.getId()).items()).isEmpty();

		Order o = orderRepository.findByOrderCode(r.orderCode()).orElseThrow();
		assertThat(o.getItems().get(0).getUnitPrice()).isEqualByComparingTo("200000"); // BR08
		assertThat(o.getItems().get(0).getProductName()).isEqualTo("Áo COD");
		List<OrderStatusHistory> hs = historyRepository.findByOrderIdOrderByChangedAtAsc(o.getId());
		assertThat(hs).hasSize(1);
		assertThat(hs.get(0).getFromStatus()).isNull();
		assertThat(hs.get(0).getToStatus()).isEqualTo(OrderStatus.CHO_XAC_NHAN);
		assertThat(hs.get(0).getNote()).isEqualTo("Tạo đơn");
	}

	@Test
	void br08_doiGiaSauKhiDat_donGiuGiaCu() {
		Customer c = fx.customer();
		Product p = fx.product(fx.category(), "Áo đổi giá", 300_000, 5);
		em.flush();
		cartService.addToCart(c.getId(), fx.variant(p).getId(), 2);
		OrderResultDto r = orderService.place(c.getId(), req(PaymentMethod.COD));
		p.updateInfo(BigDecimal.valueOf(999_000), null, null);
		flushClear();
		Order o = orderRepository.findByOrderCode(r.orderCode()).orElseThrow();
		assertThat(o.getItems().get(0).getUnitPrice()).isEqualByComparingTo("300000");
		assertThat(o.getShippingFee()).isZero(); // 600.000 >= 500.000
		assertThat(o.getTotal()).isEqualByComparingTo("600000");
	}

	@Test
	void br10_online_choThanhToan_han15Phut_taoPaymentDangXuLy() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo online", 520_000, 3));
		em.flush();
		cartService.addToCart(c.getId(), v.getId(), 1);
		LocalDateTime before = LocalDateTime.now();

		OrderResultDto r = orderService.place(c.getId(), req(PaymentMethod.ONLINE));

		assertThat(r.status()).isEqualTo(OrderStatus.CHO_THANH_TOAN);
		assertThat(r.paymentUrl()).startsWith("/mock-pay?").contains("vnp_SecureHash=");
		assertThat(r.expiresAt()).isBetween(before.plusMinutes(15).minusSeconds(5), before.plusMinutes(15).plusSeconds(5));
		Order o = orderRepository.findByOrderCode(r.orderCode()).orElseThrow();
		Payment pay = paymentRepository.findTopByOrderIdOrderByIdDesc(o.getId()).orElseThrow();
		assertThat(pay.getStatus()).isEqualTo(PaymentStatus.DANG_XU_LY);
		assertThat(pay.getAmount()).isEqualByComparingTo("520000");
	}

	@Test
	void ex06_hetHangKhiDat_khongTaoDon_gioGiuNguyen() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo sắp hết", 100_000, 2));
		em.flush();
		cartService.addToCart(c.getId(), v.getId(), 2);
		// Người khác mua mất 1 chiếc sau khi khách đã bỏ vào giỏ
		em.createNativeQuery("UPDATE product_variants SET stock_qty = 1 WHERE variant_id = " + v.getId()).executeUpdate();
		assertThatThrownBy(() -> orderService.place(c.getId(), req(PaymentMethod.COD)))
				.hasFieldOrPropertyWithValue("code", "OUT_OF_STOCK")
				.hasMessageContaining("Áo sắp hết");
	}

	@Test
	void br13_khachHuy_hoanKho_vaKhongHuyDuocKhiDangGiao() {
		Customer c = fx.customer();
		Staff s = fx.staff();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo hủy", 100_000, 5));
		em.flush();
		cartService.addToCart(c.getId(), v.getId(), 3);
		String code1 = orderService.place(c.getId(), req(PaymentMethod.COD)).orderCode();
		assertThat(stock(v)).isEqualTo(2);

		OrderDetailDto d = orderService.cancelByCustomer(c.getId(), code1, "Đổi ý");
		assertThat(d.status()).isEqualTo(OrderStatus.DA_HUY);
		assertThat(stock(v)).isEqualTo(5); // hoàn kho
		assertThat(d.histories()).extracting("note").contains("Khách hủy đơn: Đổi ý");

		cartService.addToCart(c.getId(), v.getId(), 1);
		String code2 = orderService.place(c.getId(), req(PaymentMethod.COD)).orderCode();
		orderService.updateStatusByStaff(s.getId(), code2, OrderStatus.DANG_GIAO, null);
		assertThatThrownBy(() -> orderService.cancelByCustomer(c.getId(), code2, null))
				.hasFieldOrPropertyWithValue("code", "CANCEL_NOT_ALLOWED");
	}

	@Test
	void dt03_nhanVienHuyPhaiCoLyDo_dt04_dangGiaoGhiMaVanDon() {
		Customer c = fx.customer();
		Staff s = fx.staff();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo NV", 100_000, 5));
		em.flush();
		cartService.addToCart(c.getId(), v.getId(), 1);
		String code = orderService.place(c.getId(), req(PaymentMethod.COD)).orderCode();

		assertThatThrownBy(() -> orderService.updateStatusByStaff(s.getId(), code, OrderStatus.DA_HUY, " "))
				.hasFieldOrPropertyWithValue("code", "REASON_REQUIRED");
		assertThatThrownBy(() -> orderService.updateStatusByStaff(s.getId(), code, OrderStatus.DA_GIAO, null))
				.hasFieldOrPropertyWithValue("code", "INVALID_TRANSITION");

		OrderDetailDto shipping = orderService.updateStatusByStaff(s.getId(), code, OrderStatus.DANG_GIAO, "Giao GHN");
		assertThat(shipping.trackingCode()).startsWith("VD");
		assertThat(shipping.nextStatuses()).containsExactly(OrderStatus.DA_GIAO);
		OrderDetailDto done = orderService.updateStatusByStaff(s.getId(), code, OrderStatus.DA_GIAO, null);
		assertThat(done.histories()).extracting("toStatus")
				.containsExactly(OrderStatus.CHO_XAC_NHAN, OrderStatus.DANG_GIAO, OrderStatus.DA_GIAO);
		assertThat(done.histories().get(1).changedBy()).contains(s.getFullName());
	}

	@Test
	void br12_donQuaHan_tuHuyVaHoanKho_donConHanGiuNguyen() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo quá hạn", 100_000, 5));
		em.flush();
		cartService.addToCart(c.getId(), v.getId(), 2);
		String code = orderService.place(c.getId(), req(PaymentMethod.ONLINE)).orderCode();
		assertThat(stock(v)).isEqualTo(3);

		assertThat(orderService.expireOverdue(LocalDateTime.now().plusMinutes(14))).isZero();
		int n = orderService.expireOverdue(LocalDateTime.now().plusMinutes(16));
		assertThat(n).isGreaterThanOrEqualTo(1);
		assertThat(orderRepository.findByOrderCode(code).orElseThrow().getStatus()).isEqualTo(OrderStatus.DA_HUY);
		assertThat(stock(v)).isEqualTo(5);
	}

	// ---------- DT-05 ----------

	private String placeOnline(Customer c, ProductVariant v) {
		cartService.addToCart(c.getId(), v.getId(), 1);
		return orderService.place(c.getId(), req(PaymentMethod.ONLINE)).orderCode();
	}

	private String callback(Map<String, String> params) {
		try {
			paymentService.handleCallback(params);
			return "00";
		} catch (PaymentCallbackException e) {
			return e.getRspCode();
		}
	}

	@Test
	void dt05_thanhCong_00_donSangChoXacNhan_vaGuiLai_02() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo TT", 250_000, 5));
		em.flush();
		String code = placeOnline(c, v);
		Map<String, String> ok = mockGateway.simulateCallback(code, true);

		assertThat(callback(ok)).isEqualTo("00");
		Order o = orderRepository.findByOrderCode(code).orElseThrow();
		assertThat(o.getStatus()).isEqualTo(OrderStatus.CHO_XAC_NHAN);
		Payment pay = paymentRepository.findTopByOrderIdOrderByIdDesc(o.getId()).orElseThrow();
		assertThat(pay.getStatus()).isEqualTo(PaymentStatus.THANH_CONG);
		assertThat(pay.getTxnRef()).isEqualTo(ok.get("vnp_TransactionNo"));
		assertThat(pay.getPaidAt()).isNotNull();

		assertThat(callback(ok)).isEqualTo("02"); // cổng gửi lại -> bỏ qua
	}

	@Test
	void dt05_thatBai_00_donGiuChoThanhToan() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo TB", 250_000, 5));
		em.flush();
		String code = placeOnline(c, v);
		assertThat(callback(mockGateway.simulateCallback(code, false))).isEqualTo("00");
		Order o = orderRepository.findByOrderCode(code).orElseThrow();
		assertThat(o.getStatus()).isEqualTo(OrderStatus.CHO_THANH_TOAN);
		assertThat(paymentRepository.findTopByOrderIdOrderByIdDesc(o.getId()).orElseThrow().getStatus())
				.isEqualTo(PaymentStatus.THAT_BAI);
	}

	@Test
	void dt05_saiChuKy_97() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo CK", 250_000, 5));
		em.flush();
		String code = placeOnline(c, v);
		Map<String, String> forged = new HashMap<String, String>(mockGateway.simulateCallback(code, false));
		forged.put("vnp_ResponseCode", "00"); // sửa kết quả sau khi ký
		assertThat(callback(forged)).isEqualTo("97");
		assertThat(orderRepository.findByOrderCode(code).orElseThrow().getStatus()).isEqualTo(OrderStatus.CHO_THANH_TOAN);
	}

	@Test
	void dt05_khongCoDon_01() {
		Order ghost = new Order();
		ghost.setOrderCode("KHONGCO" + fx.tag());
		ghost.setShippingFee(BigDecimal.valueOf(30_000));
		ghost.recalculate();
		mockGateway.buildPaymentUrl(ghost, ghost.getOrderCode());
		assertThat(callback(mockGateway.simulateCallback(ghost.getOrderCode(), true))).isEqualTo("01");
	}

	@Test
	void dt05_soTienLech_04_ghiSaiLech_donGiuNguyen() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo lệch", 250_000, 5));
		em.flush();
		String code = placeOnline(c, v);
		// Cổng phát hành link với số tiền khác tổng đơn (vẫn ký hợp lệ)
		Order wrong = new Order();
		wrong.setOrderCode(code);
		wrong.setShippingFee(BigDecimal.valueOf(1_000));
		wrong.recalculate();
		mockGateway.buildPaymentUrl(wrong, code);

		assertThat(callback(mockGateway.simulateCallback(code, true))).isEqualTo("04");
		Order o = orderRepository.findByOrderCode(code).orElseThrow();
		assertThat(o.getStatus()).isEqualTo(OrderStatus.CHO_THANH_TOAN);
		assertThat(paymentRepository.findTopByOrderIdOrderByIdDesc(o.getId()).orElseThrow().getStatus())
				.isEqualTo(PaymentStatus.SAI_LECH);
	}

	/** Thanh toán thành công đến SAU khi đơn đã tự hủy (BR12): không hồi sinh đơn, ghi "Sai lệch" để hoàn tiền, mã 02. */
	@Test
	void dt05_thanhCongSauKhiDonDaHuy_02_ghiSaiLech_donVanHuy() {
		Customer c = fx.customer();
		ProductVariant v = fx.variant(fx.product(fx.category(), "Áo trễ hạn", 250_000, 5));
		em.flush();
		String code = placeOnline(c, v);
		Map<String, String> lateIpn = mockGateway.simulateCallback(code, true);
		orderService.expireOverdue(LocalDateTime.now().plusMinutes(16));
		assertThat(stock(v)).isEqualTo(5);

		assertThat(callback(lateIpn)).isEqualTo("02");
		Order o = orderRepository.findByOrderCode(code).orElseThrow();
		assertThat(o.getStatus()).isEqualTo(OrderStatus.DA_HUY);
		Payment pay = paymentRepository.findTopByOrderIdOrderByIdDesc(o.getId()).orElseThrow();
		assertThat(pay.getStatus()).isEqualTo(PaymentStatus.SAI_LECH);
		assertThat(pay.getTxnRef()).isEqualTo(lateIpn.get("vnp_TransactionNo"));
		assertThat(stock(v)).isEqualTo(5); // kho không bị trừ lại
		assertThat(callback(lateIpn)).isEqualTo("02"); // cổng gửi lại -> bỏ qua
	}
}
