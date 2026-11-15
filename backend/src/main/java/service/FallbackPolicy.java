package service;

import domain.SizeChart;
import domain.FitPref;
import domain.FitType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/** FB-01: gợi ý size theo bảng size khi không có (hoặc không tin được) kết quả AI. */
@Component
public class FallbackPolicy {

	/**
	 * 1. Size đầu tiên (theo chiều cao tăng dần) có khoảng chiều cao VÀ cân nặng chứa số đo.
	 * 2. Không size nào khớp: size có khoảng chiều cao gần nhất.
	 * 3. Phom OVERSIZE + khách chọn VỪA: lùi một size.
	 * 4. Không có bảng size: null (bên gọi trả "Liên hệ cửa hàng để được tư vấn size").
	 */
	public String ruleBasedSize(List<SizeChart> chart, int heightCm, int weightKg, FitPref fitPref, FitType fitType) {
		if (chart == null || chart.isEmpty()) {
			return null;
		}
		List<SizeChart> sorted = new ArrayList<SizeChart>(chart);
		sorted.sort(Comparator.comparingInt(SizeChart::getHeightMin));

		int idx = -1;
		for (int i = 0; i < sorted.size(); i++) {
			if (sorted.get(i).contains(heightCm, weightKg)) {
				idx = i;
				break;
			}
		}
		if (idx < 0) {
			int best = Integer.MAX_VALUE;
			for (int i = 0; i < sorted.size(); i++) {
				int d = heightDistance(sorted.get(i), heightCm);
				if (d < best) {
					best = d;
					idx = i;
				}
			}
		}
		if (fitType == FitType.OVERSIZE && fitPref == FitPref.VUA && idx > 0) {
			idx--;
		}
		return sorted.get(idx).getSize();
	}

	private int heightDistance(SizeChart s, int heightCm) {
		if (heightCm < s.getHeightMin()) {
			return s.getHeightMin() - heightCm;
		}
		if (heightCm > s.getHeightMax()) {
			return heightCm - s.getHeightMax();
		}
		return 0;
	}
}
