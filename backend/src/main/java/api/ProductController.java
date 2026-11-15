package api;

import api.dto.CategoryDto;
import api.dto.ProductCardDto;
import api.dto.ProductDetailDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.ProductFilter;
import service.ProductService;

/** UC04 xem danh sách, UC05 tìm/lọc, UC06 xem chi tiết - không cần đăng nhập (BR03). */
@RestController
@RequestMapping("/api")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/categories")
	public List<CategoryDto> categories() {
		return productService.listCategories();
	}

	/** GET /api/products?keyword=&categoryId=&minPrice=&maxPrice=&size=&color=&fitType=&sort= */
	@GetMapping("/products")
	public List<ProductCardDto> search(@ModelAttribute ProductFilter filter) {
		return productService.search(filter);
	}

	@GetMapping("/products/{id}")
	public ProductDetailDto detail(@PathVariable Long id) {
		return productService.getDetail(id);
	}
}
