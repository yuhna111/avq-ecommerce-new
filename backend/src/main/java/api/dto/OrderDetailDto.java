package api.dto;

import domain.OrderStatus;
import domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailDto(String orderCode, LocalDateTime orderDate, OrderStatus status, String customerName,
		String receiverName, String receiverPhone, String shippingAddress, BigDecimal subtotal, BigDecimal shippingFee,
		BigDecimal total, PaymentMethod paymentMethod, String paymentStatus, LocalDateTime expiresAt, String trackingCode,
		List<LineDto> items, List<HistoryDto> histories, List<OrderStatus> nextStatuses, boolean cancellableByCustomer) {

	public record LineDto(Long variantId, String productName, String size, String color, BigDecimal unitPrice,
			int quantity, BigDecimal lineTotal) {
	}

	public record HistoryDto(OrderStatus fromStatus, OrderStatus toStatus, String note, LocalDateTime changedAt,
			String changedBy) {
	}
}
