package api.dto;

import domain.FitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/** UC17 - BR06 giá > 0, BR17 ít nhất một biến thể, SKU duy nhất. */
public record CreateProductRequest(
		@NotBlank(message = "Vui lòng nhập tên sản phẩm") String name,
		String description,
		String material,
		@NotNull(message = "Vui lòng nhập giá") @DecimalMin(value = "1", message = "Giá bán phải lớn hơn 0") BigDecimal price,
		@NotNull(message = "Vui lòng chọn danh mục") Long categoryId,
		@NotNull(message = "Vui lòng chọn phom dáng") FitType fitType,
		@NotEmpty(message = "Sản phẩm phải có ít nhất một biến thể") List<@Valid VariantRequest> variants,
		List<String> imageUrls) {

	public record VariantRequest(
			@NotBlank(message = "Vui lòng nhập size") String size,
			@NotBlank(message = "Vui lòng nhập màu") String color,
			@NotBlank(message = "Vui lòng nhập SKU") String sku,
			@Min(value = 0, message = "Tồn kho không được âm") int stock) {
	}
}
