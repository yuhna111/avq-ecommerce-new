package repository;

import domain.Cart;
import java.util.Optional;

public interface CartRepository {

	public Optional<Cart> findByCustomerId(Long customerId);
}