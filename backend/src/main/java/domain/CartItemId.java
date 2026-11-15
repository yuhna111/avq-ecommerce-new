package domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Lớp khóa ghép cho CartItem (cart_items: variant_id, cart_id).
 * BỔ SUNG kỹ thuật (không có trong thiết kế VP): JPA bắt buộc có lớp khóa cho @IdClass.
 * Tên trường trùng tên quan hệ trong CartItem (variant, cart), kiểu là kiểu khóa của entity cha.
 */
public class CartItemId implements Serializable {
	private Long variant;
	private Long cart;

	public CartItemId() {
	}

	public CartItemId(Long variant, Long cart) {
		this.variant = variant;
		this.cart = cart;
	}

	public Long getVariant() {
		return variant;
	}

	public Long getCart() {
		return cart;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof CartItemId other)) return false;
		return Objects.equals(variant, other.variant) && Objects.equals(cart, other.cart);
	}

	@Override
	public int hashCode() {
		return Objects.hash(variant, cart);
	}
}
