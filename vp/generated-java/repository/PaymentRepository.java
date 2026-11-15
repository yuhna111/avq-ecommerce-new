package repository;

import domain.Payment;

public interface PaymentRepository {

	public boolean existsByTxnRef(String txnRef);

	public Optional<Payment> findTopByOrderIdOrderByIdDesc(Long orderId);
}