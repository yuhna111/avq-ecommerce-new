package repository;

import domain.ChangeLog;
import java.util.List;

public interface ProductChangeLogRepository {

	public List<ChangeLog> findByProductIdOrderByChangedAtDesc(Long productId);
}