package api.dto;

/** Kết quả đăng nhập/đăng ký: JWT HS256, hết hạn sau 30 phút (NFR). */
public record TokenDto(String token, String role, Long userId, String fullName, long expiresInMinutes) {
}
