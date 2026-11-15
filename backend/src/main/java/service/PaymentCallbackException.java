package service;

/**
 * Mang mã phản hồi DT-05 khác "00" ra khỏi PaymentService.handleCallback (bổ sung - lớp hỗ trợ).
 *
 * LỆCH THIẾT KẾ: trong model VP, handleCallback(params) và PaymentCallbackController.vnpayIpn(params)
 * không khai báo kiểu trả về nên VP sinh "void", trong khi design-notes ghi handleCallback(params) : String.
 * Để giữ nguyên chữ ký VP, handleCallback trả về bình thường nghĩa là "00"; các mã 97/01/02/04 được
 * ném bằng ngoại lệ này. Đề xuất: khai báo kiểu trả về String cho handleCallback trong VP.
 */
public class PaymentCallbackException extends RuntimeException {
	private final String rspCode;

	public PaymentCallbackException(String rspCode, String message) {
		super(message);
		this.rspCode = rspCode;
	}

	public String getRspCode() {
		return rspCode;
	}
}
