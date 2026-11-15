package repository;

import domain.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

	public List<Product> search(ProductFilter filter);

	public Optional<Product> findByIdAndVisibleTrue(Long id);
}