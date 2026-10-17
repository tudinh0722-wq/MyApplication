# Cấu trúc dự án

## 1. Ba tiến trình độc lập, bảy trigger

| Tiến trình | Bắt đầu | Kết thúc |
|---|---|---|
| Làm việc | Trigger 1 | Trigger 2 |
| Thiết kế file | Trigger 3 | Trigger 4 |
| Máy CNC | Trigger 5 | Trigger 6 (tại máy) hoặc 7 (xác nhận tại bàn) |

Quy tắc:

1. Các tiến trình chạy song song, không tự đóng lẫn nhau.
2. Mỗi lần bắt đầu phải có kết thúc trước đó (trừ lần đầu trong ngày). Một ngày có thể lặp nhiều lần.
3. Trigger 6 và 7 chỉ là hai cách tạo sự kiện kết thúc CNC. Trigger 7 khi CNC đã đóng vẫn được ghi, không đổi thời lượng.
4. **Trạng thái luôn suy ra từ danh sách sự kiện trong database**, không giữ riêng trong RAM.
5. Ngoại lệ (quên kết thúc, sai giờ, thêm tay) xử lý ở màn Lịch sử.
6. Không tính tiền. Chỉ tổng hợp thời gian.

## 2. Luồng dữ liệu

```
 Thẻ NFC -> NfcService -> AppServices.handleUid -> tra bảng Tag
 Nút bấm (Home) --------------------------------+
                                                v
                                  DayController.press()
                                     validate (day_logic)
                                                v
                                    Repository -> sqflite (chamcong_v2.db)
                                                v
                  buildIntervals / summarize / findIssues (day_logic)
                                                v
                              Home · Lịch sử · Thẻ
```

Ba lớp: **domain** (luật, thuần Dart), **data** (sqflite), **ui** (giao diện).

## 3. Thư mục

```
lib/
├── main.dart                  khởi động, Material You, thanh điều hướng 3 tab
├── app_services.dart          gom repo + controller + NFC, xử lý UID quét được
├── domain/
│   ├── trigger.dart           ProcessType, Trigger (7), TriggerEvent, Interval
│   ├── tag_info.dart          mô hình thẻ NFC
│   ├── day_logic.dart         buildIntervals, validate, summarize, findIssues
│   └── day_controller.dart    state ngày hiện tại, hàm press()
├── data/
│   ├── app_database.dart      mở chamcong_v2.db, tạo bảng, nạp 7 thẻ, chịu lỗi
│   └── repository.dart        đọc/ghi Event và Tag
├── nfc/
│   └── nfc_service.dart       nfc_manager, lấy UID
├── util/
│   └── time_utils.dart        định dạng giờ, mốc đầu ngày
└── ui/
    ├── home_page.dart         timeline + 7 nút + lịch sử trong ngày
    ├── timeline_painter.dart  3 thanh song song 6:00-22:00
    ├── history_page.dart      xem theo ngày, sửa, thêm, xóa, cảnh báo
    └── tags_page.dart         gán UID bằng cách quét, bật/tắt thẻ
test/
└── day_logic_test.dart
tools/
└── patch_android_manifest.ps1
```

## 4. Database `chamcong_v2.db`

**tag**: `id` (TAG-001...), `name`, `trigger_type`, `uid` (có thể rỗng, duy nhất khi có), `active`.
Không dùng UID làm khóa chính: thẻ hỏng thì chỉ gán UID mới vào Tag cũ.

**event**: `id` (tự tăng), `tag_id` (rỗng nếu bấm nút hoặc nhập tay), `trigger_type`, `ts` (mili giây epoch).

Bảy thẻ mặc định, **chưa có UID** (gán bằng cách quét ở màn Thẻ):

| Tag | Trigger mặc định |
|---|---|
| TAG-001 | Bắt đầu làm việc |
| TAG-002 | Kết thúc làm việc |
| TAG-003 | Bắt đầu thiết kế |
| TAG-004 | Kết thúc thiết kế |
| TAG-005 | Bắt đầu máy CNC |
| TAG-006 | Kết thúc máy CNC (vị trí 1) |
| TAG-007 | Kết thúc máy CNC (vị trí 2) |

Trigger mặc định của TAG-001 đến TAG-005 là giả định theo thứ tự thường gặp, kiểm tra lại khi gán thẻ.

## 5. Giới hạn đã biết

- Tiến trình bắt đầu hôm qua mà chưa kết thúc: sang ngày mới, Home coi như chưa chạy. Sửa ở Lịch sử.
- Timeline cố định 6:00-22:00, sự kiện ngoài khung bị cắt ở mép.
- Quét thẻ chỉ hoạt động khi app đang mở (chưa hỗ trợ chạm thẻ để mở app).
- Sửa tay trong Lịch sử chỉ chọn đến phút, giây được giữ nguyên hoặc đặt 0.
