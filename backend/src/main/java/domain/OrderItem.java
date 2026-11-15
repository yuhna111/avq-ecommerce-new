package domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Khóa chính ghép order_items (variant_id, order_id) lấy từ hai quan hệ (derived identity, @IdClass).
 * Không thêm thuộc tính id mới; lớp khóa OrderItemId là lớp hỗ trợ JPA (bổ sung, ghi vào báo cáo).
 */
@Entity
@Table(name = "order_items")
@IdClass(OrderItemId.class)
public class OrderItem {
	@Column(name = "product_name", nullable = false, length = 150)
	private String productName;
	@Column(name = "size", nullable = false, length = 5)
	private String size;
	@Column(name = "color", nullable = false, length = 30)
	private String color;
	@Column(name = "unit_price", nullable = false, precision = 12, scale = 0)
	private BigDecimal unitPrice;
	@Column(name = "quantity", nullable = false, columnDefinition = "tinyint")
	private int quantity;
	// VP: public -> private + getter (CLAUDE.md 3.4)
	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;
	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "variant_id", nullable = false)
	private ProductVariant variant;

	public BigDecimal lineTotal() {
		throw new UnsupportedOperationException();
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public ProductVariant getVariant() {
		return variant;
	}

	public void setVariant(ProductVariant variant) {
		this.variant = variant;
	}
}
