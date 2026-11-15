package domain;

public class ProductVariant {
	private Long id;
	private String sku;
	private String size;
	private String color;
	private int stockQty;
	private int version;
	public Product product;

	public void setStock(int qty) {
		throw new UnsupportedOperationException();
	}

	public boolean isAvailable(int qty) {
		throw new UnsupportedOperationException();
	}
}