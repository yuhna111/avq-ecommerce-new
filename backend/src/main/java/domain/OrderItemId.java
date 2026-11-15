package domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Lớp khóa ghép cho OrderItem (order_items: variant_id, order_id).
 * BỔ SUNG kỹ thuật (không có trong thiết kế VP): JPA bắt buộc có lớp khóa cho @IdClass.
 * Tên trường trùng tên quan hệ trong OrderItem (order, variant), kiểu là kiểu khóa của entity cha.
 */
public class OrderItemId implements Serializable {
	private Long order;
	private Long variant;

	public OrderItemId() {
	}

	public OrderItemId(Long order, Long variant) {
		this.order = order;
		this.variant = variant;
	}

	public Long getOrder() {
		return order;
	}

	public Long getVariant() {
		return variant;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OrderItemId other)) return false;
		return Objects.equals(order, other.order) && Objects.equals(variant, other.variant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(order, variant);
	}
}
