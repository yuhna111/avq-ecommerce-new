package api;

import api.dto.CheckoutSummaryDto;
import java.util.List;
import api.dto.OrderDetailDto;
import api.dto.OrderResultDto;
import api.dto.OrderSummaryDto;
import api.dto.PlaceOrderRequest;

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