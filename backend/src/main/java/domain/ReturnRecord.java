package domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "return_records")
public class ReturnRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "return_id")
	private Long id;
	@Enumerated(EnumType.STRING)
	@Column(name = "reason", nullable = false, columnDefinition = "enum('SIZE_LON','SIZE_NHO','KHAC')")
	private ReturnReason reason;
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	// VP: public -> private + getter (CLAUDE.md 3.4).
	// Khóa ngoại ghép (variant_id, order_id) -> order_items (fk_returns_order_item).
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({
			@JoinColumn(name = "variant_id", referencedColumnName = "variant_id", nullable = false),
			@JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false)
	})
	private OrderItem orderItem;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", nullable = false)
	private Staff createdBy;

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public ReturnReason getReason() {
		return reason;
	}

	public void setReason(ReturnReason reason) {
		this.reason = reason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}

	public void setOrderItem(OrderItem orderItem) {
		this.orderItem = orderItem;
	}

	public Staff getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Staff createdBy) {
		this.createdBy = createdBy;
	}
}
