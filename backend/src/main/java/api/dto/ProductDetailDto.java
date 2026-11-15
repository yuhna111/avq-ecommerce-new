package api.dto;

import domain.FitType;
import java.math.BigDecimal;
import java.util.List;

public record ProductDetailDto(Long id, String name, String description, String material, BigDecimal price,
		boolean visible, FitType fitType, CategoryDto category, List<String> images, List<VariantDto> variants,
		boolean hasSizeChart) {

	public record VariantDto(Long id, String sku, String size, String color, int stockQty) {
	}
}
