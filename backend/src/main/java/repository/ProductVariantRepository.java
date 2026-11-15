package repository;

import domain.ProductVariant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Bổ sung: extends JpaRepository (CLAUDE.md 3.4); các phương thức VP giữ nguyên.
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

	/**
	 * BR11: trừ kho có điều kiện trong MỘT câu UPDATE (nguyên tử ở mức dòng).
	 * Trả về 0 nghĩa là không đủ hàng -> service rollback. Tăng version để khóa lạc quan
	 * (DB-R1) phát hiện xung đột với nhân viên đang sửa tồn kho.
	 */
	@Modifying(flushAutomatically = true)
	@Query(value = "UPDATE product_variants SET stock_qty = stock_qty - :qty, version = version + 1 "
			+ "WHERE variant_id = :variantId AND stock_qty >= :qty", nativeQuery = true)
	public int decreaseIfEnough(@Param("variantId") Long variantId, @Param("qty") int qty);

	/** BR11: hoàn kho khi đơn bị hủy. */
	@Modifying(flushAutomatically = true)
	@Query(value = "UPDATE product_variants SET stock_qty = stock_qty + :qty, version = version + 1 "
			+ "WHERE variant_id = :variantId", nativeQuery = true)
	public int increase(@Param("variantId") Long variantId, @Param("qty") int qty);

	public List<ProductVariant> findByProductId(Long productId);
}
