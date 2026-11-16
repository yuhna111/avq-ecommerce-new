package app;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import service.BusinessException;

/** Mọi lỗi trả JSON { "code": "...", "message": "..." } (CLAUDE.md Giai đoạn 5). */
@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Map<String, String>> business(BusinessException e) {
		return body(HttpStatus.valueOf(e.getStatus()), e.getCode(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
		FieldError fe = e.getBindingResult().getFieldError();
		String msg = fe != null ? fe.getDefaultMessage() : "Dữ liệu không hợp lệ";
		return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<Map<String, String>> invalidParam(HandlerMethodValidationException e) {
		return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dữ liệu không hợp lệ");
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class, MissingServletRequestPartException.class })
	public ResponseEntity<Map<String, String>> badInput(Exception e) {
		return body(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Dữ liệu gửi lên không hợp lệ");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, String>> tooLarge(MaxUploadSizeExceededException e) {
		return body(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", "Ảnh vượt quá 5 MB");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> illegal(IllegalArgumentException e) {
		// Bất biến trong entity (ví dụ số lượng 1..10, tồn kho không âm, giá > 0)
		return body(HttpStatus.BAD_REQUEST, "INVALID_VALUE", e.getMessage());
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<Map<String, String>> concurrent(ObjectOptimisticLockingFailureException e) {
		// Xung đột đồng thời (DB-R1, DB-R2): giữ giá trị mới nhất, yêu cầu tải lại
		return body(HttpStatus.CONFLICT, "CONCURRENT_UPDATE",
				"Dữ liệu vừa được người khác cập nhật. Vui lòng tải lại để xem giá trị hiện tại rồi thao tác lại.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> integrity(DataIntegrityViolationException e) {
		log.warn("Vi phạm ràng buộc CSDL: {}", e.getMostSpecificCause().getMessage());
		return body(HttpStatus.CONFLICT, "DATA_CONFLICT", "Dữ liệu vi phạm ràng buộc (trùng hoặc không hợp lệ)");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, String>> method(HttpRequestMethodNotSupportedException e) {
		return body(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Phương thức không được hỗ trợ");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, String>> notFound(NoResourceFoundException e) {
		return body(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đường dẫn");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> other(Exception e) {
		// NFR05: lỗi giữa chừng -> giao dịch đã rollback
		log.error("Lỗi không mong đợi", e);
		return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Có lỗi, vui lòng thử lại");
	}

	private static ResponseEntity<Map<String, String>> body(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(Map.of("code", code, "message", message == null ? "" : message));
	}
}
