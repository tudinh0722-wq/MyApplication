# Chấm công NFC (chamcong_v2)

Ứng dụng Flutter **offline hoàn toàn** cho xưởng CNC: ghi mốc thời gian 3 tiến trình độc lập (làm việc, thiết kế, máy CNC) bằng nút bấm hoặc thẻ NFC NTAG213. Không tính tiền, chỉ tổng hợp thời gian để nhập lên hệ thống xưởng.

- Cấu trúc & luật: [PROJECT.md](PROJECT.md)
- Tiến độ từng bước: [PROGRESS.md](PROGRESS.md)

## Dự án mới, tách biệt app cũ

Để máy đã từng cài bản cũ không bị lỗi database khi cài bản này:

| Thứ | App cũ | Bản này |
|---|---|---|
| applicationId | `com.example.chamcong` | `com.tudinh.chamcong_v2` |
| File database | (Room cũ) | `chamcong_v2.db` |
| Android Auto Backup | bật | **tắt** (script ở `tools/`) |

Hai app cài song song được, không đụng dữ liệu của nhau. Database hỏng hoặc sai schema sẽ được **đổi tên thành `.broken-*` rồi tạo mới**, không bao giờ xóa.

## Cài đặt lần đầu (Windows, PowerShell)

Repo này chỉ chứa `lib/`, `pubspec.yaml`, `test/` và công cụ. Thư mục `android/` do Flutter tạo ra.

```powershell
git clone https://github.com/tudinh0722-wq/MyApplication.git
cd MyApplication

# 1. Dọn file Android native cũ (vẫn còn trong lịch sử git)
git rm -r --ignore-unmatch app gradle .idea gradlew gradlew.bat build.gradle.kts settings.gradle.kts gradle.properties
git commit -m "Dọn project Android native cũ"

# 2. Sinh phần Android của Flutter với applicationId mới
flutter create --org com.tudinh --project-name chamcong_v2 --platforms android .

# 3. Xóa test mẫu (tham chiếu MyApp không còn)
Remove-Item test\widget_test.dart -ErrorAction SilentlyContinue

# 4. Thêm quyền NFC, tắt Auto Backup
powershell -ExecutionPolicy Bypass -File tools\patch_android_manifest.ps1

# 5. Chạy
flutter pub get
flutter analyze
flutter test
flutter run
```

Nếu `flutter run` báo lỗi `Namespace not specified` ở `nfc_manager`, báo lại để đổi phiên bản gói.

## Quy tắc dùng

- Mỗi lần bắt đầu một tiến trình phải kết thúc lần trước (trừ lần đầu trong ngày).
- Trigger 6 (kết thúc CNC tại máy) và 7 (xác nhận tại bàn) đều kết thúc tiến trình CNC.
- Mọi sửa, thêm, xóa sự kiện và xử lý quên kết thúc làm ở màn **Lịch sử**.
- UID thẻ NFC **không lưu trong mã nguồn**: gán ở màn **Thẻ** bằng cách chạm thẻ.
