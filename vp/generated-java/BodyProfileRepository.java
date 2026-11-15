package repository;

import domain.BodyProfile;

public interface BodyProfileRepository {

	public Optional<BodyProfile> findByCustomerId(Long customerId);
}