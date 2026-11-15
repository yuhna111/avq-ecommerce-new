package infra;

/** Nội dung JWT đã kiểm tra chữ ký (kiểu hỗ trợ của infra, bổ sung). */
public record TokenClaims(Long userId, String role) {
}
