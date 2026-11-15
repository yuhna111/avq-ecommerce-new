# db/seed/ — Dữ liệu demo

`demo_data.sql`: **chỉ INSERT**, không thay đổi schema (CLAUDE.md mục 7). Do Claude Code tạo ở Giai đoạn 6.

Chạy một lần trên CSDL vừa tạo từ `../V2__vp_generated.sql`:

```powershell
mysql -u root -p --default-character-set=utf8mb4 avq_db_vp -e "source db/seed/demo_data.sql"
```

Tài khoản: khách `khach@avq.vn` / `khach12345`; nhân viên `nhanvien` / `nhanvien123`; chủ cửa hàng `chucuahang` / `chucuahang123`.
