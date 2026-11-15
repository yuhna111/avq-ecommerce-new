package repository;

import domain.FitType;
import java.math.BigDecimal;

/**
 * Tiêu chí lọc sản phẩm cho ProductRepository.search (chưa có trong thiết kế VP - bổ sung).
 * Đặt trong package repository (không phải api.dto) để repository không phụ thuộc ngược lên api
 * (Hình 4.6). api và service chỉ dùng kiểu này làm tham số, không gọi repository.
 * Có setter để Spring gắn tham số query string (?keyword=&categoryId=...).
 */
public class ProductFilter {
	private String keyword;
	private Long categoryId;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private String size;
	private String color;
	private FitType fitType;
	/** price_asc | price_desc | newest (mặc định). Sắp xếp ở service. */
	private String sort;
	/** true: gồm cả sản phẩm đang ẩn (chỉ dùng cho nhân viên). */
	private boolean includeHidden;

	/** Mẫu LIKE viết thường, null khi không lọc theo từ khóa. */
	public String getKeywordPattern() {
		return keyword == null || keyword.isBlank() ? null : "%" + keyword.trim().toLowerCase() + "%";
	}

	private static String blankToNull(String s) {
		return s == null || s.isBlank() ? null : s.trim();
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public BigDecimal getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(BigDecimal minPrice) {
		this.minPrice = minPrice;
	}

	public BigDecimal getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(BigDecimal maxPrice) {
		this.maxPrice = maxPrice;
	}

	public String getSize() {
		return blankToNull(size);
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getColor() {
		return blankToNull(color);
	}

	public void setColor(String color) {
		this.color = color;
	}

	public FitType getFitType() {
		return fitType;
	}

	public void setFitType(FitType fitType) {
		this.fitType = fitType;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public boolean isIncludeHidden() {
		return includeHidden;
	}

	public void setIncludeHidden(boolean includeHidden) {
		this.includeHidden = includeHidden;
	}
}
