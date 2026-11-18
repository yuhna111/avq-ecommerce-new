package service;

import domain.Order;
import domain.OrderStatus;
import domain.Payment;
import domain.PaymentStatus;
import infra.PaymentGatewayClient;
import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.OrderRepository;
import repository.PaymentRepository;

/**
 * Chỉ phụ thuộc interface PaymentGatewayClient (design-notes mục 2): đổi cổng không phải sửa lớp này.
 */
@Service
public class PaymentService {
	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;
	private final PaymentGatewayClient paymentGatewayClient;
	private final OrderStateMachine orderStateMachine;
	private final String provider;

	public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository,
			PaymentGatewayClient paymentGatewayClient, OrderStateMachine orderStateMachine,
			@Value("${payment.provider:mock}") String provider) {
		this.paymentRepository = paymentRepository;
		this.orderRepository = orderRepository;
		this.paymentGatewayClient = paymentGatewayClient;
		this.orderStateMachine = orderStateMachine;
		this.provider = provider;
	}

	/** BR10: tạo bản ghi thanh toán "Đang xử lý" và trả về đường dẫn thanh toán của cổng. */
	@Transactional(propagation = Propagation.MANDATORY)
	public String createPayment(Order order) {
		newPayment(order);
		return paymentGatewayClient.buildPaymentUrl(order, order.getOrderCode());
	}

	/**
	 * DT-05. Trả về bình thường = mã "00" (đã ghi kết quả thành công/thất bại).
	 * Các mã khác ném PaymentCallbackException (xem lớp đó về lệch thiết kế kiểu trả về).
	 * noRollbackFor: trường hợp "04" vẫn lưu trạng thái Sai lệch.
	 */
	@Transactional(noRollbackFor = PaymentCallbackException.class)
	public void handleCallback(Map<String, String> params) {
		if (!paymentGatewayClient.verifySignature(params)) {
			log.warn("IPN sai chữ ký, bỏ qua: {}", params.get("vnp_TxnRef"));
			throw new PaymentCallbackException("97", "Invalid signature");
		}
		String orderCode = params.get("vnp_TxnRef");
		Order order = orderRepository.findByOrderCode(orderCode)
				.orElseThrow(() -> new PaymentCallbackException("01", "Order not found"));
		String txnRef = params.get("vnp_TransactionNo");
		if (txnRef != null && paymentRepository.existsByTxnRef(txnRef)) {
			throw new PaymentCallbackException("02", "Order already confirmed");
		}
		Payment payment = paymentRepository.findTopByOrderIdOrderByIdDesc(order.getId())
				.filter(p -> p.getStatus() == PaymentStatus.DANG_XU_LY)
				.orElseGet(() -> newPayment(order));

		BigDecimal amount = parseAmount(params.get("vnp_Amount"));
		if (amount == null || amount.compareTo(order.getTotal()) != 0) {
			payment.markMismatch(txnRef);
			paymentRepository.save(payment);
			log.warn("Đơn {}: số tiền cổng báo {} khác tổng đơn {} - cần nhân viên kiểm tra", orderCode, amount, order.getTotal());
			throw new PaymentCallbackException("04", "Invalid amount");
		}

		boolean success = "00".equals(params.get("vnp_ResponseCode"))
				&& (params.get("vnp_TransactionStatus") == null || "00".equals(params.get("vnp_TransactionStatus")));
		if (success && order.getStatus() != OrderStatus.CHO_THANH_TOAN) {
			// Khách đã trả tiền nhưng đơn không còn chờ thanh toán (đã tự hủy do quá hạn, hoặc đã thanh toán
			// bằng giao dịch khác): không hồi sinh đơn (kho đã hoàn), ghi "Sai lệch" để nhân viên kiểm tra, hoàn tiền.
			payment.markMismatch(txnRef);
			paymentRepository.save(payment);
			log.warn("Đơn {} đang ở trạng thái {} nhưng cổng báo thanh toán thành công (mã GD {}) - cần nhân viên hoàn tiền",
					orderCode, order.getStatus(), txnRef);
			throw new PaymentCallbackException("02", "Order already confirmed");
		}
		payment.markResult(success, txnRef);
		paymentRepository.save(payment);
		if (success) {
			orderStateMachine.transit(order, OrderStatus.CHO_XAC_NHAN, null,
					"Thanh toán trực tuyến thành công (mã GD " + txnRef + ")");
		}
		// Thất bại: đơn giữ "Chờ thanh toán" đến hạn 15 phút (EX-03)
	}

	// ===== Hỗ trợ (private) =====

	private Payment newPayment(Order order) {
		Payment p = new Payment();
		p.setOrder(order);
		p.setMethod(provider.toUpperCase());
		p.setAmount(order.getTotal());
		p.setStatus(PaymentStatus.DANG_XU_LY);
		return paymentRepository.save(p);
	}

	private BigDecimal parseAmount(String vnpAmount) {
		try {
			return new BigDecimal(vnpAmount).movePointLeft(2).stripTrailingZeros().setScale(0);
		} catch (RuntimeException e) {
			return null;
		}
	}
}
