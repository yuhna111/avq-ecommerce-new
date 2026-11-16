# Hướng dẫn sửa Visual Paradigm cho khớp với mã

## ĐỢT 2 (17/11): tải ảnh sản phẩm, quản lý nhân viên, trang Tổng quan

Mã đã làm xong và kiểm thử (67/67 test đạt). Để sơ đồ khớp mã, cần thêm **3 lớp, 9 phương thức, 4 mũi tên phụ thuộc** và một số use case.
Cách thao tác giống đợt 1 (xem "Các thao tác VP dùng nhiều lần" ở Phần 0 bên dưới). Vẫn **không** sinh lại mã hay DDL.

### A. Sơ đồ use case 1.5
- [ ] **A1.** Hình 1.5 đang đặt tên tiếng Anh (`staff`, `Customer`). Dùng công cụ **Actor** thêm actor **`owner`** (chủ cửa hàng), rồi vẽ **Generalization** từ `owner` tới `staff` (mũi tên tam giác rỗng chỉ vào `staff`): chủ cửa hàng làm được mọi việc của nhân viên.
- [ ] **A2.** Thêm use case **`manage staff`** (Quản lý nhân viên), nối **Association** với `owner`.
- [ ] **A3.** Thêm use case **`view sales overview`** (Xem tổng quan bán hàng), nối Association với `owner`.
- [ ] **A4.** Thêm use case **`upload product image`** (Tải ảnh sản phẩm), vẽ quan hệ **Extend** từ `upload product image` tới `add product` (tải ảnh là bước tùy chọn khi thêm sản phẩm).

### B. Hình 4.8 – Sản phẩm & Giỏ hàng
- [ ] **B1.** Chuột phải `AdminProductController` → Add → Operation, gõ:
  ```
  uploadImage(file : MultipartFile) : String
  ```
- [ ] **B2.** Chuột phải `ProductService` → Add → Operation, gõ cùng dòng như trên:
  ```
  uploadImage(file : MultipartFile) : String
  ```
- [ ] **B3.** Tạo lớp **`ImageStorage`** trong package `infra`. Cách nhanh: kéo `AiServiceClient` từ Model Explorer vào Hình 4.8, chuột phải → **Duplicate**, đổi tên thành `ImageStorage`, xóa phương thức chép theo, rồi xóa `AiServiceClient` khỏi sơ đồ bằng **Delete from View** (không xóa khỏi model). Thêm phương thức:
  ```
  save(file : MultipartFile) : String
  ```
  Kiểm tra trong Model Explorer: lớp nằm dưới 5. Design → `infra`, không có stereotype.
- [ ] **B4.** Vẽ **Dependency** `ProductService → ImageStorage`.

### C. Hình 4.9 – Tài khoản & Gợi ý size
- [ ] **C1.** Tạo lớp **`AdminStaffController`** (package `api`, stereotype `«RestController»`): Duplicate một controller có sẵn trên Hình 4.9 (ví dụ `AuthController`), đổi tên, xóa phương thức cũ. Thêm 3 phương thức:
  ```
  list() : List<StaffDto>
  create(req : CreateStaffRequest) : StaffDto
  resetPassword(id : Long, req : ResetPasswordRequest) : StaffDto
  ```
- [ ] **C2.** Tạo lớp **`StaffService`** (package `service`, stereotype `«Service»`): Duplicate `AuthService`, đổi tên, xóa phương thức cũ. Thêm 3 phương thức:
  ```
  listStaff() : List<StaffDto>
  createStaff(req : CreateStaffRequest) : StaffDto
  resetPassword(staffId : Long, newPassword : String) : StaffDto
  ```
- [ ] **C3.** Nếu `StaffRepository` chưa có trên Hình 4.9 thì kéo từ Model Explorer vào. Vẽ **Dependency** `AdminStaffController → StaffService` và `StaffService → StaffRepository`.
- [ ] **C4.** (Nên làm) Thêm Note cạnh `AdminStaffController`: `Chỉ vai trò OWNER (chủ cửa hàng) được gọi; tài khoản tạo mới luôn có vai trò STAFF (BR02).`

Trang **Tổng quan** của chủ cửa hàng không cần lớp mới: nó dùng `OrderService.listForStaff` và `ProductService.listAllForStaff` đã có. SKU tự sinh chỉ là gợi ý ở giao diện, không cần sửa VP.

### D. Lưu và gửi lại
- [ ] **D1.** `Ctrl+S`, rồi **Project → Export → XML…** vào `vp\xml-export`, chép `project.xml` đè lên `vp\model.xml`, chép `.vpp` đè lên `vp\AVQ_Clothing.vpp`.
- [ ] **D2.** Xuất lại ảnh `1.5.png`, `4.8.png`, `4.9.png` vào `docs\diagrams\`.
- [ ] **D3.** Báo Claude để đối chiếu lại (mục tiêu 73/73 lớp, 126/126 phương thức) và xóa dòng L18, L19 khỏi báo cáo.

---

# Đợt 1 (16/11) – đã hoàn tất

> **Trạng thái 16/11 (Claude kiểm tra `vp/model.xml` sau khi sinh viên sửa): HOÀN TẤT.**
> - Đã xong bước 1, 3–9, 10 (kiểu `void`), 13, 14, 15, `design-notes.md` và ảnh sơ đồ. Đối chiếu đạt 70/70 lớp, 117/117 phương thức, 112/112 thuộc tính; CSDL 18/18 bảng.
> - Bước 2: sinh viên chọn **bỏ** thuộc tính `Staff.active` (giữ `isActive()`). Mã đã sửa theo.
> - Các ghi chú Documentation (bước 10, 11, 12) không làm trên VP. Nội dung của chúng (mã vận đơn tự sinh, không hỗ trợ đổi sang COD) đã được chốt trong `design-notes.md` mục 9 (QĐ1, QĐ2).

Mục đích: đưa sơ đồ VP về khớp với mã đã hiện thực, để giảng viên đối chiếu "code ↔ design" không thấy lệch.
Mã lệch (L…/K…) trong ngoặc ứng với mục 2 của [`code-design-mapping.md`](code-design-mapping.md).
Đánh dấu `[x]` vào từng bước khi làm xong.

Mình đã chọn sẵn phương án cho các chỗ cần quyết định; mỗi chỗ đều ghi lý do. Tất cả phương án được chọn đều **không làm đổi mã**, nên sửa VP xong là khớp ngay.

---

## Phần 0. Chuẩn bị (5 phút)

- [ ] **0.1. Sao lưu.** Đóng VP, chép file `.vpp` thành một bản khác, ví dụ `AVQ_truoc-khi-sua_2026-11-16.vpp`.
- [ ] **0.2. Mở project, bật Model Explorer.** Nếu chưa thấy pane này: menu **View → Panes → Model Explorer**.
- [ ] **0.3. Nhớ phạm vi sửa.** Chỉ sửa:
  - các lớp trong model **"5. Design"** (package `api`, `service`, `infra`, `repository`, `domain`), trên các Hình **4.5, 4.7, 4.8, 4.9**;
  - sơ đồ tuần tự **2.10**.

  Model có ba lớp cùng tên `Category`/`Staff`/`ChangeLog`… nằm ở "2. Analysis", "4. Data Model" và "5. Design". **Chỉ sửa lớp trong "5. Design"**: đó là lớp có stereotype `«Entity»`, mở từ Hình 4.5.
- [ ] **0.4. Không bấm các lệnh sinh mã hay CSDL.** Cụ thể:
  - **Instant Generator** vào `backend/`;
  - **Generate Database**, **Synchronize to ERD**;
  - không sửa ERD 4.10 hay Data Model 4.1.

  Cột `slug` và bảng `ai_inference_logs` đã có sẵn trong CSDL.

### Các thao tác VP dùng nhiều lần bên dưới

| Muốn làm | Cách làm |
|---|---|
| Thêm thuộc tính | Chuột phải vào lớp trên sơ đồ → **Add → Attribute** (hoặc `Alt+Shift+A`), gõ `ten : Kieu` rồi Enter. Kiểm tra trước tên có dấu `-` (private). |
| Thêm phương thức | Chuột phải vào lớp → **Add → Operation** (`Alt+Shift+O`), gõ **cả dòng** `ten(thamSo : Kieu, ...) : KieuTraVe` rồi Enter. VP tự tách tham số và kiểu trả về, giống lúc bạn gõ `listCategories() : List<CategoryDto>`. |
| Sửa chi tiết phương thức | Chuột phải vào phương thức → **Open Specification…**. Tab **General** có *Return type*, *Visibility*, *Scope*; tab **Parameters** có nút *Add*, *Move Up*/*Move Down*. |
| Tạo lớp mới đúng package và stereotype | Chuột phải vào một lớp **cùng loại** trên sơ đồ → **Duplicate** (`Ctrl+E`). Đổi tên bản sao, xóa thuộc tính và phương thức cũ. Bản sao giữ nguyên package và stereotype của lớp gốc. |
| Kiểm tra package | Trong Model Explorer, lớp mới phải nằm dưới **5. Design → tên package**. Nếu nằm sai chỗ, kéo thả nó vào đúng package. |
| Đưa lớp có sẵn lên sơ đồ khác | Kéo lớp từ Model Explorer thả vào sơ đồ. Cách này không tạo lớp mới. |
| Vẽ mũi tên phụ thuộc | Trên thanh công cụ của sơ đồ chọn **Dependency** (mũi tên nét đứt), kéo từ lớp *dùng* sang lớp *được dùng*. |
| Ghi chú cho phần tử | Chọn phần tử, gõ vào pane **Documentation** ở dưới cùng. Hoặc mở *Open Specification → General → Documentation*. |
| Ghi chú hiện trên sơ đồ | Thanh công cụ → **Note**, đặt cạnh lớp, rồi dùng **Anchor** nối Note với lớp. |

---

## Phần 1. Hình 4.5 – Design Class Diagram (package `domain`)

### Bước 1. `Category`: thêm `slug` (L1)
- [ ] Mở Hình 4.5. Chuột phải lớp `Category` → Add → Attribute, gõ: `slug : String`
- [ ] Kết quả: `Category` có `- id : Long`, `- name : String`, `- slug : String`.

### Bước 2. `Staff.active`: giữ nguyên, thêm ghi chú (L2)
Phương án chọn: **giữ** thuộc tính `active` và phương thức `isActive() : boolean`, chỉ ghi chú rằng thuộc tính này không lưu trong CSDL.
Lý do: trước đó mình gợi ý xóa `active`, nhưng sơ đồ còn có `isActive()`. Xóa thì phải xóa cả hai, và mất ý tưởng khóa tài khoản nhân viên. Mã hiện tại đã đúng theo cách này: `@Transient`, luôn `true`.
- [ ] Chọn thuộc tính `active` của `Staff`. Trong pane Documentation, gõ:
  `Không lưu trong CSDL (bảng staff không có cột active). Bản demo: luôn true.`
- [ ] (Nên làm) Thêm một **Note** cạnh lớp `Staff` trên Hình 4.5 với cùng nội dung, rồi Anchor vào `Staff`. Làm vậy để người xem sơ đồ thấy được ngay.

### Bước 3. `ChangeLog.of`: thêm tham số `product` (L8)
Lý do: cột `product_change_logs.product_id` bắt buộc có giá trị. Các thay đổi giá, mô tả, ẩn/hiện là của sản phẩm, không thuộc biến thể nào.
- [ ] Chuột phải phương thức `of(...)` (đang gạch chân vì là static) trong `ChangeLog` → **Open Specification…** → tab **Parameters** → **Add**.
  - Name: `product`
  - Type: `Product`
- [ ] Bấm **Move Up** đến khi `product` đứng ngay sau `staff`.
- [ ] Bấm OK. Kết quả trên sơ đồ:
  `of(staff : Staff, product : Product, variant : ProductVariant, field : String, oldValue : String, newValue : String) : ChangeLog`
- [ ] Kiểm tra phương thức vẫn gạch chân: tab General, *Scope* = `classifier`.

### Bước 4. Tạo enum `AiFeature`
- [ ] Chuột phải enum `FitPref` → **Duplicate**, rồi đổi tên bản sao thành `AiFeature`.
- [ ] Xóa các giá trị cũ, sau đó chuột phải → **Add → Enumeration Literal**, thêm lần lượt `SIZE` và `IMAGE`.
- [ ] Kiểm tra: stereotype `«enumeration»`, nằm trong 5. Design → `domain`.

### Bước 5. Tạo lớp `AiInferenceLog` (L10)
- [ ] Chuột phải lớp `Category` → **Duplicate**, rồi đổi tên thành `AiInferenceLog`. Xóa các thuộc tính chép theo.
- [ ] Thêm 8 thuộc tính theo đúng thứ tự (Add → Attribute, gõ từng dòng):

  ```
  id : Long
  feature : AiFeature
  modelVersion : String
  inputHash : String
  outputJson : String
  latencyMs : int
  fallbackUsed : boolean
  createdAt : LocalDateTime
  ```
- [ ] Kiểm tra: stereotype `«Entity»`, nằm trong 5. Design → `domain`.
- [ ] (Nên làm) Vẽ mũi tên Dependency từ `AiInferenceLog` sang `AiFeature`, nếu các enum khác trên Hình 4.5 cũng được nối như vậy.

  `AiInferenceLog` không có quan hệ với lớp nào khác, vì bảng không có khóa ngoại.

---

## Phần 2. Hình 4.9 – Layered: Tài khoản & Gợi ý size

### Bước 6. Tạo `AiInferenceLogRepository`
- [ ] Mở Hình 4.9. Chuột phải `BodyProfileRepository` → **Duplicate**, rồi đổi tên thành `AiInferenceLogRepository`.
- [ ] **Xóa hết phương thức** chép theo. Lớp này không cần phương thức riêng vì hàm lưu có sẵn từ `JpaRepository`. Nếu tự thêm phương thức, mã sẽ phải viết thêm cho khớp.
- [ ] Kiểm tra: stereotype `«Interface»`, nằm trong 5. Design → `repository`.
- [ ] Nếu các repository khác trên Hình 4.9 có mũi tên tới entity của chúng: kéo `AiInferenceLog` từ Model Explorer vào Hình 4.9, rồi vẽ Dependency `AiInferenceLogRepository → AiInferenceLog`.

### Bước 7. Mũi tên `SizeAdvisorService → AiInferenceLogRepository`
- [ ] Công cụ **Dependency**, kéo từ `SizeAdvisorService` sang `AiInferenceLogRepository`.

### Bước 8. Ba kiểu dữ liệu của tầng `infra` (L12)
Ba lớp này đã được dùng làm kiểu tham số hoặc kiểu trả về trên sơ đồ (`parse(...) : TokenClaims`, `predictSize(features : SizeFeatures, ...)`), nhưng bản thân chúng chưa có trên sơ đồ.
- [ ] Chuột phải `AiServiceClient` → **Duplicate**, rồi đổi tên thành `SizeFeatures`. Xóa phương thức chép theo, sau đó thêm:
  ```
  productId : Long
  heightCm : int
  weightKg : int
  fitPref : FitPref
  fitType : FitType
  ```
- [ ] Làm tương tự, tạo `AiSizeResult`:
  ```
  size : String
  confidence : double
  modelVersion : String
  ```
- [ ] Làm tương tự, tạo `TokenClaims`, đặt cạnh `JwtTokenProvider`:
  ```
  userId : Long
  role : String
  ```
- [ ] Kiểm tra cả ba nằm trong 5. Design → `infra`, không có stereotype.
- [ ] (Nên làm) Vẽ Dependency `AiServiceClient → SizeFeatures`, `AiServiceClient → AiSizeResult`, `JwtTokenProvider → TokenClaims`.

---

## Phần 3. Hình 4.7 – Layered: Đặt hàng & Thanh toán

### Bước 9. `OrderService`: thêm 4 phương thức (L4)
Bốn phương thức này phục vụ màn danh sách và chi tiết đơn của khách (`OrderController`) và của nhân viên (`AdminOrderController`).
- [ ] Chuột phải `OrderService` → Add → Operation, gõ từng dòng rồi Enter:
  ```
  listByCustomer(customerId : Long) : List<OrderSummaryDto>
  getDetail(customerId : Long, orderCode : String) : OrderDetailDto
  listForStaff(status : OrderStatus) : List<OrderSummaryDto>
  getDetailForStaff(orderCode : String) : OrderDetailDto
  ```
- [ ] Kiểm tra bốn dòng hiện đúng như trên và có dấu `+` (public).

### Bước 10. Ghi rõ kiểu trả về `void` (L3)
Phương án chọn: `void`, khớp với mã hiện tại. Mã kết quả DT-05 được báo qua ngoại lệ `PaymentCallbackException`.
- [ ] Chuột phải `handleCallback(params)` trong `PaymentService` → Open Specification → tab General → **Return type**: gõ `void` → OK.
- [ ] Trong Documentation của `handleCallback`, gõ:
  `Xử lý kết quả thanh toán theo DT-05. Thành công/thất bại hợp lệ: trả về bình thường (mã 00). Sai chữ ký 97, không có đơn 01, đã xử lý 02, lệch tiền 04: báo qua PaymentCallbackException.`
- [ ] Làm tương tự cho `vnpayIpn(params)` trong `PaymentCallbackController`: Return type = `void`.
- [ ] Mở `docs/design-notes.md`, sửa dòng `handleCallback(...) : String` thành `: void`, để tài liệu khớp với sơ đồ.

### Bước 11. Ghi chú mã vận đơn (L6)
Phương án chọn: hệ thống tự sinh mã vận đơn, nhân viên không nhập. Mã hiện tại đang làm như vậy.
- [ ] Chọn phương thức `transit(...)` của `OrderStateMachine`. Trong Documentation, gõ:
  `Mọi thay đổi trạng thái đi qua đây và ghi order_status_histories. Khi chuyển sang DANG_GIAO, hệ thống tự sinh tracking_code dạng VDyyMMddxxxxxx.`

### Bước 12. Ghi chú phạm vi "đổi sang COD" (L7)
Phương án chọn: ghi ngoài phạm vi. Thiết kế không có phương thức cho việc này, và mã cũng không làm.
- [ ] Thêm một **Note** cạnh `OrderService` trên Hình 4.7, nội dung:
  `DT-04: không hỗ trợ đổi phương thức thanh toán ONLINE → COD. Khách hủy đơn (cancelByCustomer) rồi đặt lại.`
  Anchor Note vào `OrderService`.

---

## Phần 4. Hình 4.8 – Layered: Sản phẩm & Giỏ hàng

### Bước 13. `ProductService`: thêm phương thức (L5)
- [ ] Chuột phải `ProductService` → Add → Operation, gõ:
  ```
  listAllForStaff() : List<ProductDetailDto>
  ```

### Bước 14. Tạo lớp `ProductFilter` (L12)
Lớp này là một class thường. **Đừng Duplicate repository**, vì repository có stereotype `«Interface»`.
- [ ] Trên thanh công cụ của Hình 4.8 chọn **Class**, đặt cạnh `ProductRepository`, đặt tên `ProductFilter`.
- [ ] Trong Model Explorer, tìm `ProductFilter`. Nếu nó không nằm dưới 5. Design → `repository`, kéo thả vào đó.
- [ ] Thêm 9 thuộc tính:
  ```
  keyword : String
  categoryId : Long
  minPrice : BigDecimal
  maxPrice : BigDecimal
  size : String
  color : String
  fitType : FitType
  sort : String
  includeHidden : boolean
  ```
- [ ] (Nên làm) Vẽ Dependency `ProductRepository → ProductFilter` và `ProductService → ProductFilter`.

---

## Phần 5. Sơ đồ tuần tự 2.10 – Place Order (L16)

Hiện tại sơ đồ vẽ **tạo đơn trước, trừ kho sau**:
`10: create(lines, fee, status)` → Order, `11: orderId`, rồi mới đến `12: decreaseStock(lines)` → ProductVariant, `13: ok`.

Mã làm ngược lại: **trừ kho trước, tạo đơn sau** (BR11), để tránh deadlock khi nhiều khách mua cùng lúc.

- [ ] **15.1.** Mở sơ đồ 2.10. Bấm chọn mũi tên `12: decreaseStock(lines)` (từ OrderController sang ProductVariant), **kéo thẳng lên** đến khi nó nằm trên `10: create(lines, fee, status)`, ngay dưới `9: placeOrder(method)`.
- [ ] **15.2.** Kéo mũi tên trả về `13: ok` lên theo, đặt ngay dưới `decreaseStock`.
- [ ] **15.3.** Kiểm tra số thứ tự VP tự đánh lại:

  ```
  9:  placeOrder(method)          CheckoutPage → OrderController
  10: decreaseStock(lines)        OrderController → ProductVariant
  11: ok                          ProductVariant → OrderController (nét đứt)
  12: create(lines, fee, status)  OrderController → Order
  13: orderId                     Order → OrderController (nét đứt)
  14: showConfirmation(orderId)   (trong alt [COD], giữ nguyên)
  ```
  Nếu VP không tự đánh lại số: chuột phải nền sơ đồ → **Sequence Number** → chọn kiểu tự động (Single Level). Hoặc mở Specification của từng message để sửa.

  Nếu không kéo được: xóa hai mũi tên `decreaseStock` và `ok`, rồi vẽ lại ngay dưới `9: placeOrder`:
  - công cụ **Message**: kéo từ thanh kích hoạt của OrderController sang ProductVariant, đặt tên `decreaseStock(lines)`;
  - công cụ **Return Message**: kéo theo chiều ngược lại, đặt tên `ok`.
- [ ] **15.4.** Thêm một **Note** cạnh `decreaseStock(lines)`, nội dung:
  `Trừ kho có điều kiện trong cùng giao dịch: UPDATE product_variants SET stock_qty = stock_qty - ? WHERE variant_id = ? AND stock_qty >= ?. 0 dòng = hết hàng → rollback, không tạo đơn.`
- [ ] **15.5.** (Nên làm) Thêm Combined Fragment **alt** bao quanh phần từ `create(lines, fee, status)` trở xuống:
  - Operand 1, guard `[đủ hàng]`: chứa các message hiện có.
  - Operand 2, guard `[hết hàng]`: thêm message nét đứt `showOutOfStock(lines)` từ OrderController về CheckoutPage.

  Nếu bước này khó, chỉ cần bước 15.4 là đủ thể hiện nhánh hết hàng.

---

## Phần 6. Tùy chọn: chỉnh kỹ thuật (thầy cô thường chấp nhận)

Báo cáo đã giải thích ba chỗ này là chỉnh kỹ thuật bắt buộc. Chỉ làm nếu còn thời gian:
- [ ] **K3:** `Order.version` và `ProductVariant.version` trong 5. Design → Open Specification → Type: `long`.
- [ ] **K2:** các thuộc tính danh sách đang là `ArrayList<...>` → đổi thành `List<...>`.
- [ ] **K1:** đầu mút quan hệ (role) đang public. Chọn từng đường quan hệ → Open Specification → tab của đầu mút có tên role → **Visibility: private**. Có 25 chỗ, tốn thời gian nhất.

---

## Phần 7. Lưu, xuất file, báo lại

- [ ] **7.1.** Lưu project (`Ctrl+S`).
- [ ] **7.2.** Xuất XML **giống lần trước**: menu **Project → Export → XML…**, chọn thư mục `C:\avq2\avq-clothing\vp\xml-export`. VP tạo ra `project.xml` và `data.zip`, ghi đè bản cũ.
- [ ] **7.3.** Chép `vp\xml-export\project.xml` đè lên `vp\model.xml`. Lần trước hai file này giống hệt nhau.
- [ ] **7.4.** Chép file `.vpp` đã sửa đè lên `vp\AVQ_Clothing.vpp`.
- [ ] **7.5.** Xuất lại ảnh các sơ đồ đã sửa: chuột phải nền sơ đồ → **Export → Export PNG…**, lưu đè vào `docs\diagrams\` với đúng tên cũ: `2.10.png`, `4.5.png`, `4.7.png`, `4.8.png`, `4.9.png`.
- [ ] **7.6.** Báo Claude: "đã sửa VP xong". Claude sẽ:
  - đối chiếu lại `model.xml` với mã, bảo đảm các phương thức trên sơ đồ khớp đủ, gồm cả 5 phương thức mới;
  - sửa mã cho các mục còn lệch (ví dụ thêm tham số `product` vào `ChangeLog.of`, chuyển ghi log sang entity `AiInferenceLog` + repository);
  - chạy lại toàn bộ test;
  - cập nhật `docs/code-design-mapping.md`, xóa các dòng lệch đã được sửa.

---

## Bảng tóm tắt

| # | Sơ đồ | Việc | Mã lệch | Mã có phải sửa? |
|---|---|---|---|---|
| 1 | 4.5 | `Category` + `slug : String` | L1 | Không |
| 2 | 4.5 | Ghi chú `Staff.active` không lưu CSDL | L2 | Không |
| 3 | 4.5 | `ChangeLog.of` + tham số `product : Product` | L8 | Có (Claude sửa) |
| 4 | 4.5 | Enum `AiFeature` (SIZE, IMAGE) | L10 | Có (Claude thêm) |
| 5 | 4.5 | Lớp `AiInferenceLog` (8 thuộc tính) | L10 | Có (Claude thêm) |
| 6 | 4.9 | `AiInferenceLogRepository` | L10 | Có (Claude thêm) |
| 7 | 4.9 | Dependency `SizeAdvisorService →` repository mới | L10 | Có (Claude sửa) |
| 8 | 4.9 | `SizeFeatures`, `AiSizeResult`, `TokenClaims` | L12 | Không |
| 9 | 4.7 | `OrderService` + 4 phương thức | L4 | Không |
| 10 | 4.7 | `handleCallback`, `vnpayIpn` : `void` + ghi chú | L3 | Không |
| 11 | 4.7 | Ghi chú tự sinh mã vận đơn | L6 | Không |
| 12 | 4.7 | Note: không hỗ trợ đổi sang COD | L7 | Không |
| 13 | 4.8 | `ProductService.listAllForStaff()` | L5 | Không |
| 14 | 4.8 | Lớp `ProductFilter` (9 thuộc tính) | L12 | Không |
| 15 | 2.10 | Trừ kho **trước** tạo đơn + Note | L16 | Không |
| 16 | 4.5 | (Tùy chọn) K1, K2, K3 | K1–K3 | Không |
