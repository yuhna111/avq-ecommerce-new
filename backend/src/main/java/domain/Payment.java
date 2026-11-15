package domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
	private Long id;
	private String method;
	private BigDecimal amount;
	private PaymentStatus status;
	private String txnRef;
	private LocalDateTime paidAt;
	public Order order;

	public void markResult(boolean success, String txnRef) {
		throw new UnsupportedOperationException();
	}

	public void markMismatch(String txnRef) {
		throw new UnsupportedOperationException();
	}
}