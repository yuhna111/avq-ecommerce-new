# Báo cáo đối chiếu mã ↔ thiết kế ↔ CSDL

Dự án AVQ Clothing — Giai đoạn 9 (CLAUDE.md mục 6).

Hai tiêu chí giảng viên kiểm tra:

| Tiêu chí | Bằng chứng | Kết quả |
|---|---|---|
| **code ↔ design** | 63 lớp VP sinh được chép sang `backend/` và viết tiếp trên chính các file đó; 7 lớp thêm vào VP ngày 16/11 được viết tay theo đúng sơ đồ (B7). Script đối chiếu `vp/model.xml` với mã (mục 1.1) | **70/70** lớp có file cùng tên, cùng package; **117/117** phương thức và **112/112** thuộc tính trên sơ đồ có trong mã với đúng tên |
| **use DB?** | `spring.jpa.hibernate.ddl-auto=validate` trên `avq_db_vp` tạo từ `db/V2__vp_generated.sql`. Phép thử ngược: đổi sai tên một cột thì Hibernate chặn (`missing column [tracking_code_x] in table [orders]`) | Khởi động thành công; **18/18 bảng có entity**, **0 cột chưa ánh xạ** |

Chiều phụ thuộc theo Hình 4.6, kiểm tra bằng `import`: `api → service`, `service → repository, infra, domain`, `infra → domain`, `repository → domain`. Domain chỉ dùng `jakarta.persistence`. Hai ngoại lệ có lý do được ghi ở L13 và L14.

---

## 1. Bảng đối chiếu

### 1.1. Lớp và phương thức trên sơ đồ ↔ mã nguồn

Sinh tự động từ `vp/model.xml` (model "5. Design") và mã trong `backend/src/main/java`. Cột "Hình" lấy từ các sơ đồ VP có chứa lớp (4.5 Design Class Diagram, 4.7–4.9 Layered Class Diagram).

Tổng: **117/117** phương thức trên sơ đồ có mặt trong mã với đúng tên; 70/70 lớp có file cùng tên, cùng package; **112/112** thuộc tính trên sơ đồ có trường cùng tên trong mã.


**Package `api`**

| Lớp | Hình | Phương thức / thuộc tính trên sơ đồ | Vị trí trong mã |
|---|---|---|---|
| `AccountController` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/api/AccountController.java:18](../backend/src/main/java/api/AccountController.java#L18) |
| | | `listAddresses(customerId)` | [:26](../backend/src/main/java/api/AccountController.java#L26) |
| | | `addAddress(customerId, req)` | [:31](../backend/src/main/java/api/AccountController.java#L31) |
| `AdminOrderController` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/api/AdminOrderController.java:19](../backend/src/main/java/api/AdminOrderController.java#L19) |
| | | `list(status)` | [:27](../backend/src/main/java/api/AdminOrderController.java#L27) |
| | | `detail(code)` | [:32](../backend/src/main/java/api/AdminOrderController.java#L32) |
| | | `updateStatus(staffId, code, to, note)` | [:38](../backend/src/main/java/api/AdminOrderController.java#L38) |
| `AdminProductController` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/api/AdminProductController.java:23](../backend/src/main/java/api/AdminProductController.java#L23) |
| | | `listAll()` | [:33](../backend/src/main/java/api/AdminProductController.java#L33) |
| | | `create(staffId, req)` | [:38](../backend/src/main/java/api/AdminProductController.java#L38) |
| | | `update(staffId, id, req)` | [:44](../backend/src/main/java/api/AdminProductController.java#L44) |
| | | `updateStock(staffId, variantId, stock)` | [:51](../backend/src/main/java/api/AdminProductController.java#L51) |
| `AuthController` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/api/AuthController.java:16](../backend/src/main/java/api/AuthController.java#L16) |
| | | `register(req)` | [:24](../backend/src/main/java/api/AuthController.java#L24) |
| | | `login(req)` | [:29](../backend/src/main/java/api/AuthController.java#L29) |
| | | `staffLogin(req)` | [:34](../backend/src/main/java/api/AuthController.java#L34) |
| `CartController` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/api/CartController.java:18](../backend/src/main/java/api/CartController.java#L18) |
| | | `view(customerId)` | [:26](../backend/src/main/java/api/CartController.java#L26) |
| | | `add(customerId, variantId, qty)` | [:32](../backend/src/main/java/api/CartController.java#L32) |
| | | `changeQty(customerId, variantId, qty)` | [:38](../backend/src/main/java/api/CartController.java#L38) |
| | | `remove(customerId, variantId)` | [:44](../backend/src/main/java/api/CartController.java#L44) |
| `OrderController` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/api/OrderController.java:23](../backend/src/main/java/api/OrderController.java#L23) |
| | | `prepare(customerId)` | [:31](../backend/src/main/java/api/OrderController.java#L31) |
| | | `place(customerId, req)` | [:36](../backend/src/main/java/api/OrderController.java#L36) |
| | | `list(customerId)` | [:42](../backend/src/main/java/api/OrderController.java#L42) |
| | | `detail(customerId, code)` | [:47](../backend/src/main/java/api/OrderController.java#L47) |
| | | `cancel(customerId, code, reason)` | [:53](../backend/src/main/java/api/OrderController.java#L53) |
| `PaymentCallbackController` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/api/PaymentCallbackController.java:27](../backend/src/main/java/api/PaymentCallbackController.java#L27) |
| | | `vnpayIpn(params)` | [:43](../backend/src/main/java/api/PaymentCallbackController.java#L43) |
| | | `mockPay(code, success)` | [:53](../backend/src/main/java/api/PaymentCallbackController.java#L53) |
| `ProductController` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/api/ProductController.java:18](../backend/src/main/java/api/ProductController.java#L18) |
| | | `categories()` | [:26](../backend/src/main/java/api/ProductController.java#L26) |
| | | `search(filter)` | [:32](../backend/src/main/java/api/ProductController.java#L32) |
| | | `detail(id)` | [:37](../backend/src/main/java/api/ProductController.java#L37) |
| `SizeController` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/api/SizeController.java:15](../backend/src/main/java/api/SizeController.java#L15) |
| | | `advise(customerId, productId, req)` | [:23](../backend/src/main/java/api/SizeController.java#L23) |

**Package `service`**

| Lớp | Hình | Phương thức / thuộc tính trên sơ đồ | Vị trí trong mã |
|---|---|---|---|
| `AccountService` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/service/AccountService.java:16](../backend/src/main/java/service/AccountService.java#L16) |
| | | `listAddresses(customerId)` | [:26](../backend/src/main/java/service/AccountService.java#L26) |
| | | `addAddress(customerId, req)` | [:35](../backend/src/main/java/service/AccountService.java#L35) |
| `AuthService` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/service/AuthService.java:18](../backend/src/main/java/service/AuthService.java#L18) |
| | | `register(req)` | [:39](../backend/src/main/java/service/AuthService.java#L39) |
| | | `loginCustomer(identifier, password)` | [:63](../backend/src/main/java/service/AuthService.java#L63) |
| | | `loginStaff(username, password)` | [:74](../backend/src/main/java/service/AuthService.java#L74) |
| `CartService` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/service/CartService.java:21](../backend/src/main/java/service/CartService.java#L21) |
| | | `getCart(customerId)` | [:37](../backend/src/main/java/service/CartService.java#L37) |
| | | `addToCart(customerId, variantId, qty)` | [:42](../backend/src/main/java/service/CartService.java#L42) |
| | | `changeQty(customerId, variantId, qty)` | [:61](../backend/src/main/java/service/CartService.java#L61) |
| | | `removeItem(customerId, variantId)` | [:75](../backend/src/main/java/service/CartService.java#L75) |
| `FallbackPolicy` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/service/FallbackPolicy.java:13](../backend/src/main/java/service/FallbackPolicy.java#L13) |
| | | `ruleBasedSize(chart, heightCm, weightKg, fitPref, fitType)` | [:21](../backend/src/main/java/service/FallbackPolicy.java#L21) |
| `OrderExpiryScheduler` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/service/OrderExpiryScheduler.java:14](../backend/src/main/java/service/OrderExpiryScheduler.java#L14) |
| | | `expirePendingOrders()` | [:23](../backend/src/main/java/service/OrderExpiryScheduler.java#L23) |
| `OrderService` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/service/OrderService.java:35](../backend/src/main/java/service/OrderService.java#L35) |
| | | `prepare(customerId)` | [:74](../backend/src/main/java/service/OrderService.java#L74) |
| | | `place(customerId, req)` | [:89](../backend/src/main/java/service/OrderService.java#L89) |
| | | `cancelByCustomer(customerId, orderCode, reason)` | [:142](../backend/src/main/java/service/OrderService.java#L142) |
| | | `updateStatusByStaff(staffId, orderCode, to, note)` | [:161](../backend/src/main/java/service/OrderService.java#L161) |
| | | `expireOverdue(now)` | [:185](../backend/src/main/java/service/OrderService.java#L185) |
| | | `listByCustomer(customerId)` | [:198](../backend/src/main/java/service/OrderService.java#L198) |
| | | `getDetail(customerId, orderCode)` | [:203](../backend/src/main/java/service/OrderService.java#L203) |
| | | `listForStaff(status)` | [:208](../backend/src/main/java/service/OrderService.java#L208) |
| | | `getDetailForStaff(orderCode)` | [:216](../backend/src/main/java/service/OrderService.java#L216) |
| `OrderStateMachine` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/service/OrderStateMachine.java:25](../backend/src/main/java/service/OrderStateMachine.java#L25) |
| | | `transit(order, to, staff, note)` | [:45](../backend/src/main/java/service/OrderStateMachine.java#L45) |
| | | `nextStatuses(from)` | [:69](../backend/src/main/java/service/OrderStateMachine.java#L69) |
| `PaymentService` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/service/PaymentService.java:23](../backend/src/main/java/service/PaymentService.java#L23) |
| | | `createPayment(order)` | [:44](../backend/src/main/java/service/PaymentService.java#L44) |
| | | `handleCallback(params)` | [:55](../backend/src/main/java/service/PaymentService.java#L55) |
| `ProductService` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/service/ProductService.java:31](../backend/src/main/java/service/ProductService.java#L31) |
| | | `listCategories()` | [:49](../backend/src/main/java/service/ProductService.java#L49) |
| | | `search(filter)` | [:55](../backend/src/main/java/service/ProductService.java#L55) |
| | | `getDetail(id)` | [:69](../backend/src/main/java/service/ProductService.java#L69) |
| | | `create(staffId, req)` | [:77](../backend/src/main/java/service/ProductService.java#L77) |
| | | `update(staffId, id, req)` | [:131](../backend/src/main/java/service/ProductService.java#L131) |
| | | `listAllForStaff()` | [:159](../backend/src/main/java/service/ProductService.java#L159) |
| `ShippingFeeCalculator` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/service/ShippingFeeCalculator.java:8](../backend/src/main/java/service/ShippingFeeCalculator.java#L8) |
| | | `calculate(subtotal)` | [:12](../backend/src/main/java/service/ShippingFeeCalculator.java#L12) |
| `SizeAdvisorService` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/service/SizeAdvisorService.java:32](../backend/src/main/java/service/SizeAdvisorService.java#L32) |
| | | `advise(customerId, productId, req)` | [:61](../backend/src/main/java/service/SizeAdvisorService.java#L61) |
| `StockService` | 4.7, 4.8 | *(khai báo lớp)* | [backend/src/main/java/service/StockService.java:18](../backend/src/main/java/service/StockService.java#L18) |
| | | `reserve(items)` | [:36](../backend/src/main/java/service/StockService.java#L36) |
| | | `restore(items)` | [:54](../backend/src/main/java/service/StockService.java#L54) |
| | | `updateStock(staffId, variantId, stock)` | [:64](../backend/src/main/java/service/StockService.java#L64) |

**Package `infra`**

| Lớp | Hình | Phương thức / thuộc tính trên sơ đồ | Vị trí trong mã |
|---|---|---|---|
| `AiServiceClient` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/infra/AiServiceClient.java:13](../backend/src/main/java/infra/AiServiceClient.java#L13) |
| | | `predictSize(features, timeoutMs)` | [:22](../backend/src/main/java/infra/AiServiceClient.java#L22) |
| `AiSizeResult` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/infra/AiSizeResult.java:4](../backend/src/main/java/infra/AiSizeResult.java#L4) |
| `JwtTokenProvider` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/infra/JwtTokenProvider.java:14](../backend/src/main/java/infra/JwtTokenProvider.java#L14) |
| | | `createToken(userId, role)` | [:24](../backend/src/main/java/infra/JwtTokenProvider.java#L24) |
| | | `parse(token)` | [:36](../backend/src/main/java/infra/JwtTokenProvider.java#L36) |
| `PaymentGatewayClient` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/infra/PaymentGatewayClient.java:6](../backend/src/main/java/infra/PaymentGatewayClient.java#L6) |
| | | `buildPaymentUrl(order, txnRef)` | [:8](../backend/src/main/java/infra/PaymentGatewayClient.java#L8) |
| | | `verifySignature(params)` | [:10](../backend/src/main/java/infra/PaymentGatewayClient.java#L10) |
| `SizeFeatures` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/infra/SizeFeatures.java:7](../backend/src/main/java/infra/SizeFeatures.java#L7) |
| `TokenClaims` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/infra/TokenClaims.java:4](../backend/src/main/java/infra/TokenClaims.java#L4) |
| `VnPayClient` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/infra/VnPayClient.java:23](../backend/src/main/java/infra/VnPayClient.java#L23) |
| | | `buildPaymentUrl(order, txnRef)` | [:37](../backend/src/main/java/infra/VnPayClient.java#L37) |
| | | `verifySignature(params)` | [:53](../backend/src/main/java/infra/VnPayClient.java#L53) |

**Package `repository`**

| Lớp | Hình | Phương thức / thuộc tính trên sơ đồ | Vị trí trong mã |
|---|---|---|---|
| `AddressRepository` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/repository/AddressRepository.java:9](../backend/src/main/java/repository/AddressRepository.java#L9) |
| | | `findByCustomerId(customerId)` | [:11](../backend/src/main/java/repository/AddressRepository.java#L11) |
| `AiInferenceLogRepository` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/repository/AiInferenceLogRepository.java:8](../backend/src/main/java/repository/AiInferenceLogRepository.java#L8) |
| `BodyProfileRepository` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/repository/BodyProfileRepository.java:9](../backend/src/main/java/repository/BodyProfileRepository.java#L9) |
| | | `findByCustomerId(customerId)` | [:11](../backend/src/main/java/repository/BodyProfileRepository.java#L11) |
| `CartRepository` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/repository/CartRepository.java:9](../backend/src/main/java/repository/CartRepository.java#L9) |
| | | `findByCustomerId(customerId)` | [:11](../backend/src/main/java/repository/CartRepository.java#L11) |
| `CategoryRepository` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/repository/CategoryRepository.java:9](../backend/src/main/java/repository/CategoryRepository.java#L9) |
| | | `findBySlug(slug)` | [:11](../backend/src/main/java/repository/CategoryRepository.java#L11) |
| `CustomerRepository` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/repository/CustomerRepository.java:10](../backend/src/main/java/repository/CustomerRepository.java#L10) |
| | | `findByEmailOrPhone(identifier)` | [:15](../backend/src/main/java/repository/CustomerRepository.java#L15) |
| | | `existsByEmail(email)` | [:17](../backend/src/main/java/repository/CustomerRepository.java#L17) |
| | | `existsByPhone(phone)` | [:19](../backend/src/main/java/repository/CustomerRepository.java#L19) |
| `OrderRepository` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/repository/OrderRepository.java:12](../backend/src/main/java/repository/OrderRepository.java#L12) |
| | | `findByOrderCode(code)` | [:14](../backend/src/main/java/repository/OrderRepository.java#L14) |
| | | `findByCustomerIdOrderByOrderDateDesc(customerId)` | [:16](../backend/src/main/java/repository/OrderRepository.java#L16) |
| | | `findByStatusAndExpiresAtBefore(status, time)` | [:18](../backend/src/main/java/repository/OrderRepository.java#L18) |
| `OrderStatusHistoryRepository` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/repository/OrderStatusHistoryRepository.java:9](../backend/src/main/java/repository/OrderStatusHistoryRepository.java#L9) |
| | | `findByOrderIdOrderByChangedAtAsc(orderId)` | [:11](../backend/src/main/java/repository/OrderStatusHistoryRepository.java#L11) |
| `PaymentRepository` | 4.7 | *(khai báo lớp)* | [backend/src/main/java/repository/PaymentRepository.java:9](../backend/src/main/java/repository/PaymentRepository.java#L9) |
| | | `existsByTxnRef(txnRef)` | [:11](../backend/src/main/java/repository/PaymentRepository.java#L11) |
| | | `findTopByOrderIdOrderByIdDesc(orderId)` | [:13](../backend/src/main/java/repository/PaymentRepository.java#L13) |
| `ProductChangeLogRepository` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/repository/ProductChangeLogRepository.java:9](../backend/src/main/java/repository/ProductChangeLogRepository.java#L9) |
| | | `findByProductIdOrderByChangedAtDesc(productId)` | [:11](../backend/src/main/java/repository/ProductChangeLogRepository.java#L11) |
| `ProductFilter` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/repository/ProductFilter.java:12](../backend/src/main/java/repository/ProductFilter.java#L12) |
| `ProductRepository` | 4.8 | *(khai báo lớp)* | [backend/src/main/java/repository/ProductRepository.java:11](../backend/src/main/java/repository/ProductRepository.java#L11) |
| | | `search(filter)` | [:31](../backend/src/main/java/repository/ProductRepository.java#L31) |
| | | `findByIdAndVisibleTrue(id)` | [:33](../backend/src/main/java/repository/ProductRepository.java#L33) |
| `ProductVariantRepository` | 4.7, 4.8 | *(khai báo lớp)* | [backend/src/main/java/repository/ProductVariantRepository.java:11](../backend/src/main/java/repository/ProductVariantRepository.java#L11) |
| | | `decreaseIfEnough(variantId, qty)` | [:21](../backend/src/main/java/repository/ProductVariantRepository.java#L21) |
| | | `increase(variantId, qty)` | [:27](../backend/src/main/java/repository/ProductVariantRepository.java#L27) |
| | | `findByProductId(productId)` | [:29](../backend/src/main/java/repository/ProductVariantRepository.java#L29) |
| `SizeChartRepository` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/repository/SizeChartRepository.java:9](../backend/src/main/java/repository/SizeChartRepository.java#L9) |
| | | `findByProductIdOrderByHeightMinAsc(productId)` | [:11](../backend/src/main/java/repository/SizeChartRepository.java#L11) |
| `StaffRepository` | 4.9 | *(khai báo lớp)* | [backend/src/main/java/repository/StaffRepository.java:9](../backend/src/main/java/repository/StaffRepository.java#L9) |
| | | `findByUsername(username)` | [:11](../backend/src/main/java/repository/StaffRepository.java#L11) |

**Package `domain`**

| Lớp | Hình | Phương thức / thuộc tính trên sơ đồ | Vị trí trong mã |
|---|---|---|---|
| `Address` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/Address.java:15](../backend/src/main/java/domain/Address.java#L15) |
| | | `setDefault(isDefault)` | [:33](../backend/src/main/java/domain/Address.java#L33) |
| | | `toSnapshot()` | [:37](../backend/src/main/java/domain/Address.java#L37) |
| `AiFeature` | 4.5 | *(khai báo lớp)* | [backend/src/main/java/domain/AiFeature.java:4](../backend/src/main/java/domain/AiFeature.java#L4) |
| | | giá trị: SIZE, IMAGE | [:5](../backend/src/main/java/domain/AiFeature.java#L5) |
| `AiInferenceLog` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/AiInferenceLog.java:20](../backend/src/main/java/domain/AiInferenceLog.java#L20) |
| `BodyProfile` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/BodyProfile.java:21](../backend/src/main/java/domain/BodyProfile.java#L21) |
| | | `update(heightCm, weightKg, fitPref)` | [:42](../backend/src/main/java/domain/BodyProfile.java#L42) |
| `Cart` | 4.5, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/Cart.java:24](../backend/src/main/java/domain/Cart.java#L24) |
| | | `findItem(variantId)` | [:39](../backend/src/main/java/domain/Cart.java#L39) |
| | | `addOrIncrease(variant, qty)` | [:45](../backend/src/main/java/domain/Cart.java#L45) |
| | | `removeItems(variantIds)` | [:62](../backend/src/main/java/domain/Cart.java#L62) |
| | | `subtotal()` | [:66](../backend/src/main/java/domain/Cart.java#L66) |
| `CartItem` | 4.5, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/CartItem.java:22](../backend/src/main/java/domain/CartItem.java#L22) |
| | | `changeQty(qty)` | [:40](../backend/src/main/java/domain/CartItem.java#L40) |
| | | `lineTotal()` | [:48](../backend/src/main/java/domain/CartItem.java#L48) |
| `Category` | 4.5, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/Category.java:12](../backend/src/main/java/domain/Category.java#L12) |
| `ChangeLog` | 4.5, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/ChangeLog.java:17](../backend/src/main/java/domain/ChangeLog.java#L17) |
| | | `of(staff, product, variant, field, oldValue, newValue)` | [:41](../backend/src/main/java/domain/ChangeLog.java#L41) |
| `Customer` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/Customer.java:17](../backend/src/main/java/domain/Customer.java#L17) |
| | | `addAddress(address)` | [:26](../backend/src/main/java/domain/Customer.java#L26) |
| `FitPref` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/FitPref.java:3](../backend/src/main/java/domain/FitPref.java#L3) |
| | | giá trị: OM, VUA, RONG | [:4](../backend/src/main/java/domain/FitPref.java#L4) |
| `FitType` | 4.5, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/FitType.java:3](../backend/src/main/java/domain/FitType.java#L3) |
| | | giá trị: SLIM, REGULAR, OVERSIZE | [:4](../backend/src/main/java/domain/FitType.java#L4) |
| `Order` | 4.5, 4.7 | *(khai báo lớp)* | [backend/src/main/java/domain/Order.java:27](../backend/src/main/java/domain/Order.java#L27) |
| | | `addItem(variant, qty)` | [:71](../backend/src/main/java/domain/Order.java#L71) |
| | | `recalculate()` | [:84](../backend/src/main/java/domain/Order.java#L84) |
| | | `getVariantIds()` | [:93](../backend/src/main/java/domain/Order.java#L93) |
| `OrderItem` | 4.5, 4.7 | *(khai báo lớp)* | [backend/src/main/java/domain/OrderItem.java:21](../backend/src/main/java/domain/OrderItem.java#L21) |
| | | `lineTotal()` | [:42](../backend/src/main/java/domain/OrderItem.java#L42) |
| `OrderStatus` | 4.5, 4.7 | *(khai báo lớp)* | [backend/src/main/java/domain/OrderStatus.java:3](../backend/src/main/java/domain/OrderStatus.java#L3) |
| | | giá trị: CHO_THANH_TOAN, CHO_XAC_NHAN, DANG_GIAO, DA_GIAO, DA_HUY | [:4](../backend/src/main/java/domain/OrderStatus.java#L4) |
| `OrderStatusHistory` | 4.5, 4.7 | *(khai báo lớp)* | [backend/src/main/java/domain/OrderStatusHistory.java:19](../backend/src/main/java/domain/OrderStatusHistory.java#L19) |
| `Payment` | 4.5, 4.7 | *(khai báo lớp)* | [backend/src/main/java/domain/Payment.java:20](../backend/src/main/java/domain/Payment.java#L20) |
| | | `markResult(success, txnRef)` | [:43](../backend/src/main/java/domain/Payment.java#L43) |
| | | `markMismatch(txnRef)` | [:51](../backend/src/main/java/domain/Payment.java#L51) |
| `PaymentMethod` | 4.5 | *(khai báo lớp)* | [backend/src/main/java/domain/PaymentMethod.java:3](../backend/src/main/java/domain/PaymentMethod.java#L3) |
| | | giá trị: COD, ONLINE | [:4](../backend/src/main/java/domain/PaymentMethod.java#L4) |
| `PaymentStatus` | 4.5 | *(khai báo lớp)* | [backend/src/main/java/domain/PaymentStatus.java:3](../backend/src/main/java/domain/PaymentStatus.java#L3) |
| | | giá trị: DANG_XU_LY, THANH_CONG, THAT_BAI, SAI_LECH | [:4](../backend/src/main/java/domain/PaymentStatus.java#L4) |
| `Product` | 4.5, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/Product.java:23](../backend/src/main/java/domain/Product.java#L23) |
| | | `updateInfo(price, description, material)` | [:52](../backend/src/main/java/domain/Product.java#L52) |
| | | `setVisible(visible)` | [:68](../backend/src/main/java/domain/Product.java#L68) |
| | | `addVariant(size, color, sku)` | [:72](../backend/src/main/java/domain/Product.java#L72) |
| `ProductImage` | 4.5, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/ProductImage.java:15](../backend/src/main/java/domain/ProductImage.java#L15) |
| `ProductVariant` | 4.5, 4.7, 4.8 | *(khai báo lớp)* | [backend/src/main/java/domain/ProductVariant.java:16](../backend/src/main/java/domain/ProductVariant.java#L16) |
| | | `setStock(qty)` | [:38](../backend/src/main/java/domain/ProductVariant.java#L38) |
| | | `isAvailable(qty)` | [:46](../backend/src/main/java/domain/ProductVariant.java#L46) |
| `ReturnReason` | 4.5 | *(khai báo lớp)* | [backend/src/main/java/domain/ReturnReason.java:3](../backend/src/main/java/domain/ReturnReason.java#L3) |
| | | giá trị: SIZE_LON, SIZE_NHO, KHAC | [:4](../backend/src/main/java/domain/ReturnReason.java#L4) |
| `ReturnRecord` | 4.5 | *(khai báo lớp)* | [backend/src/main/java/domain/ReturnRecord.java:20](../backend/src/main/java/domain/ReturnRecord.java#L20) |
| `SizeChart` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/SizeChart.java:15](../backend/src/main/java/domain/SizeChart.java#L15) |
| | | `contains(heightCm, weightKg)` | [:35](../backend/src/main/java/domain/SizeChart.java#L35) |
| `Staff` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/Staff.java:13](../backend/src/main/java/domain/Staff.java#L13) |
| | | `isActive()` | [:27](../backend/src/main/java/domain/Staff.java#L27) |
| `StaffRole` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/StaffRole.java:3](../backend/src/main/java/domain/StaffRole.java#L3) |
| | | giá trị: STAFF, OWNER | [:4](../backend/src/main/java/domain/StaffRole.java#L4) |
| `User` | 4.5, 4.9 | *(khai báo lớp)* | [backend/src/main/java/domain/User.java:16](../backend/src/main/java/domain/User.java#L16) |
| | | `recordFailedLogin(now)` | [:33](../backend/src/main/java/domain/User.java#L33) |
| | | `resetFailedCount()` | [:42](../backend/src/main/java/domain/User.java#L42) |
| | | `isLocked(now)` | [:47](../backend/src/main/java/domain/User.java#L47) |

### 1.2. Bảng / cột CSDL (V2) ↔ entity / thuộc tính

Sinh tự động từ `db/V2__vp_generated.sql` và annotation JPA trong `backend/src/main/java/domain`. Ứng dụng khởi động với `ddl-auto=validate` nên mọi ánh xạ dưới đây đã được Hibernate kiểm tra trên `avq_db_vp`.

Kết quả: 18/18 bảng có entity; số cột của các bảng đó chưa ánh xạ: **0**.


**`addresses`** ↔ `domain.Address`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `address_id` | bigint(20) | `id` | [Address.java:19](../backend/src/main/java/domain/Address.java#L19) |
| `customer_id` | bigint(20) | `customer` | [Address.java:31](../backend/src/main/java/domain/Address.java#L31) |
| `receiver_name` | varchar(100) | `receiverName` | [Address.java:23](../backend/src/main/java/domain/Address.java#L23) |
| `receiver_phone` | varchar(15) | `receiverPhone` | [Address.java:25](../backend/src/main/java/domain/Address.java#L25) |
| `address_line` | varchar(255) | `addressLine` | [Address.java:27](../backend/src/main/java/domain/Address.java#L27) |
| `is_default` | tinyint(1) | `isDefault` | [Address.java:21](../backend/src/main/java/domain/Address.java#L21) |

**`customers`** ↔ `domain.Customer`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `customer_id` | bigint(20) | `email` | [Customer.java:19](../backend/src/main/java/domain/Customer.java#L19) |
| `full_name` | varchar(100) | `fullName` | [User.java:25](../backend/src/main/java/domain/User.java#L25) |
| `password_hash` | varchar(255) | `passwordHash` | [User.java:27](../backend/src/main/java/domain/User.java#L27) |
| `failed_login_count` | tinyint(3) | `failedLoginCount` | [User.java:29](../backend/src/main/java/domain/User.java#L29) |
| `locked_until` | datetime | `lockedUntil` | [User.java:31](../backend/src/main/java/domain/User.java#L31) |
| `email` | varchar(120) | `email` | [Customer.java:19](../backend/src/main/java/domain/Customer.java#L19) |
| `phone` | varchar(15) | `phone` | [Customer.java:21](../backend/src/main/java/domain/Customer.java#L21) |

**`carts`** ↔ `domain.Cart`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `cart_id` | bigint(20) | `id` | [Cart.java:28](../backend/src/main/java/domain/Cart.java#L28) |
| `customer_id` | bigint(20) | `customer` | [Cart.java:34](../backend/src/main/java/domain/Cart.java#L34) |
| `updated_at` | datetime | `updatedAt` | [Cart.java:30](../backend/src/main/java/domain/Cart.java#L30) |

**`orders`** ↔ `domain.Order`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `order_id` | bigint(20) | `id` | [Order.java:31](../backend/src/main/java/domain/Order.java#L31) |
| `customer_id` | bigint(20) | `customer` | [Order.java:66](../backend/src/main/java/domain/Order.java#L66) |
| `order_code` | varchar(20) | `orderCode` | [Order.java:33](../backend/src/main/java/domain/Order.java#L33) |
| `order_date` | datetime | `orderDate` | [Order.java:35](../backend/src/main/java/domain/Order.java#L35) |
| `receiver_name` | varchar(100) | `receiverName` | [Order.java:37](../backend/src/main/java/domain/Order.java#L37) |
| `receiver_phone` | varchar(15) | `receiverPhone` | [Order.java:39](../backend/src/main/java/domain/Order.java#L39) |
| `shipping_address` | varchar(255) | `shippingAddress` | [Order.java:41](../backend/src/main/java/domain/Order.java#L41) |
| `subtotal` | decimal(12, 0) | `subtotal` | [Order.java:43](../backend/src/main/java/domain/Order.java#L43) |
| `shipping_fee` | decimal(12, 0) | `shippingFee` | [Order.java:45](../backend/src/main/java/domain/Order.java#L45) |
| `total` | decimal(12, 0) | `total` | [Order.java:47](../backend/src/main/java/domain/Order.java#L47) |
| `payment_method` | ENUM('COD','ONLINE') | `paymentMethod` | [Order.java:50](../backend/src/main/java/domain/Order.java#L50) |
| `status` | ENUM('CHO_THANH_TOAN','CHO_XAC_NHAN','DANG_GIAO','DA_GIAO','DA_HUY') | `status` | [Order.java:54](../backend/src/main/java/domain/Order.java#L54) |
| `expires_at` | datetime | `expiresAt` | [Order.java:56](../backend/src/main/java/domain/Order.java#L56) |
| `version` | bigint(20) | `version` | [Order.java:60](../backend/src/main/java/domain/Order.java#L60) |
| `tracking_code` | varchar(40) | `trackingCode` | [Order.java:62](../backend/src/main/java/domain/Order.java#L62) |

**`payments`** ↔ `domain.Payment`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `payment_id` | bigint(20) | `id` | [Payment.java:24](../backend/src/main/java/domain/Payment.java#L24) |
| `order_id` | bigint(20) | `order` | [Payment.java:41](../backend/src/main/java/domain/Payment.java#L41) |
| `method` | varchar(10) | `method` | [Payment.java:26](../backend/src/main/java/domain/Payment.java#L26) |
| `amount` | decimal(12, 0) | `amount` | [Payment.java:28](../backend/src/main/java/domain/Payment.java#L28) |
| `status` | ENUM('DANG_XU_LY','THANH_CONG','THAT_BAI','SAI_LECH') | `status` | [Payment.java:32](../backend/src/main/java/domain/Payment.java#L32) |
| `txn_ref` | varchar(64) | `txnRef` | [Payment.java:35](../backend/src/main/java/domain/Payment.java#L35) |
| `paid_at` | datetime | `paidAt` | [Payment.java:37](../backend/src/main/java/domain/Payment.java#L37) |

**`order_status_histories`** ↔ `domain.OrderStatusHistory`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `history_id` | bigint(20) | `id` | [OrderStatusHistory.java:23](../backend/src/main/java/domain/OrderStatusHistory.java#L23) |
| `staff_id` | bigint(20) | `staff` | [OrderStatusHistory.java:43](../backend/src/main/java/domain/OrderStatusHistory.java#L43) |
| `order_id` | bigint(20) | `order` | [OrderStatusHistory.java:39](../backend/src/main/java/domain/OrderStatusHistory.java#L39) |
| `from_status` | varchar(20) | `fromStatus` | [OrderStatusHistory.java:28](../backend/src/main/java/domain/OrderStatusHistory.java#L28) |
| `to_status` | varchar(20) | `toStatus` | [OrderStatusHistory.java:31](../backend/src/main/java/domain/OrderStatusHistory.java#L31) |
| `note` | varchar(255) | `note` | [OrderStatusHistory.java:33](../backend/src/main/java/domain/OrderStatusHistory.java#L33) |
| `changed_at` | datetime | `changedAt` | [OrderStatusHistory.java:35](../backend/src/main/java/domain/OrderStatusHistory.java#L35) |

**`staff`** ↔ `domain.Staff`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `staff_id` | bigint(20) | `username` | [Staff.java:15](../backend/src/main/java/domain/Staff.java#L15) |
| `full_name` | varchar(100) | `fullName` | [User.java:25](../backend/src/main/java/domain/User.java#L25) |
| `password_hash` | varchar(255) | `passwordHash` | [User.java:27](../backend/src/main/java/domain/User.java#L27) |
| `failed_login_count` | tinyint(3) | `failedLoginCount` | [User.java:29](../backend/src/main/java/domain/User.java#L29) |
| `locked_until` | datetime | `lockedUntil` | [User.java:31](../backend/src/main/java/domain/User.java#L31) |
| `username` | varchar(50) | `username` | [Staff.java:15](../backend/src/main/java/domain/Staff.java#L15) |
| `role` | varchar(20) | `role` | [Staff.java:19](../backend/src/main/java/domain/Staff.java#L19) |

**`order_items`** ↔ `domain.OrderItem`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `product_name` | varchar(150) | `productName` | [OrderItem.java:23](../backend/src/main/java/domain/OrderItem.java#L23) |
| `variant_id` | bigint(20) | `variant` | [OrderItem.java:40](../backend/src/main/java/domain/OrderItem.java#L40) |
| `order_id` | bigint(20) | `order` | [OrderItem.java:36](../backend/src/main/java/domain/OrderItem.java#L36) |
| `size` | varchar(5) | `size` | [OrderItem.java:25](../backend/src/main/java/domain/OrderItem.java#L25) |
| `color` | varchar(30) | `color` | [OrderItem.java:27](../backend/src/main/java/domain/OrderItem.java#L27) |
| `unit_price` | decimal(12, 0) | `unitPrice` | [OrderItem.java:29](../backend/src/main/java/domain/OrderItem.java#L29) |
| `quantity` | tinyint(3) | `quantity` | [OrderItem.java:31](../backend/src/main/java/domain/OrderItem.java#L31) |

**`product_variants`** ↔ `domain.ProductVariant`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `variant_id` | bigint(20) | `id` | [ProductVariant.java:20](../backend/src/main/java/domain/ProductVariant.java#L20) |
| `product_id` | bigint(20) | `product` | [ProductVariant.java:36](../backend/src/main/java/domain/ProductVariant.java#L36) |
| `sku` | varchar(40) | `sku` | [ProductVariant.java:22](../backend/src/main/java/domain/ProductVariant.java#L22) |
| `size` | varchar(5) | `size` | [ProductVariant.java:24](../backend/src/main/java/domain/ProductVariant.java#L24) |
| `color` | varchar(30) | `color` | [ProductVariant.java:26](../backend/src/main/java/domain/ProductVariant.java#L26) |
| `stock_qty` | int(10) | `stockQty` | [ProductVariant.java:28](../backend/src/main/java/domain/ProductVariant.java#L28) |
| `version` | bigint(20) | `version` | [ProductVariant.java:32](../backend/src/main/java/domain/ProductVariant.java#L32) |

**`cart_items`** ↔ `domain.CartItem`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `quantity` | tinyint(3) | `quantity` | [CartItem.java:27](../backend/src/main/java/domain/CartItem.java#L27) |
| `variant_id` | bigint(20) | `variant` | [CartItem.java:34](../backend/src/main/java/domain/CartItem.java#L34) |
| `cart_id` | bigint(20) | `cart` | [CartItem.java:38](../backend/src/main/java/domain/CartItem.java#L38) |
| `added_at` | datetime | `addedAt` | [CartItem.java:29](../backend/src/main/java/domain/CartItem.java#L29) |

**`products`** ↔ `domain.Product`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `product_id` | bigint(20) | `id` | [Product.java:27](../backend/src/main/java/domain/Product.java#L27) |
| `category_id` | bigint(20) | `category` | [Product.java:47](../backend/src/main/java/domain/Product.java#L47) |
| `name` | varchar(150) | `name` | [Product.java:29](../backend/src/main/java/domain/Product.java#L29) |
| `description` | text | `description` | [Product.java:31](../backend/src/main/java/domain/Product.java#L31) |
| `material` | varchar(80) | `material` | [Product.java:33](../backend/src/main/java/domain/Product.java#L33) |
| `price` | decimal(12, 0) | `price` | [Product.java:35](../backend/src/main/java/domain/Product.java#L35) |
| `is_visible` | tinyint(1) | `visible` | [Product.java:37](../backend/src/main/java/domain/Product.java#L37) |
| `fit_type` | ENUM('SLIM','REGULAR','OVERSIZE') | `fitType` | [Product.java:40](../backend/src/main/java/domain/Product.java#L40) |

**`categories`** ↔ `domain.Category`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `category_id` | bigint(20) | `id` | [Category.java:16](../backend/src/main/java/domain/Category.java#L16) |
| `name` | varchar(80) | `name` | [Category.java:18](../backend/src/main/java/domain/Category.java#L18) |
| `slug` | varchar(80) | `slug` | [Category.java:23](../backend/src/main/java/domain/Category.java#L23) |

**`product_images`** ↔ `domain.ProductImage`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `image_id` | bigint(20) | `id` | [ProductImage.java:19](../backend/src/main/java/domain/ProductImage.java#L19) |
| `product_id` | bigint(20) | `product` | [ProductImage.java:27](../backend/src/main/java/domain/ProductImage.java#L27) |
| `url` | varchar(255) | `url` | [ProductImage.java:23](../backend/src/main/java/domain/ProductImage.java#L23) |
| `is_main` | tinyint(1) | `main` | [ProductImage.java:21](../backend/src/main/java/domain/ProductImage.java#L21) |

**`product_change_logs`** ↔ `domain.ChangeLog`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `log_id` | bigint(20) | `id` | [ChangeLog.java:29](../backend/src/main/java/domain/ChangeLog.java#L29) |
| `product_id` | bigint(20) | `product` | [ChangeLog.java:33](../backend/src/main/java/domain/ChangeLog.java#L33) |
| `staff_id` | bigint(20) | `staff` | [ChangeLog.java:36](../backend/src/main/java/domain/ChangeLog.java#L36) |
| `variant_id` | bigint(20) | `productVariant` | [ChangeLog.java:39](../backend/src/main/java/domain/ChangeLog.java#L39) |
| `field_name` | varchar(30) | `fieldName` | [ChangeLog.java:19](../backend/src/main/java/domain/ChangeLog.java#L19) |
| `old_value` | varchar(255) | `oldValue` | [ChangeLog.java:21](../backend/src/main/java/domain/ChangeLog.java#L21) |
| `new_value` | varchar(255) | `newValue` | [ChangeLog.java:23](../backend/src/main/java/domain/ChangeLog.java#L23) |
| `changed_at` | datetime | `changedAt` | [ChangeLog.java:25](../backend/src/main/java/domain/ChangeLog.java#L25) |

**`body_profiles`** ↔ `domain.BodyProfile`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `height_cm` | smallint(6) | `heightCm` | [BodyProfile.java:28](../backend/src/main/java/domain/BodyProfile.java#L28) |
| `customer_id` | bigint(20) | `customer` | [BodyProfile.java:40](../backend/src/main/java/domain/BodyProfile.java#L40) |
| `weight_kg` | smallint(6) | `weightKg` | [BodyProfile.java:30](../backend/src/main/java/domain/BodyProfile.java#L30) |
| `fit_pref` | ENUM('OM','VUA','RONG') | `fitPref` | [BodyProfile.java:33](../backend/src/main/java/domain/BodyProfile.java#L33) |
| `consent_at` | datetime | `consentAt` | [BodyProfile.java:35](../backend/src/main/java/domain/BodyProfile.java#L35) |

**`size_charts`** ↔ `domain.SizeChart`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `chart_id` | bigint(20) | `id` | [SizeChart.java:19](../backend/src/main/java/domain/SizeChart.java#L19) |
| `product_id` | bigint(20) | `product` | [SizeChart.java:33](../backend/src/main/java/domain/SizeChart.java#L33) |
| `size` | varchar(5) | `size` | [SizeChart.java:23](../backend/src/main/java/domain/SizeChart.java#L23) |
| `height_min` | smallint(6) | `heightMin` | [SizeChart.java:21](../backend/src/main/java/domain/SizeChart.java#L21) |
| `height_max` | smallint(6) | `heightMax` | [SizeChart.java:25](../backend/src/main/java/domain/SizeChart.java#L25) |
| `weight_min` | smallint(6) | `weightMin` | [SizeChart.java:27](../backend/src/main/java/domain/SizeChart.java#L27) |
| `weight_max` | smallint(6) | `weightMax` | [SizeChart.java:29](../backend/src/main/java/domain/SizeChart.java#L29) |

**`return_records`** ↔ `domain.ReturnRecord`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `return_id` | bigint(20) | `id` | [ReturnRecord.java:24](../backend/src/main/java/domain/ReturnRecord.java#L24) |
| `created_by` | bigint(20) | `createdBy` | [ReturnRecord.java:40](../backend/src/main/java/domain/ReturnRecord.java#L40) |
| `reason` | ENUM('SIZE_LON','SIZE_NHO','KHAC') | `reason` | [ReturnRecord.java:27](../backend/src/main/java/domain/ReturnRecord.java#L27) |
| `created_at` | datetime | `createdAt` | [ReturnRecord.java:29](../backend/src/main/java/domain/ReturnRecord.java#L29) |
| `variant_id` | bigint(20) | `orderItem` | [ReturnRecord.java:37](../backend/src/main/java/domain/ReturnRecord.java#L37) |
| `order_id` | bigint(20) | `orderItem` | [ReturnRecord.java:37](../backend/src/main/java/domain/ReturnRecord.java#L37) |

**`ai_inference_logs`** ↔ `domain.AiInferenceLog`

| Cột | Kiểu DDL | Thuộc tính | Vị trí |
|---|---|---|---|
| `log_id` | bigint(20) | `id` | [AiInferenceLog.java:24](../backend/src/main/java/domain/AiInferenceLog.java#L24) |
| `feature` | ENUM('SIZE','IMAGE') | `feature` | [AiInferenceLog.java:27](../backend/src/main/java/domain/AiInferenceLog.java#L27) |
| `model_version` | varchar(40) | `modelVersion` | [AiInferenceLog.java:29](../backend/src/main/java/domain/AiInferenceLog.java#L29) |
| `input_hash` | char(64) | `inputHash` | [AiInferenceLog.java:31](../backend/src/main/java/domain/AiInferenceLog.java#L31) |
| `output_json` | text | `outputJson` | [AiInferenceLog.java:33](../backend/src/main/java/domain/AiInferenceLog.java#L33) |
| `latency_ms` | int(11) | `latencyMs` | [AiInferenceLog.java:35](../backend/src/main/java/domain/AiInferenceLog.java#L35) |
| `fallback_used` | tinyint(1) | `fallbackUsed` | [AiInferenceLog.java:37](../backend/src/main/java/domain/AiInferenceLog.java#L37) |
| `created_at` | datetime | `createdAt` | [AiInferenceLog.java:39](../backend/src/main/java/domain/AiInferenceLog.java#L39) |

---

## 2. Danh sách mọi chỗ lệch thiết kế

### 2.1. Chỉnh kỹ thuật được phép (CLAUDE.md 3.4)

| Mã | Thay đổi | Phạm vi | Lý do |
|---|---|---|---|
| K1 | Trường quan hệ `public` → `private` + getter/setter | 25 trường trong 15 entity: 20 quan hệ đơn (`Order.customer`, `OrderItem.order`, `Product.category`…) + 5 collection ở K2 | Đóng gói; JPA ánh xạ theo field, tên trường giữ nguyên |
| K2 | `ArrayList<X>` → `List<X>` (vẫn khởi tạo bằng `new ArrayList<X>()`) | `Cart.items`, `Customer.addresses`, `Order.items`, `Product.variants`, `Product.image` | JPA cần kiểu interface cho collection |
| K3 | `version : int` → `long` | `Order.version`, `ProductVariant.version` | Cột `version` là `BIGINT` (DB-R1, DB-R2); `int` làm `validate` lỗi |
| K4 | Repository `extends JpaRepository<Entity, Long>`; giữ nguyên mọi phương thức đã khai báo | 13 repository | Spring Data sinh truy vấn. Thêm `@Query` cho `findByEmailOrPhone` (một tham số so cả email và phone), `search(ProductFilter)`, `decreaseIfEnough`/`increase` (UPDATE có điều kiện, BR11) |

### 2.2. Bổ sung được phép (CLAUDE.md 3.3)

| Mã | Bổ sung | Ghi chú |
|---|---|---|
| B1 | `import`, annotation JPA/Spring/Validation | Không đổi tên hay chữ ký nào |
| B2 | Trường `private final` + constructor cho phụ thuộc (mũi tên Dependency) | Mọi service/controller |
| B3 | Getter/setter, constructor | Ví dụ constructor `AiInferenceLog(feature, modelVersion, ...)` |
| B4 | Phương thức và hằng số `private` hỗ trợ | Ví dụ `OrderService.nonEmptyCart`, `User.MAX_FAILED_LOGIN` |
| B5 | 22 DTO (record) trong `api.dto` | TokenDto, PlaceOrderRequest, OrderDetailDto, CartDto, StaffDto, CreateStaffRequest, ResetPasswordRequest… |
| B6 | Lớp cấu hình trong `app`: `AvqApplication`, `SecurityConfig`, `CoreConfig`, `GlobalExceptionHandler`, `security.AuthUser`, `security.JwtAuthFilter`, `WebConfig` (phục vụ `/uploads/**`) | |
| B7 | Các lớp thêm vào VP ngày 16/11 (`AiFeature`, `AiInferenceLog`, `AiInferenceLogRepository`) được **viết tay theo đúng sơ đồ**, không chạy lại Instant Generator, để không ghi đè mã đã viết. `TokenClaims`, `SizeFeatures`, `AiSizeResult`, `ProductFilter` đã có sẵn trong mã từ trước | Tên lớp, package, thuộc tính khớp `vp/model.xml` (mục 1.1) |

### 2.3. Lệch so với thiết kế

Ngày 16/11 sinh viên đã cập nhật VP theo [`vp-changes.md`](vp-changes.md). Các dòng **Đã cập nhật VP** không còn lệch. L6, L7 được **chốt** thành quyết định thiết kế trong [`design-notes.md`](design-notes.md) mục 9, nên mã, tài liệu và sơ đồ thống nhất. Các dòng còn lại (L9, L11, L13–L15, L17) là chi tiết cài đặt hoặc phần chỉ có ở bản demo, không cần đưa vào VP.

| Mã | Chỗ lệch | Xử lý trong mã | Đề xuất cho VP |
|---|---|---|---|
| L1 | Bảng `categories` có cột `slug` (NOT NULL, UNIQUE), `CategoryRepository.findBySlug` dùng nó, nhưng lớp `Category` **không có** thuộc tính `slug` | Bổ sung trường `Category.slug` | **Đã cập nhật VP (16/11).** `Category` có `slug : String` (Hình 4.5) |
| L2 | `Staff.active` có trong thiết kế nhưng bảng `staff` **không có cột** `active` | VP đã bỏ thuộc tính `active`, giữ phương thức `isActive() : boolean`. Mã: không còn trường `active`; `isActive()` trả `true` (mọi nhân viên trong bảng đều đang hoạt động) | **Đã cập nhật VP (16/11).** Bỏ thuộc tính khỏi lớp `Staff` |
| L3 | `PaymentService.handleCallback(params)` và `PaymentCallbackController.vnpayIpn(params)` **không khai báo kiểu trả về** trong model nên VP sinh `void`; design-notes ghi `: String` | Giữ `void`. Trả về bình thường = mã `00`; các mã `97/01/02/04` ném `PaymentCallbackException`, với `noRollbackFor` để vẫn lưu trạng thái "Sai lệch". `vnpayIpn` trả HTTP 200 và ghi nhật ký mã | **Đã cập nhật VP (16/11).** Cả hai khai báo `: void`; `design-notes.md` sửa theo |
| L4 | Hình 4.7 **thiếu phương thức service** cho `OrderController.list/detail` và `AdminOrderController.list/detail` | Thêm 4 phương thức public vào `OrderService`: `listByCustomer`, `getDetail`, `listForStaff`, `getDetailForStaff` | **Đã cập nhật VP (16/11).** 4 phương thức có trên Hình 4.7 |
| L5 | Hình 4.8 **thiếu phương thức service** cho `AdminProductController.listAll` | Thêm `ProductService.listAllForStaff()` | **Đã cập nhật VP (16/11).** Có trên Hình 4.8 |
| L6 | `updateStatus(staffId, code, to, note)` không có tham số mã vận đơn, nhưng DB-R3 yêu cầu ghi `tracking_code` khi chuyển sang Đang giao | `OrderStateMachine.transit` tự sinh mã `VDyyMMddxxxxxx` khi chuyển sang `DANG_GIAO` | **Đã chốt (16/11):** design-notes mục 9, QĐ1. Chữ ký trên sơ đồ giữ nguyên; hệ thống tự sinh mã. `business-rules.md` DT-04 ghi theo |
| L7 | DT-04 có "khách đổi sang COD" (Chờ thanh toán → Chờ xác nhận), nhưng thiết kế **không có phương thức** tương ứng | Không hiện thực (theo docs/business-rules.md). Khách chỉ có thể hủy rồi đặt lại | **Đã chốt (16/11):** design-notes mục 9, QĐ2. Ngoài phạm vi demo; bảng DT-04 trong `business-rules.md` đã bỏ ô "khách đổi sang COD" |
| L8 | `ChangeLog.of(staff, variant, field, old, new)` không nhận `product`, trong khi `product_id` NOT NULL. Thay đổi cấp sản phẩm (giá, mô tả) có `variant = null` | Mã đã theo chữ ký mới `of(staff, product, variant, field, oldValue, newValue)`; kiểm thử ở `ChangeLogServiceTest` | **Đã cập nhật VP (16/11).** Thêm tham số `product` |
| L9 | `body_profiles` khóa chính = khóa ngoại `customer_id` (R8), lớp `BodyProfile` không có thuộc tính khóa | Thêm `BodyProfile.customerId` + `@MapsId` (CLAUDE.md mục 4) | Không bắt buộc (chi tiết ORM) |
| L10 | Bảng `ai_inference_logs` (DB-R7) **chưa có lớp** tương ứng | Mã: entity `domain.AiInferenceLog` + enum `AiFeature` + `repository.AiInferenceLogRepository`; `SizeAdvisorService` lưu qua repository (`feature = SIZE`, `fallback_used = 1`, `input_hash` = SHA-256 số đo), thay cho JdbcTemplate | **Đã cập nhật VP (16/11).** Lớp, enum, repository và Dependency `SizeAdvisorService → AiInferenceLogRepository` (Hình 4.5, 4.9) |
| L11 | Khóa ghép `order_items`, `cart_items` | Thêm 2 lớp khóa `OrderItemId`, `CartItemId` (`@IdClass`, khóa lấy từ hai quan hệ). Không thêm thuộc tính vào entity | Không bắt buộc |
| L12 | DTO/kiểu chưa có trong VP mà tầng `infra` và `repository` dùng: `TokenClaims`, `SizeFeatures`, `AiSizeResult`, `ProductFilter` | Đặt trong `infra` và `repository` (không đặt ở `api.dto`) để không tạo phụ thuộc ngược lên `api` | **Đã cập nhật VP (16/11).** Đã thêm 4 lớp `TokenClaims`, `SizeFeatures`, `AiSizeResult`, `ProductFilter` (Hình 4.8, 4.9) |
| L13 | `PaymentCallbackController.mockPay` dùng `infra.MockPaymentGatewayClient` (api → infra) | Cổng giả lập đóng vai hệ thống thanh toán bên ngoài gửi IPN đã ký; kết quả vẫn đi qua `PaymentService.handleCallback`. Chỉ có ở bản demo. Để giao dịch không mất khi khởi động lại máy chủ, trang `/mock-pay` gửi kèm các tham số `vnp_*` của link đã ký; controller đọc qua `HttpServletRequest` (tiêm qua constructor, **giữ nguyên chữ ký** `mockPay(code, success)`), cổng kiểm chữ ký rồi mới nhận lại (`restoreIssued`) | Không cần: lớp giả lập ngoài phạm vi thiết kế |
| L14 | `api` và `service` dùng kiểu tham số `repository.ProductFilter`; `service` trả DTO thuộc `api.dto` | Chỉ là kiểu dữ liệu, controller không gọi repository. Phương án DTO trong `api.dto` theo CLAUDE.md 3.3 | Có thể tách package `dto` dùng chung |
| L15 | Lớp hỗ trợ không có trong VP: `MockPaymentGatewayClient` (CLAUDE.md yêu cầu), `service.BusinessException`, `service.PaymentCallbackException`, `service.DtoMapper` | Lỗi nghiệp vụ có mã + thông báo tiếng Việt; chuyển entity sang DTO dùng chung | Không bắt buộc |
| L16 | Sequence 2.10 (Chương 2) vẽ "tạo đơn rồi trừ kho" | Mã làm ngược lại: **trừ kho trước** (`StockService.reserve` trước khi lưu đơn), theo design-notes và CLAUDE.md, để tránh deadlock | **Đã cập nhật VP (16/11).** 2.10: `10: decreaseStock(lines)` → `11: ok` → `12: create(lines, fee, status)` |
| L17 | Cổng VNPay thật | `VnPayClient` đã hiện thực ký/kiểm chữ ký HMAC-SHA512 nhưng chỉ bật khi `payment.provider=vnpay`; mặc định dùng cổng giả lập (CLAUDE.md mục 7) | – |
| L18 | Tải ảnh sản phẩm từ máy (bổ sung 17/11, design-notes QĐ4) chưa có trên sơ đồ | Lớp `infra.ImageStorage.save(file)`, `ProductService.uploadImage(file)`, `AdminProductController.uploadImage(file)` | **Chờ cập nhật VP (đợt 2):** thêm lớp, 3 phương thức và Dependency `ProductService → ImageStorage` (Hình 4.8); use case "upload product image" |
| L19 | Chức năng riêng của chủ cửa hàng (bổ sung 17/11, design-notes QĐ3, BR02) chưa có trên sơ đồ | Lớp `api.AdminStaffController` (`list`, `create`, `resetPassword`), `service.StaffService` (`listStaff`, `createStaff`, `resetPassword`); SecurityConfig chặn `/api/admin/staff/**` cho vai trò khác OWNER. Trang Tổng quan chỉ ở giao diện, dùng `listForStaff`, `listAllForStaff` | **Chờ cập nhật VP (đợt 2):** thêm 2 lớp, 6 phương thức, Dependency (Hình 4.9); actor "Chủ của hàng" kế thừa "Nhân viên", use case "manage staff", "view sales overview" (Hình 1.5) |

Các điểm trong `vp/` đã được sửa trước khi hiện thực: `PaymentStatus.SAIL_LECH` → `SAI_LECH` (sinh lại từ VP); stereotype `RestController` có ký tự TAB thừa; tên role tiếng Việt có dấu (gây tên trường lỗi `g?m`, `c\xF3_bi?n_th?`); tên model chứa dấu chấm làm Instant Generator lưu sai thư mục (đã sắp lại file theo package, SHA-256 khớp 63/63).

---

## 3. Kết quả kiểm thử

Chạy `.\mvnw.cmd test` trong `backend/`. Kiểm thử tích hợp chạy trên CSDL thật `avq_db_vp` (`ddl-auto=validate`); mỗi test chạy trong một giao dịch rồi rollback. Riêng test đồng thời cần commit thật nên tự dọn dữ liệu sau khi chạy.

| Lớp kiểm thử | Số test | Kết quả | Nội dung |
|---|---|---|---|
| `service.RuleUnitTest` | 22 | Đạt | **BR09/DT-01** (0; 249.000; 350.000; 499.999 → 30.000 đ; 500.000; 520.000; 708.000 → 0 đ); **DT-04** bảng chuyển hợp lệ + 8 chuyển không hợp lệ bị từ chối, sinh mã vận đơn; **BR16** sai 5 lần khóa 15 phút, đúng thì reset; **FB-01** khớp bảng, ngoài bảng theo chiều cao gần nhất, Oversize + Vừa lùi một size, không có bảng size |
| `service.CartAndAuthServiceTest` | 7 | Đạt | **DT-02** R2 chưa chọn biến thể, R3 vượt tồn / quá 10, R4 thêm dòng, R5 cộng dồn; đổi số lượng, xóa dòng; **BR01** đăng ký email/SĐT không trùng; **BR16** qua `AuthService` (khóa sau 5 lần sai, mật khẩu đúng vẫn bị chặn; đăng nhập đúng reset bộ đếm) |
| `service.OrderAndPaymentServiceTest` | 12 | Đạt | **BR10** COD → Chờ xác nhận / ONLINE → Chờ thanh toán + hạn 15 phút + Payment Đang xử lý; **BR08** đổi giá sau khi đặt, đơn giữ giá cũ; **BR11** trừ kho, hoàn kho; **EX-06** hết hàng khi đặt; **BR13/DT-03** khách hủy, không hủy được khi đang giao, nhân viên hủy phải có lý do; **DT-04** lịch sử + mã vận đơn; **BR12** đơn quá hạn tự hủy và hoàn kho; **DT-05** thành công `00` → Chờ xác nhận, gửi lại `02`, thất bại `00` giữ nguyên, sai chữ ký `97`, không có đơn `01`, lệch tiền `04` → Sai lệch |
| `service.LastItemConcurrencyTest` | 1 | Đạt | **BR11**: 10 khách cùng lúc đặt chiếc cuối cùng → đúng 1 đơn thành công, 9 lỗi `OUT_OF_STOCK`, tồn kho về 0 |
| `service.SizeAdvisorServiceTest` | 2 | Đạt | **FB-01** qua service: dùng bảng size, ghi `ai_inference_logs` (`fallback_used = 1`, `input_hash` 64 ký tự), lưu số đo khi khách đồng ý; sản phẩm không có bảng size |
| `repository.RepositoryQueryTest` | 6 | Đạt | `findByEmailOrPhone`, `decreaseIfEnough`/`increase` (+ version), `search` (từ khóa, danh mục, size, giá, ẩn/hiện), truy vấn đơn theo khách / mã / quá hạn, `existsByTxnRef`, `findTopByOrderIdOrderByIdDesc`, bảng size theo thứ tự, `findBySlug` |
| `service.ChangeLogServiceTest` | 2 | Đạt | **FR31**: sửa tồn kho ghi log có `product_id` + `variant_id`; sửa giá, ẩn sản phẩm ghi log cấp sản phẩm (`variant_id` null) |
| `infra.MockGatewayRestoreTest` | 2 | Đạt | Cổng giả lập: sau khi khởi động lại, link đã ký khôi phục được giao dịch (IPN ký đúng, số tiền đúng); link bị sửa số tiền thì bị từ chối |
| `infra.ImageStorageTest` | 3 | Đạt | Lưu ảnh PNG/JPEG trả về `/uploads/...`; file giả dạng ảnh (đuôi .png nhưng nội dung HTML) bị từ chối; file rỗng, file quá 5 MB bị từ chối |
| `service.StaffServiceTest` | 4 | Đạt | **BR02**: chủ tạo nhân viên (vai trò luôn STAFF, đăng nhập được); trùng tên đăng nhập bị từ chối; đặt lại mật khẩu và mở khóa; không đặt lại mật khẩu cho chủ cửa hàng |
| `api.ApiSecurityTest` | 6 | Đạt | **BR03** xem sản phẩm không cần đăng nhập; **BR15** tách quyền khách/nhân viên (401/403), token giả bị từ chối; lỗi trả JSON `{code, message}`; luồng HTTP đặt hàng ONLINE → thanh toán giả lập → nhân viên giao hàng → khách không hủy được; **BR02** quản lý nhân viên chỉ OWNER (nhân viên bị 403); tải ảnh: nhân viên được, khách 403, file giả bị từ chối |
| **Tổng** | **67** | **67/67 đạt** | |

Ngoài ra trên backend thật (dữ liệu thử đã dọn khỏi CSDL demo sau mỗi lần chạy):

- Kịch bản E2E 21 bước qua proxy Vite (cùng các lời gọi giao diện thực hiện): tất cả đạt.
- Chạy giao diện thật bằng Chrome headless (puppeteer-core), 19 bước: trang chủ, tìm kiếm, chưa đăng nhập bị chuyển sang đăng nhập, gợi ý size, chọn biến thể, giỏ hàng, xác nhận đặt hàng ONLINE, cổng giả lập, thanh toán thành công, đơn của tôi, đăng xuất, nhân viên đăng nhập, xem đơn, chuyển Đang giao (sinh mã vận đơn), sản phẩm & tồn kho, bố cục điện thoại 390px không tràn ngang: tất cả đạt, 0 lỗi console.
- Khởi động lại backend giữa lúc đặt đơn ONLINE và lúc thanh toán: đơn vẫn thanh toán được, chuyển sang Chờ xác nhận.
