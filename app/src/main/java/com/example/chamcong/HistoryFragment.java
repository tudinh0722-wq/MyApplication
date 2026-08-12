package com.example.chamcong;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private TextView tvContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
        tvContent = new TextView(getContext());
        tvContent.setPadding(40, 40, 40, 40);
        tvContent.setTextSize(16);
        tvContent.setTextColor(android.graphics.Color.BLACK);
        tvContent.setLineSpacing(0, 1.2f);
        scrollView.addView(tvContent);
        return scrollView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<EventEntity> all = db.eventDao().getAll();

            if (all.isEmpty()) {
                updateUI("Chưa có dữ liệu lịch sử.");
                return;
            }

            // Nhóm theo ngày
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Map<String, List<EventEntity>> groups = new TreeMap<>((a, b) -> b.compareTo(a)); // Mới nhất lên đầu

            for (EventEntity e : all) {
                String day = dayFormat.format(new Date(e.timestamp));
                if (!groups.containsKey(day)) groups.put(day, new ArrayList<>());
                groups.get(day).add(e);
            }

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (Map.Entry<String, List<EventEntity>> entry : groups.entrySet()) {
                String day = entry.getKey();
                List<EventEntity> dayEvents = entry.getValue();
                dayEvents.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

                sb.append(day).append("\n\n");

                // Tính toán
                long workStart = -1, workEnd = -1;
                long designTotal = 0, designStart = -1;
                long cncTotal = 0, cncStart = -1;

                for (EventEntity e : dayEvents) {
                    switch (e.eventType) {
                        case "WORK_START": workStart = e.timestamp; break;
                        case "WORK_END":   workEnd = e.timestamp; break;
                        case "DESIGN_START": designStart = e.timestamp; break;
                        case "DESIGN_END":
                            if (designStart != -1) {
                                designTotal += (e.timestamp - designStart);
                                designStart = -1;
                            }
                            break;
                        case "CNC_START": cncStart = e.timestamp; break;
                        case "CNC_END":
                            if (cncStart != -1) {
                                cncTotal += (e.timestamp - cncStart);
                                cncStart = -1;
                            }
                            break;
                    }
                }

                long workTotal = 0;
                if (workStart != -1 && workEnd != -1) workTotal = workEnd - workStart;

                sb.append("Tại xưởng   ").append(formatDuration(workTotal)).append("\n");
                sb.append("Thiết kế    ").append(formatDuration(designTotal)).append("\n");
                sb.append("CNC         ").append(formatDuration(cncTotal)).append("\n\n");

                for (EventEntity e : dayEvents) {
                    sb.append(timeFormat.format(new Date(e.timestamp)))
                            .append("  ")
                            .append(eventLabel(e.eventType))
                            .append("\n");
                }
                sb.append("\n--------------------------\n\n");
            }

            updateUI(sb.toString());
        });
    }

    private void updateUI(String text) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> tvContent.setText(text));
        }
    }

    private String eventLabel(String eventType) {
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

    private String formatDuration(long ms) {
        long totalMin = ms / 60000;
        long h = totalMin / 60;
        long m = totalMin % 60;
        return h + "h " + m + "m";
    }
}