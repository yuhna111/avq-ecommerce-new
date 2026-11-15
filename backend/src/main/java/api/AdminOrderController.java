package api;

import domain.OrderStatus;
import java.util.List;
import api.dto.OrderDetailDto;
import api.dto.OrderSummaryDto;

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