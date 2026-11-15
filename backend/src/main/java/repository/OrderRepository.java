package repository;

import domain.Order;
import domain.OrderStatus;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên,
// Spring Data sinh truy vấn theo tên phương thức.
public interface OrderRepository extends JpaRepository<Order, Long> {

	public Optional<Order> findByOrderCode(String code);

	public List<Order> findByCustomerIdOrderByOrderDateDesc(Long customerId);

	public List<Order> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime time);
}