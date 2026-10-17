# Tiến độ

Chú thích: ✅ đã viết xong và đẩy lên repo · ⬜ chưa làm · 🧪 cần bạn chạy thử trên máy.
**Toàn bộ mã được viết mà chưa compile hay chạy thử** (môi trường viết không có Flutter). Mỗi bước bên dưới cần `flutter analyze` và chạy thật để xác nhận.

| # | Bước | Trạng thái | Kiểm chứng |
|---|---|---|---|
| M0 | Thiết lập: pubspec, README, PROJECT, script manifest | ✅ | 🧪 `flutter create` + `flutter pub get` |
| M1 | Domain: trigger, luật 3 tiến trình, kiểm tra, tổng hợp, test | ✅ | 🧪 `flutter test` |
| M2 | Data: sqflite, 2 bảng, nạp 7 thẻ, chịu lỗi database | ✅ | 🧪 |
| M3 | Home: controller, timeline, 7 nút, lịch sử trong ngày | ⬜ | 🧪 |
| M4 | NFC: đọc UID, xử lý thẻ quét, main, điều hướng | ⬜ | 🧪 trên S23 |
| M5 | Lịch sử: theo ngày, sửa/thêm/xóa, cảnh báo quên kết thúc | ⬜ | 🧪 |
| M6 | Thẻ: gán UID bằng cách quét, bật/tắt | ⬜ | 🧪 trên S23 |
| M7 | Chạy thử thật cả luồng, sửa lỗi phát sinh | ⬜ | bạn làm |

## Nhật ký

- **2026-10-17 · M0**: thêm `pubspec.yaml` (sqflite, nfc_manager 3.x, dynamic_color), `.gitignore` Flutter, `analysis_options.yaml`, `README.md` (cài đặt, dọn file cũ), `PROJECT.md`, `tools/patch_android_manifest.ps1`. Chọn applicationId mới `com.tudinh.chamcong_v2` và database `chamcong_v2.db` để không đụng dữ liệu app cũ.
- **2026-10-17 · M1**: thêm `util/time_utils.dart`, `domain/trigger.dart` (3 tiến trình, 7 trigger, `Interval` có cờ `broken`), `domain/tag_info.dart`, `domain/day_logic.dart` (`buildIntervals`, `validate`, `summarize`, `findIssues`), `test/day_logic_test.dart` (13 ca). Quy tắc đã mã hóa: bắt đầu khi đang chạy bị chặn, kết thúc khi chưa bắt đầu bị chặn (trừ xác nhận CNC), ngày cũ chưa kết thúc tính 0 và cảnh báo.
- **2026-10-17 · M2**: thêm `data/app_database.dart` (file `chamcong_v2.db`, bảng `tag` và `event`, chỉ mục UID duy nhất, nạp 7 thẻ chưa có UID, nếu mở lỗi thì đổi tên file thành `.broken-*` rồi tạo mới, không xóa) và `data/repository.dart` (đọc/ghi sự kiện theo khoảng ngày, gán UID cho thẻ trong transaction).
