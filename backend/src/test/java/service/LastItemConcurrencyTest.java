package service;

import static org.assertj.core.api.Assertions.assertThat;

import api.dto.PlaceOrderRequest;
import app.AvqApplication;
import domain.Customer;
import domain.PaymentMethod;
import domain.Product;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import testsupport.Fixtures;

/**
 * BR11: 10 khách cùng lúc đặt chiếc cuối cùng -> đúng 1 đơn thành công, tồn kho về 0, không âm.
 * Dữ liệu phải được commit thật (các luồng dùng giao dịch riêng) nên test tự dọn dẹp ở @AfterEach.
 */
@SpringBootTest(classes = AvqApplication.class, properties = "app.scheduling.enabled=false")
class LastItemConcurrencyTest {
	private static final int CUSTOMERS = 10;

	@Autowired EntityManager em;
	@Autowired TransactionTemplate tx;
	@Autowired JdbcTemplate jdbc;
	@Autowired CartService cartService;
	@Autowired OrderService orderService;

	private Long productId;
	private Long categoryId;
	private final List<Long> customerIds = new ArrayList<Long>();

	@Test
	void muoiKhachMuaChiecCuoiCung_chiMotNguoiThanhCong() throws Exception {
		Long variantId = tx.execute(s -> {
			Fixtures fx = new Fixtures(em);
			Product p = fx.product(fx.category(), "Áo chiếc cuối", 199_000, 1);
			productId = p.getId();
			categoryId = p.getCategory().getId();
			for (int i = 0; i < CUSTOMERS; i++) {
				Customer c = fx.customer();
				customerIds.add(c.getId());
			}
			return fx.variant(p).getId();
		});
		for (Long cid : customerIds) {
			cartService.addToCart(cid, variantId, 1); // mỗi khách đều thấy còn 1 chiếc
		}

		ExecutorService pool = Executors.newFixedThreadPool(CUSTOMERS);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<String>> results = new ArrayList<Future<String>>();
		for (Long cid : customerIds) {
			Callable<String> task = () -> {
				start.await();
				try {
					orderService.place(cid, new PlaceOrderRequest("Khách " + cid, "0900000000", "Hà Nội", PaymentMethod.COD));
					return "OK";
				} catch (BusinessException e) {
					return e.getCode();
				}
			};
			results.add(pool.submit(task));
		}
		start.countDown();
		List<String> outcomes = new ArrayList<String>();
		for (Future<String> f : results) {
			outcomes.add(f.get());
		}
		pool.shutdown();

		assertThat(outcomes).filteredOn("OK"::equals).hasSize(1);
		assertThat(outcomes).filteredOn("OUT_OF_STOCK"::equals).hasSize(CUSTOMERS - 1);
		assertThat(jdbc.queryForObject("SELECT stock_qty FROM product_variants WHERE variant_id = ?", Integer.class, variantId)).isZero();
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM order_items WHERE variant_id = ?", Integer.class, variantId)).isEqualTo(1);
	}

	@AfterEach
	void cleanUp() {
		if (productId == null) {
			return;
		}
		String ids = String.join(",", customerIds.stream().map(String::valueOf).toList());
		jdbc.update("DELETE FROM payments WHERE order_id IN (SELECT order_id FROM orders WHERE customer_id IN (" + ids + "))");
		jdbc.update("DELETE FROM orders WHERE customer_id IN (" + ids + ")"); // cascade order_items, histories
		jdbc.update("DELETE FROM carts WHERE customer_id IN (" + ids + ")"); // cascade cart_items
		jdbc.update("DELETE FROM customers WHERE customer_id IN (" + ids + ")");
		jdbc.update("DELETE FROM products WHERE product_id = ?", productId); // cascade variants
		jdbc.update("DELETE FROM categories WHERE category_id = ?", categoryId);
	}
}
