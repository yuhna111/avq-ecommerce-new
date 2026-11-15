package domain;

public class OrderStatusHistory {
	private Long id;
	private OrderStatus fromStatus;
	private OrderStatus toStatus;
	private String note;
	private LocalDateTime changedAt;
	public Order order;
	public Staff staff;
}