# Vá AndroidManifest.xml của dự án Flutter sau khi chạy `flutter create`.
# - Thêm quyền NFC.
# - Tắt Auto Backup (tránh Android khôi phục database cũ sai schema khi cài lại app).
# Chạy từ thư mục gốc dự án:  powershell -ExecutionPolicy Bypass -File tools\patch_android_manifest.ps1

$path = Join-Path $PSScriptRoot '..\android\app\src\main\AndroidManifest.xml'
if (-not (Test-Path $path)) {
    Write-Error "Không thấy $path - hãy chạy 'flutter create' trước."
    exit 1
}

$xml = Get-Content $path -Raw -Encoding UTF8

if ($xml -notmatch 'android.permission.NFC') {
    $perm = "`n    <uses-permission android:name=`"android.permission.NFC`" />`n    <uses-feature android:name=`"android.hardware.nfc`" android:required=`"true`" />"
    $xml = $xml -replace '(<manifest[^>]*>)', ('$1' + $perm)
    Write-Host 'Đã thêm quyền NFC.'
} else {
    Write-Host 'Quyền NFC đã có.'
}

if ($xml -notmatch 'android:allowBackup') {
    $xml = $xml -replace '<application', '<application android:allowBackup="false"'
    Write-Host 'Đã tắt allowBackup.'
} else {
    Write-Host 'allowBackup đã được khai báo - kiểm tra thủ công.'
}

Set-Content $path $xml -Encoding UTF8
Write-Host 'Xong.'
