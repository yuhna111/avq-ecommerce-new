package repository;

import domain.Order;
import domain.OrderStatus;

public interface OrderRepository {

	public Optional<Order> findByOrderCode(String code);

	public List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

	public List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime time);
}