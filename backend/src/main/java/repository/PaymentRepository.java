package repository;

import domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên,
// Spring Data sinh truy vấn theo tên phương thức.
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	public boolean existsByTxnRef(String txnRef);

	public Optional<Payment> findTopByOrderIdOrderByIdDesc(Long orderId);
}