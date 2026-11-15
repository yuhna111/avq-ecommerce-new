package domain;

import java.time.LocalDateTime;

public class ReturnRecord {
	private Long id;
	private ReturnReason reason;
	private LocalDateTime createdAt;
	public OrderItem orderItem;
	public Staff createdBy;
}