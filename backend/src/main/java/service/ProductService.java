package service;

import api.dto.CategoryDto;
import api.dto.CreateProductRequest;
import java.util.List;
import api.dto.ProductCardDto;
import api.dto.ProductDetailDto;
import repository.ProductFilter;
import api.dto.UpdateProductRequest;

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