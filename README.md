# AVQ Clothing

Website bán quần áo AVQ Clothing — bản demo minh họa cho tiểu luận môn **Phân tích thiết kế hệ thống thông tin**.

Mô hình phân tích/thiết kế được xây dựng bằng Visual Paradigm (VP). Mã khung Java và DDL cơ sở dữ liệu do VP sinh ra là **đầu vào** của dự án; phần backend và frontend sẽ được xây dựng dựa trên các đầu vào này và các tài liệu trong `docs/`.

> Trạng thái hiện tại: mới khởi tạo cấu trúc thư mục, chưa có mã nguồn backend/frontend.

## Cấu trúc thư mục

```
avq-clothing/
├── CLAUDE.md
├── README.md
├── .gitignore
├── vp/
│   ├── generated-java/   (mã khung Java do Visual Paradigm Instant Generator sinh)
│   └── (model.xml, AVQ_Clothing.vpp sẽ được chép vào)
├── db/                   (DDL do Visual Paradigm sinh: V1__vp_generated.sql, V2__vp_generated.sql)
├── docs/
│   ├── business-rules.md
│   ├── design-notes.md
│   └── diagrams/         (ảnh PNG các sơ đồ VP)
├── backend/              (để trống)
└── frontend/             (để trống)
```

## Quy tắc chung

- Các file đầu vào trong `vp/` và `db/` là kết quả sinh từ Visual Paradigm — **không sửa tay**. Muốn thay đổi thì sửa trong mô hình VP rồi sinh lại.
- Mỗi thư mục có một `README.md` mô tả nội dung và người chịu trách nhiệm đặt file vào.
