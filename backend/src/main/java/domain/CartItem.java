package domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItem {
	private int quantity;
	private LocalDateTime addedAt;
	public ProductVariant variant;
	public Cart cart;

	public void changeQty(int qty) {
		throw new UnsupportedOperationException();
	}

	public BigDecimal lineTotal() {
		throw new UnsupportedOperationException();
	}
}