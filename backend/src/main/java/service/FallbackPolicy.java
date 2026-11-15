package service;

import domain.SizeChart;
import domain.FitPref;
import domain.FitType;
import java.util.List;

public class FallbackPolicy {

	public String ruleBasedSize(List<SizeChart> chart, int heightCm, int weightKg, FitPref fitPref, FitType fitType) {
		throw new UnsupportedOperationException();
	}
}