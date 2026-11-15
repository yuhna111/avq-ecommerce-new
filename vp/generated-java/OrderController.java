package api;

public class OrderController {

	public CheckoutSummaryDto prepare(Long customerId) {
		throw new UnsupportedOperationException();
	}

	public OrderResultDto place(Long customerId, PlaceOrderRequest req) {
		throw new UnsupportedOperationException();
	}

	public List<OrderSummaryDto> list(Long customerId) {
		throw new UnsupportedOperationException();
	}

	public OrderDetailDto detail(Long customerId, String code) {
		throw new UnsupportedOperationException();
	}

	public OrderDetailDto cancel(Long customerId, String code, String reason) {
		throw new UnsupportedOperationException();
	}
}