package api.dto;

import jakarta.validation.constraints.NotBlank;

/** identifier: email hoặc SĐT (khách), username (nhân viên). */
public record LoginRequest(
		@NotBlank(message = "Vui lòng nhập tài khoản") String identifier,
		@NotBlank(message = "Vui lòng nhập mật khẩu") String password) {
}
