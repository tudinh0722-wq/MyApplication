package com.example.chamcong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimelineView extends View {

    private Paint paintWork, paintDesign, paintCnc, paintGrid, paintText;
    private long dayStart, dayEnd;
    private List<Long[]> workPairs = new ArrayList<>();
    private List<Long[]> designPairs = new ArrayList<>();
    private List<Long[]> cncPairs = new ArrayList<>();
    
    private long activeWork = -1, activeDesign = -1, activeCnc = -1;

    public TimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintWork = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintWork.setColor(ContextCompat.getColor(getContext(), R.color.work_color));

        paintDesign = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDesign.setColor(ContextCompat.getColor(getContext(), R.color.design_color));

        paintCnc = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintCnc.setColor(ContextCompat.getColor(getContext(), R.color.cnc_color));

        paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGrid.setColor(ContextCompat.getColor(getContext(), R.color.divider));
        paintGrid.setStrokeWidth(2f);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(ContextCompat.getColor(getContext(), R.color.label_gray));
        paintText.setTextSize(24f);

        // Mặc định hiển thị từ 8h đến 18h cho Preview
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        dayStart = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 18);
        dayEnd = cal.getTimeInMillis();
    }

    public void setData(List<Long[]> work, List<Long[]> design, List<Long[]> cnc, 
                        long aWork, long aDesign, long aCnc) {
        this.workPairs = work;
        this.designPairs = design;
        this.cncPairs = cnc;
        this.activeWork = aWork;
        this.activeDesign = aDesign;
        this.activeCnc = aCnc;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (dayEnd <= dayStart) return;

        float pixelsPerMs = w / (float) (dayEnd - dayStart);

        // Vẽ vạch giờ
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dayStart);
        while (cal.getTimeInMillis() < dayEnd) {
            float x = (cal.getTimeInMillis() - dayStart) * pixelsPerMs;
            canvas.drawLine(x, 0, x, h - 40, paintGrid);
            canvas.drawText(cal.get(Calendar.HOUR_OF_DAY) + ":00", x + 5, h - 10, paintText);
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        drawBlocks(canvas, workPairs, activeWork, 10, 40, paintWork, pixelsPerMs);
        drawBlocks(canvas, designPairs, activeDesign, 45, 75, paintDesign, pixelsPerMs);
        drawBlocks(canvas, cncPairs, activeCnc, 80, 110, paintCnc, pixelsPerMs);
    }

    private void drawBlocks(Canvas canvas, List<Long[]> pairs, long active, int top, int bottom, Paint paint, float scale) {
        for (Long[] p : pairs) {
            float left = (p[0] - dayStart) * scale;
            float right = (p[1] - dayStart) * scale;
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }
}