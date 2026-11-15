package domain;

public class SizeChart {
	private Long id;
	private int heightMin;
	private String size;
	private int heightMax;
	private int weightMin;
	private int weightMax;
	public Product product;

	public boolean contains(int heightCm, int weightKg) {
		throw new UnsupportedOperationException();
	}
}