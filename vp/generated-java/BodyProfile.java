package domain;

public class BodyProfile {
	private int heightCm;
	private int weightKg;
	private FitPref fitPref;
	private LocalDateTime consentAt;
	public Customer customer;

	public void update(int heightCm, int weightKg, FitPref fitPref) {
		throw new UnsupportedOperationException();
	}
}