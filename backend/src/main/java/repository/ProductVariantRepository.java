package repository;

import domain.ProductVariant;
import java.util.List;

public interface ProductVariantRepository {

	public int decreaseIfEnough(Long variantId, int qty);

	public int increase(Long variantId, int qty);

	public List<ProductVariant> findByProductId(Long productId);
}