package api.dto;

import java.math.BigDecimal;
import java.util.List;

/** UC09 bước xác nhận: khách thấy giá hiện hành trước khi đặt (BR08), phí giao theo DT-01. */
public record CheckoutSummaryDto(List<CartDto.CartLineDto> items, BigDecimal subtotal, BigDecimal shippingFee,
		BigDecimal total, List<AddressDto> addresses) {
}
