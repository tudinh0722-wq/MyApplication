package com.example.chamcong;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
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

    private TextView tvHeader, tvTotalWork, tvTotalDesign, tvTotalCnc, tvEventLog;
    private TimelineView timelineView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);
        tvHeader = view.findViewById(R.id.tv_header);
        tvTotalWork = view.findViewById(R.id.tv_total_work);
        tvTotalDesign = view.findViewById(R.id.tv_total_design);
        tvTotalCnc = view.findViewById(R.id.tv_total_cnc);
        tvEventLog = view.findViewById(R.id.tv_event_log);
        timelineView = view.findViewById(R.id.timeline_view);
        return view;
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

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("EEEE, dd/MM", Locale.getDefault());
            String dateStr = df.format(cal.getTime());

            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();

            List<EventEntity> today = new ArrayList<>();
            for (EventEntity e : all) {
                if (e.timestamp >= startOfDay) today.add(e);
            }
            today.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

            // Thuật toán ghép cặp (Pairing)
            List<Long[]> workPairs = new ArrayList<>();
            List<Long[]> designPairs = new ArrayList<>();
            List<Long[]> cncPairs = new ArrayList<>();
            long curWorkStart = -1, curDesignStart = -1, curCncStart = -1;

            StringBuilder logBuilder = new StringBuilder();
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (EventEntity e : today) {
                switch (e.eventType) {
                    case "WORK_START":
                        if (curWorkStart == -1) curWorkStart = e.timestamp;
                        break;
                    case "WORK_END":
                        if (curWorkStart != -1) {
                            workPairs.add(new Long[]{curWorkStart, e.timestamp});
                            curWorkStart = -1;
                        }
                        break;
                    case "DESIGN_START":
                        if (curDesignStart == -1) curDesignStart = e.timestamp;
                        break;
                    case "DESIGN_END":
                        if (curDesignStart != -1) {
                            designPairs.add(new Long[]{curDesignStart, e.timestamp});
                            curDesignStart = -1;
                        }
                        break;
                    case "CNC_START":
                        if (curCncStart == -1) curCncStart = e.timestamp;
                        break;
                    case "CNC_END":
                        if (curCncStart != -1) {
                            cncPairs.add(new Long[]{curCncStart, e.timestamp});
                            curCncStart = -1;
                        }
                        break;
                }
                logBuilder.append(tf.format(new Date(e.timestamp))).append("  ")
                        .append(eventLabel(e.eventType)).append("\n");
            }

            long totalWork = calculateTotal(workPairs, curWorkStart);
            long totalDesign = calculateTotal(designPairs, curDesignStart);
            long totalCnc = calculateTotal(cncPairs, curCncStart);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvHeader.setText("HÔM NAY · " + dateStr);
                    tvTotalWork.setText(formatDuration(totalWork));
                    tvTotalDesign.setText(formatDuration(totalDesign));
                    tvTotalCnc.setText(formatDuration(totalCnc));
                    tvEventLog.setText(logBuilder.toString());
                    timelineView.setData(workPairs, designPairs, cncPairs, curWorkStart, curDesignStart, curCncStart);
                });
            }
        });
    }

    private long calculateTotal(List<Long[]> pairs, long activeStart) {
        long total = 0;
        for (Long[] p : pairs) total += (p[1] - p[0]);
        if (activeStart != -1) total += (System.currentTimeMillis() - activeStart);
        return total;
    }

    private String formatDuration(long ms) {
        long min = ms / 60000;
        return (min / 60) + "h " + (min % 60) + "m";
    }

    private String eventLabel(String type) {
        switch (type) {
            case "WORK_START": return "Vào xưởng";
            case "WORK_END": return "Ra xưởng";
            case "DESIGN_START": return "Bắt đầu thiết kế";
            case "DESIGN_END": return "Kết thúc thiết kế";
            case "CNC_START": return "CNC bắt đầu";
            case "CNC_END": return "CNC kết thúc";
            default: return type;
        }
    }
}