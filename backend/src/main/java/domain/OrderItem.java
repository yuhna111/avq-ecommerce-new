package domain;

import java.math.BigDecimal;

public class OrderItem {
	private String productName;
	private String size;
	private String color;
	private BigDecimal unitPrice;
	private int quantity;
	public Order order;
	public ProductVariant variant;

	public BigDecimal lineTotal() {
		throw new UnsupportedOperationException();
	}
}