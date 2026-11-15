package api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** UC01 - BR01: email HOẶC số điện thoại (ít nhất một), BR16: mật khẩu tối thiểu 8 ký tự. */
public record RegisterRequest(
		@NotBlank(message = "Vui lòng nhập họ tên") @Size(max = 100) String fullName,
		@Email(message = "Email không hợp lệ") @Size(max = 120) String email,
		@Pattern(regexp = "^$|^0\\d{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
		@NotBlank(message = "Vui lòng nhập mật khẩu") @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự") String password) {
}
