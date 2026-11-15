package repository;

import domain.Customer;
import java.util.Optional;

public interface CustomerRepository {

	public Optional<Customer> findByEmailOrPhone(String identifier);

	public boolean existsByEmail(String email);

	public boolean existsByPhone(String phone);
}