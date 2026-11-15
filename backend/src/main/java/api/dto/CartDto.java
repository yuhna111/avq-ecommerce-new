package api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(Long cartId, List<CartLineDto> items, BigDecimal subtotal, int totalQuantity) {

	public record CartLineDto(Long variantId, Long productId, String productName, String size, String color,
			String image, BigDecimal unitPrice, int quantity, BigDecimal lineTotal, int stockQty) {
	}
}
