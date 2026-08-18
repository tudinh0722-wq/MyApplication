package com.example.chamcong;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    private TodayFragment todayFragment = new TodayFragment();
    private HistoryFragment historyFragment = new HistoryFragment();
    private TagsFragment tagsFragment = new TagsFragment();

    private int updatingTagId = -1;
    private boolean isWriteMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? PendingIntent.FLAG_MUTABLE : 0;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_nav);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) return todayFragment;
                if (position == 1) return historyFragment;
                return tagsFragment;
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) bottomNav.setSelectedItemId(R.id.nav_today);
                else if (position == 1) bottomNav.setSelectedItemId(R.id.nav_history);
                else bottomNav.setSelectedItemId(R.id.nav_tags);
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_today) viewPager.setCurrentItem(0);
            else if (id == R.id.nav_history) viewPager.setCurrentItem(1);
            else viewPager.setCurrentItem(2);
            return true;
        });

        handleNfcIntent(getIntent());
    }

    public void startTagUpdate(int id, String name, boolean writeMode) {
        this.updatingTagId = id;
        this.isWriteMode = writeMode;
        String msg = writeMode ? "Chạm thẻ để ĐĂNG KÝ (Ghi dữ liệu)..." : "Chạm thẻ để cập nhật UID...";
        Toast.makeText(this, msg + "\n(" + name + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNfcIntent(intent);
    }

    private void handleNfcIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        if (isWriteMode && updatingTagId != -1) {
            writeNdefUri(tag, "chamcong://checkin/" + updatingTagId);
            return;
        }

        String uidHex = bytesToHex(tag.getId());
        if (!isWriteMode && updatingTagId != -1) {
            handleTagUpdate(uidHex);
            return;
        }

        if (intent.getData() != null && "chamcong".equals(intent.getData().getScheme())) {
            try {
                int tagId = Integer.parseInt(intent.getData().getLastPathSegment());
                handleEventScanById(tagId);
                return;
            } catch (Exception ignored) {}
        }

        handleEventScanByUid(uidHex);
    }

    private void writeNdefUri(Tag tag, String uri) {
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            Toast.makeText(this, "Thẻ không hỗ trợ định dạng NDEF!", Toast.LENGTH_SHORT).show();
            isWriteMode = false;
            updatingTagId = -1;
            return;
        }

        try {
            NdefRecord record = NdefRecord.createUri(uri);
            NdefMessage message = new NdefMessage(new NdefRecord[]{record});
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();

            handleTagUpdate(bytesToHex(tag.getId()));
            Toast.makeText(this, "Đã ghi định danh và Đăng ký thẻ thành công!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi ghi thẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            isWriteMode = false;
            updatingTagId = -1;
        }
    }

    private void handleTagUpdate(String newUid) {
        int id = updatingTagId;
        updatingTagId = -1;
        isWriteMode = false;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            TagEntity tag = null;
            for (TagEntity t : db.tagDao().getAll()) {
                if (t.id == id) {
                    tag = t;
                    break;
                }
            }
            if (tag != null) {
                tag.uid = newUid;
                db.tagDao().update(tag);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Đã cập nhật UID: " + newUid, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleEventScanByUid(String uidHex) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            TagEntity tagEntity = db.tagDao().findByUid(uidHex);
            if (tagEntity != null) {
                checkAndProcessEvent(tagEntity);
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Thẻ chưa đăng ký: " + uidHex, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleEventScanById(int tagId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            TagEntity tag = null;
            for (TagEntity t : db.tagDao().getAll()) {
                if (t.id == tagId) {
                    tag = t;
                    break;
                }
            }
            if (tag != null) {
                checkAndProcessEvent(tag);
            }
        });
    }

    private void checkAndProcessEvent(TagEntity tag) {
        AppDatabase db = AppDatabase.getInstance(this);
        String eType = tag.eventType != null ? tag.eventType : "";
        String baseType = eType.split("_")[0];
        EventEntity lastEvent = db.eventDao().getLastEventByType(baseType);

        boolean isStart = eType.endsWith("_START");
        boolean isEnd = eType.endsWith("_END");

        String errorMsg = null;
        if (lastEvent != null) {
            boolean lastIsStart = lastEvent.eventType != null && lastEvent.eventType.endsWith("_START");
            if (isStart && lastIsStart) {
                errorMsg = "Đang ở trạng thái BẮT ĐẦU. Vui lòng kết thúc trước.";
            } else if (isEnd && !lastIsStart) {
                errorMsg = "Chưa có sự kiện BẮT ĐẦU tương ứng.";
            }
        } else if (isEnd) {
            errorMsg = "Chưa có dữ liệu BẮT ĐẦU trước đó.";
        }

        if (errorMsg != null) {
            final String msg = errorMsg;
            runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show());
            return;
        }

        processEvent(tag);
    }

    private void processEvent(TagEntity tag) {
        long now = System.currentTimeMillis();
        EventEntity event = new EventEntity();
        event.tagId = tag.id;
        event.eventType = tag.eventType;
        event.timestamp = now;

        AppDatabase db = AppDatabase.getInstance(this);
        db.eventDao().insert(event);

        runOnUiThread(() -> {
            todayFragment.refresh();
            historyFragment.refresh();
            String timeStr = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(now));
            Toast.makeText(MainActivity.this, "✓ " + eventTypeToLabel(tag.eventType) + " " + timeStr, Toast.LENGTH_SHORT).show();
        });
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X:", b));
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String eventTypeToLabel(String eventType) {
        if ("WORK_START".equals(eventType)) return "Bắt đầu thời gian làm việc";
        if ("WORK_END".equals(eventType)) return "Kết thúc thời gian làm việc";
        if ("DESIGN_START".equals(eventType)) return "Bắt đầu thiết kế";
        if ("DESIGN_END".equals(eventType)) return "Kết thúc thiết kế";
        if ("CNC_START".equals(eventType)) return "CNC bắt đầu";
        if ("CNC_END".equals(eventType)) return "CNC kết thúc";
        return eventType;
    }
}
