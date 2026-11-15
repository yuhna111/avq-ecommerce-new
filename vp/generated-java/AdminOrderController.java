package api;

import domain.OrderStatus;

public class AdminOrderController {

	public List<OrderSummaryDto> list(OrderStatus status) {
		throw new UnsupportedOperationException();
	}

	public OrderDetailDto detail(String code) {
		throw new UnsupportedOperationException();
	}

	public OrderDetailDto updateStatus(Long staffId, String code, OrderStatus to, String note) {
		throw new UnsupportedOperationException();
	}
}