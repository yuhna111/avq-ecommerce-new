package service;

/**
 * Lỗi nghiệp vụ có mã và thông báo tiếng Việt cho người dùng (bổ sung - lớp hỗ trợ, không có trong VP).
 * app.GlobalExceptionHandler chuyển thành JSON { "code": "...", "message": "..." } với HTTP status tương ứng.
 */
public class BusinessException extends RuntimeException {
	private final String code;
	private final int status;

	public BusinessException(int status, String code, String message) {
		super(message);
		this.status = status;
		this.code = code;
	}

	public static BusinessException badRequest(String code, String message) {
		return new BusinessException(400, code, message);
	}

	public static BusinessException unauthorized(String code, String message) {
		return new BusinessException(401, code, message);
	}

	public static BusinessException forbidden(String code, String message) {
		return new BusinessException(403, code, message);
	}

	public static BusinessException notFound(String code, String message) {
		return new BusinessException(404, code, message);
	}

	public static BusinessException conflict(String code, String message) {
		return new BusinessException(409, code, message);
	}

	public static BusinessException locked(String code, String message) {
		return new BusinessException(423, code, message);
	}

	public String getCode() {
		return code;
	}

	public int getStatus() {
		return status;
	}
}
