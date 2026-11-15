package api.dto;

import domain.OrderStatus;
import domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDto(String orderCode, LocalDateTime orderDate, OrderStatus status, BigDecimal total,
		PaymentMethod paymentMethod, int itemCount, String customerName, String receiverName) {
}
