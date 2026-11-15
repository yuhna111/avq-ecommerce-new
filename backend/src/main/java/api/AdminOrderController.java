package api;

import domain.OrderStatus;
import api.dto.OrderDetailDto;
import api.dto.OrderSummaryDto;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.OrderService;

/** UC20 nhân viên xem và cập nhật trạng thái đơn (DT-03, DT-04). */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
	private final OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public List<OrderSummaryDto> list(@RequestParam(required = false) OrderStatus status) {
		return orderService.listForStaff(status);
	}

	@GetMapping("/{code}")
	public OrderDetailDto detail(@PathVariable String code) {
		return orderService.getDetailForStaff(code);
	}

	/** PUT /api/admin/orders/{code}/status?to=DANG_GIAO&note=... */
	@PutMapping("/{code}/status")
	public OrderDetailDto updateStatus(@AuthenticationPrincipal(expression = "staffId") Long staffId,
			@PathVariable String code, @RequestParam OrderStatus to, @RequestParam(required = false) String note) {
		return orderService.updateStatusByStaff(staffId, code, to, note);
	}
}
