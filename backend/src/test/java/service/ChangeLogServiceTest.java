package service;

import static org.assertj.core.api.Assertions.assertThat;

import api.dto.UpdateProductRequest;
import domain.Product;
import domain.ProductVariant;
import domain.Staff;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** FR31 – product_change_logs qua ChangeLog.of(staff, product, variant, ...): luôn có product_id. */
class ChangeLogServiceTest extends ServiceTestBase {
	@Autowired StockService stockService;
	@Autowired ProductService productService;
	@Autowired JdbcTemplate jdbc;

	private List<Map<String, Object>> logs(Long productId) {
		return jdbc.queryForList("SELECT staff_id, variant_id, field_name, old_value, new_value FROM product_change_logs "
				+ "WHERE product_id = ? ORDER BY log_id", productId);
	}

	@Test
	void suaTonKho_ghiLogCoProductVaVariant() {
		Product p = fx.product(fx.category(), "Áo log " + fx.tag(), 300000, 5);
		ProductVariant v = fx.variant(p);
		Staff s = fx.staff();
		flushClear();

		stockService.updateStock(s.getId(), v.getId(), 12);
		em.flush();

		List<Map<String, Object>> rows = logs(p.getId());
		assertThat(rows).hasSize(1);
		assertThat(rows.get(0)).containsEntry("field_name", "stock_qty").containsEntry("old_value", "5")
				.containsEntry("new_value", "12");
		assertThat(((Number) rows.get(0).get("variant_id")).longValue()).isEqualTo(v.getId());
		assertThat(((Number) rows.get(0).get("staff_id")).longValue()).isEqualTo(s.getId());
	}

	@Test
	void suaGiaVaAnSanPham_ghiLogCapSanPham_variantNull() {
		Product p = fx.product(fx.category(), "Áo giá " + fx.tag(), 300000, 5);
		Staff s = fx.staff();
		flushClear();

		productService.update(s.getId(), p.getId(), new UpdateProductRequest(new BigDecimal("350000"), null, null, false));
		em.flush();

		List<Map<String, Object>> rows = logs(p.getId());
		assertThat(rows).extracting(r -> r.get("field_name")).contains("price", "is_visible");
		assertThat(rows).allSatisfy(r -> assertThat(r.get("variant_id")).isNull());
	}
}
