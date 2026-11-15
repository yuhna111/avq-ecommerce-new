package api.dto;

import domain.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
		@NotBlank(message = "Vui lòng nhập tên người nhận") @Size(max = 100) String receiverName,
		@NotBlank(message = "Vui lòng nhập số điện thoại") @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String receiverPhone,
		@NotBlank(message = "Vui lòng nhập địa chỉ giao hàng") @Size(max = 255) String shippingAddress,
		@NotNull(message = "Vui lòng chọn phương thức thanh toán") PaymentMethod paymentMethod) {
}
