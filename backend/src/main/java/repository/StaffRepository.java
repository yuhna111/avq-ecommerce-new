package repository;

import domain.Staff;
import java.util.Optional;

public interface StaffRepository {

	public Optional<Staff> findByUsername(String username);
}