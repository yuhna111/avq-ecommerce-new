package repository;

import static org.assertj.core.api.Assertions.assertThat;

import app.AvqApplication;
import domain.Category;
import domain.Customer;
import domain.Order;
import domain.OrderStatus;
import domain.Payment;
import domain.PaymentMethod;
import domain.PaymentStatus;
import domain.Product;
import domain.ProductVariant;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import testsupport.Fixtures;

/**
 * Giai đoạn 3: kiểm thử các truy vấn chính trên CSDL thật avq_db_vp (ddl-auto=validate).
 * Mỗi test chạy trong giao dịch và được rollback, không để lại dữ liệu.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = AvqApplication.class)
class RepositoryQueryTest {

	@Autowired EntityManager em;
	@Autowired CustomerRepository customerRepository;
	@Autowired ProductVariantRepository variantRepository;
	@Autowired ProductRepository productRepository;
	@Autowired OrderRepository orderRepository;
	@Autowired PaymentRepository paymentRepository;
	@Autowired SizeChartRepository sizeChartRepository;
	@Autowired CategoryRepository categoryRepository;

	Fixtures fx;

	@BeforeEach
	void setUp() {
		fx = new Fixtures(em);
	}

	@Test
	void findByEmailOrPhone_timTheoEmailHoacSoDienThoai() {
		Customer c = fx.customer();
		em.flush();
		assertThat(customerRepository.findByEmailOrPhone(c.getEmail())).contains(c);
		assertThat(customerRepository.findByEmailOrPhone(c.getPhone())).contains(c);
		assertThat(customerRepository.findByEmailOrPhone("khong-ton-tai@x.y")).isEmpty();
		assertThat(customerRepository.existsByEmail(c.getEmail())).isTrue();
		assertThat(customerRepository.existsByPhone(c.getPhone())).isTrue();
	}

	@Test
	void decreaseIfEnough_chiTruKhiDuHang_BR11() {
		Product p = fx.product(fx.category(), "Áo test", 200_000, 5);
		ProductVariant v = fx.variant(p);
		em.flush();
		long v0 = v.getVersion();

		assertThat(variantRepository.decreaseIfEnough(v.getId(), 3)).isEqualTo(1);
		assertThat(variantRepository.decreaseIfEnough(v.getId(), 3)).isZero(); // còn 2, không đủ 3
		em.clear();
		ProductVariant after = variantRepository.findById(v.getId()).orElseThrow();
		assertThat(after.getStockQty()).isEqualTo(2);
		assertThat(after.getVersion()).isEqualTo(v0 + 1);

		assertThat(variantRepository.increase(v.getId(), 4)).isEqualTo(1);
		em.clear();
		assertThat(variantRepository.findById(v.getId()).orElseThrow().getStockQty()).isEqualTo(6);
	}

	@Test
	void search_locTheoTuKhoaDanhMucSizeVaAnSanPhamAn() {
		Category c = fx.category();
		Product shown = fx.product(c, "Áo thun " + fx.tag(), 150_000, 3);
		Product hidden = fx.product(c, "Áo ẩn " + fx.tag(), 150_000, 3);
		hidden.setVisible(false);
		em.flush();

		ProductFilter f = new ProductFilter();
		f.setCategoryId(c.getId());
		assertThat(productRepository.search(f)).containsExactly(shown);

		f.setIncludeHidden(true);
		assertThat(productRepository.search(f)).containsExactlyInAnyOrder(shown, hidden);

		ProductFilter kw = new ProductFilter();
		kw.setKeyword("thun " + fx.tag());
		assertThat(productRepository.search(kw)).containsExactly(shown);

		ProductFilter size = new ProductFilter();
		size.setCategoryId(c.getId());
		size.setSize("XXL");
		assertThat(productRepository.search(size)).isEmpty();
		size.setSize("M");
		size.setMaxPrice(BigDecimal.valueOf(100_000));
		assertThat(productRepository.search(size)).isEmpty();

		assertThat(productRepository.findByIdAndVisibleTrue(hidden.getId())).isEmpty();
		assertThat(productRepository.findByIdAndVisibleTrue(shown.getId())).contains(shown);
	}

	@Test
	void orderQueries_theoKhachTheoMaVaDonQuaHan() {
		Customer cus = fx.customer();
		Product p = fx.product(fx.category(), "Quần test", 300_000, 10);
		LocalDateTime now = LocalDateTime.now().withNano(0);
		Order overdue = newOrder(cus, p, "T" + fx.tag() + "A", OrderStatus.CHO_THANH_TOAN, now.minusMinutes(30), now.minusMinutes(15));
		Order fresh = newOrder(cus, p, "T" + fx.tag() + "B", OrderStatus.CHO_THANH_TOAN, now, now.plusMinutes(15));
		em.flush();

		assertThat(orderRepository.findByOrderCode(fresh.getOrderCode())).contains(fresh);
		assertThat(orderRepository.findByCustomerIdOrderByOrderDateDesc(cus.getId())).containsExactly(fresh, overdue);
		List<Order> expired = orderRepository.findByStatusAndExpiresAtBefore(OrderStatus.CHO_THANH_TOAN, now);
		assertThat(expired).contains(overdue).doesNotContain(fresh);
	}

	@Test
	void paymentQueries_chongTrungTxnRefVaLayBanGhiMoiNhat_DBR6() {
		Customer cus = fx.customer();
		Product p = fx.product(fx.category(), "Váy test", 400_000, 10);
		Order o = newOrder(cus, p, "T" + fx.tag() + "P", OrderStatus.CHO_THANH_TOAN, LocalDateTime.now(), null);
		Payment first = newPayment(o);
		first.markResult(false, "TXN-" + fx.tag() + "-1");
		Payment second = newPayment(o);
		em.flush();

		assertThat(paymentRepository.existsByTxnRef("TXN-" + fx.tag() + "-1")).isTrue();
		assertThat(paymentRepository.existsByTxnRef("TXN-" + fx.tag() + "-2")).isFalse();
		assertThat(paymentRepository.findTopByOrderIdOrderByIdDesc(o.getId())).contains(second);
	}

	@Test
	void sizeChart_vaCategory_theoSapXepVaSlug() {
		Category c = fx.category();
		Product p = fx.product(c, "Áo size test", 200_000, 1);
		fx.sizeChart(p, "L", 170, 180, 65, 75);
		fx.sizeChart(p, "S", 150, 160, 45, 55);
		fx.sizeChart(p, "M", 160, 170, 55, 65);
		em.flush();
		assertThat(sizeChartRepository.findByProductIdOrderByHeightMinAsc(p.getId()))
				.extracting("size").containsExactly("S", "M", "L");
		assertThat(categoryRepository.findBySlug(c.getSlug())).contains(c);
	}

	private Order newOrder(Customer cus, Product p, String code, OrderStatus st, LocalDateTime date, LocalDateTime expires) {
		Order o = new Order();
		o.setCustomer(cus);
		o.setOrderCode(code);
		o.setOrderDate(date);
		o.setReceiverName("Người nhận");
		o.setReceiverPhone("0900000000");
		o.setShippingAddress("Hà Nội");
		o.setPaymentMethod(PaymentMethod.ONLINE);
		o.setStatus(st);
		o.setExpiresAt(expires);
		o.addItem(fx.variant(p), 1);
		o.setShippingFee(BigDecimal.valueOf(30_000));
		o.recalculate();
		em.persist(o);
		return o;
	}

	private Payment newPayment(Order o) {
		Payment pay = new Payment();
		pay.setOrder(o);
		pay.setMethod("MOCK");
		pay.setAmount(o.getTotal());
		pay.setStatus(PaymentStatus.DANG_XU_LY);
		em.persist(pay);
		return pay;
	}
}
