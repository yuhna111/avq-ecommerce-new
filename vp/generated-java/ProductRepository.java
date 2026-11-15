package repository;

import domain.Product;

public interface ProductRepository {

	public List<Product> search(ProductFilter filter);

	public Optional<Product> findByIdAndVisibleTrue(Long id);
}