package repository;

import domain.SizeChart;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên,
// Spring Data sinh truy vấn theo tên phương thức.
public interface SizeChartRepository extends JpaRepository<SizeChart, Long> {

	public List<SizeChart> findByProductIdOrderByHeightMinAsc(Long productId);
}