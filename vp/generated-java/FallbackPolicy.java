package service;

import domain.SizeChart;
import domain.FitPref;
import domain.FitType;

public class FallbackPolicy {

	public String ruleBasedSize(List<SizeChart> chart, int heightCm, int weightKg, FitPref fitPref, FitType fitType) {
		throw new UnsupportedOperationException();
	}
}