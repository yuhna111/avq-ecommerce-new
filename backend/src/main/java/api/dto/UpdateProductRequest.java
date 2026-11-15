package api.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/** Sửa giá, mô tả, chất liệu, ẩn/hiện. Trường null = giữ nguyên. */
public record UpdateProductRequest(
		@DecimalMin(value = "1", message = "Giá bán phải lớn hơn 0") BigDecimal price,
		String description,
		String material,
		Boolean visible) {
}
