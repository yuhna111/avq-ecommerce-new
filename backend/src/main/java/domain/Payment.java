package domain;

import java.math.BigDecimal;
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
@Table(name = "payments")
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private Long id;
	@Column(name = "method", nullable = false, length = 10)
	private String method;
	@Column(name = "amount", nullable = false, precision = 12, scale = 0)
	private BigDecimal amount;
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false,
			columnDefinition = "enum('DANG_XU_LY','THANH_CONG','THAT_BAI','SAI_LECH')")
	private PaymentStatus status;
	// UNIQUE (DB-R6): chống ghi trùng khi cổng gửi lại kết quả
	@Column(name = "txn_ref", length = 64, unique = true)
	private String txnRef;
	@Column(name = "paid_at")
	private LocalDateTime paidAt;
	// VP: public -> private + getter (CLAUDE.md 3.4). Payment -> Order một chiều.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	public void markResult(boolean success, String txnRef) {
		this.status = success ? PaymentStatus.THANH_CONG : PaymentStatus.THAT_BAI;
		this.txnRef = txnRef;
		if (success) {
			this.paidAt = LocalDateTime.now();
		}
	}

	public void markMismatch(String txnRef) {
		// DT-05: số tiền cổng báo khác tổng đơn
		this.status = PaymentStatus.SAI_LECH;
		this.txnRef = txnRef;
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getTxnRef() {
		return txnRef;
	}

	public LocalDateTime getPaidAt() {
		return paidAt;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}
