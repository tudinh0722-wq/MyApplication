package com.example.chamcong;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TodayFragment todayFragment;
    private int updatingTagId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(
                    this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE
            );
        }

        // Setup fragments
        todayFragment = new TodayFragment();
        HistoryFragment historyFragment = new HistoryFragment();
        TagsFragment tagsFragment = new TagsFragment();

        // Hiện tab Hôm nay mặc định
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, todayFragment, "today")
                .add(R.id.fragment_container, historyFragment, "history")
                .add(R.id.fragment_container, tagsFragment, "tags")
                .hide(historyFragment)
                .hide(tagsFragment)
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected;
            int id = item.getItemId();
            if (id == R.id.nav_today) {
                selected = todayFragment;
            } else if (id == R.id.nav_history) {
                selected = historyFragment;
            } else {
                selected = tagsFragment;
            }
            getSupportFragmentManager().beginTransaction()
                    .hide(todayFragment)
                    .hide(historyFragment)
                    .hide(tagsFragment)
                    .show(selected)
                    .commit();
            return true;
        });

        // Xử lý NFC ngay khi mở app bằng cách chạm thẻ (Cold Start)
        if (getIntent() != null) {
            handleNfcIntent(getIntent());
        }
    }

    public void startTagUpdate(int id, String name) {
        this.updatingTagId = id;
        Toast.makeText(this, "Quét thẻ mới cho: " + name, Toast.LENGTH_LONG).show();
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
        setIntent(intent); // Quan trọng để các lần quét sau hoạt động đúng
        handleNfcIntent(intent);
    }

    private void handleNfcIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        String uidHex = bytesToHex(tag.getId());

        if (updatingTagId != -1) {
            handleTagUpdate(uidHex);
            return;
        }

        handleEventScan(uidHex);
    }

    private void handleTagUpdate(String newUid) {
        int id = updatingTagId;
        updatingTagId = -1; // Reset mode

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<TagEntity> all = db.tagDao().getAll();
            TagEntity target = null;
            for (TagEntity t : all) {
                if (t.id == id) {
                    target = t;
                    break;
                }
            }

            if (target != null) {
                target.uid = newUid;
                db.tagDao().update(target);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã cập nhật thẻ mới: " + newUid, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleEventScan(String uidHex) {
        long now = System.currentTimeMillis();
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            TagEntity tagEntity = db.tagDao().findByUid(uidHex);

            runOnUiThread(() -> {
                if (tagEntity == null) {
                    Toast.makeText(this, "Tag lạ: " + uidHex, Toast.LENGTH_SHORT).show();
                    return;
                }

                EventEntity event = new EventEntity();
                event.tagId = tagEntity.id;
                event.eventType = tagEntity.eventType;
                event.timestamp = now;

                Executors.newSingleThreadExecutor().execute(() -> {
                    db.eventDao().insert(event);
                    runOnUiThread(() -> {
                        if (todayFragment != null) todayFragment.refresh();
                    });
                });

                String timeStr = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(new Date(now));
                Toast.makeText(this,
                        "✓ " + eventTypeToLabel(tagEntity.eventType) + " " + timeStr,
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X:", b));
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String eventTypeToLabel(String eventType) {
        switch (eventType) {
            case "WORK_START":   return "Vào xưởng";
            case "WORK_END":     return "Ra xưởng";
            case "DESIGN_START": return "Bắt đầu thiết kế";
            case "DESIGN_END":   return "Kết thúc thiết kế";
            case "CNC_START":    return "CNC bắt đầu";
            case "CNC_END":      return "CNC kết thúc";
            default:             return eventType;
        }
    }
}