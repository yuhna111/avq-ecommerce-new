package service;

public class CartService {

	public CartDto getCart(Long customerId) {
		throw new UnsupportedOperationException();
	}

	public CartDto addToCart(Long customerId, Long variantId, int qty) {
		throw new UnsupportedOperationException();
	}

	public CartDto changeQty(Long customerId, Long variantId, int qty) {
		throw new UnsupportedOperationException();
	}

	public CartDto removeItem(Long customerId, Long variantId) {
		throw new UnsupportedOperationException();
	}
}