package domain;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Long id;
	@Column(name = "order_code", nullable = false, length = 20, unique = true)
	private String orderCode;
	@Column(name = "order_date", nullable = false)
	private LocalDateTime orderDate;
	@Column(name = "receiver_name", nullable = false, length = 100)
	private String receiverName;
	@Column(name = "receiver_phone", nullable = false, length = 15)
	private String receiverPhone;
	@Column(name = "shipping_address", nullable = false, length = 255)
	private String shippingAddress;
	@Column(name = "subtotal", nullable = false, precision = 12, scale = 0)
	private BigDecimal subtotal;
	@Column(name = "shipping_fee", nullable = false, precision = 12, scale = 0)
	private BigDecimal shippingFee;
	@Column(name = "total", nullable = false, precision = 12, scale = 0)
	private BigDecimal total;
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, columnDefinition = "enum('COD','ONLINE')")
	private PaymentMethod paymentMethod;
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false,
			columnDefinition = "enum('CHO_THANH_TOAN','CHO_XAC_NHAN','DANG_GIAO','DA_GIAO','DA_HUY')")
	private OrderStatus status;
	@Column(name = "expires_at")
	private LocalDateTime expiresAt;
	// VP: int -> long vì cột version là BIGINT (CLAUDE.md 3.4). Khóa lạc quan DB-R2.
	@Version
	@Column(name = "version", nullable = false)
	private long version;
	@Column(name = "tracking_code", length = 40)
	private String trackingCode;
	// VP: public -> private + getter (CLAUDE.md 3.4). Order -> Customer một chiều.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;
	// VP: public ArrayList<OrderItem> -> private List<OrderItem>. Order ◆ OrderItem hai chiều.
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<OrderItem>();

	public void addItem(ProductVariant variant, int qty) {
		// BR08: chốt giá hiện hành và thông tin biến thể vào order_items
		OrderItem item = new OrderItem();
		item.setOrder(this);
		item.setVariant(variant);
		item.setProductName(variant.getProduct().getName());
		item.setSize(variant.getSize());
		item.setColor(variant.getColor());
		item.setUnitPrice(variant.getProduct().getPrice());
		item.setQuantity(qty);
		this.items.add(item);
	}

	public void recalculate() {
		// DB-R4: total = subtotal + shipping_fee
		this.subtotal = this.items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		if (this.shippingFee == null) {
			this.shippingFee = BigDecimal.ZERO;
		}
		this.total = this.subtotal.add(this.shippingFee);
	}

	public Set<Long> getVariantIds() {
		Set<Long> ids = new HashSet<Long>();
		for (OrderItem item : this.items) {
			ids.add(item.getVariant().getId());
		}
		return ids;
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public BigDecimal getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(BigDecimal shippingFee) {
		this.shippingFee = shippingFee;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public long getVersion() {
		return version;
	}

	public String getTrackingCode() {
		return trackingCode;
	}

	public void setTrackingCode(String trackingCode) {
		this.trackingCode = trackingCode;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<OrderItem> getItems() {
		return items;
	}
}
