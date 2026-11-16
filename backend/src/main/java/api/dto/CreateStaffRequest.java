package api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Bổ sung 17/11 (BR02): chủ cửa hàng cấp tài khoản nhân viên. */
public record CreateStaffRequest(
		@NotBlank(message = "Vui lòng nhập tên đăng nhập")
		@Pattern(regexp = "^[a-z0-9._]{3,50}$", message = "Tên đăng nhập 3–50 ký tự, chỉ gồm chữ thường không dấu, số, dấu chấm, gạch dưới") String username,
		@NotBlank(message = "Vui lòng nhập họ tên") @Size(max = 100) String fullName,
		@NotBlank(message = "Vui lòng nhập mật khẩu") @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự") String password) {
}
