# frontend/ — Giao diện người dùng (React 18 + Vite)

Giao diện tiếng Việt cho khách hàng và nhân viên AVQ Clothing.

- `npm install` rồi `npm run dev` → http://localhost:5173 (proxy `/api` sang backend cổng 8080).
- `src/pages/`: trang khách (`ProductList`, `ProductDetail`, `Cart`, `Checkout`, `MockPay`, `MyOrders`, `Auth`) và trang nhân viên (`StaffOrders`, `StaffProducts`).
- `public/images/`: ảnh minh họa sản phẩm (SVG) dùng cho dữ liệu demo `../db/seed/demo_data.sql`.

**Quy tắc:** không đặt file đầu vào của VP ở đây. Các file trong `vp/` và `db/` **không sửa tay**.
