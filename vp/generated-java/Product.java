package domain;

import java.util.ArrayList;

public class Product {
	private Long id;
	private String name;
	private String description;
	private String material;
	private BigDecimal price;
	private boolean visible;
	private FitType fitType;
	public ArrayList<ProductVariant> variants = new ArrayList<ProductVariant>();
	public Category category;
	public ArrayList<ProductImage> image = new ArrayList<ProductImage>();

	public void updateInfo(BigDecimal price, String description, String material) {
		throw new UnsupportedOperationException();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public ProductVariant addVariant(String size, String color, String sku) {
		throw new UnsupportedOperationException();
	}
}