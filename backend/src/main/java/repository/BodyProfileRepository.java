package repository;

import domain.BodyProfile;
import java.util.Optional;

public interface BodyProfileRepository {

	public Optional<BodyProfile> findByCustomerId(Long customerId);
}