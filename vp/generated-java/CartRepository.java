package repository;

import domain.Cart;

public interface CartRepository {

	public Optional<Cart> findByCustomerId(Long customerId);
}