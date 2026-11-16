# Quy tắc nghiệp vụ BR01–BR17 và bảng quyết định DT-01…DT-05

Nguồn: tiểu luận, Chương 1 (Mục quy tắc nghiệp vụ) và Chương 2 (Mục 3.2 Logic quyết định, Mục 3.3 Xử lý ngoại lệ).
Trạng thái đơn trong mã dùng enum `OrderStatus`: `CHO_THANH_TOAN` (Chờ thanh toán), `CHO_XAC_NHAN` (Chờ xác nhận), `DANG_GIAO` (Đang giao), `DA_GIAO` (Đã giao), `DA_HUY` (Đã hủy).

## 1. Quy tắc nghiệp vụ

| Mã | Quy tắc | Nơi thực thi trong thiết kế |
|---|---|---|
| BR01 | Mỗi email hoặc số điện thoại chỉ đăng ký được một tài khoản (EX-04) | `AuthService.register`; UNIQUE `customers.email`, `customers.phone` |
| BR02 | Tài khoản nhân viên do chủ cửa hàng cấp, không có đăng ký công khai. Chủ cửa hàng có mọi quyền của nhân viên, thêm quyền xem Tổng quan và quản lý tài khoản nhân viên (tạo, đặt lại mật khẩu) | `StaffService.createStaff`, `StaffService.resetPassword` (chỉ vai trò OWNER, bổ sung 17/11) |
| BR03 | Xem, tìm, lọc sản phẩm không cần đăng nhập; thêm vào giỏ, đặt hàng, theo dõi đơn cần đăng nhập | Phân quyền Spring Security |
| BR04 | Phải chọn đủ size và màu (một biến thể) trước khi thêm vào giỏ | `CartService.addToCart` (DT-02) |
| BR05 | Số lượng mỗi dòng là số nguyên dương, tối đa 10 và không vượt tồn kho của biến thể | `CartService`; CHECK `cart_items.quantity BETWEEN 1 AND 10` |
| BR06 | Giá bán phải lớn hơn 0; tồn kho là số nguyên không âm | `ProductService`, `StockService.updateStock`; CHECK `price > 0`, `stock_qty >= 0` |
| BR07 | Mỗi biến thể chỉ có một dòng trong giỏ; thêm lại thì cộng dồn số lượng | `Cart.addOrIncrease`; khóa chính `cart_items (variant_id, cart_id)` |
| BR08 | Giá áp dụng cho đơn là giá hiện hành lúc khách xác nhận; khách phải thấy giá đó trước khi xác nhận | `OrderService.prepare` hiển thị, `OrderService.place` chốt vào `order_items` |
| BR09 | Phí giao hàng 30.000 đ mỗi đơn; miễn phí khi tạm tính từ 500.000 đ trở lên | `ShippingFeeCalculator.calculate` (DT-01); CHECK `ck_order_total` |
| BR10 | Đơn COD chuyển ngay sang "Chờ xác nhận"; đơn thanh toán trực tuyến ở "Chờ thanh toán" đến khi cổng báo thành công | `OrderService.place`, `PaymentService.handleCallback` |
| BR11 | Tồn kho bị trừ khi đơn được tạo (giữ hàng) và được cộng lại khi đơn bị hủy | `StockService.reserve` / `restore`, `ProductVariantRepository.decreaseIfEnough` / `increase` |
| BR12 | Đơn "Chờ thanh toán" quá 15 phút không thanh toán thành công thì tự động hủy | `OrderExpiryScheduler` (mỗi 60 giây) → `OrderService.expireOverdue` |
| BR13 | Khách chỉ được hủy đơn ở "Chờ thanh toán" hoặc "Chờ xác nhận" | `OrderService.cancelByCustomer` (DT-03) |
| BR14 | Vòng đời đơn: Chờ thanh toán → Chờ xác nhận hoặc Đã hủy; Chờ xác nhận → Đang giao hoặc Đã hủy; Đang giao → Đã giao. Không có chuyển trạng thái nào khác | `OrderStateMachine.transit` (DT-04) |
| BR15 | Khách chỉ xem được giỏ và đơn của mình; chỉ nhân viên được quản lý sản phẩm và cập nhật trạng thái đơn | Phân quyền; service luôn lọc theo `customerId` lấy từ token |
| BR16 | Mật khẩu tối thiểu 8 ký tự; sai mật khẩu 5 lần liên tiếp thì khóa đăng nhập 15 phút | `AuthService`, `User.recordFailedLogin`, `User.isLocked` |
| BR17 | Mỗi sản phẩm có ít nhất một biến thể; mã SKU của biến thể là duy nhất | `ProductService.create`; UNIQUE `sku`, UNIQUE `(product_id, size, color)` |

## 2. Bảng quyết định

### DT-01 — Phí giao hàng (BR09)

| Điều kiện: tạm tính | Phí giao | Ví dụ |
|---|---|---|
| Dưới 500.000 đ | 30.000 đ | Tạm tính 350.000 đ → tổng 380.000 đ |
| Từ 500.000 đ trở lên | 0 đ | Tạm tính 520.000 đ → tổng 520.000 đ |

`total = subtotal + shippingFee`. Các ca kiểm thử biên: 249.000 → 30.000; 499.999 → 30.000; 500.000 → 0; 708.000 → 0.

### DT-02 — Thêm vào giỏ (BR03, BR04, BR05, BR07)

| Điều kiện | R1 | R2 | R3 | R4 | R5 |
|---|---|---|---|---|---|
| Đã đăng nhập | Không | Có | Có | Có | Có |
| Đã chọn đủ size, màu | – | Không | Có | Có | Có |
| Số lượng cộng dồn ≤ tồn kho và ≤ 10 | – | – | Không | Có | Có |
| Biến thể đã có trong giỏ | – | – | – | Không | Có |
| **Hành động** | Chuyển trang đăng nhập | Nhắc chọn biến thể | Báo số lượng còn lại | Thêm dòng mới | Cộng dồn |

### DT-03 — Ai được hủy đơn và hệ quả (BR11, BR13, BR14)

| Trạng thái hiện tại | Khách hủy | Nhân viên hủy | Hệ thống tự hủy | Hoàn tồn kho |
|---|---|---|---|---|
| Chờ thanh toán | Được | Không | Được (quá 15 phút) | Có |
| Chờ xác nhận | Được | Được (bắt buộc lý do) | Không | Có |
| Đang giao | Không | Không | Không | – |
| Đã giao, Đã hủy | Không | Không | Không | – |

### DT-04 — Bảng chuyển trạng thái đơn (BR14)

| Từ \ Sang | Chờ xác nhận | Đang giao | Đã giao | Đã hủy |
|---|---|---|---|---|
| Chờ thanh toán | Hệ thống (thanh toán thành công) | – | – | Khách, hệ thống |
| Chờ xác nhận | – | Nhân viên | – | Khách, nhân viên |
| Đang giao | – | – | Nhân viên | – |
| Đã giao | – | – | – | – |

- Ô "–" là chuyển trạng thái không hợp lệ: từ chối và không ghi lịch sử. "Đã giao", "Đã hủy" là trạng thái kết thúc.
- Mỗi lần chuyển hợp lệ ghi một dòng `order_status_histories` (người thực hiện, thời điểm, ghi chú). Đơn mới tạo ghi `fromStatus = null`, ghi chú "Tạo đơn".
- Chuyển sang "Đang giao" lưu mã vận đơn vào `orders.tracking_code`. Mã do hệ thống tự sinh, dạng `VDyyMMddxxxxxx` (design-notes QĐ1).
- **Phạm vi:** "khách đổi sang COD" từ "Chờ thanh toán" **không thuộc bản demo** (design-notes QĐ2). Khách thanh toán lại trong 15 phút, hoặc hủy đơn rồi đặt đơn mới chọn COD.

### DT-05 — Xử lý kết quả thanh toán (BR10, NFR11)

| Chữ ký hợp lệ | Số tiền khớp đơn | Kết quả từ cổng | Đã ghi trước đó | Hành động | Mã phản hồi |
|---|---|---|---|---|---|
| Không | – | – | – | Bỏ qua, ghi nhật ký cảnh báo | `97` |
| Có | Không | – | – | Ghi thanh toán "Sai lệch" (`markMismatch`), đơn giữ nguyên, báo nhân viên | `04` |
| Có | Có | Thành công | Không | Ghi "Thành công" (`markResult(true)`), đơn sang "Chờ xác nhận" | `00` |
| Có | Có | Thất bại | Không | Ghi "Thất bại" (`markResult(false)`), đơn giữ "Chờ thanh toán" | `00` |
| Có | Có | – | Có | Bỏ qua (kết quả trùng, `txn_ref` đã có) | `02` |

Không tìm thấy đơn theo mã: mã `01`.

## 3. Xử lý ngoại lệ (EX)

| Ngoại lệ | Phát hiện ở | Hành vi | Dữ liệu sau xử lý | Thông báo cho người dùng |
|---|---|---|---|---|
| EX-01 Sai đăng nhập | UC02, UC13 | Tăng bộ đếm; khóa khi đủ 5 lần | Bộ đếm, thời điểm khóa | "Email/SĐT hoặc mật khẩu không đúng" (không nói rõ trường nào sai) |
| EX-02 Vượt tồn kho | UC07, UC08 | Từ chối thêm hoặc đổi số lượng | Giỏ không đổi | "Chỉ còn N sản phẩm size M màu đen" |
| EX-03 Thanh toán thất bại | UC10 | Giữ đơn "Chờ thanh toán" đến hạn 15 phút | Payment "Thất bại" | Lý do từ cổng và ba lựa chọn tiếp theo |
| EX-04 Trùng tài khoản | UC01 | Từ chối đăng ký | Không tạo tài khoản | "Email/SĐT đã được đăng ký, vui lòng đăng nhập" |
| EX-05 Hủy đơn không cho phép | UC12 | Không hiển thị nút hủy; từ chối nếu gửi trực tiếp | Đơn không đổi | Hướng dẫn liên hệ cửa hàng |
| EX-06 Hết hàng khi đặt | UC09 | Dừng đặt hàng trước khi tạo đơn | Không tạo đơn; giỏ giữ nguyên | Danh sách dòng cần chỉnh |
| Lỗi lưu giữa chừng | UC09, UC17, UC19 | Hoàn tác toàn bộ giao dịch (NFR05) | Như trước thao tác | "Có lỗi, vui lòng thử lại" |
| Xung đột đồng thời | UC17, UC19 | So phiên bản (`version`); từ chối ghi đè | Giữ giá trị mới nhất | Hiển thị giá trị hiện tại để thao tác lại |

## 4. Gợi ý size (Chương 3, FB-01)

- Đầu vào: chiều cao (cm), cân nặng (kg), kiểu mặc ưa thích (`FitPref`: OM, VUA, RONG); khách có thể đồng ý lưu số đo (`body_profiles`).
- Bản demo **không có dịch vụ AI thật**: luôn dùng `FallbackPolicy.ruleBasedSize`.
  1. Chọn size đầu tiên trong `size_charts` của sản phẩm có khoảng chiều cao **và** cân nặng chứa số đo.
  2. Không size nào khớp: chọn size có khoảng chiều cao gần nhất.
  3. Sản phẩm phom `OVERSIZE` và khách chọn `VUA`: lùi một size.
  4. Sản phẩm không có bảng size: trả "Liên hệ cửa hàng để được tư vấn size".
- Khi có AI thật (ngoài phạm vi demo): ngưỡng tin cậy 0,6 và thời gian chờ 1,5 giây; dưới ngưỡng, lỗi hoặc quá giờ thì dùng bảng size.
- Mỗi lần gợi ý ghi một dòng `ai_inference_logs` (`feature = SIZE`, `fallback_used = 1`, `input_hash` là SHA-256 của số đo, không lưu số đo thô).

## 5. Yêu cầu phi chức năng liên quan đến mã

- Token đăng nhập (JWT) hết hạn sau 30 phút.
- Mọi thao tác ghi nhiều bảng (đặt hàng, hủy đơn, cập nhật trạng thái, sửa tồn kho) chạy trong một giao dịch; lỗi giữa chừng thì hoàn tác toàn bộ (NFR05).
- Sửa giá, mô tả, tồn kho đều ghi `product_change_logs` (FR31).
