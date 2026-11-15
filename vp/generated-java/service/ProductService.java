package service;

public class ProductService {

	public List<CategoryDto> listCategories() {
		throw new UnsupportedOperationException();
	}

	public List<ProductCardDto> search(ProductFilter filter) {
		throw new UnsupportedOperationException();
	}

	public ProductDetailDto getDetail(Long id) {
		throw new UnsupportedOperationException();
	}

	public ProductDetailDto create(Long staffId, CreateProductRequest req) {
		throw new UnsupportedOperationException();
	}

	public ProductDetailDto update(Long staffId, Long id, UpdateProductRequest req) {
		throw new UnsupportedOperationException();
	}
}