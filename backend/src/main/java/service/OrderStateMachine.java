package service;

import domain.Order;
import domain.OrderStatus;
import domain.OrderStatusHistory;
import domain.Staff;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.OrderRepository;
import repository.OrderStatusHistoryRepository;

/**
 * BR14 / DT-04: nơi DUY NHẤT đổi trạng thái đơn và ghi order_status_histories.
 * Đơn mới (status = null) được gán trạng thái đầu và ghi lịch sử fromStatus = null.
 */
@Component
public class OrderStateMachine {
	private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<OrderStatus, Set<OrderStatus>>(OrderStatus.class);
	static {
		TRANSITIONS.put(OrderStatus.CHO_THANH_TOAN, EnumSet.of(OrderStatus.CHO_XAC_NHAN, OrderStatus.DA_HUY));
		TRANSITIONS.put(OrderStatus.CHO_XAC_NHAN, EnumSet.of(OrderStatus.DANG_GIAO, OrderStatus.DA_HUY));
		TRANSITIONS.put(OrderStatus.DANG_GIAO, EnumSet.of(OrderStatus.DA_GIAO));
		TRANSITIONS.put(OrderStatus.DA_GIAO, EnumSet.noneOf(OrderStatus.class));
		TRANSITIONS.put(OrderStatus.DA_HUY, EnumSet.noneOf(OrderStatus.class));
	}
	private static final DateTimeFormatter TRACKING_DATE = DateTimeFormatter.ofPattern("yyMMdd");

	private final OrderRepository orderRepository;
	private final OrderStatusHistoryRepository orderStatusHistoryRepository;

	public OrderStateMachine(OrderRepository orderRepository, OrderStatusHistoryRepository orderStatusHistoryRepository) {
		this.orderRepository = orderRepository;
		this.orderStatusHistoryRepository = orderStatusHistoryRepository;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void transit(Order order, OrderStatus to, Staff staff, String note) {
		OrderStatus from = order.getStatus();
		if (from != null && !nextStatuses(from).contains(to)) {
			// Ô "–" trong DT-04: từ chối, không ghi lịch sử
			throw BusinessException.conflict("INVALID_TRANSITION",
					"Không thể chuyển đơn từ \"" + label(from) + "\" sang \"" + label(to) + "\"");
		}
		order.setStatus(to);
		if (to == OrderStatus.DANG_GIAO && order.getTrackingCode() == null) {
			// DB-R3: thiết kế không có tham số mã vận đơn -> hệ thống tự sinh khi chuyển sang Đang giao
			order.setTrackingCode(newTrackingCode());
		}
		orderRepository.save(order);

		OrderStatusHistory h = new OrderStatusHistory();
		h.setOrder(order);
		h.setFromStatus(from);
		h.setToStatus(to);
		h.setStaff(staff);
		h.setNote(note);
		h.setChangedAt(LocalDateTime.now());
		orderStatusHistoryRepository.save(h);
	}

	public Set<OrderStatus> nextStatuses(OrderStatus from) {
		// Bản sao để bên gọi không sửa được bảng chuyển trạng thái
		return EnumSet.copyOf(TRANSITIONS.get(from));
	}

	// ===== Hỗ trợ (private) =====

	private String newTrackingCode() {
		return "VD" + LocalDateTime.now().format(TRACKING_DATE) + ThreadLocalRandom.current().nextInt(100_000, 999_999);
	}

	static String label(OrderStatus s) {
		return switch (s) {
			case CHO_THANH_TOAN -> "Chờ thanh toán";
			case CHO_XAC_NHAN -> "Chờ xác nhận";
			case DANG_GIAO -> "Đang giao";
			case DA_GIAO -> "Đã giao";
			case DA_HUY -> "Đã hủy";
		};
	}
}
