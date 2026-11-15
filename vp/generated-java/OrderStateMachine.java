package service;

import domain.Order;
import domain.OrderStatus;
import domain.Staff;

public class OrderStateMachine {

	public void transit(Order order, OrderStatus to, Staff staff, String note) {
		throw new UnsupportedOperationException();
	}

	public Set<OrderStatus> nextStatuses(OrderStatus from) {
		throw new UnsupportedOperationException();
	}
}