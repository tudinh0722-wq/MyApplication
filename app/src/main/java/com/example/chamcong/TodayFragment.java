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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TodayFragment extends Fragment {

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
        loadToday();
    }

    public void refresh() {
        loadToday();
    }

    private void loadToday() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<EventEntity> all = db.eventDao().getAll();

            // Lọc event hôm nay
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();

            List<EventEntity> today = new ArrayList<>();
            for (EventEntity e : all) {
                if (e.timestamp >= startOfDay) today.add(e);
            }

            // Tính thời gian
            long workStart = -1, workEnd = -1;
            List<Long[]> designPairs = new ArrayList<>();
            List<Long[]> cncPairs = new ArrayList<>();
            long designStart = -1, cncStart = -1;

            // Sắp xếp theo thời gian tăng dần
            today.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

            for (EventEntity e : today) {
                switch (e.eventType) {
                    case "WORK_START": workStart = e.timestamp; break;
                    case "WORK_END":   workEnd = e.timestamp; break;
                    case "DESIGN_START": designStart = e.timestamp; break;
                    case "DESIGN_END":
                        if (designStart != -1) {
                            designPairs.add(new Long[]{designStart, e.timestamp});
                            designStart = -1;
                        }
                        break;
                    case "CNC_START": cncStart = e.timestamp; break;
                    case "CNC_END":
                        if (cncStart != -1) {
                            cncPairs.add(new Long[]{cncStart, e.timestamp});
                            cncStart = -1;
                        }
                        break;
                }
            }

            long totalWork = 0;
            if (workStart != -1) {
                totalWork = (workEnd != -1 ? workEnd : System.currentTimeMillis()) - workStart;
            }

            long totalDesign = 0;
            for (Long[] p : designPairs) totalDesign += p[1] - p[0];
            if (designStart != -1) totalDesign += System.currentTimeMillis() - designStart;

            long totalCnc = 0;
            for (Long[] p : cncPairs) totalCnc += p[1] - p[0];
            if (cncStart != -1) totalCnc += System.currentTimeMillis() - cncStart;

            StringBuilder status = new StringBuilder();
            status.append("HÔM NAY\n\n");

            status.append("Tại xưởng\n").append(formatDuration(totalWork)).append("\n\n");
            status.append("Thiết kế\n").append(formatDuration(totalDesign)).append("\n\n");
            status.append("CNC\n").append(formatDuration(totalCnc)).append("\n\n");

            status.append("---\n\n");

            SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            if (today.isEmpty()) {
                status.append("Chưa có sự kiện nào.");
            } else {
                for (EventEntity e : today) {
                    status.append(tf.format(new Date(e.timestamp)))
                            .append("  ")
                            .append(eventLabel(e.eventType))
                            .append("\n");
                }
            }

            String result = status.toString();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> tvContent.setText(result));
            }
        });
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