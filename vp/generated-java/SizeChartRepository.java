package repository;

import domain.SizeChart;

public interface SizeChartRepository {

	public List<SizeChart> findByProductIdOrderByHeightMinAsc(Long productId);
}