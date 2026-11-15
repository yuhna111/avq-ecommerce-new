package api;

import api.dto.IpnResponse;
import infra.MockPaymentGatewayClient;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.PaymentCallbackException;
import service.PaymentService;

/**
 * UC10 kết quả thanh toán trực tuyến (DT-05).
 * LỆCH THIẾT KẾ: mockPay dùng MockPaymentGatewayClient (infra) để đóng vai cổng thanh toán bên ngoài
 * gửi IPN đã ký; đây là đường giả lập cho demo, kết quả vẫn đi qua PaymentService.handleCallback.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentCallbackController {
	private static final Logger log = LoggerFactory.getLogger(PaymentCallbackController.class);
	private final PaymentService paymentService;
	private final Optional<MockPaymentGatewayClient> mockGateway;

	public PaymentCallbackController(PaymentService paymentService, Optional<MockPaymentGatewayClient> mockGateway) {
		this.paymentService = paymentService;
		this.mockGateway = mockGateway;
	}

	/** IPN từ VNPay (thiết kế: void). Mã DT-05 được ghi nhật ký; HTTP 200. */
	@GetMapping("/vnpay-ipn")
	public void vnpayIpn(@RequestParam Map<String, String> params) {
		try {
			paymentService.handleCallback(params);
		} catch (PaymentCallbackException e) {
			log.warn("IPN {} -> RspCode {} ({})", params.get("vnp_TxnRef"), e.getRspCode(), e.getMessage());
		}
	}

	/** Trang thanh toán giả lập gọi: POST /api/payments/mock/{code}?success=true|false */
	@PostMapping("/mock/{code}")
	public IpnResponse mockPay(@PathVariable String code, @RequestParam boolean success) {
		MockPaymentGatewayClient gateway = mockGateway.orElseThrow(
				() -> service.BusinessException.notFound("MOCK_DISABLED", "Cổng thanh toán giả lập đang tắt"));
		Map<String, String> ipn = gateway.simulateCallback(code, success);
		if (ipn == null) {
			return new IpnResponse("01", "Không tìm thấy giao dịch cho đơn " + code + " (hãy đặt lại đơn)");
		}
		try {
			paymentService.handleCallback(ipn);
			return new IpnResponse("00", success ? "Thanh toán thành công" : "Giao dịch không thành công");
		} catch (PaymentCallbackException e) {
			return new IpnResponse(e.getRspCode(), e.getMessage());
		}
	}
}
