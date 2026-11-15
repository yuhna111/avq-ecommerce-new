package domain;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

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

@Entity
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long id;
	@Column(name = "name", nullable = false, length = 150)
	private String name;
	@Column(name = "description", columnDefinition = "text")
	private String description;
	@Column(name = "material", length = 80)
	private String material;
	@Column(name = "price", nullable = false, precision = 12, scale = 0)
	private BigDecimal price;
	@Column(name = "is_visible", nullable = false)
	private boolean visible;
	@Enumerated(EnumType.STRING)
	@Column(name = "fit_type", nullable = false, columnDefinition = "enum('SLIM','REGULAR','OVERSIZE')")
	private FitType fitType;
	// VP: public ArrayList<ProductVariant> -> private List<ProductVariant>. Product ◆ ProductVariant hai chiều.
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductVariant> variants = new ArrayList<ProductVariant>();
	// VP: public -> private + getter. Product -> Category một chiều.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
	// VP: public ArrayList<ProductImage> -> private List<ProductImage>. Product ◆ ProductImage hai chiều.
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductImage> image = new ArrayList<ProductImage>();

	public void updateInfo(BigDecimal price, String description, String material) {
		// Tham số null = giữ nguyên. BR06: giá > 0
		if (price != null) {
			if (price.signum() <= 0) {
				throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
			}
			this.price = price;
		}
		if (description != null) {
			this.description = description;
		}
		if (material != null) {
			this.material = material;
		}
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public ProductVariant addVariant(String size, String color, String sku) {
		ProductVariant variant = new ProductVariant();
		variant.setProduct(this);
		variant.setSize(size);
		variant.setColor(color);
		variant.setSku(sku);
		this.variants.add(variant);
		return variant;
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public String getMaterial() {
		return material;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public boolean isVisible() {
		return visible;
	}

	public FitType getFitType() {
		return fitType;
	}

	public void setFitType(FitType fitType) {
		this.fitType = fitType;
	}

	public List<ProductVariant> getVariants() {
		return variants;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<ProductImage> getImage() {
		return image;
	}
}
