# Ảnh chụp màn hình theo use case

Chụp tự động ngày 16/11/2026 trên bản chạy thật (frontend `localhost:5173`, backend `8080`, CSDL `avq_db_vp`), màn hình máy tính 1366×860 và điện thoại 390×844.
Tên file bắt đầu bằng mã use case (UC) và mã quy tắc nghiệp vụ như trong `docs/business-rules.md`, theo đúng thứ tự kịch bản demo ở `README.md` mục 2.
Dữ liệu tạo ra khi chụp (khách đăng ký thử, đơn hàng, sản phẩm thêm thử, giá và tồn kho đã sửa) đã được xóa hoặc khôi phục sau khi chụp.

Tài khoản dùng khi chụp: khách `0901234567` / `khach12345`, nhân viên `nhanvien` / `nhanvien123`.

## Khách hàng: tài khoản

| File | Nội dung |
|---|---|
| [`UC01-1-thieu-email-sdt.png`](UC01-1-thieu-email-sdt.png) | UC01 Đăng ký: không nhập email lẫn số điện thoại → báo lỗi (BR01) |
| [`UC01-2-email-da-dang-ky.png`](UC01-2-email-da-dang-ky.png) | UC01 Đăng ký: email đã có tài khoản → báo lỗi (EX-04) |
| [`UC01-3-dang-ky-thanh-cong.png`](UC01-3-dang-ky-thanh-cong.png) | UC01 Đăng ký thành công: tự đăng nhập, tên hiện trên đầu trang |
| [`UC02-1-dang-nhap.png`](UC02-1-dang-nhap.png) | UC02 Đăng nhập bằng số điện thoại hoặc email (BR01) |
| [`UC02-2-dang-nhap-thanh-cong.png`](UC02-2-dang-nhap-thanh-cong.png) | UC02 Đăng nhập thành công: tên khách và giỏ hàng trên đầu trang |

## Khách hàng: xem, tìm, chọn sản phẩm

| File | Nội dung |
|---|---|
| [`UC04-1-trang-chu.png`](UC04-1-trang-chu.png) | UC04 Xem sản phẩm: trang chủ khi chưa đăng nhập (BR03) |
| [`UC04-2-danh-sach-san-pham.png`](UC04-2-danh-sach-san-pham.png) | UC04 Xem sản phẩm: danh sách 8 sản phẩm, có giá, màu, phom, size |
| [`UC05-1-tim-kiem-jean.png`](UC05-1-tim-kiem-jean.png) | UC05 Tìm kiếm: từ khóa "jean" ra 2 quần jean |
| [`UC05-2-loc-danh-muc-size-sap-xep.png`](UC05-2-loc-danh-muc-size-sap-xep.png) | UC05 Lọc: danh mục Áo thun, size XL, giá tăng dần |
| [`UC06-chi-tiet-san-pham.png`](UC06-chi-tiet-san-pham.png) | UC06 Xem chi tiết: Áo thun Oversize Street, màu Xám size XL còn 1 sản phẩm |
| [`UC06a-1-goi-y-size.png`](UC06a-1-goi-y-size.png) | UC06a Gợi ý size: cao 175, nặng 70, mặc vừa → size M (phom Oversize lùi một size, FB-01) |
| [`UC06a-2-khong-co-bang-size.png`](UC06a-2-khong-co-bang-size.png) | UC06a Gợi ý size: Đầm Midi không có bảng size → "Liên hệ cửa hàng để được tư vấn size" |

## Khách hàng: giỏ hàng

| File | Nội dung |
|---|---|
| [`UC07-1-chua-dang-nhap.png`](UC07-1-chua-dang-nhap.png) | UC07 Thêm vào giỏ khi chưa đăng nhập → chuyển sang trang đăng nhập (DT-02 R1) |
| [`UC07-2-chua-chon-size.png`](UC07-2-chua-chon-size.png) | UC07 Thêm vào giỏ khi chưa chọn size → nhắc chọn biến thể (DT-02 R2) |
| [`UC07-3-vuot-ton-kho.png`](UC07-3-vuot-ton-kho.png) | UC07 Thêm 2 chiếc khi chỉ còn 1 → báo vượt tồn kho (DT-02 R3, EX-02) |
| [`UC07-4-them-thanh-cong.png`](UC07-4-them-thanh-cong.png) | UC07 Thêm vào giỏ thành công, số lượng giỏ trên đầu trang tăng (DT-02 R4) |
| [`UC08-gio-hang.png`](UC08-gio-hang.png) | UC08 Giỏ hàng: sửa số lượng, xóa dòng, tạm tính |

## Khách hàng: đặt hàng, thanh toán, theo dõi, hủy đơn

| File | Nội dung |
|---|---|
| [`UC09-1-xac-nhan-dat-hang-cod.png`](UC09-1-xac-nhan-dat-hang-cod.png) | UC09 Xác nhận đặt hàng COD: tạm tính dưới 500.000 đ nên phí giao 30.000 đ (DT-01) |
| [`UC09-2-dat-hang-cod-thanh-cong.png`](UC09-2-dat-hang-cod-thanh-cong.png) | UC09 Đặt hàng COD xong: đơn ở "Chờ xác nhận", lịch sử ghi "Tạo đơn" (BR10) |
| [`UC09-3-xac-nhan-dat-hang-online.png`](UC09-3-xac-nhan-dat-hang-online.png) | UC09 Đặt hàng trực tuyến: tạm tính từ 500.000 đ nên miễn phí giao (DT-01) |
| [`UC10-1-cong-thanh-toan.png`](UC10-1-cong-thanh-toan.png) | UC10 Cổng thanh toán giả lập AVQPay: đơn đang "Chờ thanh toán", giữ 15 phút (BR10) |
| [`UC10-2-thanh-toan-that-bai.png`](UC10-2-thanh-toan-that-bai.png) | UC10 Giao dịch thất bại: cổng báo kết quả, đơn vẫn chờ thanh toán (DT-05) |
| [`UC10-3-don-cho-thanh-toan-lai.png`](UC10-3-don-cho-thanh-toan-lai.png) | UC10 Đơn sau khi thanh toán thất bại: còn nút "Thanh toán ngay" (EX-03) |
| [`UC10-4-thanh-toan-thanh-cong.png`](UC10-4-thanh-toan-thanh-cong.png) | UC10 Thanh toán thành công: mã phản hồi 00 (DT-05) |
| [`UC11-1-danh-sach-don.png`](UC11-1-danh-sach-don.png) | UC11 Danh sách đơn của tôi: trạng thái và tổng tiền từng đơn |
| [`UC11-2-chi-tiet-don.png`](UC11-2-chi-tiet-don.png) | UC11 Theo dõi đơn: đơn sang "Chờ xác nhận", giá đã chốt (BR08), lịch sử trạng thái kèm mã giao dịch |
| [`UC12-huy-don.png`](UC12-huy-don.png) | UC12 Khách hủy đơn "Chờ xác nhận" → "Đã hủy", tồn kho được hoàn (BR11, BR13) |

## Nhân viên: đơn hàng

| File | Nội dung |
|---|---|
| [`UC13-dang-nhap-nhan-vien.png`](UC13-dang-nhap-nhan-vien.png) | UC13 Nhân viên đăng nhập → trang Quản lý đơn hàng (tách quyền BR15) |
| [`UC20-1-danh-sach-don-cho-xac-nhan.png`](UC20-1-danh-sach-don-cho-xac-nhan.png) | UC20 Xem đơn: lọc các đơn "Chờ xác nhận" |
| [`UC20-2-chi-tiet-don.png`](UC20-2-chi-tiet-don.png) | UC20 Chi tiết đơn phía nhân viên: chỉ hiện các nút đúng bảng chuyển trạng thái (DT-04) |
| [`UC20-3-huy-thieu-ly-do.png`](UC20-3-huy-thieu-ly-do.png) | UC20 Nhân viên hủy đơn mà không nhập lý do → bị chặn (DT-03) |
| [`UC20-4-dang-giao-ma-van-don.png`](UC20-4-dang-giao-ma-van-don.png) | UC20 Chuyển sang "Đang giao": hệ thống tự sinh mã vận đơn, lịch sử ghi tên nhân viên (QĐ1) |
| [`UC20-5-da-giao.png`](UC20-5-da-giao.png) | UC20 Chuyển sang "Đã giao": trạng thái kết thúc, không còn nút chuyển tiếp |

## Nhân viên: sản phẩm và tồn kho

| File | Nội dung |
|---|---|
| [`UC17-0-danh-sach-san-pham.png`](UC17-0-danh-sach-san-pham.png) | Quản lý sản phẩm & tồn kho: danh sách sản phẩm (kể cả sản phẩm đang ẩn) |
| [`UC17-1-sku-trung.png`](UC17-1-sku-trung.png) | UC17 Thêm sản phẩm: SKU đã tồn tại → bị từ chối (BR17) |
| [`UC17-2-them-thanh-cong.png`](UC17-2-them-thanh-cong.png) | UC17 Thêm sản phẩm thành công: xuất hiện trong danh sách |
| [`UC17-3-tai-anh-sku-tu-sinh.png`](UC17-3-tai-anh-sku-tu-sinh.png) | UC17 Thêm sản phẩm: tải ảnh từ máy (ảnh đầu là ảnh chính), file không phải ảnh bị từ chối, SKU tự sinh AVQ-TS03-DEN-M (bổ sung 17/11) |
| [`UC18-1-sua-gia.png`](UC18-1-sua-gia.png) | UC18 Sửa giá sản phẩm (389.000 → 399.000 đ); mỗi thay đổi ghi product_change_logs (FR31) |
| [`UC18-2-an-san-pham.png`](UC18-2-an-san-pham.png) | UC18 Ẩn sản phẩm: trạng thái "Đang ẩn", khách không còn thấy trên trang |
| [`UC19-sua-ton-kho.png`](UC19-sua-ton-kho.png) | UC19 Cập nhật tồn kho một biến thể (StockService.updateStock), có ghi nhật ký |

## Chủ cửa hàng (bổ sung 17/11)

| File | Nội dung |
|---|---|
| [`OWNER-1-tong-quan.png`](OWNER-1-tong-quan.png) | Chủ cửa hàng đăng nhập vào thẳng Tổng quan: doanh thu, đơn theo trạng thái, doanh thu 14 ngày, hàng sắp hết, đơn mới nhất |
| [`OWNER-2-quan-ly-nhan-vien.png`](OWNER-2-quan-ly-nhan-vien.png) | BR02 Quản lý nhân viên: danh sách tài khoản, tạo tài khoản nhân viên, đặt lại mật khẩu |
| [`OWNER-3-nhan-vien-khong-thay-muc-cua-chu.png`](OWNER-3-nhan-vien-khong-thay-muc-cua-chu.png) | Nhân viên thường không có mục Tổng quan và Nhân viên; gõ thẳng đường dẫn thì bị đưa về Quản lý đơn hàng |

## Giao diện điện thoại

| File | Nội dung |
|---|---|
| [`MOBILE-1-trang-chu.png`](MOBILE-1-trang-chu.png) | Giao diện điện thoại (390px): trang chủ |
| [`MOBILE-2-chi-tiet.png`](MOBILE-2-chi-tiet.png) | Giao diện điện thoại: chi tiết sản phẩm, thanh mua dính đáy màn hình |

## Use case không có ảnh

- **BR12 tự hủy đơn quá hạn:** cần chờ 15 phút; đã kiểm thử bằng JUnit (`OrderAndPaymentServiceTest`).
- **BR16 khóa đăng nhập sau 5 lần sai:** sẽ khóa tài khoản demo 15 phút; đã kiểm thử bằng JUnit (`RuleUnitTest`, `CartAndAuthServiceTest`).
