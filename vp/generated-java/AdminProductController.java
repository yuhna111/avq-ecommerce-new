package api;

public class AdminProductController {

	public List<ProductDetailDto> listAll() {
		throw new UnsupportedOperationException();
	}

	public ProductDetailDto create(Long staffId, CreateProductRequest req) {
		throw new UnsupportedOperationException();
	}

	public ProductDetailDto update(Long staffId, Long id, UpdateProductRequest req) {
		throw new UnsupportedOperationException();
	}

	public void updateStock(Long staffId, Long variantId, int stock) {
		throw new UnsupportedOperationException();
	}
}