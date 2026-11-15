package repository;

import domain.Address;

public interface AddressRepository {

	public List<Address> findByCustomerId(Long customerId);
}