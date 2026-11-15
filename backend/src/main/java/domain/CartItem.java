package domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Khóa chính ghép cart_items (variant_id, cart_id) lấy từ hai quan hệ (derived identity, @IdClass).
 * Lớp khóa CartItemId là lớp hỗ trợ JPA (bổ sung, ghi vào báo cáo).
 */
@Entity
@Table(name = "cart_items")
@IdClass(CartItemId.class)
public class CartItem {
	/** BR05 (bổ sung hằng số, private) */
	private static final int MAX_QTY = 10;

	@Column(name = "quantity", nullable = false, columnDefinition = "tinyint")
	private int quantity;
	@Column(name = "added_at", nullable = false)
	private LocalDateTime addedAt;
	// VP: public -> private + getter (CLAUDE.md 3.4)
	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "variant_id", nullable = false)
	private ProductVariant variant;
	@Id
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	public void changeQty(int qty) {
		// BR05 (CHECK quantity BETWEEN 1 AND 10); kiểm tra tồn kho ở CartService
		if (qty < 1 || qty > MAX_QTY) {
			throw new IllegalArgumentException("Số lượng mỗi dòng phải từ 1 đến " + MAX_QTY);
		}
		this.quantity = qty;
	}

	public BigDecimal lineTotal() {
		return this.variant.getProduct().getPrice().multiply(BigDecimal.valueOf(this.quantity));
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public int getQuantity() {
		return quantity;
	}

	public LocalDateTime getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(LocalDateTime addedAt) {
		this.addedAt = addedAt;
	}

	public ProductVariant getVariant() {
		return variant;
	}

	public void setVariant(ProductVariant variant) {
		this.variant = variant;
	}

	public Cart getCart() {
		return cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}
}
