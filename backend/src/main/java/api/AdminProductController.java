package api;

import api.dto.CreateProductRequest;
import api.dto.ProductDetailDto;
import api.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import service.ProductService;
import service.StockService;

/** UC17 thêm sản phẩm, UC18 sửa/ẩn hiện, UC19 cập nhật tồn kho. */
@RestController
@RequestMapping("/api/admin")
public class AdminProductController {
	private final ProductService productService;
	private final StockService stockService;

	public AdminProductController(ProductService productService, StockService stockService) {
		this.productService = productService;
		this.stockService = stockService;
	}

	@GetMapping("/products")
	public List<ProductDetailDto> listAll() {
		return productService.listAllForStaff();
	}

	@PostMapping("/products")
	public ProductDetailDto create(@AuthenticationPrincipal(expression = "staffId") Long staffId,
			@Valid @RequestBody CreateProductRequest req) {
		return productService.create(staffId, req);
	}

	@PutMapping("/products/{id}")
	public ProductDetailDto update(@AuthenticationPrincipal(expression = "staffId") Long staffId,
			@PathVariable Long id, @Valid @RequestBody UpdateProductRequest req) {
		return productService.update(staffId, id, req);
	}

	/** Bổ sung 17/11: POST /api/admin/products/images (multipart, trường "file") → đường dẫn ảnh dạng chuỗi. */
	@PostMapping(value = "/products/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String uploadImage(@RequestParam("file") MultipartFile file) {
		return productService.uploadImage(file);
	}

	/** PUT /api/admin/variants/{variantId}/stock?stock=25 */
	@PutMapping("/variants/{variantId}/stock")
	public void updateStock(@AuthenticationPrincipal(expression = "staffId") Long staffId,
			@PathVariable Long variantId, @RequestParam int stock) {
		stockService.updateStock(staffId, variantId, stock);
	}
}
