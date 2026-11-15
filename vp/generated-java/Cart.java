package domain;

import java.util.ArrayList;

public class Cart {
	private Long id;
	private LocalDateTime updatedAt;
	public Customer customer;
	public ArrayList<CartItem> items = new ArrayList<CartItem>();

	public Optional<CartItem> findItem(Long variantId) {
		throw new UnsupportedOperationException();
	}

	public CartItem addOrIncrease(ProductVariant variant, int qty) {
		throw new UnsupportedOperationException();
	}

	public void removeItems(Set<Long> variantIds) {
		throw new UnsupportedOperationException();
	}

	public BigDecimal subtotal() {
		throw new UnsupportedOperationException();
	}
}