package repository;

/**
 * Tiêu chí lọc sản phẩm cho ProductRepository.search (chưa có trong thiết kế VP).
 * Đặt trong package repository (không phải api.dto) để repository không phụ thuộc ngược lên api
 * (Hình 4.6). api và service chỉ dùng kiểu này làm tham số, không gọi repository.
 * Giai đoạn 1-2: khung tối thiểu; các trường bổ sung ở Giai đoạn 3.
 */
public class ProductFilter {
}
