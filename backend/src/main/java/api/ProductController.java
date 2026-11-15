package api;

import api.dto.CategoryDto;
import java.util.List;
import api.dto.ProductCardDto;
import api.dto.ProductDetailDto;
import repository.ProductFilter;

public class ProductController {

	public List<CategoryDto> categories() {
		throw new UnsupportedOperationException();
	}

	public List<ProductCardDto> search(ProductFilter filter) {
		throw new UnsupportedOperationException();
	}

	public ProductDetailDto detail(Long id) {
		throw new UnsupportedOperationException();
	}
}