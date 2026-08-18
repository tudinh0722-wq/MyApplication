package com.example.chamcong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimelineView extends View {

    private Paint paintWork, paintDesign, paintCnc, paintGrid, paintText, paintBg;
    private long dayStart, dayEnd;
    private List<Long[]> workPairs   = new ArrayList<>();
    private List<Long[]> designPairs = new ArrayList<>();
    private List<Long[]> cncPairs    = new ArrayList<>();
    private long activeWork = -1, activeDesign = -1, activeCnc = -1;

    // Row layout (fraction of height)
    private static final float ROW_WORK_TOP    = 0.08f;
    private static final float ROW_WORK_BOT    = 0.38f;
    private static final float ROW_DESIGN_TOP  = 0.42f;
    private static final float ROW_DESIGN_BOT  = 0.65f;
    private static final float ROW_CNC_TOP     = 0.69f;
    private static final float ROW_CNC_BOT     = 0.92f;

    public TimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintWork = makePaint(ContextCompat.getColor(getContext(), R.color.work_color));
        paintDesign = makePaint(ContextCompat.getColor(getContext(), R.color.design_color));
        paintCnc = makePaint(ContextCompat.getColor(getContext(), R.color.cnc_color));

        paintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintGrid.setColor(ContextCompat.getColor(getContext(), R.color.divider));
        paintGrid.setStrokeWidth(1.5f);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setColor(ContextCompat.getColor(getContext(), R.color.label_gray));
        paintText.setTextSize(22f);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBg.setColor(ContextCompat.getColor(getContext(), R.color.divider));
        paintBg.setAlpha(80);

        // Default window: 8:00 – 18:00
        resetDayWindow();
    }

    private Paint makePaint(int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        return p;
    }

    private void resetDayWindow() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        dayStart = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 18);
        dayEnd = cal.getTimeInMillis();
    }

    public void setData(List<Long[]> work, List<Long[]> design, List<Long[]> cnc,
                        long aWork, long aDesign, long aCnc) {
        this.workPairs   = work   != null ? work   : new ArrayList<>();
        this.designPairs = design != null ? design : new ArrayList<>();
        this.cncPairs    = cnc   != null ? cnc    : new ArrayList<>();
        this.activeWork   = aWork;
        this.activeDesign = aDesign;
        this.activeCnc    = aCnc;
        recalcWindow();
        invalidate();
    }

    /** Tự động mở rộng cửa sổ thời gian nếu data vượt ra ngoài 8-18 */
    private void recalcWindow() {
        resetDayWindow();
        long now = System.currentTimeMillis();
        // Tìm min/max của tất cả event
        long min = dayStart, max = dayEnd;
        for (Long[] p : workPairs)   { min = Math.min(min, p[0]); max = Math.max(max, p[1]); }
        for (Long[] p : designPairs) { min = Math.min(min, p[0]); max = Math.max(max, p[1]); }
        for (Long[] p : cncPairs)    { min = Math.min(min, p[0]); max = Math.max(max, p[1]); }
        if (activeWork   != -1) max = Math.max(max, now);
        if (activeDesign != -1) max = Math.max(max, now);
        if (activeCnc    != -1) max = Math.max(max, now);

        // Snap to hour boundaries
        Calendar calMin = Calendar.getInstance(); calMin.setTimeInMillis(min);
        calMin.set(Calendar.MINUTE, 0); calMin.set(Calendar.SECOND, 0); calMin.set(Calendar.MILLISECOND, 0);
        Calendar calMax = Calendar.getInstance(); calMax.setTimeInMillis(max);
        calMax.set(Calendar.MINUTE, 0); calMax.set(Calendar.SECOND, 0); calMax.set(Calendar.MILLISECOND, 0);
        calMax.add(Calendar.HOUR_OF_DAY, 1);

        dayStart = calMin.getTimeInMillis();
        dayEnd   = calMax.getTimeInMillis();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (dayEnd <= dayStart || w == 0 || h == 0) return;

        float scale = w / (float)(dayEnd - dayStart);
        float textY = h - 6f;

        // Background track
        float trackPad = 4f;
        canvas.drawRoundRect(new RectF(0, h * ROW_WORK_TOP   - trackPad, w, h * ROW_WORK_BOT   + trackPad), 6, 6, paintBg);
        canvas.drawRoundRect(new RectF(0, h * ROW_DESIGN_TOP - trackPad, w, h * ROW_DESIGN_BOT + trackPad), 6, 6, paintBg);
        canvas.drawRoundRect(new RectF(0, h * ROW_CNC_TOP    - trackPad, w, h * ROW_CNC_BOT    + trackPad), 6, 6, paintBg);

        // Grid lines + hour labels
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dayStart);
        while (cal.getTimeInMillis() <= dayEnd) {
            float x = (cal.getTimeInMillis() - dayStart) * scale;
            canvas.drawLine(x, h * ROW_WORK_TOP, x, h * ROW_CNC_BOT, paintGrid);
            canvas.drawText(cal.get(Calendar.HOUR_OF_DAY) + ":00", x, textY, paintText);
            cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        long now = System.currentTimeMillis();

        // Vẽ blocks
        drawBlocks(canvas, workPairs,   activeWork,   now, h * ROW_WORK_TOP,   h * ROW_WORK_BOT,   paintWork,   scale);
        drawBlocks(canvas, designPairs, activeDesign, now, h * ROW_DESIGN_TOP, h * ROW_DESIGN_BOT, paintDesign, scale);
        drawBlocks(canvas, cncPairs,    activeCnc,    now, h * ROW_CNC_TOP,    h * ROW_CNC_BOT,    paintCnc,    scale);
    }

    private void drawBlocks(Canvas canvas,
                             List<Long[]> pairs, long activeStart, long now,
                             float top, float bottom,
                             Paint paint, float scale) {
        RectF rect = new RectF();
        float radius = 5f;
        for (Long[] p : pairs) {
            rect.set(clamp((p[0] - dayStart) * scale),
                     top,
                     clamp((p[1] - dayStart) * scale),
                     bottom);
            if (rect.width() > 0) canvas.drawRoundRect(rect, radius, radius, paint);
        }
        // Active (belum selesai)
        if (activeStart != -1) {
            Paint activePaint = new Paint(paint);
            activePaint.setAlpha(160);
            rect.set(clamp((activeStart - dayStart) * scale),
                     top,
                     clamp((now - dayStart) * scale),
                     bottom);
            if (rect.width() > 0) canvas.drawRoundRect(rect, radius, radius, activePaint);
        }
    }

    private float clamp(float x) {
        return Math.max(0, x);
    }
}
