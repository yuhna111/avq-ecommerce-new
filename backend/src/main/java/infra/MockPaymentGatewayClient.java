package infra;

import domain.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayClient.class);
	/** Khóa mặc định chỉ để bản demo chạy ngay; nên đặt payment.mock.hash-secret trong application-local.yml. */
	static final String DEFAULT_SECRET = "AVQ-MOCK-GATEWAY-SECRET";

	public MockPaymentGatewayClient(@Value("${payment.mock.hash-secret:" + DEFAULT_SECRET + "}") String hashSecret) {
		super("/mock-pay", "AVQMOCK", hashSecret);
		if (DEFAULT_SECRET.equals(hashSecret)) {
			log.warn("Cổng thanh toán giả lập đang dùng khóa ký mặc định; đặt payment.mock.hash-secret trong application-local.yml");
		}
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
	 * Nhận lại link thanh toán đã ký do chính cổng phát hành (trang /mock-pay gửi kèm các tham số vnp_*).
	 * Dùng khi bộ nhớ "issued" bị mất do khởi động lại máy chủ. Chữ ký sai hoặc thiếu tham số thì bỏ qua.
	 */
	public boolean restoreIssued(Map<String, String> linkParams) {
		if (linkParams == null || !verifySignature(linkParams)) {
			return false;
		}
		String txnRef = linkParams.get("vnp_TxnRef");
		String amount = linkParams.get("vnp_Amount");
		if (txnRef == null || amount == null || !amount.matches("\\d+")) {
			return false;
		}
		issued.putIfAbsent(txnRef, new BigDecimal(amount).movePointLeft(2));
		return true;
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
