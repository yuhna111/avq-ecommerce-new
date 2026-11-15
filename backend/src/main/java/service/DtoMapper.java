package service;

import api.dto.AddressDto;
import api.dto.CartDto;
import api.dto.CategoryDto;
import api.dto.OrderDetailDto;
import api.dto.OrderSummaryDto;
import api.dto.ProductCardDto;
import api.dto.ProductDetailDto;
import domain.Address;
import domain.Cart;
import domain.CartItem;
import domain.Category;
import domain.Order;
import domain.OrderItem;
import domain.OrderStatus;
import domain.OrderStatusHistory;
import domain.Product;
import domain.ProductImage;
import domain.ProductVariant;
import java.util.Comparator;
import java.util.List;

/**
 * Chuyển entity -> DTO dùng chung cho các service (bổ sung - lớp hỗ trợ, không có trong VP).
 * Chỉ đọc dữ liệu, không chứa quy tắc nghiệp vụ.
 */
final class DtoMapper {

	private DtoMapper() {
	}

	static CategoryDto category(Category c) {
		return new CategoryDto(c.getId(), c.getName(), c.getSlug());
	}

	static String mainImage(Product p) {
		return p.getImage().stream()
				.sorted(Comparator.comparing(ProductImage::isMain).reversed().thenComparing(ProductImage::getId))
				.map(ProductImage::getUrl)
				.findFirst()
				.orElse(null);
	}

	static ProductCardDto productCard(Product p) {
		List<ProductVariant> vs = p.getVariants();
		return new ProductCardDto(p.getId(), p.getName(), p.getPrice(), mainImage(p), p.getCategory().getName(),
				p.getFitType(), p.isVisible(),
				vs.stream().anyMatch(v -> v.getStockQty() > 0),
				vs.stream().map(ProductVariant::getSize).distinct().toList(),
				vs.stream().map(ProductVariant::getColor).distinct().toList());
	}

	static ProductDetailDto productDetail(Product p, boolean hasSizeChart) {
		List<String> images = p.getImage().stream()
				.sorted(Comparator.comparing(ProductImage::isMain).reversed().thenComparing(ProductImage::getId))
				.map(ProductImage::getUrl)
				.toList();
		List<ProductDetailDto.VariantDto> variants = p.getVariants().stream()
				.sorted(Comparator.comparing(ProductVariant::getId))
				.map(v -> new ProductDetailDto.VariantDto(v.getId(), v.getSku(), v.getSize(), v.getColor(), v.getStockQty()))
				.toList();
		return new ProductDetailDto(p.getId(), p.getName(), p.getDescription(), p.getMaterial(), p.getPrice(),
				p.isVisible(), p.getFitType(), category(p.getCategory()), images, variants, hasSizeChart);
	}

	static CartDto.CartLineDto cartLine(CartItem i) {
		ProductVariant v = i.getVariant();
		Product p = v.getProduct();
		return new CartDto.CartLineDto(v.getId(), p.getId(), p.getName(), v.getSize(), v.getColor(), mainImage(p),
				p.getPrice(), i.getQuantity(), i.lineTotal(), v.getStockQty());
	}

	static CartDto cart(Cart cart) {
		if (cart == null) {
			return new CartDto(null, List.of(), java.math.BigDecimal.ZERO, 0);
		}
		List<CartDto.CartLineDto> lines = cart.getItems().stream()
				.sorted(Comparator.comparing(CartItem::getAddedAt))
				.map(DtoMapper::cartLine)
				.toList();
		int qty = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
		return new CartDto(cart.getId(), lines, cart.subtotal(), qty);
	}

	static AddressDto address(Address a) {
		return new AddressDto(a.getId(), a.getReceiverName(), a.getReceiverPhone(), a.getAddressLine(), a.isDefault());
	}

	static OrderSummaryDto orderSummary(Order o) {
		int count = o.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
		return new OrderSummaryDto(o.getOrderCode(), o.getOrderDate(), o.getStatus(), o.getTotal(),
				o.getPaymentMethod(), count, o.getCustomer().getFullName(), o.getReceiverName());
	}

	static OrderDetailDto orderDetail(Order o, List<OrderStatusHistory> histories, String paymentStatus,
			List<OrderStatus> nextStatuses, boolean cancellableByCustomer) {
		List<OrderDetailDto.LineDto> lines = o.getItems().stream()
				.map(i -> new OrderDetailDto.LineDto(i.getVariant().getId(), i.getProductName(), i.getSize(),
						i.getColor(), i.getUnitPrice(), i.getQuantity(), i.lineTotal()))
				.toList();
		List<OrderDetailDto.HistoryDto> hs = histories.stream()
				.map(h -> new OrderDetailDto.HistoryDto(h.getFromStatus(), h.getToStatus(), h.getNote(), h.getChangedAt(),
						h.getStaff() != null ? "Nhân viên " + h.getStaff().getFullName() : null))
				.toList();
		return new OrderDetailDto(o.getOrderCode(), o.getOrderDate(), o.getStatus(), o.getCustomer().getFullName(),
				o.getReceiverName(), o.getReceiverPhone(), o.getShippingAddress(), o.getSubtotal(), o.getShippingFee(),
				o.getTotal(), o.getPaymentMethod(), paymentStatus, o.getExpiresAt(), o.getTrackingCode(), lines, hs,
				nextStatuses, cancellableByCustomer);
	}
}
