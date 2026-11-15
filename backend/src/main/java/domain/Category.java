package domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Long id;
	@Column(name = "name", nullable = false, length = 80)
	private String name;
	// LỆCH THIẾT KẾ: thuộc tính slug KHÔNG có trong lớp Category của VP, nhưng bảng categories có
	// cột slug (NOT NULL, UNIQUE) và CategoryRepository.findBySlug dùng nó. Bổ sung, ghi vào báo cáo
	// kèm đề xuất thêm "slug : String" vào lớp Category trong VP.
	@Column(name = "slug", nullable = false, length = 80, unique = true)
	private String slug;

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

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}
}
