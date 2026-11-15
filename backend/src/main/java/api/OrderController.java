package api;

import api.dto.CheckoutSummaryDto;
import api.dto.OrderDetailDto;
import api.dto.OrderResultDto;
import api.dto.OrderSummaryDto;
import api.dto.PlaceOrderRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.OrderService;

/** UC09 đặt hàng, UC11 theo dõi đơn, UC12 hủy đơn. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/checkout")
	public CheckoutSummaryDto prepare(@AuthenticationPrincipal(expression = "customerId") Long customerId) {
		return orderService.prepare(customerId);
	}

	@PostMapping
	public OrderResultDto place(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@Valid @RequestBody PlaceOrderRequest req) {
		return orderService.place(customerId, req);
	}

	@GetMapping
	public List<OrderSummaryDto> list(@AuthenticationPrincipal(expression = "customerId") Long customerId) {
		return orderService.listByCustomer(customerId);
	}

	@GetMapping("/{code}")
	public OrderDetailDto detail(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@PathVariable String code) {
		return orderService.getDetail(customerId, code);
	}

	@PostMapping("/{code}/cancel")
	public OrderDetailDto cancel(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@PathVariable String code, @RequestParam(required = false) String reason) {
		return orderService.cancelByCustomer(customerId, code, reason);
	}
}
