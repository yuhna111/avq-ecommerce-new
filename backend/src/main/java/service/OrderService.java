package service;

import api.dto.CheckoutSummaryDto;
import api.dto.OrderDetailDto;
import api.dto.OrderResultDto;
import api.dto.OrderSummaryDto;
import api.dto.PlaceOrderRequest;
import domain.Cart;
import domain.CartItem;
import domain.Customer;
import domain.Order;
import domain.OrderStatus;
import domain.PaymentMethod;
import domain.Staff;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.AddressRepository;
import repository.CartRepository;
import repository.CustomerRepository;
import repository.OrderRepository;
import repository.OrderStatusHistoryRepository;
import repository.PaymentRepository;
import repository.StaffRepository;

@Service
public class OrderService {
	/** BR12: hạn thanh toán trực tuyến */
	private static final int PAYMENT_TIMEOUT_MINUTES = 15;
	/** DT-03: khách chỉ hủy ở hai trạng thái này (BR13) */
	private static final EnumSet<OrderStatus> CUSTOMER_CANCELLABLE = EnumSet.of(OrderStatus.CHO_THANH_TOAN, OrderStatus.CHO_XAC_NHAN);
	private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd");

	private final OrderRepository orderRepository;
	private final CartRepository cartRepository;
	private final CustomerRepository customerRepository;
	private final StaffRepository staffRepository;
	private final AddressRepository addressRepository;
	private final OrderStatusHistoryRepository orderStatusHistoryRepository;
	private final PaymentRepository paymentRepository;
	private final StockService stockService;
	private final ShippingFeeCalculator shippingFeeCalculator;
	private final OrderStateMachine orderStateMachine;
	private final PaymentService paymentService;

	public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
			CustomerRepository customerRepository, StaffRepository staffRepository, AddressRepository addressRepository,
			OrderStatusHistoryRepository orderStatusHistoryRepository, PaymentRepository paymentRepository,
			StockService stockService, ShippingFeeCalculator shippingFeeCalculator, OrderStateMachine orderStateMachine,
			PaymentService paymentService) {
		this.orderRepository = orderRepository;
		this.cartRepository = cartRepository;
		this.customerRepository = customerRepository;
		this.staffRepository = staffRepository;
		this.addressRepository = addressRepository;
		this.orderStatusHistoryRepository = orderStatusHistoryRepository;
		this.paymentRepository = paymentRepository;
		this.stockService = stockService;
		this.shippingFeeCalculator = shippingFeeCalculator;
		this.orderStateMachine = orderStateMachine;
		this.paymentService = paymentService;
	}

	/** UC09 bước 1 - BR08: hiển thị giá hiện hành, phí giao (DT-01) trước khi khách xác nhận. */
	@Transactional(readOnly = true)
	public CheckoutSummaryDto prepare(Long customerId) {
		Cart cart = nonEmptyCart(customerId);
		BigDecimal subtotal = cart.subtotal();
		BigDecimal fee = shippingFeeCalculator.calculate(subtotal);
		return new CheckoutSummaryDto(
				cart.getItems().stream().sorted(Comparator.comparing(CartItem::getAddedAt)).map(DtoMapper::cartLine).toList(),
				subtotal, fee, subtotal.add(fee),
				addressRepository.findByCustomerId(customerId).stream().map(DtoMapper::address).toList());
	}

	/**
	 * UC09 - một giao dịch (NFR05): chốt giá (BR08) -> phí (BR09) -> TRỪ KHO TRƯỚC (BR11) -> lưu đơn qua
	 * OrderStateMachine (lịch sử "Tạo đơn") -> tạo Payment nếu ONLINE (BR10) -> bỏ các dòng đã đặt khỏi giỏ.
	 */
	@Transactional
	public OrderResultDto place(Long customerId, PlaceOrderRequest req) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> BusinessException.unauthorized("CUSTOMER_NOT_FOUND", "Vui lòng đăng nhập lại"));
		Cart cart = nonEmptyCart(customerId);
		LocalDateTime now = LocalDateTime.now();

		Order order = new Order();
		order.setCustomer(customer);
		order.setOrderCode(newOrderCode(now));
		order.setOrderDate(now);
		order.setReceiverName(req.receiverName().trim());
		order.setReceiverPhone(req.receiverPhone().trim());
		order.setShippingAddress(req.shippingAddress().trim());
		order.setPaymentMethod(req.paymentMethod());
		for (CartItem item : cart.getItems()) {
			if (!item.getVariant().getProduct().isVisible()) {
				throw BusinessException.conflict("PRODUCT_UNAVAILABLE",
						"Sản phẩm \"" + item.getVariant().getProduct().getName() + "\" đã ngừng bán, vui lòng bỏ khỏi giỏ");
			}
			order.addItem(item.getVariant(), item.getQuantity());
		}
		order.setShippingFee(BigDecimal.ZERO);
		order.recalculate();
		order.setShippingFee(shippingFeeCalculator.calculate(order.getSubtotal()));
		order.recalculate();

		// BR11: trừ kho TRƯỚC khi lưu đơn (thiếu hàng -> ném lỗi, rollback toàn bộ)
		stockService.reserve(order.getItems());

		OrderStatus initial;
		if (req.paymentMethod() == PaymentMethod.COD) {
			initial = OrderStatus.CHO_XAC_NHAN;
		} else {
			initial = OrderStatus.CHO_THANH_TOAN;
			order.setExpiresAt(now.plusMinutes(PAYMENT_TIMEOUT_MINUTES));
		}
		orderStateMachine.transit(order, initial, null, "Tạo đơn");

		String paymentUrl = null;
		if (req.paymentMethod() == PaymentMethod.ONLINE) {
			paymentUrl = paymentService.createPayment(order);
		}

		cart.removeItems(order.getVariantIds());
		cart.setUpdatedAt(now);
		cartRepository.save(cart);

		return new OrderResultDto(order.getOrderCode(), order.getStatus(), order.getTotal(), order.getPaymentMethod(),
				paymentUrl, order.getExpiresAt());
	}

	/** UC12 - BR13/DT-03: khách hủy ở Chờ thanh toán / Chờ xác nhận; hoàn kho (BR11). */
	@Transactional
	public OrderDetailDto cancelByCustomer(Long customerId, String orderCode, String reason) {
		Order order = customerOrder(customerId, orderCode);
		if (!CUSTOMER_CANCELLABLE.contains(order.getStatus())) {
			// EX-05
			throw BusinessException.conflict("CANCEL_NOT_ALLOWED",
					"Đơn đang ở trạng thái \"" + OrderStateMachine.label(order.getStatus())
							+ "\" nên không thể hủy. Vui lòng liên hệ cửa hàng để được hỗ trợ.");
		}
		String note = "Khách hủy đơn" + (reason == null || reason.isBlank() ? "" : ": " + reason.trim());
		orderStateMachine.transit(order, OrderStatus.DA_HUY, null, note);
		stockService.restore(order.getItems());
		return detail(order);
	}

	/**
	 * UC20 - DT-03/DT-04: nhân viên chuyển Chờ xác nhận -> Đang giao (sinh mã vận đơn) -> Đã giao,
	 * hoặc hủy đơn Chờ xác nhận (bắt buộc lý do, hoàn kho). Không được xác nhận đơn Chờ thanh toán.
	 */
	@Transactional
	public OrderDetailDto updateStatusByStaff(Long staffId, String orderCode, OrderStatus to, String note) {
		Staff staff = staffRepository.findById(staffId)
				.orElseThrow(() -> BusinessException.forbidden("STAFF_NOT_FOUND", "Không tìm thấy nhân viên"));
		Order order = orderRepository.findByOrderCode(orderCode)
				.orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng " + orderCode));
		if (to == null) {
			throw BusinessException.badRequest("STATUS_REQUIRED", "Vui lòng chọn trạng thái mới");
		}
		if (order.getStatus() == OrderStatus.CHO_THANH_TOAN) {
			throw BusinessException.conflict("WAITING_PAYMENT",
					"Đơn đang chờ khách thanh toán trực tuyến; hệ thống sẽ tự cập nhật khi có kết quả thanh toán");
		}
		if (to == OrderStatus.DA_HUY && (note == null || note.isBlank())) {
			throw BusinessException.badRequest("REASON_REQUIRED", "Nhân viên hủy đơn phải nhập lý do");
		}
		orderStateMachine.transit(order, to, staff, note == null || note.isBlank() ? null : note.trim());
		if (to == OrderStatus.DA_HUY) {
			stockService.restore(order.getItems());
		}
		return staffDetail(order);
	}

	/** BR12: hủy các đơn Chờ thanh toán quá hạn và hoàn kho. Trả về số đơn đã hủy. */
	@Transactional
	public int expireOverdue(LocalDateTime now) {
		List<Order> overdue = orderRepository.findByStatusAndExpiresAtBefore(OrderStatus.CHO_THANH_TOAN, now);
		for (Order order : overdue) {
			orderStateMachine.transit(order, OrderStatus.DA_HUY, null, "Hệ thống tự hủy: quá " + PAYMENT_TIMEOUT_MINUTES + " phút chưa thanh toán");
			stockService.restore(order.getItems());
		}
		return overdue.size();
	}

	// ===== Truy vấn cho màn hình (BỔ SUNG - thiết kế Hình 4.7 thiếu phương thức service cho
	// OrderController.list/detail và AdminOrderController.list/detail; ghi vào báo cáo lệch thiết kế) =====

	@Transactional(readOnly = true)
	public List<OrderSummaryDto> listByCustomer(Long customerId) {
		return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId).stream().map(DtoMapper::orderSummary).toList();
	}

	@Transactional(readOnly = true)
	public OrderDetailDto getDetail(Long customerId, String orderCode) {
		return detail(customerOrder(customerId, orderCode));
	}

	@Transactional(readOnly = true)
	public List<OrderSummaryDto> listForStaff(OrderStatus status) {
		Sort newestFirst = Sort.by(Sort.Direction.DESC, "orderDate");
		List<Order> orders;
		if (status == null) {
			orders = orderRepository.findAll(newestFirst);
		} else {
			// Lọc trong CSDL (WHERE status = ?) bằng findAll(Example, Sort) kế thừa từ JpaRepository,
			// không thêm phương thức vào OrderRepository. Bỏ qua "version" (kiểu long, mặc định 0).
			Order probe = new Order();
			probe.setStatus(status);
			orders = orderRepository.findAll(Example.of(probe, ExampleMatcher.matching().withIgnorePaths("version")), newestFirst);
		}
		return orders.stream().map(DtoMapper::orderSummary).toList();
	}

	@Transactional(readOnly = true)
	public OrderDetailDto getDetailForStaff(String orderCode) {
		return staffDetail(orderRepository.findByOrderCode(orderCode)
				.orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng " + orderCode)));
	}

	// ===== Hỗ trợ (private) =====

	private Cart nonEmptyCart(Long customerId) {
		return cartRepository.findByCustomerId(customerId)
				.filter(c -> !c.getItems().isEmpty())
				.orElseThrow(() -> BusinessException.badRequest("EMPTY_CART", "Giỏ hàng đang trống"));
	}

	/** BR15: khách chỉ xem/hủy đơn của chính mình. */
	private Order customerOrder(Long customerId, String orderCode) {
		Order order = orderRepository.findByOrderCode(orderCode)
				.orElseThrow(() -> BusinessException.notFound("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng " + orderCode));
		if (!order.getCustomer().getId().equals(customerId)) {
			throw BusinessException.notFound("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng " + orderCode);
		}
		return order;
	}

	private String newOrderCode(LocalDateTime now) {
		String code;
		do {
			code = "AVQ" + now.format(CODE_DATE) + ThreadLocalRandom.current().nextInt(100_000, 999_999);
		} while (orderRepository.findByOrderCode(code).isPresent());
		return code;
	}

	private String paymentStatus(Order order) {
		return paymentRepository.findTopByOrderIdOrderByIdDesc(order.getId()).map(p -> p.getStatus().name()).orElse(null);
	}

	private OrderDetailDto detail(Order order) {
		return DtoMapper.orderDetail(order, orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(order.getId()),
				paymentStatus(order), List.of(), CUSTOMER_CANCELLABLE.contains(order.getStatus()));
	}

	private OrderDetailDto staffDetail(Order order) {
		// Nhân viên không xác nhận được đơn Chờ thanh toán (chỉ hệ thống khi thanh toán thành công)
		List<OrderStatus> next = new ArrayList<OrderStatus>();
		if (order.getStatus() != OrderStatus.CHO_THANH_TOAN) {
			next.addAll(orderStateMachine.nextStatuses(order.getStatus()));
		}
		return DtoMapper.orderDetail(order, orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(order.getId()),
				paymentStatus(order), next, false);
	}
}
