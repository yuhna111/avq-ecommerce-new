package repository;

import domain.ChangeLog;

public interface ProductChangeLogRepository {

	public List<ChangeLog> findByProductIdOrderByChangedAtDesc(Long productId);
}