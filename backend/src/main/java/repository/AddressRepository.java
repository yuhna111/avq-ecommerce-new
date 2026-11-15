package repository;

import domain.Address;
import java.util.List;

public interface AddressRepository {

	public List<Address> findByCustomerId(Long customerId);
}