package service;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/** BR09 / DT-01: phí giao 30.000 đ; miễn phí khi tạm tính từ 500.000 đ trở lên. */
@Component
public class ShippingFeeCalculator {
	private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(500_000);
	private static final BigDecimal SHIPPING_FEE = BigDecimal.valueOf(30_000);

	public BigDecimal calculate(BigDecimal subtotal) {
		if (subtotal == null || subtotal.signum() < 0) {
			throw new IllegalArgumentException("Tạm tính không hợp lệ");
		}
		return subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : SHIPPING_FEE;
	}
}
