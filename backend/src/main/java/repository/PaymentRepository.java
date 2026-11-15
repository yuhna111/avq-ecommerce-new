package repository;

import domain.Payment;
import java.util.Optional;

public interface PaymentRepository {

	public boolean existsByTxnRef(String txnRef);

	public Optional<Payment> findTopByOrderIdOrderByIdDesc(Long orderId);
}