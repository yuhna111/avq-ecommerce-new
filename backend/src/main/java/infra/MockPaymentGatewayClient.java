package infra;

import domain.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Cổng thanh toán GIẢ LẬP (bổ sung theo CLAUDE.md mục 5; không có trong VP). Cùng thực hiện
 * PaymentGatewayClient, ký HMAC-SHA512 như VNPay, và kết quả đi qua đúng PaymentService.handleCallback.
 *
 * buildPaymentUrl trả về đường dẫn trang thanh toán giả lập của frontend (/mock-pay?...).
 * simulateCallback đóng vai VNPay gửi IPN: tạo bộ tham số kết quả đã ký.
 */
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGatewayClient extends VnPayClient {
	private static final DateTimeFormatter VNP_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	/** txnRef (mã đơn) -> số tiền đã phát hành link thanh toán (giữ trong bộ nhớ, đủ cho demo). */
	private final Map<String, BigDecimal> issued = new ConcurrentHashMap<String, BigDecimal>();

	public MockPaymentGatewayClient(@Value("${payment.mock.hash-secret:AVQ-MOCK-GATEWAY-SECRET}") String hashSecret) {
		super("/mock-pay", "AVQMOCK", hashSecret);
	}

	@Override
	public String buildPaymentUrl(Order order, String txnRef) {
		issued.put(txnRef, order.getTotal());
		Map<String, String> p = new TreeMap<String, String>();
		p.put("vnp_TmnCode", getTmnCode());
		p.put("vnp_TxnRef", txnRef);
		p.put("vnp_Amount", toVnpAmount(order.getTotal()));
		p.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
		p.put("vnp_CreateDate", LocalDateTime.now().format(VNP_DATE));
		String query = buildQuery(p);
		return "/mock-pay?" + query + "&vnp_SecureHash=" + hmacSHA512(query);
	}

	/**
	 * Mô phỏng IPN từ cổng: vnp_ResponseCode "00" (thành công) hoặc "24" (khách hủy giao dịch).
	 * Trả về null nếu cổng chưa phát hành link cho mã này (ví dụ sau khi khởi động lại máy chủ).
	 */
	public Map<String, String> simulateCallback(String txnRef, boolean success) {
		BigDecimal amount = issued.get(txnRef);
		if (amount == null) {
			return null;
		}
		Map<String, String> p = new TreeMap<String, String>();
		p.put("vnp_TmnCode", getTmnCode());
		p.put("vnp_TxnRef", txnRef);
		p.put("vnp_Amount", toVnpAmount(amount));
		p.put("vnp_ResponseCode", success ? "00" : "24");
		p.put("vnp_TransactionStatus", success ? "00" : "02");
		p.put("vnp_TransactionNo", "MOCK" + ThreadLocalRandom.current().nextLong(10_000_000L, 99_999_999L));
		p.put("vnp_PayDate", LocalDateTime.now().format(VNP_DATE));
		p.put("vnp_SecureHash", hmacSHA512(buildQuery(p)));
		return p;
	}
}
