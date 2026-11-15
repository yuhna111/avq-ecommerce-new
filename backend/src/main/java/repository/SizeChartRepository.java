package repository;

import domain.SizeChart;
import java.util.List;

public interface SizeChartRepository {

	public List<SizeChart> findByProductIdOrderByHeightMinAsc(Long productId);
}