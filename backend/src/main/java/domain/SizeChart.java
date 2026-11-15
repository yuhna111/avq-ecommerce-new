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

@Entity
@Table(name = "size_charts")
public class SizeChart {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chart_id")
	private Long id;
	@Column(name = "height_min", nullable = false, columnDefinition = "smallint")
	private int heightMin;
	@Column(name = "size", nullable = false, length = 5)
	private String size;
	@Column(name = "height_max", nullable = false, columnDefinition = "smallint")
	private int heightMax;
	@Column(name = "weight_min", nullable = false, columnDefinition = "smallint")
	private int weightMin;
	@Column(name = "weight_max", nullable = false, columnDefinition = "smallint")
	private int weightMax;
	// VP: public -> private + getter (CLAUDE.md 3.4). SizeChart -> Product một chiều.
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	public boolean contains(int heightCm, int weightKg) {
		return heightCm >= this.heightMin && heightCm <= this.heightMax
				&& weightKg >= this.weightMin && weightKg <= this.weightMax;
	}

	// ===== Getter/Setter (bổ sung - CLAUDE.md 3.3) =====

	public Long getId() {
		return id;
	}

	public int getHeightMin() {
		return heightMin;
	}

	public void setHeightMin(int heightMin) {
		this.heightMin = heightMin;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public int getHeightMax() {
		return heightMax;
	}

	public void setHeightMax(int heightMax) {
		this.heightMax = heightMax;
	}

	public int getWeightMin() {
		return weightMin;
	}

	public void setWeightMin(int weightMin) {
		this.weightMin = weightMin;
	}

	public int getWeightMax() {
		return weightMax;
	}

	public void setWeightMax(int weightMax) {
		this.weightMax = weightMax;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
}
