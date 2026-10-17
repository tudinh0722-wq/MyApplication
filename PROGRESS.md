# Tiến độ

Chú thích: ✅ đã viết và đẩy lên nhánh `flutter-testable` · 🧪 cần chạy thử trên máy thật · ⬜ chưa hoàn tất.
**Chưa có build/test thực tế trong môi trường viết** (không có Flutter SDK và không thể clone repo về môi trường này). Trạng thái “đã viết” không đồng nghĩa với “đã compile thành công”.

| # | Bước | Trạng thái mã | Kiểm chứng còn lại |
|---|---|---|---|
| M0 | Thiết lập Flutter, pubspec, README, Android host | ✅ | 🧪 `flutter pub get`, Gradle sync |
| M1 | Domain: trigger, luật 3 tiến trình, tổng hợp, test | ✅ | 🧪 `flutter test` |
| M2 | SQLite, schema, 7 tag mặc định, repository | ✅ | 🧪 kiểm tra database trên Android |
| M3 | Home, timeline, 7 nút, lịch sử trong ngày | ✅ | 🧪 bấm nút và khởi động lại app |
| M4 | Entry point, điều hướng, xử lý UID NFC | ✅ | 🧪 NFC trên điện thoại thật |
| M5 | Lịch sử theo ngày, thêm/sửa/xóa, tổng kết, cảnh báo | ✅ | 🧪 kiểm tra sửa sự kiện hôm nay và ngày cũ |
| M6 | Quản lý tag, gán/gỡ UID, bật/tắt, đổi tên | ✅ | 🧪 thử với các thẻ NFC thật |
| M7 | Build, chạy thử toàn luồng và sửa lỗi phát sinh | ⬜ | Chờ kết quả test trên máy |

## Nhật ký

- **2026-10-17 · M0–M3**: domain, data, controller, Home và timeline đã có từ trước.
- **2026-10-17 · M4**: thêm `lib/main.dart`, `lib/app_services.dart`, điều hướng 3 tab và luồng quét UID → tag → trigger. Có chống xử lý lặp cùng UID trong 2 giây.
- **2026-10-17 · M5**: thêm lịch sử theo ngày, tổng thời gian, cảnh báo sự kiện bất thường và thao tác thêm/sửa/xóa. Sau khi sửa lịch sử, Home được nạp lại để phản ánh dữ liệu mới.
- **2026-10-17 · M6**: thêm quản lý 7 tag: gán UID bằng quét NFC, gỡ UID, bật/tắt và đổi tên.
- **2026-10-17 · Android host**: thêm `android/` Gradle project, Manifest NFC, `MainActivity` và wrapper để có thể mở thư mục gốc bằng Flutter tooling. Cần xác minh Gradle/AGP trên máy có Flutter SDK.
- **Còn cần làm sau vòng test đầu**: sửa các lỗi analyzer/build phát sinh, kiểm tra trạng thái NFC khi app quay lại foreground, và xác minh luật nghiệp vụ với dữ liệu thực tế.
