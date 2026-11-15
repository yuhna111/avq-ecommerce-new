package repository;

import domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên.
public interface ProductRepository extends JpaRepository<Product, Long> {

	// Lọc động theo ProductFilter: mỗi điều kiện bỏ qua khi tham số null.
	// Dùng chỉ mục idx_product_cat (category_id, is_visible) - DB-R5.
	@Query("""
			select p from Product p
			where (:#{#filter.includeHidden} = true or p.visible = true)
			  and (:#{#filter.categoryId} is null or p.category.id = :#{#filter.categoryId})
			  and (:#{#filter.keywordPattern} is null
			       or lower(p.name) like :#{#filter.keywordPattern}
			       or lower(p.description) like :#{#filter.keywordPattern})
			  and (:#{#filter.minPrice} is null or p.price >= :#{#filter.minPrice})
			  and (:#{#filter.maxPrice} is null or p.price <= :#{#filter.maxPrice})
			  and (:#{#filter.fitType} is null or p.fitType = :#{#filter.fitType})
			  and (:#{#filter.size} is null or exists (
			       select v from ProductVariant v where v.product = p and v.size = :#{#filter.size}))
			  and (:#{#filter.color} is null or exists (
			       select v from ProductVariant v where v.product = p and v.color = :#{#filter.color}))
			order by p.id desc
			""")
	public List<Product> search(@Param("filter") ProductFilter filter);

	public Optional<Product> findByIdAndVisibleTrue(Long id);
}
