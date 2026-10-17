# Chấm công NFC (chamcong_v2)

Ứng dụng Flutter offline cho xưởng CNC: ghi mốc thời gian của 3 tiến trình độc lập (làm việc, thiết kế, máy CNC) bằng nút bấm hoặc thẻ NFC. Không tính tiền; chỉ tổng hợp thời gian để nhập lên hệ thống xưởng.

- Cấu trúc & quy tắc: [PROJECT.md](PROJECT.md)
- Tiến độ và phần cần kiểm thử: [PROGRESS.md](PROGRESS.md)

## Mở và chạy thử trên Windows

Cài Flutter SDK, Android Studio, Android SDK và bật Flutter/Dart plugins trong Android Studio. Mở terminal trong Android Studio hoặc PowerShell.

```powershell
git clone --branch flutter-testable https://github.com/tudinh0722-wq/MyApplication.git
cd MyApplication

flutter doctor
flutter pub get
flutter analyze
flutter test
flutter run
```

Trong Android Studio, chọn **Open** và mở thư mục gốc `MyApplication` (thư mục có `pubspec.yaml`), không mở thư mục `app/` cũ. Chạy `flutter pub get` lần đầu để Flutter tạo `android/local.properties` trỏ tới Flutter SDK. Kết nối điện thoại Android, bật Developer options/USB debugging rồi chọn thiết bị trong thanh chạy.

Để tạo APK debug:

```powershell
flutter build apk --debug
```

APK nằm tại `build\app\outputs\flutter-apk\app-debug.apk`.

## Hành vi cần biết khi test

- Application ID mới: `com.tudinh.chamcong_v2`; database riêng: `chamcong_v2.db`, không dùng database Room của app cũ.
- Android Auto Backup bị tắt để tránh khôi phục database cũ sai schema.
- Database hỏng/sai schema sẽ được đổi tên thành `.broken-*` rồi tạo mới; app không chủ động xóa database đó.
- 7 tag được tạo sẵn nhưng chưa gán UID. Vào tab **Thẻ NFC**, chọn **Quét để gán** rồi chạm thẻ.
- NFC chỉ đọc khi app đang mở. UID chưa gán hoặc tag đang tắt sẽ không tạo sự kiện.
- Trigger 6 (kết thúc CNC tại máy) và Trigger 7 (xác nhận xong CNC tại bàn) đều kết thúc tiến trình CNC.
- Các tiến trình độc lập, nên bắt đầu/kết thúc một tiến trình không tự đóng tiến trình khác.

## Nếu build lỗi

Gửi lại toàn bộ output của `flutter doctor -v`, `flutter analyze`, và lệnh build bị lỗi. Đặc biệt, nếu Gradle báo không tương thích giữa Flutter và Android Gradle Plugin, đừng tự nâng phiên bản hàng loạt trước khi xem log.

## Ghi chú cấu trúc repo

Repo còn giữ một số file Android native cũ ở thư mục gốc để không xóa lịch sử hiện có. Dự án Flutter mới nằm ở `lib/`, `pubspec.yaml` và `android/`. Trong Android Studio, luôn mở thư mục gốc để Flutter plugin nhận đúng dự án.
