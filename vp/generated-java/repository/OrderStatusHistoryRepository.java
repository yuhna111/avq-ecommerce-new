package repository;

import domain.OrderStatusHistory;

public interface OrderStatusHistoryRepository {

	public List<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(Long orderId);
}