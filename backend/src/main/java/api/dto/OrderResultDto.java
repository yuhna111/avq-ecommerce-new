package api.dto;

import domain.OrderStatus;
import domain.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** paymentUrl chỉ có với đơn ONLINE (BR10). */
public record OrderResultDto(String orderCode, OrderStatus status, BigDecimal total, PaymentMethod paymentMethod,
		String paymentUrl, LocalDateTime expiresAt) {
}
