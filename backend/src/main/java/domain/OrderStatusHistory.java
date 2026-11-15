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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_status_histories")
public class OrderStatusHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "history_id")
	private Long id;
	// Cột from_status / to_status là varchar(20) (không phải ENUM) -> columnDefinition khớp DDL.
	// fromStatus = null với dòng "Tạo đơn" (DT-04).
	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", columnDefinition = "varchar(20)")
	private OrderStatus fromStatus;
	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, columnDefinition = "varchar(20)")
	private OrderStatus toStatus;
	@Column(name = "note", length = 255)
	private String note;
	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt;
	// VP: public -> private + getter (CLAUDE.md 3.4). OrderStatusHistory -> Order, Staff một chiều.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;
	// staff_id NULL khi khách hoặc hệ thống thực hiện
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "staff_id")
	private Staff staff;

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public OrderStatus getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(OrderStatus fromStatus) {
		this.fromStatus = fromStatus;
	}

	public OrderStatus getToStatus() {
		return toStatus;
	}

	public void setToStatus(OrderStatus toStatus) {
		this.toStatus = toStatus;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(LocalDateTime changedAt) {
		this.changedAt = changedAt;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}
}
