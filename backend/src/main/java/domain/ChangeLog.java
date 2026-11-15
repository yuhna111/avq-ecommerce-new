package domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_change_logs")
public class ChangeLog {
	@Column(name = "field_name", nullable = false, length = 30)
	private String fieldName;
	@Column(name = "old_value", length = 255)
	private String oldValue;
	@Column(name = "new_value", length = 255)
	private String newValue;
	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id")
	private Long id;
	// VP: public -> private + getter (CLAUDE.md 3.4). ChangeLog -> Product, Staff, ProductVariant một chiều.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "staff_id", nullable = false)
	private Staff staff;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variant_id")
	private ProductVariant productVariant;

	public static ChangeLog of(Staff staff, ProductVariant variant, String field, String oldValue, String newValue) {
		// FR31. Khi thay đổi cấp sản phẩm (variant = null), service gán product bằng setProduct.
		ChangeLog log = new ChangeLog();
		log.staff = staff;
		log.productVariant = variant;
		log.product = variant != null ? variant.getProduct() : null;
		log.fieldName = field;
		log.oldValue = oldValue;
		log.newValue = newValue;
		log.changedAt = LocalDateTime.now();
		return log;
	}

	// ===== Getter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getOldValue() {
		return oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public Product getProduct() {
		return product;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public ProductVariant getProductVariant() {
		return productVariant;
	}
}
