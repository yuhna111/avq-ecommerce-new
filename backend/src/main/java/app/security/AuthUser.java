package app.security;

/**
 * Principal trong SecurityContext sau khi kiểm tra JWT. Controller lấy định danh bằng
 * {@code @AuthenticationPrincipal(expression = "customerId")} hoặc {@code (expression = "staffId")}:
 * giá trị chỉ khác null khi đúng vai trò, nên mã nhân viên không bao giờ bị hiểu nhầm là mã khách.
 */
public final class AuthUser {
	public static final AuthUser ANONYMOUS = new AuthUser(null, null);

	private final Long userId;
	private final String role;

	public AuthUser(Long userId, String role) {
		this.userId = userId;
		this.role = role;
	}

	public Long getUserId() {
		return userId;
	}

	public String getRole() {
		return role;
	}

	public Long getCustomerId() {
		return "CUSTOMER".equals(role) ? userId : null;
	}

	public Long getStaffId() {
		return "STAFF".equals(role) || "OWNER".equals(role) ? userId : null;
	}

	@Override
	public String toString() {
		return role == null ? "anonymous" : role + ":" + userId;
	}
}
