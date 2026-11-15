# CLAUDE.md — AVQ Clothing

Website bán quần áo, làm demo cho tiểu luận **Phân tích thiết kế hệ thống thông tin** (PTIT).
Mọi sơ đồ và CSDL đã được sinh viên tự làm trong Visual Paradigm (VP). Nhiệm vụ của bạn là **hiện thực mã đúng theo thiết kế đó**.
Giảng viên sẽ kiểm tra hai tiêu chí: **code ↔ design** (mã khớp sơ đồ) và **use DB?** (mã chạy trên đúng CSDL do VP sinh).

## 1. Nguồn sự thật — không được tự ý thay đổi

| Nguồn | Quy định |
|---|---|
| `vp/generated-java/` | 63 lớp do VP Instant Generator sinh từ các sơ đồ 4.5, 4.7, 4.8, 4.9. **Tên lớp, tên package, tên và chữ ký phương thức công khai, tên thuộc tính phải giữ nguyên.** |
| `db/V2__vp_generated.sql` | CSDL cuối cùng (18 bảng) do VP sinh. **Không sửa schema.** Mã phải ánh xạ đúng tên bảng, tên cột, kiểu dữ liệu. |
| `db/V1__vp_generated.sql` | CSDL phiên bản trước (17 bảng), chỉ để tham khảo. |
| `vp/model.xml` | Toàn bộ model VP dạng XML: dùng để đối chiếu quan hệ, bội số, chiều điều hướng khi cần. |
| `docs/` | Quy tắc nghiệp vụ, ghi chú thiết kế, ảnh sơ đồ. Đọc trước khi viết service. |

Các thư mục `vp/` và `db/` là **đầu vào chỉ đọc**. Không sửa, không xóa file trong đó.

## 2. Công nghệ đã chốt

- **Backend:** Java 17 trở lên, Spring Boot 3.x, Maven, Spring Web, Spring Data JPA (Hibernate 6), Spring Validation, Spring Security + JWT (HS256), MySQL Connector/J. Không dùng Lombok (để mã dễ đối chiếu với sơ đồ).
- **CSDL:** MySQL 8, database `avq_db_vp` (đã được VP tạo sẵn trên máy). Cấu hình `spring.jpa.hibernate.ddl-auto=validate` — **Hibernate chỉ kiểm tra, không tạo hay sửa bảng**. Chạy được với `validate` chính là bằng chứng mã khớp CSDL.
- **Frontend:** React 18 + Vite, chạy `npm run dev` (cổng 5173), proxy `/api` sang backend cổng 8080.
- **Thanh toán:** cổng **giả lập** (không gọi VNPay thật).
- **Gợi ý size:** **không có dịch vụ AI thật**; luôn dùng bảng size (`FallbackPolicy`).
- Bạn được tự chọn phiên bản cụ thể, thư viện phụ (ví dụ thư viện JWT) và cách tổ chức bên trong, miễn không vi phạm mục 1 và mục 3.

## 3. Quy tắc giữ "code ↔ design"

1. **Giữ đúng package của VP:** `api`, `service`, `infra`, `repository`, `domain` (không thêm tiền tố `vn.avq...`). Lớp main đặt ở package gốc (default package không được phép trong Spring) — dùng một package riêng `app` cho lớp `AvqApplication` và cấu hình `scanBasePackages`, `@EntityScan("domain")`, `@EnableJpaRepositories("repository")`.
2. **Chép file từ `vp/generated-java/` sang `backend/src/main/java/` rồi viết tiếp trên chính các file đó.** Không viết lại lớp mới cùng tên.
3. **Được phép thêm** (mỗi thứ phải ghi vào báo cáo lệch thiết kế ở mục 7):
   - import, annotation (JPA, Spring, validation);
   - trường `private final` cho các phụ thuộc được tiêm qua constructor (tương ứng mũi tên Dependency trên sơ đồ);
   - getter/setter, constructor;
   - phương thức `private` hỗ trợ;
   - lớp DTO/Request trong package con `api.dto` (VP chưa có các lớp này: TokenDto, PlaceOrderRequest, OrderDetailDto…);
   - lớp cấu hình trong package `app` (Security, CORS, xử lý lỗi).
4. **Được phép chỉnh khi bắt buộc về kỹ thuật** (ghi vào báo cáo):
   - trường quan hệ VP sinh là `public` → đổi thành `private` + getter;
   - `ArrayList<X>` → `List<X>` (JPA cần kiểu interface);
   - `version : int` trong thiết kế nhưng cột CSDL là `BIGINT` → dùng `long`/`Long` để `validate` không lỗi;
   - `CategoryRepository` và các repository: `extends JpaRepository<Entity, Id>`, giữ nguyên các phương thức đã khai báo.
5. **Không được** đổi tên lớp/phương thức/thuộc tính, xóa phương thức có trong sơ đồ, hoặc chuyển phương thức sang lớp khác. Nếu thấy thiết kế sai hoặc thiếu, **dừng lại hỏi**, không tự sửa.
6. **Chiều phụ thuộc** đúng Hình 4.6: `api → service → repository → domain`, `service → infra`, `infra → domain`. Controller không gọi thẳng repository. Domain không phụ thuộc Spring ngoài annotation JPA.

## 4. Ánh xạ CSDL cần lưu ý

- `User` là `@MappedSuperclass`; `Customer` và `Staff` là entity riêng (bảng `customers`, `staff`). Thuộc tính `id` của `User` ánh xạ sang `customer_id` / `staff_id` bằng `@AttributeOverride`.
- Khóa chính ghép: `cart_items (variant_id, cart_id)`, `order_items (variant_id, order_id)` → `@EmbeddedId` hoặc `@IdClass`.
- `body_profiles.customer_id` vừa là khóa chính vừa là khóa ngoại → `@MapsId`.
- `return_records` có khóa ngoại ghép `(variant_id, order_id)` tới `order_items`.
- Cột ENUM của MySQL (`orders.status`, `payment_method`, `payments.status`, `fit_type`, `fit_pref`, `reason`, `ai_inference_logs.feature`) ↔ enum Java `@Enumerated(EnumType.STRING)`. Nếu `validate` báo lệch kiểu, thêm `columnDefinition` khớp DDL; **không sửa CSDL**.
- `tinyint(1)` ↔ `boolean`; `decimal(12,0)` ↔ `BigDecimal` (precision 12, scale 0); `datetime` ↔ `LocalDateTime`.
- Khóa lạc quan: `@Version` trên `Order.version` và `ProductVariant.version` (DB-R1, DB-R2).
- Bảng `ai_inference_logs` (DB-R7) **chưa có lớp tương ứng trong thiết kế**. Ghi log từ `AiServiceClient`/`SizeAdvisorService` bằng `JdbcTemplate`, và ghi vào báo cáo lệch thiết kế kèm đề xuất bổ sung lớp `AiInferenceLog` vào VP.
- Các CHECK, UNIQUE, chỉ mục đã có trong CSDL; mã vẫn phải kiểm tra trước ở tầng service để trả lỗi rõ ràng cho người dùng.

## 5. Quy tắc nghiệp vụ chính

Đọc đầy đủ ở `docs/business-rules.md`. Tóm tắt những điểm bắt buộc:

- **BR01/BR16 – Tài khoản:** khách đăng ký bằng email **hoặc** số điện thoại (không trùng). Sai mật khẩu 5 lần liên tiếp → khóa 15 phút (`lockedUntil`); đăng nhập đúng thì reset bộ đếm. Mật khẩu băm BCrypt. JWT hết hạn sau 30 phút.
- **BR04/BR05/BR07 – Giỏ hàng (DT-02):** phải chọn biến thể (size + màu); mỗi dòng 1–10 sản phẩm và không vượt tồn kho; thêm lại cùng biến thể thì cộng dồn.
- **BR08 – Đặt hàng:** chốt giá hiện hành vào `order_items` (tên, size, màu, đơn giá).
- **BR09 – Phí giao (DT-01):** 30.000 đ; miễn phí khi tạm tính ≥ 500.000 đ. `total = subtotal + shippingFee`.
- **BR10:** COD → trạng thái `CHO_XAC_NHAN`; ONLINE → `CHO_THANH_TOAN`, `expiresAt = now + 15 phút`, tạo `Payment` trạng thái `DANG_XU_LY`.
- **BR11 – Tồn kho:** trong **một giao dịch**, **trừ kho TRƯỚC khi lưu đơn** bằng câu UPDATE có điều kiện `... SET stock_qty = stock_qty - ? WHERE variant_id = ? AND stock_qty >= ?` (`ProductVariantRepository.decreaseIfEnough`, trả về 0 nghĩa là hết hàng → rollback). Làm ngược thứ tự sẽ gây deadlock khi nhiều khách mua cùng lúc. Hủy đơn thì hoàn kho.
- **BR12:** `OrderExpiryScheduler` chạy mỗi 60 giây, hủy đơn `CHO_THANH_TOAN` quá hạn và hoàn kho.
- **BR13/BR14 – Trạng thái (DT-04):** mọi thay đổi trạng thái đi qua `OrderStateMachine.transit`. Hợp lệ: `CHO_THANH_TOAN → CHO_XAC_NHAN | DA_HUY`; `CHO_XAC_NHAN → DANG_GIAO | DA_HUY`; `DANG_GIAO → DA_GIAO`. Khách chỉ hủy được ở `CHO_THANH_TOAN`, `CHO_XAC_NHAN`. Nhân viên hủy phải nhập lý do. Mỗi lần chuyển ghi một dòng `order_status_histories` (đơn mới: `fromStatus = null`, ghi chú "Tạo đơn"). Chuyển sang `DANG_GIAO` ghi `trackingCode`.
- **DT-05 – Kết quả thanh toán** (`PaymentService.handleCallback`): sai chữ ký → mã `97`; `txnRef` đã xử lý → `02`; không có đơn → `01`; số tiền lệch → `markMismatch`, mã `04`; thành công → `markResult(true)` và đơn sang `CHO_XAC_NHAN`; thất bại → `markResult(false)`, đơn giữ nguyên; trả `00`.
- **Cổng giả lập:** tạo lớp `MockPaymentGatewayClient` (package `infra`) cùng thực hiện `PaymentGatewayClient`, ký HMAC-SHA512 như VNPay và đi qua đúng `handleCallback`. Dùng mặc định (`payment.provider=mock`). Giữ `VnPayClient` theo thiết kế (có thể chỉ hiện thực khung).
- **Gợi ý size (FB-01):** `AiServiceClient` không có dịch vụ thật (`ai.enabled=false`), nên `SizeAdvisorService` luôn dùng `FallbackPolicy.ruleBasedSize`: chọn size đầu tiên trong `size_charts` có khoảng chiều cao và cân nặng chứa số đo; ngoài bảng thì theo chiều cao gần nhất; phom `OVERSIZE` + ưa thích `VUA` thì lùi một size. Không có bảng size → trả thông báo "Liên hệ cửa hàng để được tư vấn size". Mỗi lần gợi ý ghi một dòng `ai_inference_logs` với `fallback_used = 1`.
- **Nhân viên:** quản lý sản phẩm (thêm, sửa giá/mô tả, ẩn/hiện), sửa tồn kho (`StockService.updateStock`, ghi `product_change_logs`), xem và cập nhật trạng thái đơn.

## 6. Thứ tự làm — dừng sau mỗi giai đoạn để tôi kiểm tra

0. **Kiểm tra môi trường:** chạy `java -version`, `mvn -v`, `node -v`, kiểm tra kết nối MySQL tới `avq_db_vp`. Báo kết quả và những gì còn thiếu. Chưa cài đặt gì khi chưa hỏi.
1. **Dựng project Maven** trong `backend/`, chép 63 file từ `vp/generated-java/`, thêm lớp `app.AvqApplication` và cấu hình. Mục tiêu: `mvn compile` thành công (tạo DTO tối thiểu, thêm import).
2. **Entity + ánh xạ JPA** cho 18 lớp domain. Mục tiêu: ứng dụng khởi động được với `ddl-auto=validate` trên `avq_db_vp`.
3. **Repository:** hiện thực các phương thức truy vấn (đặt tên theo quy ước Spring Data hoặc `@Query`). Viết test truy vấn chính.
4. **Service + kiểm thử JUnit 5** cho từng quy tắc ở mục 5 (BR09, DT-02, DT-04, BR16, DT-05, BR12, và test 10 khách cùng mua chiếc cuối cùng: chỉ 1 thành công).
5. **Controller + bảo mật JWT** (khách và nhân viên tách quyền). Lỗi trả JSON `{ "code": "...", "message": "..." }`.
6. **Dữ liệu demo:** `db/seed/demo_data.sql` (chỉ INSERT, không đổi schema): danh mục, khoảng 8 sản phẩm có ảnh, biến thể, bảng size, một tài khoản khách và một tài khoản nhân viên. Ảnh để trong `frontend/public/images`.
7. **Frontend React + Vite:** trang khách (danh sách, lọc, chi tiết, gợi ý size, giỏ hàng, đặt hàng COD/ONLINE với trang thanh toán giả lập, đơn của tôi, hủy đơn) và trang nhân viên (đơn hàng, cập nhật trạng thái, sản phẩm, tồn kho). Giao diện tiếng Việt.
8. **Chạy thử toàn bộ, viết `README.md`** hướng dẫn chạy từ đầu trên máy Windows (tạo CSDL từ `db/V2__vp_generated.sql`, nạp dữ liệu demo, chạy backend, chạy frontend), kèm kịch bản demo theo từng use case.
9. **Báo cáo đối chiếu** `docs/code-design-mapping.md`:
   - bảng: lớp/phương thức trên sơ đồ (Hình 4.5, 4.7–4.9) ↔ file và dòng trong mã;
   - bảng: bảng/cột CSDL (V2) ↔ entity/thuộc tính;
   - **danh sách mọi chỗ lệch thiết kế** (mục 3.3, 3.4, bảng `ai_inference_logs`) kèm lý do;
   - kết quả kiểm thử.

Sau mỗi giai đoạn: commit với thông điệp rõ ràng, tóm tắt đã làm gì, chạy được gì, còn vướng gì.

## 7. Không được làm

- Sửa file trong `vp/` hoặc `db/` (trừ thêm `db/seed/`).
- Dùng `ddl-auto=update/create` hoặc bất kỳ cách nào làm thay đổi schema.
- Đổi tên, xóa, gộp lớp/phương thức của thiết kế.
- Gọi dịch vụ thanh toán hoặc AI thật.
- Ghi mật khẩu thật vào mã; cấu hình kết nối MySQL đọc từ biến môi trường hoặc `application-local.yml` (đưa vào `.gitignore`), kèm file mẫu `application-local.example.yml`.
