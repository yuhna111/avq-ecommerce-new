# backend/ — Spring Boot

Mã nguồn backend AVQ Clothing, hiện thực theo thiết kế Visual Paradigm (xem `../CLAUDE.md`).

| Package | Nội dung | Nguồn gốc |
|---|---|---|
| `api` | 9 controller REST | VP sinh, viết tiếp |
| `api.dto` | DTO/Request (record) | Bổ sung (VP chưa có) |
| `service` | 12 lớp nghiệp vụ | VP sinh, viết tiếp; + `BusinessException`, `PaymentCallbackException`, `DtoMapper` (hỗ trợ) |
| `infra` | JWT, cổng thanh toán, client AI | VP sinh, viết tiếp; + `MockPaymentGatewayClient`, các kiểu hỗ trợ |
| `repository` | 13 repository Spring Data JPA | VP sinh, viết tiếp; + `ProductFilter` |
| `domain` | 18 entity + 7 enum | VP sinh, viết tiếp; + `OrderItemId`, `CartItemId` (khóa ghép) |
| `app` | `AvqApplication`, cấu hình bảo mật, xử lý lỗi | Bổ sung |

Chạy: `.\mvnw.cmd spring-boot:run` (cần `src/main/resources/application-local.yml`, xem file mẫu `application-local.example.yml`).
Kiểm thử: `.\mvnw.cmd test` — chạy trên CSDL `avq_db_vp`, mỗi test tự rollback.

**Quy tắc:** các file đầu vào trong `../vp/` và `../db/` **không sửa tay**; mọi điều chỉnh làm ở đây và ghi vào `../docs/code-design-mapping.md`.
