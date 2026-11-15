package infra;

import domain.Order;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Cổng VNPay theo thiết kế. Bản demo KHÔNG gọi VNPay thật (CLAUDE.md mục 7): chỉ bật khi
 * payment.provider=vnpay; mặc định dùng MockPaymentGatewayClient (lớp con, cùng cách ký HMAC-SHA512).
 */
@Component
@ConditionalOnProperty(name = "payment.provider", havingValue = "vnpay")
public class VnPayClient implements PaymentGatewayClient {
	private static final DateTimeFormatter VNP_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private final String payUrl;
	private final String tmnCode;
	private final String hashSecret;

	public VnPayClient(@Value("${payment.vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") String payUrl,
			@Value("${payment.vnpay.tmn-code:}") String tmnCode,
			@Value("${payment.vnpay.hash-secret:}") String hashSecret) {
		this.payUrl = payUrl;
		this.tmnCode = tmnCode;
		this.hashSecret = hashSecret;
	}

	public String buildPaymentUrl(Order order, String txnRef) {
		Map<String, String> p = new TreeMap<String, String>();
		p.put("vnp_Version", "2.1.0");
		p.put("vnp_Command", "pay");
		p.put("vnp_TmnCode", tmnCode);
		p.put("vnp_Amount", toVnpAmount(order.getTotal()));
		p.put("vnp_CurrCode", "VND");
		p.put("vnp_TxnRef", txnRef);
		p.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
		p.put("vnp_OrderType", "other");
		p.put("vnp_Locale", "vn");
		p.put("vnp_CreateDate", LocalDateTime.now().format(VNP_DATE));
		String query = buildQuery(p);
		return payUrl + "?" + query + "&vnp_SecureHash=" + hmacSHA512(query);
	}

	public boolean verifySignature(Map<String, String> params) {
		String received = params.get("vnp_SecureHash");
		if (received == null) {
			return false;
		}
		Map<String, String> p = new TreeMap<String, String>(params);
		p.remove("vnp_SecureHash");
		p.remove("vnp_SecureHashType");
		return hmacSHA512(buildQuery(p)).equalsIgnoreCase(received);
	}

	// ===== Hỗ trợ (bổ sung; protected để lớp cổng giả lập dùng lại cách ký) =====

	protected String toVnpAmount(BigDecimal amount) {
		// VNPay tính theo đơn vị nhỏ nhất: số tiền x 100
		return amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString();
	}

	protected String buildQuery(Map<String, String> sorted) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : new TreeMap<String, String>(sorted).entrySet()) {
			if (e.getValue() == null || e.getValue().isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append('&');
			}
			sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII))
					.append('=')
					.append(URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII));
		}
		return sb.toString();
	}

	protected String hmacSHA512(String data) {
		try {
			Mac mac = Mac.getInstance("HmacSHA512");
			mac.init(new SecretKeySpec(hashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
			byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(raw.length * 2);
			for (byte b : raw) {
				hex.append(String.format("%02x", b & 0xff));
			}
			return hex.toString();
		} catch (Exception e) {
			throw new IllegalStateException("Không ký được HMAC-SHA512", e);
		}
	}

	protected String getTmnCode() {
		return tmnCode;
	}
}
