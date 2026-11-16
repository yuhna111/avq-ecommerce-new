# AVQ Clothing

Website bán quần áo AVQ Clothing — bản demo minh họa cho tiểu luận môn **Phân tích thiết kế hệ thống thông tin** (PTIT).

Mã nguồn được hiện thực **đúng theo thiết kế trong Visual Paradigm**:
- 63 lớp do VP Instant Generator sinh (`vp/generated-java/`) được chép sang `backend/` và viết tiếp trên chính các file đó: giữ nguyên tên lớp, package, thuộc tính và chữ ký phương thức.
- Ứng dụng chạy trên **đúng CSDL do VP sinh** (`db/V2__vp_generated.sql`, 18 bảng) với `ddl-auto=validate`: Hibernate chỉ kiểm tra, không tạo hay sửa bảng.
- Đối chiếu chi tiết mã ↔ thiết kế và các chỗ lệch: [`docs/code-design-mapping.md`](docs/code-design-mapping.md).

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 17+, Spring Boot 3.5, Spring Web, Spring Data JPA (Hibernate 6), Validation, Spring Security + JWT (HS256) |
| CSDL | MySQL 8, database `avq_db_vp` |
| Frontend | React 18 + Vite 5 (cổng 5173, proxy `/api` sang 8080) |
| Thanh toán | Cổng **giả lập** ký HMAC-SHA512 như VNPay (không gọi VNPay thật) |
| Gợi ý size | Không có AI thật, luôn dùng bảng size (`FallbackPolicy`) |

## Cấu trúc thư mục

```
avq-clothing/
├── CLAUDE.md                  hướng dẫn hiện thực (nguồn sự thật, quy tắc code ↔ design)
├── vp/                        ĐẦU VÀO CHỈ ĐỌC: AVQ_Clothing.vpp, model.xml, generated-java/ (63 lớp VP sinh)
├── db/                        ĐẦU VÀO CHỈ ĐỌC: V1__vp_generated.sql, V2__vp_generated.sql (DDL VP sinh)
│   └── seed/demo_data.sql     dữ liệu demo (chỉ INSERT)
├── docs/                      quy tắc nghiệp vụ, ghi chú thiết kế, ảnh sơ đồ, báo cáo đối chiếu
├── backend/                   Spring Boot (package api, service, infra, repository, domain, app)
└── frontend/                  React + Vite
```

---

## 1. Cài đặt và chạy từ đầu trên Windows

### 1.1. Cần có sẵn

| Phần mềm | Phiên bản | Kiểm tra |
|---|---|---|
| JDK | 17 trở lên | `java -version` |
| MySQL Server | 8.x, đang chạy ở cổng 3306 | `mysql --version` |
| Node.js | 18 trở lên (kèm npm) | `node -v` |

Không cần cài Maven: backend có sẵn Maven Wrapper (`mvnw.cmd`), lần chạy đầu tự tải Maven 3.9.9.

Các lệnh dưới đây chạy trong **PowerShell**, tại thư mục gốc `avq-clothing`.

### 1.2. Tạo CSDL từ DDL của Visual Paradigm

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS avq_db_vp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p avq_db_vp -e "source db/V2__vp_generated.sql"
```

Muốn làm lại từ đầu thì xóa CSDL trước: `mysql -u root -p -e "DROP DATABASE avq_db_vp;"`.

### 1.3. Nạp dữ liệu demo

```powershell
mysql -u root -p --default-character-set=utf8mb4 avq_db_vp -e "source db/seed/demo_data.sql"
```

Chỉ chạy **một lần** trên CSDL vừa tạo. Dữ liệu gồm 5 danh mục, 8 sản phẩm có ảnh, 62 biến thể, bảng size, 1 khách hàng và 2 nhân viên.

### 1.4. Cấu hình kết nối cho backend

```powershell
Copy-Item backend\src\main\resources\application-local.example.yml backend\src\main\resources\application-local.yml
notepad backend\src\main\resources\application-local.yml
```

Điền mật khẩu MySQL vào `password`, đổi `jwt.secret` thành chuỗi ngẫu nhiên dài ít nhất 32 ký tự. File `application-local.yml` đã nằm trong `.gitignore`, không đưa lên git.

### 1.5. Chạy backend (cổng 8080)

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Thấy dòng `Started AvqApplication` là xong. Nếu mã không khớp CSDL, ứng dụng sẽ dừng ngay với lỗi `Schema-validation`; chạy được chính là bằng chứng mã khớp CSDL.

Chạy kiểm thử (58 test: đơn vị + tích hợp trên MySQL, tự rollback, không để lại dữ liệu):

```powershell
.\mvnw.cmd test
```

### 1.6. Chạy frontend (cổng 5173)

Mở cửa sổ PowerShell thứ hai:

```powershell
cd frontend
npm install
npm run dev
```

Mở trình duyệt: **http://localhost:5173**

### 1.7. Tài khoản demo

| Vai trò | Đăng nhập tại | Tài khoản | Mật khẩu |
|---|---|---|---|
| Khách hàng | `/dang-nhap` | `khach@avq.vn` hoặc `0901234567` | `khach12345` |
| Nhân viên | `/nhan-vien/dang-nhap` | `nhanvien` | `nhanvien123` |
| Chủ cửa hàng | `/nhan-vien/dang-nhap` | `chucuahang` | `chucuahang123` |

---

## 2. Kịch bản demo theo use case

Chuẩn bị: backend và frontend đang chạy; nên mở một cửa sổ thường cho **khách** và một cửa sổ ẩn danh cho **nhân viên**.

### Phần khách hàng

| # | Use case | Thao tác | Kết quả mong đợi (quy tắc) |
|---|---|---|---|
| 1 | UC04 Xem sản phẩm | Mở trang chủ khi chưa đăng nhập | Thấy 8 sản phẩm, không cần đăng nhập (BR03) |
| 2 | UC05 Tìm, lọc | Gõ "jean" → Tìm; chọn danh mục "Áo thun", size "XL", sắp xếp "Giá tăng dần" | Danh sách thay đổi theo bộ lọc |
| 3 | UC06 Chi tiết | Mở "Áo thun Oversize Street", chọn màu **Xám**, size **XL** | Hiện "Còn 1 sản phẩm" (biến thể còn 1 chiếc) |
| 4 | UC07 Thêm vào giỏ khi chưa đăng nhập | Bấm "Thêm vào giỏ" | Chuyển sang trang đăng nhập (DT-02 R1) |
| 5 | UC01 Đăng ký | `/dang-ky`: bỏ trống cả email lẫn SĐT; sau đó đăng ký bằng `khach@avq.vn` | Báo phải nhập email hoặc SĐT; email trùng → "Email/SĐT đã được đăng ký, vui lòng đăng nhập" (BR01, EX-04) |
| 6 | UC02 Đăng nhập | Đăng nhập `0901234567` / `khach12345` | Vào được, tên hiện trên thanh đầu trang |
| 7 | UC06a Gợi ý size | Ở "Áo thun Oversize Street": cao 175, nặng 70, kiểu mặc "Mặc vừa" | Gợi ý **M**: bảng size cho ra L, nhưng phom Oversize + mặc vừa nên lùi một size (FB-01) |
| 8 | UC06a không có bảng size | "Đầm Midi Hoa Nhí" → Gợi ý size | "Liên hệ cửa hàng để được tư vấn size" |
| 9 | UC07 Thêm vào giỏ (DT-02) | Không chọn size/màu → Thêm; chọn đủ → Thêm 2; Thêm 2 lần nữa | Nhắc chọn biến thể (R2); thêm dòng (R4); cộng dồn vẫn 1 dòng (R5, BR07) |
| 10 | DT-02 R3 vượt tồn | Áo Oversize Xám XL (còn 1): thêm số lượng 2 | "Chỉ còn 1 sản phẩm size XL màu Xám" (EX-02) |
| 11 | UC08 Sửa giỏ | Vào Giỏ hàng, bấm +/− , Xóa | Tạm tính cập nhật; tối đa 10 và không vượt tồn (BR05) |
| 12 | UC09 Đặt hàng COD | Giỏ dưới 500.000 đ → Đặt hàng → chọn địa chỉ có sẵn → COD → Xác nhận | Màn xác nhận hiện phí giao 30.000 đ (DT-01); đơn ở **Chờ xác nhận** (BR10); tồn kho giảm (BR11) |
| 13 | DT-01 miễn phí giao | Giỏ từ 500.000 đ trở lên | Phí giao "Miễn phí" |
| 14 | UC09 + UC10 Thanh toán trực tuyến | Đặt hàng → "Thanh toán trực tuyến" | Chuyển tới cổng **AVQPay (giả lập)**; đơn ở **Chờ thanh toán**, hạn 15 phút |
| 15 | DT-05 thất bại | Bấm "Hủy / thất bại" → Xem đơn | Thanh toán "Thất bại", đơn vẫn Chờ thanh toán, có nút "Thanh toán ngay" (EX-03) |
| 16 | DT-05 thành công | "Thanh toán ngay" → "Thanh toán thành công" | Đơn sang **Chờ xác nhận**, thanh toán "Thành công"; lịch sử ghi mã giao dịch |
| 17 | UC11 Theo dõi đơn | Menu "Đơn của tôi" → mở một đơn | Thấy chi tiết, giá đã chốt (BR08), lịch sử trạng thái (dòng đầu "Tạo đơn") |
| 18 | UC12 Hủy đơn | Đơn Chờ xác nhận → "Hủy đơn hàng" | Đơn **Đã hủy**, tồn kho được hoàn (BR11, BR13) |
| 19 | BR12 Tự hủy quá hạn | Đặt một đơn trực tuyến rồi **không** thanh toán, đợi 15 phút (bộ hẹn giờ chạy mỗi 60 giây) | Đơn tự chuyển **Đã hủy** với ghi chú "Hệ thống tự hủy", tồn kho hoàn lại |
| 20 | BR16 Khóa đăng nhập | Đăng xuất, nhập sai mật khẩu 5 lần | Lần thứ 5: "Tài khoản tạm khóa đến HH:mm"; mật khẩu đúng cũng bị chặn trong 15 phút |

> Mục 20 sẽ khóa tài khoản demo trong 15 phút. Mở khóa ngay: `mysql -u root -p avq_db_vp -e "UPDATE customers SET failed_login_count=0, locked_until=NULL WHERE email='khach@avq.vn';"`

### Phần nhân viên

| # | Use case | Thao tác | Kết quả mong đợi (quy tắc) |
|---|---|---|---|
| 21 | UC13 Đăng nhập nhân viên | `/nhan-vien/dang-nhap`: `nhanvien` / `nhanvien123` | Vào trang Quản lý đơn hàng |
| 22 | BR15 Tách quyền | Dùng tài khoản khách gõ thẳng `/nhan-vien/don-hang` | Bị chuyển về trang đăng nhập nhân viên; API trả 403 |
| 23 | UC20 Xem đơn | Lọc theo "Chờ xác nhận", mở đơn vừa đặt | Thấy chi tiết + nút theo DT-04 |
| 24 | DT-04 Không xác nhận đơn chưa thanh toán | Mở đơn "Chờ thanh toán" | Không có nút chuyển trạng thái (chỉ hệ thống chuyển khi thanh toán thành công) |
| 25 | DT-03 Hủy cần lý do | Bấm "Hủy đơn" khi ô ghi chú trống | "Nhân viên hủy đơn phải nhập lý do" |
| 26 | DT-04 Giao hàng | "Xác nhận & giao hàng" → "Đã giao thành công" | Sinh **mã vận đơn**; lịch sử ghi tên nhân viên; "Đã giao" là trạng thái kết thúc |
| 27 | BR13 phía khách | Khách mở lại đơn đang giao | Không còn nút hủy, hướng dẫn liên hệ cửa hàng (EX-05) |
| 28 | UC17 Thêm sản phẩm | "Sản phẩm & tồn kho" → "+ Thêm sản phẩm"; thử SKU trùng `AVQ-TS01-TRG-S` | SKU trùng bị từ chối (BR17); SKU mới → sản phẩm xuất hiện ở trang khách |
| 29 | UC18 Sửa, ẩn/hiện | "Sửa / tồn kho" → đổi giá → Lưu; bấm "Ẩn" | Giá mới hiển thị cho khách; sản phẩm ẩn biến mất khỏi trang khách; mỗi thay đổi ghi `product_change_logs` (FR31) |
| 30 | UC19 Tồn kho | Đổi tồn của một biến thể → Lưu; thử số âm | Lưu thành công, có ghi nhật ký; số âm bị từ chối (BR06) |

Xem dữ liệu ghi thật trong CSDL (minh chứng "use DB?"):

```powershell
mysql -u root -p --default-character-set=utf8mb4 avq_db_vp -e "SELECT order_code,status,total,tracking_code FROM orders; SELECT * FROM order_status_histories; SELECT * FROM product_change_logs; SELECT feature,model_version,fallback_used,created_at FROM ai_inference_logs;"
```

---

## 3. Ghi chú

- Cổng thanh toán giả lập: nếu khởi động lại backend, đơn trực tuyến đang chờ vẫn thanh toán tiếp được từ trang giả lập (link thanh toán có chữ ký HMAC nên cổng nhận lại được giao dịch). Đơn quá 15 phút chưa thanh toán sẽ tự hủy.
- Token đăng nhập hết hạn sau 30 phút; giao diện tự đăng xuất khi token hết hạn.
- Đổi cổng thanh toán: `payment.provider=vnpay` (cần cấu hình `payment.vnpay.*`); bản demo mặc định `mock`.
