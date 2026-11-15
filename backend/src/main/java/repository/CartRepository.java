package repository;

import domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên,
// Spring Data sinh truy vấn theo tên phương thức.
public interface CartRepository extends JpaRepository<Cart, Long> {

	public Optional<Cart> findByCustomerId(Long customerId);
}