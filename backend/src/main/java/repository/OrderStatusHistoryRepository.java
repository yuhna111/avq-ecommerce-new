package repository;

import domain.OrderStatusHistory;
import java.util.List;

public interface OrderStatusHistoryRepository {

	public List<OrderStatusHistory> findByOrderIdOrderByChangedAtAsc(Long orderId);
}