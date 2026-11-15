package api.dto;

import domain.FitType;
import java.math.BigDecimal;
import java.util.List;

public record ProductCardDto(Long id, String name, BigDecimal price, String mainImage, String categoryName,
		FitType fitType, boolean visible, boolean inStock, List<String> sizes, List<String> colors) {
}
