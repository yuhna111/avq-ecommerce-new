package service;

import domain.OrderStatus;

public class OrderService {

	public CheckoutSummaryDto prepare(Long customerId) {
		throw new UnsupportedOperationException();
	}

	public OrderResultDto place(Long customerId, PlaceOrderRequest req) {
		throw new UnsupportedOperationException();
	}

	public OrderDetailDto cancelByCustomer(Long customerId, String orderCode, String reason) {
		throw new UnsupportedOperationException();
	}

	public OrderDetailDto updateStatusByStaff(Long staffId, String orderCode, OrderStatus to, String note) {
		throw new UnsupportedOperationException();
	}

	public int expireOverdue(LocalDateTime now) {
		throw new UnsupportedOperationException();
	}
}