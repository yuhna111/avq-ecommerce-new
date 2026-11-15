package api.dto;

/** size = null khi sản phẩm chưa có bảng size (message hướng dẫn liên hệ cửa hàng). */
public record SizeAdviceDto(String size, String message, boolean fallbackUsed, String source) {
}
