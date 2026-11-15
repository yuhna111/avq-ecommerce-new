-- =====================================================================
-- Dữ liệu demo AVQ Clothing (Giai đoạn 6). CHỈ INSERT, không thay đổi schema.
-- Chạy MỘT lần trên CSDL avq_db_vp vừa tạo từ db/V2__vp_generated.sql:
--   mysql -u root -p --default-character-set=utf8mb4 avq_db_vp < db/seed/demo_data.sql
-- Tài khoản demo:
--   Khách hàng : khach@avq.vn hoặc 0901234567 / khach12345
--   Nhân viên  : nhanvien / nhanvien123      Chủ cửa hàng: chucuahang / chucuahang123
-- Không ghi id cố định: các bảng liên kết tra theo slug / sku / LAST_INSERT_ID().
-- =====================================================================
SET NAMES utf8mb4;
START TRANSACTION;

-- Danh mục
INSERT INTO categories (name, slug) VALUES
  ('Áo thun', 'ao-thun'),
  ('Áo sơ mi', 'ao-so-mi'),
  ('Quần jean', 'quan-jean'),
  ('Áo khoác', 'ao-khoac'),
  ('Váy & Đầm', 'vay-dam');

-- Sản phẩm 1: Áo thun Basic Cotton
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Áo thun Basic Cotton', 'Áo thun cổ tròn 100% cotton, thấm hút mồ hôi, dễ phối đồ hằng ngày.', 'Cotton 100%', 199000, 1, 'REGULAR' FROM categories WHERE slug = 'ao-thun';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-TS01-TRG-S', 'S', 'Trắng', 15, 0),
  (@p, 'AVQ-TS01-TRG-M', 'M', 'Trắng', 18, 0),
  (@p, 'AVQ-TS01-TRG-L', 'L', 'Trắng', 21, 0),
  (@p, 'AVQ-TS01-TRG-XL', 'XL', 'Trắng', 24, 0),
  (@p, 'AVQ-TS01-DEN-S', 'S', 'Đen', 20, 0),
  (@p, 'AVQ-TS01-DEN-M', 'M', 'Đen', 23, 0),
  (@p, 'AVQ-TS01-DEN-L', 'L', 'Đen', 9, 0),
  (@p, 'AVQ-TS01-DEN-XL', 'XL', 'Đen', 12, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p1.svg', 1), (@p, '/images/p1-2.svg', 0);
INSERT INTO size_charts (product_id, `size`, height_min, height_max, weight_min, weight_max) VALUES
  (@p, 'S', 150, 160, 45, 55),
  (@p, 'M', 160, 170, 55, 65),
  (@p, 'L', 170, 178, 65, 75),
  (@p, 'XL', 178, 188, 75, 90);

-- Sản phẩm 2: Áo thun Oversize Street
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Áo thun Oversize Street', 'Form rộng phong cách đường phố, vai rớt, vải dày dặn không xù.', 'Cotton 2 chiều', 259000, 1, 'OVERSIZE' FROM categories WHERE slug = 'ao-thun';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-TS02-DEN-S', 'S', 'Đen', 22, 0),
  (@p, 'AVQ-TS02-DEN-M', 'M', 'Đen', 8, 0),
  (@p, 'AVQ-TS02-DEN-L', 'L', 'Đen', 11, 0),
  (@p, 'AVQ-TS02-DEN-XL', 'XL', 'Đen', 14, 0),
  (@p, 'AVQ-TS02-XAM-S', 'S', 'Xám', 10, 0),
  (@p, 'AVQ-TS02-XAM-M', 'M', 'Xám', 13, 0),
  (@p, 'AVQ-TS02-XAM-L', 'L', 'Xám', 16, 0),
  (@p, 'AVQ-TS02-XAM-XL', 'XL', 'Xám', 1, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p2.svg', 1), (@p, '/images/p2-2.svg', 0);
INSERT INTO size_charts (product_id, `size`, height_min, height_max, weight_min, weight_max) VALUES
  (@p, 'S', 150, 160, 45, 55),
  (@p, 'M', 160, 170, 55, 65),
  (@p, 'L', 170, 178, 65, 75),
  (@p, 'XL', 178, 188, 75, 90);

-- Sản phẩm 3: Áo sơ mi Oxford
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Áo sơ mi Oxford', 'Sơ mi Oxford dáng ôm, cổ button-down lịch sự cho công sở.', 'Cotton Oxford', 389000, 1, 'SLIM' FROM categories WHERE slug = 'ao-so-mi';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-SM01-TRG-S', 'S', 'Trắng', 12, 0),
  (@p, 'AVQ-SM01-TRG-M', 'M', 'Trắng', 15, 0),
  (@p, 'AVQ-SM01-TRG-L', 'L', 'Trắng', 18, 0),
  (@p, 'AVQ-SM01-TRG-XL', 'XL', 'Trắng', 21, 0),
  (@p, 'AVQ-SM01-XNH-S', 'S', 'Xanh nhạt', 17, 0),
  (@p, 'AVQ-SM01-XNH-M', 'M', 'Xanh nhạt', 20, 0),
  (@p, 'AVQ-SM01-XNH-L', 'L', 'Xanh nhạt', 23, 0),
  (@p, 'AVQ-SM01-XNH-XL', 'XL', 'Xanh nhạt', 9, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p3.svg', 1), (@p, '/images/p3-2.svg', 0);
INSERT INTO size_charts (product_id, `size`, height_min, height_max, weight_min, weight_max) VALUES
  (@p, 'S', 150, 160, 45, 55),
  (@p, 'M', 160, 170, 55, 65),
  (@p, 'L', 170, 178, 65, 75),
  (@p, 'XL', 178, 188, 75, 90);

-- Sản phẩm 4: Áo sơ mi Linen
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Áo sơ mi Linen', 'Sơ mi linen thoáng mát cho mùa hè, dáng suông thoải mái.', 'Linen pha cotton', 429000, 1, 'REGULAR' FROM categories WHERE slug = 'ao-so-mi';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-SM02-BE-S', 'S', 'Be', 0, 0),
  (@p, 'AVQ-SM02-BE-M', 'M', 'Be', 22, 0),
  (@p, 'AVQ-SM02-BE-L', 'L', 'Be', 8, 0),
  (@p, 'AVQ-SM02-BE-XL', 'XL', 'Be', 11, 0),
  (@p, 'AVQ-SM02-TRG-S', 'S', 'Trắng', 24, 0),
  (@p, 'AVQ-SM02-TRG-M', 'M', 'Trắng', 10, 0),
  (@p, 'AVQ-SM02-TRG-L', 'L', 'Trắng', 13, 0),
  (@p, 'AVQ-SM02-TRG-XL', 'XL', 'Trắng', 16, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p4.svg', 1), (@p, '/images/p4-2.svg', 0);
INSERT INTO size_charts (product_id, `size`, height_min, height_max, weight_min, weight_max) VALUES
  (@p, 'S', 150, 160, 45, 55),
  (@p, 'M', 160, 170, 55, 65),
  (@p, 'L', 170, 178, 65, 75),
  (@p, 'XL', 178, 188, 75, 90);

-- Sản phẩm 5: Quần jean Slim Fit
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Quần jean Slim Fit', 'Jean co giãn nhẹ, dáng ôm vừa, màu wash bền đẹp.', 'Denim co giãn', 499000, 1, 'SLIM' FROM categories WHERE slug = 'quan-jean';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-QJ01-XDA-29', '29', 'Xanh đậm', 9, 0),
  (@p, 'AVQ-QJ01-XDA-30', '30', 'Xanh đậm', 12, 0),
  (@p, 'AVQ-QJ01-XDA-31', '31', 'Xanh đậm', 15, 0),
  (@p, 'AVQ-QJ01-XDA-32', '32', 'Xanh đậm', 18, 0),
  (@p, 'AVQ-QJ01-XNH-29', '29', 'Xanh nhạt', 14, 0),
  (@p, 'AVQ-QJ01-XNH-30', '30', 'Xanh nhạt', 17, 0),
  (@p, 'AVQ-QJ01-XNH-31', '31', 'Xanh nhạt', 20, 0),
  (@p, 'AVQ-QJ01-XNH-32', '32', 'Xanh nhạt', 23, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p5.svg', 1), (@p, '/images/p5-2.svg', 0);
INSERT INTO size_charts (product_id, `size`, height_min, height_max, weight_min, weight_max) VALUES
  (@p, '29', 155, 165, 45, 56),
  (@p, '30', 162, 170, 55, 63),
  (@p, '31', 168, 176, 62, 70),
  (@p, '32', 174, 185, 69, 82);

-- Sản phẩm 6: Quần jean Ống Rộng
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Quần jean Ống Rộng', 'Quần jean ống suông rộng, lưng cao, tôn dáng.', 'Denim 100% cotton', 549000, 1, 'REGULAR' FROM categories WHERE slug = 'quan-jean';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-QJ02-XNH-29', '29', 'Xanh nhạt', 16, 0),
  (@p, 'AVQ-QJ02-XNH-30', '30', 'Xanh nhạt', 19, 0),
  (@p, 'AVQ-QJ02-XNH-31', '31', 'Xanh nhạt', 22, 0),
  (@p, 'AVQ-QJ02-XNH-32', '32', 'Xanh nhạt', 8, 0),
  (@p, 'AVQ-QJ02-DEN-29', '29', 'Đen', 21, 0),
  (@p, 'AVQ-QJ02-DEN-30', '30', 'Đen', 24, 0),
  (@p, 'AVQ-QJ02-DEN-31', '31', 'Đen', 10, 0),
  (@p, 'AVQ-QJ02-DEN-32', '32', 'Đen', 13, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p6.svg', 1), (@p, '/images/p6-2.svg', 0);
INSERT INTO size_charts (product_id, `size`, height_min, height_max, weight_min, weight_max) VALUES
  (@p, '29', 155, 165, 45, 56),
  (@p, '30', 162, 170, 55, 63),
  (@p, '31', 168, 176, 62, 70),
  (@p, '32', 174, 185, 69, 82);

-- Sản phẩm 7: Áo khoác Bomber
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Áo khoác Bomber', 'Bomber hai lớp, bo chun cổ tay, giữ ấm tốt mùa thu đông.', 'Polyester, lót dù', 699000, 1, 'OVERSIZE' FROM categories WHERE slug = 'ao-khoac';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-AK01-DEN-S', 'S', 'Đen', 23, 0),
  (@p, 'AVQ-AK01-DEN-M', 'M', 'Đen', 9, 0),
  (@p, 'AVQ-AK01-DEN-L', 'L', 'Đen', 12, 0),
  (@p, 'AVQ-AK01-DEN-XL', 'XL', 'Đen', 15, 0),
  (@p, 'AVQ-AK01-REU-S', 'S', 'Rêu', 11, 0),
  (@p, 'AVQ-AK01-REU-M', 'M', 'Rêu', 14, 0),
  (@p, 'AVQ-AK01-REU-L', 'L', 'Rêu', 17, 0),
  (@p, 'AVQ-AK01-REU-XL', 'XL', 'Rêu', 20, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p7.svg', 1), (@p, '/images/p7-2.svg', 0);
INSERT INTO size_charts (product_id, `size`, height_min, height_max, weight_min, weight_max) VALUES
  (@p, 'S', 150, 160, 45, 55),
  (@p, 'M', 160, 170, 55, 65),
  (@p, 'L', 170, 178, 65, 75),
  (@p, 'XL', 178, 188, 75, 90);

-- Sản phẩm 8: Đầm Midi Hoa Nhí
INSERT INTO products (category_id, name, description, material, price, is_visible, fit_type)
SELECT category_id, 'Đầm Midi Hoa Nhí', 'Đầm midi họa tiết hoa nhí, eo chun, phù hợp đi chơi, dạo phố.', 'Voan lụa', 459000, 1, 'REGULAR' FROM categories WHERE slug = 'vay-dam';
SET @p = LAST_INSERT_ID();
INSERT INTO product_variants (product_id, sku, `size`, color, stock_qty, version) VALUES
  (@p, 'AVQ-VD01-HONG-S', 'S', 'Hồng', 13, 0),
  (@p, 'AVQ-VD01-HONG-M', 'M', 'Hồng', 16, 0),
  (@p, 'AVQ-VD01-HONG-L', 'L', 'Hồng', 19, 0),
  (@p, 'AVQ-VD01-XANH-S', 'S', 'Xanh', 18, 0),
  (@p, 'AVQ-VD01-XANH-M', 'M', 'Xanh', 21, 0),
  (@p, 'AVQ-VD01-XANH-L', 'L', 'Xanh', 24, 0);
INSERT INTO product_images (product_id, url, is_main) VALUES (@p, '/images/p8.svg', 1), (@p, '/images/p8-2.svg', 0);
-- (Không có bảng size: minh họa FB-01 bước 4 "Liên hệ cửa hàng để được tư vấn size")

-- Tài khoản khách hàng (mật khẩu khach12345, băm BCrypt)
INSERT INTO customers (full_name, password_hash, failed_login_count, locked_until, email, phone) VALUES
  ('Nguyễn Minh Anh', '$2a$10$bza1tWk5GqFuoLoU9sUGm.lndSpjA33p8xzBYQ41229./FUQUKx16', 0, NULL, 'khach@avq.vn', '0901234567');
SET @c = LAST_INSERT_ID();
INSERT INTO addresses (customer_id, receiver_name, receiver_phone, address_line, is_default) VALUES
  (@c, 'Nguyễn Minh Anh', '0901234567', '97 Man Thiện, phường Hiệp Phú, TP. Thủ Đức, TP. Hồ Chí Minh', 1),
  (@c, 'Nguyễn Minh Anh', '0901234567', '122 Hoàng Quốc Việt, quận Cầu Giấy, Hà Nội', 0);

-- Tài khoản nhân viên (BR02: do chủ cửa hàng cấp, không có đăng ký công khai)
INSERT INTO staff (full_name, password_hash, failed_login_count, locked_until, username, role) VALUES
  ('Trần Văn Bình', '$2a$10$BU2HTwovIhKg95DTORmptOFEQCcgcyghQdY.jhyviiCsLfDlsBxdi', 0, NULL, 'nhanvien', 'STAFF'),
  ('Lê Thu Hà', '$2a$10$wutJCNnc5YsOgrFY5HOVd.LyIWSBQ.g5wF2TuY2Q0ITE4wZB1TlNe', 0, NULL, 'chucuahang', 'OWNER');

COMMIT;
