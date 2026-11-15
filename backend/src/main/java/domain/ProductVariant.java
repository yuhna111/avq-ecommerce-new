package domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "variant_id")
	private Long id;
	@Column(name = "sku", nullable = false, length = 40, unique = true)
	private String sku;
	@Column(name = "size", nullable = false, length = 5)
	private String size;
	@Column(name = "color", nullable = false, length = 30)
	private String color;
	@Column(name = "stock_qty", nullable = false)
	private int stockQty;
	// VP: int -> long vì cột version là BIGINT (CLAUDE.md 3.4). Khóa lạc quan DB-R1.
	@Version
	@Column(name = "version", nullable = false)
	private long version;
	// VP: public -> private + getter (CLAUDE.md 3.4)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	public void setStock(int qty) {
		throw new UnsupportedOperationException();
	}

	public boolean isAvailable(int qty) {
		throw new UnsupportedOperationException();
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getStockQty() {
		return stockQty;
	}

	public long getVersion() {
		return version;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
}
