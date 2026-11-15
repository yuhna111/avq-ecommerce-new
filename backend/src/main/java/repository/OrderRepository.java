package repository;

import domain.Order;
import domain.OrderStatus;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository {

	public Optional<Order> findByOrderCode(String code);

	public List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

	public List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime time);
}