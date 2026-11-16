# Phân bổ trách nhiệm khi thiết kế, các thay đổi CSDL DB-R1…DB-R7

Nguồn: các sơ đồ thiết kế trong Visual Paradigm (Hình 4.5 Design Class Diagram, 4.6 Kiến trúc phân lớp, 4.7–4.9 Layered Class Diagram, 4.10 ERD V2) và các quyết định ghi lại trong quá trình làm.

## 1. Kiến trúc phân lớp (Hình 4.6)

| Package | Chứa | Được phụ thuộc vào |
|---|---|---|
| `api` | 9 controller (`RestController`) | `service` |
| `service` | 12 lớp nghiệp vụ (`Service`, `Component`) | `repository`, `infra`, `domain` |
| `infra` | Kết nối bên ngoài: `PaymentGatewayClient` (interface), `VnPayClient`, `AiServiceClient`, `JwtTokenProvider` | `domain` |
| `repository` | 13 interface truy cập dữ liệu | `domain` |
| `domain` | 18 entity + 7 enum | không phụ thuộc tầng nào |

Không có phụ thuộc đi ngược lên. Controller không gọi thẳng repository.

## 2. Quyết định thiết kế đáng chú ý

| Quyết định | Lý do |
|---|---|
| `logout()` bị bỏ khỏi `User` | Dùng JWT: máy chủ không lưu phiên, phía trình duyệt tự xóa token |
| `OrderExpiryScheduler` đặt trong `service`, không phải `infra` | Nó gọi `OrderService`; đặt ở `infra` sẽ tạo phụ thuộc vòng `service ↔ infra` |
| `PaymentService` chỉ phụ thuộc interface `PaymentGatewayClient` | Đổi cổng thanh toán (VNPay, cổng giả lập, MoMo…) không phải sửa `PaymentService` |
| `CartService` dùng thẳng `ProductVariantRepository`, không gọi `ProductService` | Tránh phụ thuộc giữa các service |
| `SizeAdvisorService` phụ thuộc cả `AiServiceClient` và `FallbackPolicy` | Cơ chế dự phòng FB-01 khi AI lỗi, chậm hoặc kém tin cậy |
| `AccountController` / `AccountService` | Quản lý địa chỉ giao hàng, phục vụ bước đặt hàng |
| Trừ kho trước khi lưu đơn (`StockService.reserve` gọi trước `orderRepo.save`) | Tránh deadlock khi nhiều khách mua cùng lúc; đã kiểm chứng trên MariaDB. Sequence 2.10 cần vẽ theo thứ tự này |

## 3. Phân bổ lại trách nhiệm: phân tích (Hình 4.1) → thiết kế (Hình 4.5, 4.7–4.9)

| Phương thức ở phân tích | Quyết định | Ở thiết kế | Lý do |
|---|---|---|---|
| `User.login(identifier, password)` | Chuyển | `AuthService.loginCustomer(identifier, password) : TokenDto`, `AuthService.loginStaff(username, password) : TokenDto` | Cần tìm tài khoản trong CSDL, so khớp mật khẩu băm, phát token |
| `User.logout()` | Bỏ | – | JWT không lưu phiên |
| `Customer.signUp()` | Chuyển | `AuthService.register(req) : TokenDto` | Kiểm tra trùng email/SĐT trong CSDL, băm mật khẩu |
| `Customer.findByIdentifier(id)` | Chuyển | `CustomerRepository.findByEmailOrPhone(identifier) : Optional<Customer>` | Truy vấn CSDL là việc của repository |
| `Product.findByProduct()` | Chuyển | `ProductVariantRepository.findByProductId(productId) : List<ProductVariant>` | Như trên |
| `ProductVariant.getStock()` | Bỏ khỏi sơ đồ | getter `getStockQty()` | Getter/setter không vẽ |
| `ProductVariant.decreaseStock(qty)` | Chuyển | `StockService.reserve(items)` → `ProductVariantRepository.decreaseIfEnough(variantId, qty) : int` | Trừ kho có điều kiện trong một câu UPDATE, cần giao dịch |
| `ProductVariant.increaseStock(qty)` | Chuyển | `StockService.restore(items)` → `ProductVariantRepository.increase(variantId, qty) : int` | Hoàn kho cùng giao dịch với hủy đơn |
| `Order.create(lines, fee, status)` | Chuyển | `OrderService.place(customerId, req) : OrderResultDto` | Phối hợp giỏ, tồn kho, phí, thanh toán, lịch sử trong một giao dịch |
| `Order.calcShippingFee(subtotal)` | Chuyển | `ShippingFeeCalculator.calculate(subtotal) : BigDecimal` | Tách quy tắc BR09, dễ đổi mức phí và dễ kiểm thử |
| `Order.setStatus(newStatus)` | Chuyển | `OrderStateMachine.transit(order, to, staff, note) : void` | Một chỗ duy nhất kiểm tra DT-04 và ghi lịch sử |
| `Order.cancel(reason)` | Chuyển | `OrderService.cancelByCustomer(customerId, orderCode, reason)` (gọi `OrderStateMachine`) | Kiểm tra quyền (BR13), chuyển trạng thái, hoàn kho |
| `Payment.create(orderId, amount)` | Chuyển | `PaymentService.createPayment(order) : String` (trả link thanh toán) | Ghi bản ghi thanh toán và gọi cổng |
| `Payment.updateResult(status, txnRef)` | Tách | `Payment.markResult(success, txnRef)`, `Payment.markMismatch(txnRef)`; `PaymentService.handleCallback(params) : void` | Payment chỉ tự đổi trạng thái của mình; xác thực chữ ký, chống trùng, đối chiếu số tiền ở service (DT-05) |
| `ChangeLog.record(staff, old, new)` | Đổi | `ChangeLog.of(staff, variant, field, oldValue, newValue) : ChangeLog` (static) | Phương thức tạo (factory), lúc gọi chưa có đối tượng |
| `Cart.removeItem(variantId)` | Đổi | `Cart.removeItems(variantIds : Set<Long>)` | Sau khi đặt hàng phải bỏ nhiều dòng cùng lúc |

Phương thức giữ lại trong entity chỉ thao tác trên dữ liệu của chính nó (hoặc đối tượng con nó sở hữu): `Cart.addOrIncrease`, `Cart.subtotal`, `Order.addItem`, `Order.recalculate`, `ProductVariant.isAvailable`, `User.isLocked`, `SizeChart.contains`…

## 4. Thay đổi so với phân tích (thuộc tính, kiểu)

- Kiểu Java: `Long` (định danh), `String`, `BigDecimal` (tiền), `LocalDateTime`, `boolean`, `int` (số đếm).
- Định danh các lớp đổi tên thành `id`.
- Thêm thuộc tính: `Order.version`, `Order.trackingCode`, `ProductVariant.version`, `Product.fitType`. (`Staff.active` từng được thêm, đã bỏ ngày 16/11 vì bảng `staff` không có cột này; `Staff.isActive()` giữ lại.)
- `isVisible`, `isMain` → `visible`, `main` (quy ước Java cho boolean).
- 7 enum thay cho chuỗi: `OrderStatus`, `PaymentMethod`, `PaymentStatus`, `FitType`, `FitPref`, `StaffRole`, `ReturnReason`.
- Stereotype: `User` là `MappedSuperclass`; 17 lớp còn lại là `Entity`.

## 5. Chiều điều hướng (Hình 4.5)

- **Một chiều:** Order → Customer; Cart → Customer; BodyProfile → Customer; Payment → Order; OrderStatusHistory → Order, Staff; OrderItem → ProductVariant; CartItem → ProductVariant; Product → Category; SizeChart → Product; ChangeLog → Product, ProductVariant, Staff; ReturnRecord → OrderItem, Staff.
- **Hai chiều:** Order ◆ OrderItem (`Order.items`, `OrderItem.order`); Cart ◆ CartItem; Product ◆ ProductVariant (`Product.variants`, `ProductVariant.product`); Product ◆ ProductImage; Customer ◆ Address.

## 6. ORM: điều chỉnh khi ánh xạ lớp → bảng (Hình 4.2, 4.3)

| VP sinh ra | Đã chỉnh thành | Lý do |
|---|---|---|
| Quan hệ không sinh khóa ngoại | Thêm role name hai đầu, Persistable = Yes | VP chỉ ánh xạ quan hệ có role |
| Khóa của cây kế thừa là `user_id` | `customer_id`, `staff_id` | Rõ nghĩa; mỗi lớp con một bảng (R3) |
| `datetime` → `integer(10)` | `datetime` | Bảng ánh xạ kiểu của VP sai |
| `int(10)`, `varchar(255)`, `numeric(19,0)`, `bit` | `bigint`, độ dài đúng, `decimal(12,0)`, `tinyint(1)` | Đúng thiết kế dữ liệu |
| `cart_items`, `order_items` không có khóa | Khóa chính ghép từ hai khóa ngoại (R5) | Composition |
| `body_profiles` không có khóa | Khóa chính là `customer_id` (R8) | Quan hệ 1–0..1 |
| Trạng thái kiểu chuỗi | ENUM (qua ô User type) | Giới hạn giá trị hợp lệ |
| Không có ràng buộc nghiệp vụ | CHECK qua DDL clauses; UNIQUE bộ ba `(product_id, size, color)` | BR05, BR06, BR17 |
| `return_records` hai khóa ngoại riêng | Một khóa ngoại ghép tới `order_items` | Bảo đảm món đổi trả thuộc đúng đơn |
| Tên ràng buộc có dấu (`đặt2`…) | `fk_<bảng con>_<bảng cha>` | Tránh lỗi bảng mã |
| Không có quy tắc xóa | 8 CASCADE (cha – con), 12 RESTRICT | Giữ toàn vẹn dữ liệu đơn hàng |

## 7. CSDL V2 — thay đổi do thiết kế đòi hỏi (Hình 4.10, 4.11)

| Mã | Thay đổi | Sinh ra từ |
|---|---|---|
| DB-R1 | `product_variants.version BIGINT NOT NULL DEFAULT 0` | `ProductVariant.version`: khóa lạc quan khi nhân viên cùng sửa tồn kho |
| DB-R2 | `orders.version BIGINT NOT NULL DEFAULT 0` | `Order.version`: khóa lạc quan khi cùng đổi trạng thái đơn |
| DB-R3 | `orders.tracking_code VARCHAR(40) NULL` | `Order.trackingCode`; hệ thống tự sinh trong `OrderStateMachine.transit` khi chuyển sang Đang giao (QĐ1, mục 9) |
| DB-R4 | `CONSTRAINT ck_order_total CHECK (total = subtotal + shipping_fee)` | Bảo vệ kết quả `Order.recalculate()` |
| DB-R5 | `idx_order_customer (customer_id, order_date)` | `OrderRepository.findByCustomerIdOrderByOrderDateDesc` |
| DB-R5 | `idx_order_expiry (status, expires_at)` | `OrderRepository.findByStatusAndExpiresAtBefore` |
| DB-R5 | `idx_osh_order (order_id, changed_at)` | `OrderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc` |
| DB-R5 | `idx_log_product (product_id, changed_at)` | `ProductChangeLogRepository.findByProductIdOrderByChangedAtDesc` |
| DB-R5 | `idx_product_cat (category_id, is_visible)` | `ProductRepository.search` |
| DB-R5 | `ft_product FULLTEXT (name, description)` | Tìm sản phẩm theo từ khóa |
| DB-R6 | `payments.txn_ref UNIQUE` | `PaymentRepository.existsByTxnRef`: không ghi trùng khi cổng gửi lại kết quả |
| DB-R7 | Bảng `ai_inference_logs` | Ghi mỗi lần gọi gợi ý size (Chương 3) |

V1: 17 bảng, 20 khóa ngoại, 5 CHECK. V2: 18 bảng, 20 khóa ngoại, 6 CHECK, 6 chỉ mục phụ. Đã kiểm thử trên MariaDB: tổng tiền sai bị chặn, `txn_ref` trùng bị chặn, cập nhật với `version` cũ trả 0 dòng.

## 8. Những điểm đã biết là chưa khớp (ghi vào báo cáo `code-design-mapping.md`)

Cập nhật 16/11, sau khi sửa VP theo `vp-changes.md`:

- Đã giải quyết trên VP: lớp `AiInferenceLog` (+ `AiFeature`, `AiInferenceLogRepository`); `Category.slug`; bỏ `Staff.active` (bảng `staff` không có cột này); `ChangeLog.of` thêm `product`; 5 phương thức service còn thiếu; `handleCallback`/`vnpayIpn` : `void`; các kiểu `TokenClaims`, `SizeFeatures`, `AiSizeResult`, `ProductFilter`; Sequence 2.10 vẽ lại "trừ kho rồi mới tạo đơn".
- Còn chỉnh kỹ thuật khi hiện thực (nội dung thiết kế không đổi): `version` là `int` trong thiết kế, cột CSDL `BIGINT` nên mã dùng `long`; mã VP sinh có trường quan hệ `public`, dùng `ArrayList`, thiếu import và DTO.
- Instant Generator lưu phần lớn file vào thư mục gốc; đã sắp lại theo package (SHA-256 khớp 63/63).

## 9. Quyết định thiết kế chốt (16/11)

| Mã | Vấn đề | Quyết định | Lý do |
|---|---|---|---|
| QĐ1 | DB-R3 yêu cầu ghi `orders.tracking_code` khi chuyển sang "Đang giao", nhưng `AdminOrderController.updateStatus(staffId, code, to, note)` / `OrderService.updateStatusByStaff(...)` không có tham số mã vận đơn | **Hệ thống tự sinh** mã vận đơn trong `OrderStateMachine.transit` khi đơn chuyển sang `DANG_GIAO`, dạng `VDyyMMddxxxxxx` (ngày + 6 chữ số ngẫu nhiên) | Bản demo không kết nối đơn vị vận chuyển thật nên không có mã thật để nhập. Tự sinh giúp giữ nguyên chữ ký phương thức trên sơ đồ, và mọi đơn "Đang giao" đều có mã để khách tra cứu |
| QĐ2 | DT-04 ghi "khách đổi sang COD" (Chờ thanh toán → Chờ xác nhận), nhưng thiết kế không có phương thức tương ứng | **Ngoài phạm vi bản demo.** Đơn ONLINE chưa thanh toán: khách thanh toán lại trong 15 phút, hoặc hủy (`cancelByCustomer`) rồi đặt đơn mới chọn COD. Quá hạn 15 phút thì `OrderExpiryScheduler` tự hủy và hoàn kho | Thêm phương thức ngoài sơ đồ sẽ làm lệch "code ↔ design". Hủy rồi đặt lại vẫn đạt mục tiêu của khách, kho được hoàn đúng (BR11), lịch sử trạng thái đầy đủ (BR14) |
