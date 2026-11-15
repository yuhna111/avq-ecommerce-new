package service;

import api.dto.CartDto;
import domain.Cart;
import domain.CartItem;
import domain.Customer;
import domain.ProductVariant;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CartRepository;
import repository.CustomerRepository;
import repository.ProductVariantRepository;

/**
 * DT-02. Dùng thẳng ProductVariantRepository, không gọi ProductService (design-notes mục 2).
 * R1 (chưa đăng nhập) do Spring Security chặn trước khi vào service.
 */
@Service
public class CartService {
	/** BR05 */
	private static final int MAX_QTY_PER_LINE = 10;

	private final CartRepository cartRepository;
	private final CustomerRepository customerRepository;
	private final ProductVariantRepository productVariantRepository;

	public CartService(CartRepository cartRepository, CustomerRepository customerRepository,
			ProductVariantRepository productVariantRepository) {
		this.cartRepository = cartRepository;
		this.customerRepository = customerRepository;
		this.productVariantRepository = productVariantRepository;
	}

	@Transactional(readOnly = true)
	public CartDto getCart(Long customerId) {
		return DtoMapper.cart(cartRepository.findByCustomerId(customerId).orElse(null));
	}

	@Transactional
	public CartDto addToCart(Long customerId, Long variantId, int qty) {
		// R2 - BR04
		if (variantId == null) {
			throw BusinessException.badRequest("VARIANT_REQUIRED", "Vui lòng chọn size và màu trước khi thêm vào giỏ");
		}
		if (qty < 1) {
			throw BusinessException.badRequest("INVALID_QTY", "Số lượng phải là số nguyên dương");
		}
		ProductVariant variant = sellableVariant(variantId);
		Cart cart = cartRepository.findByCustomerId(customerId).orElseGet(() -> newCart(customerId));
		int inCart = cart.findItem(variantId).map(CartItem::getQuantity).orElse(0);
		// R3 - BR05: số lượng cộng dồn <= 10 và <= tồn kho
		checkQuantity(variant, inCart + qty, inCart);
		cart.addOrIncrease(variant, qty); // R4 thêm dòng mới / R5 cộng dồn (BR07)
		cart.setUpdatedAt(LocalDateTime.now());
		return DtoMapper.cart(cartRepository.save(cart));
	}

	@Transactional
	public CartDto changeQty(Long customerId, Long variantId, int qty) {
		Cart cart = existingCart(customerId);
		CartItem item = cart.findItem(variantId)
				.orElseThrow(() -> BusinessException.notFound("CART_ITEM_NOT_FOUND", "Sản phẩm không có trong giỏ"));
		if (qty < 1) {
			throw BusinessException.badRequest("INVALID_QTY", "Số lượng phải là số nguyên dương");
		}
		checkQuantity(item.getVariant(), qty, 0);
		item.changeQty(qty);
		cart.setUpdatedAt(LocalDateTime.now());
		return DtoMapper.cart(cartRepository.save(cart));
	}

	@Transactional
	public CartDto removeItem(Long customerId, Long variantId) {
		Cart cart = existingCart(customerId);
		cart.removeItems(Set.of(variantId));
		cart.setUpdatedAt(LocalDateTime.now());
		return DtoMapper.cart(cartRepository.save(cart));
	}

	// ===== Hỗ trợ (private) =====

	private ProductVariant sellableVariant(Long variantId) {
		ProductVariant v = productVariantRepository.findById(variantId)
				.orElseThrow(() -> BusinessException.notFound("VARIANT_NOT_FOUND", "Không tìm thấy biến thể sản phẩm"));
		if (!v.getProduct().isVisible()) {
			throw BusinessException.notFound("PRODUCT_NOT_FOUND", "Sản phẩm đã ngừng bán");
		}
		return v;
	}

	private void checkQuantity(ProductVariant v, int wanted, int inCart) {
		if (wanted > MAX_QTY_PER_LINE) {
			throw BusinessException.badRequest("QTY_LIMIT", "Mỗi sản phẩm tối đa " + MAX_QTY_PER_LINE + " chiếc"
					+ (inCart > 0 ? " (trong giỏ đã có " + inCart + ")" : ""));
		}
		if (wanted > v.getStockQty()) {
			// EX-02
			throw BusinessException.conflict("OUT_OF_STOCK", "Chỉ còn " + v.getStockQty() + " sản phẩm size "
					+ v.getSize() + " màu " + v.getColor() + (inCart > 0 ? " (trong giỏ đã có " + inCart + ")" : ""));
		}
	}

	private Cart existingCart(Long customerId) {
		return cartRepository.findByCustomerId(customerId)
				.orElseThrow(() -> BusinessException.notFound("CART_NOT_FOUND", "Giỏ hàng đang trống"));
	}

	private Cart newCart(Long customerId) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> BusinessException.unauthorized("CUSTOMER_NOT_FOUND", "Vui lòng đăng nhập lại"));
		Cart cart = new Cart();
		cart.setCustomer(customer);
		cart.setUpdatedAt(LocalDateTime.now());
		return cart;
	}
}
