# Danh sách thay đổi cần làm trong Visual Paradigm

Mục đích: đưa sơ đồ VP về khớp với mã đã hiện thực, để giảng viên đối chiếu không thấy lệch.
Mỗi mục ghi mã lệch (L…/K…) tương ứng trong [`code-design-mapping.md`](code-design-mapping.md), mục 2.

> **Quan trọng:** chỉ sửa model và sơ đồ, rồi xuất lại `vp/model.xml`.
> **Không** sinh lại mã Java đè lên `backend/`, **không** sinh lại DDL đè lên `db/`, vì sẽ mất phần mã đã viết.
> CSDL không cần đổi (trừ khi chọn cách 2 ở mục 2).

Đánh dấu `[x]` khi đã sửa xong.

---

## A. Cần sửa: giảng viên đối chiếu sẽ thấy lệch

### Sơ đồ lớp thiết kế (Hình 4.5), package `domain`

- [ ] **1. `Category`: thêm `slug : String`** (L1)
  Cột `categories.slug` (NOT NULL, UNIQUE) đã có sẵn trong CSDL, và `CategoryRepository.findBySlug` cần nó. Chỉ cần thêm vào sơ đồ.

- [ ] **2. `Staff.active`** (L2): bảng `staff` không có cột `active`. Chọn **một** trong hai cách:
  - **Cách 1 (khuyên dùng):** xóa thuộc tính `active` khỏi `Staff`. CSDL giữ nguyên. Sau đó Claude bỏ trường `@Transient` và dòng kiểm tra trong `AuthService.loginStaff`.
  - **Cách 2:** giữ thuộc tính, thêm cột `active tinyint(1) DEFAULT 1 NOT NULL` vào bảng `staff` trong ERD, rồi sinh lại DDL và tạo lại CSDL.

- [ ] **3. `ChangeLog.of`: thêm tham số `product : Product`** (L8)
  Cột `product_change_logs.product_id` NOT NULL; các thay đổi cấp sản phẩm (giá, mô tả, ẩn/hiện) không có biến thể.
  - Hiện tại: `of(staff : Staff, variant : ProductVariant, field : String, oldValue : String, newValue : String) : ChangeLog`
  - Sửa thành: `of(staff : Staff, product : Product, variant : ProductVariant, field : String, oldValue : String, newValue : String) : ChangeLog`

- [ ] **4. Thêm lớp `AiInferenceLog`** (L10), ánh xạ bảng `ai_inference_logs` đã có (DB-R7):

  | Thuộc tính | Kiểu | Cột |
  |---|---|---|
  | `id` | `Long` | `log_id` |
  | `feature` | `AiFeature` (enum mới: `SIZE`, `IMAGE`) | `feature` |
  | `modelVersion` | `String` | `model_version` |
  | `inputHash` | `String` | `input_hash` |
  | `outputJson` | `String` | `output_json` |
  | `latencyMs` | `int` | `latency_ms` |
  | `fallbackUsed` | `boolean` | `fallback_used` |
  | `createdAt` | `LocalDateTime` | `created_at` |

  Kèm theo:
  - `repository.AiInferenceLogRepository`;
  - mũi tên Dependency `SizeAdvisorService → AiInferenceLogRepository` (Hình 4.9).

### Sơ đồ lớp phân tầng (Hình 4.7, 4.8), package `service`

- [ ] **5. `OrderService`: thêm 4 phương thức** (L4), phục vụ `OrderController.list/detail` và `AdminOrderController.list/detail`:
  - `listByCustomer(customerId : Long) : List<OrderSummaryDto>`
  - `getDetail(customerId : Long, orderCode : String) : OrderDetailDto`
  - `listForStaff(status : OrderStatus) : List<OrderSummaryDto>`
  - `getDetailForStaff(orderCode : String) : OrderDetailDto`

- [ ] **6. `ProductService`: thêm `listAllForStaff() : List<ProductDetailDto>`** (L5), phục vụ `AdminProductController.listAll`.

- [ ] **7. Kiểu trả về của `PaymentService.handleCallback(params)` và `PaymentCallbackController.vnpayIpn(params)`** (L3): model đang để trống. Chọn **một** trong hai cách:
  - **Cách 1:** ghi rõ kiểu trả về là `void`. Mã giữ nguyên: mã DT-05 lỗi trả qua `PaymentCallbackException`.
  - **Cách 2 (đúng với design-notes):** khai báo `handleCallback(...) : String`. Sau đó Claude sửa mã cho trả về `00`/`97`/`01`/`02`/`04`.

### Các kiểu dữ liệu chưa có trên sơ đồ (L12)

- [ ] **8. Thêm 4 lớp sau:**
  - `infra.TokenClaims`: `userId : Long`, `role : String`
  - `infra.SizeFeatures`: `productId : Long`, `heightCm : int`, `weightKg : int`, `fitPref : FitPref`, `fitType : FitType`
  - `infra.AiSizeResult`: `size : String`, `confidence : double`, `modelVersion : String`
  - `repository.ProductFilter`: `keyword : String`, `categoryId : Long`, `minPrice : BigDecimal`, `maxPrice : BigDecimal`, `size : String`, `color : String`, `fitType : FitType`, `sort : String`, `includeHidden : boolean`

### Sơ đồ tuần tự 2.10 (Chương 2)

- [ ] **9. Vẽ lại thứ tự đặt hàng** (L16): mã giữ kho **trước** rồi mới lưu đơn (BR11), để tránh deadlock.
  - Đang vẽ: tạo đơn → trừ kho.
  - Sửa thành: `OrderService.place` → `StockService.reserve(items)` (UPDATE có điều kiện, theo thứ tự `variant_id`) → lưu `Order` + `OrderItem` → (ONLINE) tạo `Payment` → `PaymentGatewayClient.buildPaymentUrl`.
  - Nhánh hết hàng: `reserve` ném lỗi `OUT_OF_STOCK`, rollback, không tạo đơn.

## B. Nên sửa: ghi chú cho khớp nghiệp vụ (mã không đổi)

- [ ] **10. Mã vận đơn** (L6): thêm ghi chú vào `OrderService.updateStatusByStaff` hoặc `OrderStateMachine.transit`: "Khi chuyển sang DANG_GIAO, hệ thống tự sinh tracking_code dạng VDyyMMddxxxxxx".
  Nếu muốn nhân viên nhập tay thì thêm tham số `trackingCode : String`; khi đó Claude phải sửa mã.

- [ ] **11. Đổi sang COD** (L7, DT-04): chọn **một** trong hai cách:
  - ghi chú phạm vi: "Khách hủy đơn rồi đặt lại, không hỗ trợ đổi phương thức thanh toán";
  - hoặc thêm `OrderService.switchToCod(customerId : Long, orderCode : String)`; Claude sẽ viết thêm mã.

## C. Tùy chọn: chỉnh kỹ thuật, thầy cô thường chấp nhận

- [ ] **12.** Nếu muốn sơ đồ khớp hoàn toàn với mã:
  - `Order.version`, `ProductVariant.version`: đổi `int` → `long` (K3);
  - thuộc tính danh sách: đổi `ArrayList<X>` → `List<X>` (K2);
  - 25 trường quan hệ: đổi `+` (public) → `-` (private) (K1).

## Không cần đưa vào VP

| Mã | Lý do |
|---|---|
| L9 `BodyProfile.customerId`, L11 `OrderItemId` / `CartItemId` | Chi tiết ORM (khóa dùng chung, khóa ghép) |
| L13 cổng thanh toán giả lập, L15 `BusinessException`, `PaymentCallbackException`, `DtoMapper` | Chỉ có ở bản demo, hoặc là lớp hỗ trợ |
| L14 vị trí package DTO, L17 VNPay thật | Không cần thay đổi |

---

## Sau khi sửa xong

1. Xuất `model.xml` mới, chép đè vào `vp/model.xml`. Nếu muốn, xuất cả ảnh sơ đồ vào `docs/diagrams/`.
2. Báo Claude kèm lựa chọn ở **mục 2, 7, 10, 11**.
3. Claude sẽ:
   - chạy lại phần đối chiếu;
   - sửa mã cho khớp (`ChangeLog.of`, `AiInferenceLog` + repository, `Staff`, `handleCallback`…);
   - chạy lại toàn bộ test;
   - cập nhật `docs/code-design-mapping.md`.
