package com.example.chamcong;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment {

    private LinearLayout llHistoryContainer;
    private Calendar calendarEntry = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        llHistoryContainer = root.findViewById(R.id.ll_history_container);
        View fab = root.findViewById(R.id.fab_add_event);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                calendarEntry = Calendar.getInstance();
                showEditDialog(null);
            });
        }
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    public void refresh() {
        loadHistory();
    }

    private void loadHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (getContext() == null) return;
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<EventEntity> all = db.eventDao().getAll();

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
            Map<String, List<EventEntity>> groups = new TreeMap<>(Collections.reverseOrder());

            for (EventEntity e : all) {
                String day = dayFormat.format(new Date(e.timestamp));
                if (!groups.containsKey(day)) {
                    groups.put(day, new ArrayList<>());
                }
                groups.get(day).add(e);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> renderHistory(groups));
            }
        });
    }

    private void renderHistory(Map<String, List<EventEntity>> groups) {
        if (llHistoryContainer == null || getContext() == null) return;
        llHistoryContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        String todayKey = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault()).format(new Date());

        if (groups.isEmpty()) {
            TextView tvEmpty = new TextView(getContext());
            tvEmpty.setText("Chưa có dữ liệu lịch sử.");
            tvEmpty.setPadding(40, 40, 40, 40);
            llHistoryContainer.addView(tvEmpty);
            return;
        }

        for (Map.Entry<String, List<EventEntity>> entry : groups.entrySet()) {
            String dayKey = entry.getKey();
            List<EventEntity> events = entry.getValue();

            View dayCard = inflater.inflate(R.layout.item_history_day, llHistoryContainer, false);
            TextView tvTitle = dayCard.findViewById(R.id.tv_day_title);
            TextView tvWork = dayCard.findViewById(R.id.tv_total_work);
            TextView tvDesign = dayCard.findViewById(R.id.tv_total_design);
            TextView tvCnc = dayCard.findViewById(R.id.tv_total_cnc);
            LinearLayout llEvents = dayCard.findViewById(R.id.ll_event_list);

            tvTitle.setText(dayKey);
            boolean isToday = dayKey.equals(todayKey);
            events.sort((a, b) -> Long.compare(a.timestamp, b.timestamp));

            boolean isWRunning = false, isDRunning = false, isCRunning = false;
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (EventEntity e : events) {
                if ("WORK_START".equals(e.eventType)) isWRunning = true;
                if ("DESIGN_START".equals(e.eventType)) isDRunning = true;
                if ("CNC_START".equals(e.eventType)) isCRunning = true;

                View row = inflater.inflate(R.layout.item_history_event, llEvents, false);
                TextView tvTime = row.findViewById(R.id.tv_event_time);
                TextView tvLabel = row.findViewById(R.id.tv_event_label);
                
                tvTime.setText(tf.format(new Date(e.timestamp)));
                tvLabel.setText(eventTypeToLabel(e.eventType));

                View laneWork = row.findViewById(R.id.view_lane_work);
                if (laneWork != null) laneWork.setAlpha(isWRunning ? 1.0f : 0.1f);
                if (e.eventType != null && e.eventType.startsWith("WORK")) {
                    row.findViewById(R.id.dot_work).setVisibility(View.VISIBLE);
                    tvLabel.setTextColor(getResources().getColor(R.color.work_color, null));
                }

                View laneDesign = row.findViewById(R.id.view_lane_design);
                if (laneDesign != null) laneDesign.setAlpha(isDRunning ? 1.0f : 0.1f);
                if (e.eventType != null && e.eventType.startsWith("DESIGN")) {
                    row.findViewById(R.id.dot_design).setVisibility(View.VISIBLE);
                    tvLabel.setTextColor(getResources().getColor(R.color.design_color, null));
                }

                View laneCnc = row.findViewById(R.id.view_lane_cnc);
                if (laneCnc != null) laneCnc.setAlpha(isCRunning ? 1.0f : 0.1f);
                if (e.eventType != null && e.eventType.startsWith("CNC")) {
                    row.findViewById(R.id.dot_cnc).setVisibility(View.VISIBLE);
                    tvLabel.setTextColor(getResources().getColor(R.color.cnc_color, null));
                }

                if ("WORK_END".equals(e.eventType)) isWRunning = false;
                if ("DESIGN_END".equals(e.eventType)) isDRunning = false;
                if ("CNC_END".equals(e.eventType)) isCRunning = false;

                row.setOnLongClickListener(v -> {
                    calendarEntry.setTimeInMillis(e.timestamp);
                    showEditDialog(e);
                    return true;
                });
                llEvents.addView(row);
            }

            tvWork.setText(formatDuration(calculateTotal(events, "WORK", isToday)));
            tvDesign.setText(formatDuration(calculateTotal(events, "DESIGN", isToday)));
            tvCnc.setText(formatDuration(calculateTotal(events, "CNC", isToday)));

            llHistoryContainer.addView(dayCard);
        }
    }

    private long calculateTotal(List<EventEntity> events, String typePrefix, boolean isToday) {
        long total = 0;
        long startTs = -1;
        for (EventEntity e : events) {
            String type = e.eventType != null ? e.eventType : "";
            if (type.startsWith(typePrefix)) {
                if (type.endsWith("_START")) {
                    if (startTs == -1) startTs = e.timestamp;
                } else if (type.endsWith("_END")) {
                    if (startTs != -1) {
                        total += (e.timestamp - startTs);
                        startTs = -1;
                    }
                }
            }
        }
        if (startTs != -1) {
            if (isToday) {
                total += (System.currentTimeMillis() - startTs);
            } else {
                Calendar endOfDay = Calendar.getInstance();
                endOfDay.setTimeInMillis(startTs);
                endOfDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfDay.set(Calendar.MINUTE, 59);
                endOfDay.set(Calendar.SECOND, 59);
                total += (endOfDay.getTimeInMillis() - startTs);
            }
        }
        return total;
    }

    private void showEditDialog(EventEntity event) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_event, null);
        Spinner spinner = view.findViewById(R.id.spinner_tag_type);
        DatePicker datePicker = view.findViewById(R.id.date_picker);
        TimePicker timePicker = view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            if (getActivity() == null || getContext() == null) return;
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<TagEntity> tags = db.tagDao().getAll();
            
            getActivity().runOnUiThread(() -> {
                if (getContext() == null) return;
                List<String> tagNames = new ArrayList<>();
                for (TagEntity t : tags) tagNames.add(t.name);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tagNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                if (event != null) {
                    for (int i = 0; i < tags.size(); i++) {
                        if (tags.get(i).id == event.tagId) {
                            spinner.setSelection(i);
                            break;
                        }
                    }
                }

                datePicker.init(calendarEntry.get(Calendar.YEAR), calendarEntry.get(Calendar.MONTH), calendarEntry.get(Calendar.DAY_OF_MONTH), null);
                timePicker.setHour(calendarEntry.get(Calendar.HOUR_OF_DAY));
                timePicker.setMinute(calendarEntry.get(Calendar.MINUTE));
            });
        });

        builder.setView(view)
            .setTitle(event == null ? "Thêm sự kiện" : "Sửa sự kiện")
            .setPositiveButton("Lưu", (d, w) -> {
                calendarEntry.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
                saveManualEvent(event, spinner, calendarEntry.getTimeInMillis());
            })
            .setNegativeButton("Hủy", null);

        if (event != null) {
            builder.setNeutralButton("Xóa", (d, w) -> deleteEvent(event));
        }
        builder.show();
    }

    private void saveManualEvent(EventEntity oldEvent, Spinner spinner, long timestamp) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (getContext() == null || getActivity() == null) return;
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<TagEntity> tags = db.tagDao().getAll();
            TagEntity selectedTag = tags.get(spinner.getSelectedItemPosition());

            EventEntity event = oldEvent != null ? oldEvent : new EventEntity();
            event.tagId = selectedTag.id;
            event.eventType = selectedTag.eventType;
            event.timestamp = timestamp;

            if (oldEvent == null) db.eventDao().insert(event);
            else db.eventDao().update(event);

            getActivity().runOnUiThread(() -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã lưu!", Toast.LENGTH_SHORT).show();
                    loadHistory();
                }
            });
        });
    }

    private void deleteEvent(EventEntity event) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (getContext() == null || getActivity() == null) return;
            AppDatabase db = AppDatabase.getInstance(getContext());
            db.eventDao().delete(event);
            getActivity().runOnUiThread(() -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                    loadHistory();
                }
            });
        });
    }

    private String formatDuration(long ms) {
        long min = ms / 60000;
        return (min / 60) + "h " + (min % 60) + "m";
    }

    private String eventTypeToLabel(String type) {
        if ("WORK_START".equals(type)) return "✓ Bắt đầu thời gian làm việc";
        if ("WORK_END".equals(type)) return "✓ Kết thúc thời gian làm việc";
        if ("DESIGN_START".equals(type)) return "✓ Bắt đầu thiết kế";
        if ("DESIGN_END".equals(type)) return "✓ Kết thúc thiết kế";
        if ("CNC_START".equals(type)) return "✓ CNC bắt đầu";
        if ("CNC_END".equals(type)) return "✓ CNC kết thúc";
        return type;
    }
}
