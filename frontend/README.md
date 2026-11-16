# frontend/ — Giao diện người dùng (React 18 + Vite)

Giao diện tiếng Việt cho khách hàng và nhân viên AVQ Clothing.

- `npm install` rồi `npm run dev` → http://localhost:5173 (proxy `/api` sang backend cổng 8080).
- `src/pages/`: trang khách (`ProductList`, `ProductDetail`, `Cart`, `Checkout`, `MockPay`, `MyOrders`, `Auth`) và trang nhân viên (`StaffOrders`, `StaffProducts`).
- `public/images/products/`: ảnh sản phẩm thật (Unsplash License, nguồn từng ảnh trong `CREDITS.md`) dùng cho dữ liệu demo `../db/seed/demo_data.sql`. `public/images/*.svg` là hình vẽ cũ, giữ làm biểu tượng trang.

**Quy tắc:** không đặt file đầu vào của VP ở đây. Các file trong `vp/` và `db/` **không sửa tay**.
