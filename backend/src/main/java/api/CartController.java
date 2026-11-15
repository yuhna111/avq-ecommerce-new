package api;

import api.dto.CartDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.CartService;

/** UC07 thêm vào giỏ, UC08 xem/sửa giỏ. customerId lấy từ JWT (BR15), không nhận từ client. */
@RestController
@RequestMapping("/api/cart")
public class CartController {
	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	public CartDto view(@AuthenticationPrincipal(expression = "customerId") Long customerId) {
		return cartService.getCart(customerId);
	}

	/** POST /api/cart/items?variantId=&qty= ; thiếu variantId -> "Vui lòng chọn size và màu" (DT-02 R2). */
	@PostMapping("/items")
	public CartDto add(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@RequestParam(required = false) Long variantId, @RequestParam(defaultValue = "1") int qty) {
		return cartService.addToCart(customerId, variantId, qty);
	}

	@PutMapping("/items/{variantId}")
	public CartDto changeQty(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@PathVariable Long variantId, @RequestParam int qty) {
		return cartService.changeQty(customerId, variantId, qty);
	}

	@DeleteMapping("/items/{variantId}")
	public CartDto remove(@AuthenticationPrincipal(expression = "customerId") Long customerId,
			@PathVariable Long variantId) {
		return cartService.removeItem(customerId, variantId);
	}
}
