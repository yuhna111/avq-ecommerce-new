package domain;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class Order {
	private Long id;
	private String orderCode;
	private LocalDateTime orderDate;
	private String receiverName;
	private String receiverPhone;
	private String shippingAddress;
	private BigDecimal subtotal;
	private BigDecimal shippingFee;
	private BigDecimal total;
	private PaymentMethod paymentMethod;
	private OrderStatus status;
	private LocalDateTime expiresAt;
	private int version;
	private String trackingCode;
	public Customer customer;
	public ArrayList<OrderItem> items = new ArrayList<OrderItem>();

	public void addItem(ProductVariant variant, int qty) {
		throw new UnsupportedOperationException();
	}

	public void recalculate() {
		throw new UnsupportedOperationException();
	}

	public Set<Long> getVariantIds() {
		throw new UnsupportedOperationException();
	}
}