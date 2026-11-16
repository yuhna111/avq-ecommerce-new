package api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Bổ sung 17/11: chủ cửa hàng đặt lại mật khẩu cho nhân viên. */
public record ResetPasswordRequest(
		@NotBlank(message = "Vui lòng nhập mật khẩu mới") @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự") String password) {
}
