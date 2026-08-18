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

    private TextView tvHeader, tvTotalWork, tvTotalDesign, tvTotalCnc, tvEventLog;
    private TimelineView timelineView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);
        tvHeader      = view.findViewById(R.id.tv_header);
        tvTotalWork   = view.findViewById(R.id.tv_total_work);
        tvTotalDesign = view.findViewById(R.id.tv_total_design);
        tvTotalCnc    = view.findViewById(R.id.tv_total_cnc);
        tvEventLog    = view.findViewById(R.id.tv_event_log);
        timelineView  = view.findViewById(R.id.timeline_view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadToday();
    }

    /** Gọi từ MainActivity sau khi ghi event NFC mới */
    public void refresh() {
        loadToday();
    }

    private void loadToday() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<EventEntity> all = db.eventDao().getAll();

            // Header date
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("EEEE, dd/MM", new Locale("vi", "VN"));
            String dateStr = df.format(cal.getTime());

            // Đầu ngày hôm nay
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();

            // Lọc & sort event hôm nay
            List<EventEntity> today = new ArrayList<>();
            for (EventEntity e : all) {
                if (e.timestamp >= startOfDay) today.add(e);
            }
            today.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

            // Ghép cặp
            List<Long[]> workPairs   = new ArrayList<>();
            List<Long[]> designPairs = new ArrayList<>();
            List<Long[]> cncPairs    = new ArrayList<>();
            long curWork = -1, curDesign = -1, curCnc = -1;

            StringBuilder logBuilder = new StringBuilder();
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (EventEntity e : today) {
                switch (e.eventType) {
                    case "WORK_START":
                        if (curWork == -1) curWork = e.timestamp;
                        break;
                    case "WORK_END":
                        if (curWork != -1) {
                            workPairs.add(new Long[]{curWork, e.timestamp});
                            curWork = -1;
                        }
                        break;
                    case "DESIGN_START":
                        if (curDesign == -1) curDesign = e.timestamp;
                        break;
                    case "DESIGN_END":
                        if (curDesign != -1) {
                            designPairs.add(new Long[]{curDesign, e.timestamp});
                            curDesign = -1;
                        }
                        break;
                    case "CNC_START":
                        if (curCnc == -1) curCnc = e.timestamp;
                        break;
                    case "CNC_END":
                        if (curCnc != -1) {
                            cncPairs.add(new Long[]{curCnc, e.timestamp});
                            curCnc = -1;
                        }
                        break;
                }
                logBuilder.append(tf.format(new Date(e.timestamp)))
                        .append("  ")
                        .append(eventLabel(e.eventType))
                        .append("\n");
            }

            long totalWork   = calcTotal(workPairs,   curWork);
            long totalDesign = calcTotal(designPairs, curDesign);
            long totalCnc    = calcTotal(cncPairs,    curCnc);

            // Snapshot để dùng trong lambda
            final List<Long[]> wP = workPairs;
            final List<Long[]> dP = designPairs;
            final List<Long[]> cP = cncPairs;
            final long aW = curWork, aD = curDesign, aC = curCnc;
            final String header = "HÔM NAY · " + dateStr;
            final String log = today.isEmpty() ? "Chưa có sự kiện nào." : logBuilder.toString().trim();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvHeader.setText(header);
                    tvTotalWork.setText(fmt(totalWork));
                    tvTotalDesign.setText(fmt(totalDesign));
                    tvTotalCnc.setText(fmt(totalCnc));
                    tvEventLog.setText(log);
                    timelineView.setData(wP, dP, cP, aW, aD, aC);
                });
            }
        });
    }

    private long calcTotal(List<Long[]> pairs, long activeStart) {
        long total = 0;
        for (Long[] p : pairs) total += (p[1] - p[0]);
        if (activeStart != -1) total += System.currentTimeMillis() - activeStart;
        return total;
    }

    private String fmt(long ms) {
        if (ms <= 0) return "—";
        long min = ms / 60000;
        return (min / 60) + "h " + (min % 60) + "m";
    }

    private String eventLabel(String type) {
        switch (type) {
            case "WORK_START":   return "Vào xưởng";
            case "WORK_END":     return "Ra xưởng";
            case "DESIGN_START": return "Bắt đầu thiết kế";
            case "DESIGN_END":   return "Kết thúc thiết kế";
            case "CNC_START":    return "CNC bắt đầu";
            case "CNC_END":      return "CNC kết thúc";
            default:             return type;
        }
    }
}
