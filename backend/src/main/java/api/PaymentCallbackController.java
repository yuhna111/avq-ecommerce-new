package api;

import api.dto.IpnResponse;
import app.security.AuthUser;
import domain.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import infra.MockPaymentGatewayClient;
import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.BusinessException;
import service.OrderService;
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
	/** Kiểm tra đơn thuộc khách đang đăng nhập trước khi giả lập thanh toán (BR15). */
	private final OrderService orderService;
	private final Optional<MockPaymentGatewayClient> mockGateway;
	/** Proxy request hiện tại (Spring tiêm): đọc các tham số vnp_* của link đã ký mà không đổi chữ ký mockPay. */
	private final HttpServletRequest request;

	public PaymentCallbackController(PaymentService paymentService, OrderService orderService,
			Optional<MockPaymentGatewayClient> mockGateway, HttpServletRequest request) {
		this.paymentService = paymentService;
		this.orderService = orderService;
		this.mockGateway = mockGateway;
		this.request = request;
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
				() -> BusinessException.notFound("MOCK_DISABLED", "Cổng thanh toán giả lập đang tắt"));
		// BR15: khách chỉ thanh toán đơn của chính mình (đơn của người khác -> ORDER_NOT_FOUND, như xem chi tiết)
		OrderStatus status = orderService.getDetail(currentCustomerId(), code).status();
		if (status != OrderStatus.CHO_THANH_TOAN) {
			throw BusinessException.conflict("ORDER_NOT_PENDING",
					"Đơn " + code + " không còn chờ thanh toán (đã quá hạn, đã hủy hoặc đã thanh toán)");
		}
		Map<String, String> link = signedLinkParams();
		if (code.equals(link.get("vnp_TxnRef"))) {
			gateway.restoreIssued(link); // link đã ký: khôi phục giao dịch sau khi máy chủ khởi động lại
		}
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

	private static Long currentCustomerId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Long id = auth != null && auth.getPrincipal() instanceof AuthUser u ? u.getCustomerId() : null;
		if (id == null) {
			throw BusinessException.unauthorized("UNAUTHORIZED", "Vui lòng đăng nhập để tiếp tục");
		}
		return id;
	}

	/** Các tham số vnp_* (link thanh toán đã ký) mà trang /mock-pay gửi kèm trên query string. */
	private Map<String, String> signedLinkParams() {
		Map<String, String> p = new TreeMap<String, String>();
		request.getParameterMap().forEach((k, v) -> {
			if (k.startsWith("vnp_") && v.length > 0) {
				p.put(k, v[0]);
			}
		});
		return p;
	}
}
