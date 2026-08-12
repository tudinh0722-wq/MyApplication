package com.example.chamcong;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.concurrent.Executors;

public class TagsFragment extends Fragment {

    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
        this.container = new LinearLayout(getContext());
        this.container.setOrientation(LinearLayout.VERTICAL);
        this.container.setPadding(40, 40, 40, 40);
        scrollView.addView(this.container);
        return scrollView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTags();
    }

    private void loadTags() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            List<TagEntity> tags = db.tagDao().getAll();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    container.removeAllViews();
                    TextView title = new TextView(getContext());
                    title.setText("DANH SÁCH THẺ\n");
                    title.setTextSize(18);
                    title.setTextColor(android.graphics.Color.BLACK);
                    container.addView(title);

                    for (TagEntity t : tags) {
                        addTagView(t);
                    }
                });
            }
        });
    }

    private void addTagView(TagEntity t) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 20, 0, 40);

        TextView info = new TextView(getContext());
        info.setText("TAG-00" + t.id + ": " + t.name + "\nUID: " + t.uid + "\nLoại: " + t.eventType);
        info.setTextSize(14);
        row.addView(info);

        Button btnReplace = new Button(getContext());
        btnReplace.setText("Thay thẻ mới");
        btnReplace.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).startTagUpdate(t.id, t.name);
            }
        });
        row.addView(btnReplace);

        container.addView(row);
    }
}