# Tiến độ

Chú thích: ✅ đã viết xong và đẩy lên repo · ⬜ chưa làm · 🧪 cần bạn chạy thử trên máy.
**Toàn bộ mã được viết mà chưa compile hay chạy thử** (môi trường viết không có Flutter). Mỗi bước bên dưới cần `flutter analyze` và chạy thật để xác nhận.

| # | Bước | Trạng thái | Kiểm chứng |
|---|---|---|---|
| M0 | Thiết lập: pubspec, README, PROJECT, script manifest | ✅ | 🧪 `flutter create` + `flutter pub get` |
| M1 | Domain: trigger, luật 3 tiến trình, kiểm tra, tổng hợp, test | ⬜ | 🧪 `flutter test` |
| M2 | Data: sqflite, 2 bảng, nạp 7 thẻ, chịu lỗi database | ⬜ | 🧪 |
| M3 | Home: controller, timeline, 7 nút, lịch sử trong ngày | ⬜ | 🧪 |
| M4 | NFC: đọc UID, xử lý thẻ quét, main, điều hướng | ⬜ | 🧪 trên S23 |
| M5 | Lịch sử: theo ngày, sửa/thêm/xóa, cảnh báo quên kết thúc | ⬜ | 🧪 |
| M6 | Thẻ: gán UID bằng cách quét, bật/tắt | ⬜ | 🧪 trên S23 |
| M7 | Chạy thử thật cả luồng, sửa lỗi phát sinh | ⬜ | bạn làm |

## Nhật ký

- **2026-10-17 · M0**: thêm `pubspec.yaml` (sqflite, nfc_manager 3.x, dynamic_color), `.gitignore` Flutter, `analysis_options.yaml`, `README.md` (cài đặt, dọn file cũ), `PROJECT.md`, `tools/patch_android_manifest.ps1`. Chọn applicationId mới `com.tudinh.chamcong_v2` và database `chamcong_v2.db` để không đụng dữ liệu app cũ.
