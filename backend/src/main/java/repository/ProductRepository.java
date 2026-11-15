package repository;

import domain.Product;
import java.util.List;
import java.util.Optional;
import api.dto.ProductFilter;

public interface ProductRepository {

	public List<Product> search(ProductFilter filter);

	public Optional<Product> findByIdAndVisibleTrue(Long id);
}