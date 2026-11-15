# db/ — DDL cơ sở dữ liệu do VP sinh

**Chứa gì:** các script DDL do Visual Paradigm sinh từ sơ đồ ERD, đặt tên theo quy ước migration:
- `V1__vp_generated.sql`
- `V2__vp_generated.sql`

**Ai đặt file vào:** Tôi (tác giả tiểu luận) sinh DDL từ VP và chép vào. Claude Code chỉ đọc, không sửa các file này.

**Quy tắc:** ⚠️ **KHÔNG SỬA TAY** các file `V*__vp_generated.sql`. Muốn thay đổi lược đồ thì sửa ERD trong VP rồi sinh lại. Các thay đổi CSDL phát sinh khi thiết kế (DB-R1…DB-R7) được ghi trong `docs/design-notes.md`.
