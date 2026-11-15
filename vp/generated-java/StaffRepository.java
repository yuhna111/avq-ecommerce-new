package repository;

import domain.Staff;

public interface StaffRepository {

	public Optional<Staff> findByUsername(String username);
}