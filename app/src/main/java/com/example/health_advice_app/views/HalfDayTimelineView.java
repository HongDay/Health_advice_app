package com.example.health_advice_app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.example.health_advice_app.Data.MyData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Calendar;
import java.util.Objects;

public class HalfDayTimelineView extends View {
    // 시작 시각: 5시
    private static final int START_HOUR = 0;
    // 몇 시간치 표시할지 (5시→13시 = 8시간)
    private static final int VISIBLE_HOURS = 12;
    private static final int TOTAL_MINUTES = VISIBLE_HOURS * 60;
    // 그리드 간격 (분)
    private static final int INTERVAL_MIN = 15;
    private static final int STEPS = TOTAL_MINUTES / INTERVAL_MIN; // 32

    private long baseTime;      // 밀리초 단위: “오늘 5시 0분”으로 세팅
    private List<MyData> events;

    private Paint gridPaint;
    private Paint textPaint;
    private Paint blockPaint;
    private float leftLabelWidth;

    public HalfDayTimelineView(Context context) {
        super(context);
        init(context);
    }
    public HalfDayTimelineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public HalfDayTimelineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context ctx) {
        // 기본 baseTime = 오늘 START_HOUR:00
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, START_HOUR);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        baseTime = c.getTimeInMillis();

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(2);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_SP,
                        12,
                        ctx.getResources().getDisplayMetrics()
                )
        );

        blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // 왼쪽 레이블 너비: 약 60dp
        leftLabelWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                40,
                ctx.getResources().getDisplayMetrics()
        );
    }

    /** 표시 시작 시각(밀리초)만 바꾸고 싶으면 호출 */
    public void setBaseTime(long ts) {
        this.baseTime = ts;
        invalidate();
    }

    /** 이벤트 리스트만 바뀌면 호출 */
    public void setEvents(List<MyData> ev) {
        if (ev != null) {
            events = new ArrayList<>(ev);
            Collections.sort(events, Comparator.comparingLong(MyData::getTimestamp));
        } else {
            events = null;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float contentW = w - leftLabelWidth;
        float stepH = h / STEPS;

        // 1) 배경 그리드 + “정시” 라벨
        for (int i = 0; i <= STEPS; i++) {
            float y = i * stepH;
            canvas.drawLine(leftLabelWidth, y, w, y, gridPaint);

            int minuteOffset = i * INTERVAL_MIN;      // 0,15,30,45...
            if (minuteOffset % 60 == 0) {             // 정시(00분)만 라벨
                int hour = (START_HOUR + minuteOffset/60) % 24;
                String lbl = String.format("%2d:00", hour);
                float tx = 0;
                float ty = y + textPaint.getTextSize()/2 + 5;
                canvas.drawText(lbl, tx, ty, textPaint);
            }
        }

        // 2) 이벤트 블록
        if (events != null) {
            for (int idx = 0; idx < events.size(); idx++) {
                MyData d = events.get(idx);
                if (Objects.equals(d.getContent(), "Other")) continue;

                int startMin = (int)(d.getTimestamp() / 60);
                if (startMin < 0 || startMin > TOTAL_MINUTES) continue;

                int durMin = d.getDurationMinutes();

                // 색상 매핑
                switch (d.getContent()) {
                    case "Sleep":    blockPaint.setColor(Color.parseColor("#90CAF9")); break;
                    case "Workout":  blockPaint.setColor(Color.parseColor("#A5D6A7")); break;
                    case "Study":    blockPaint.setColor(Color.parseColor("#F48FB1")); break;
                    case "In Class": blockPaint.setColor(Color.parseColor("#FFE082")); break;
                    default:         blockPaint.setColor(Color.LTGRAY);              break;
                }

                float top    = (startMin / (float)INTERVAL_MIN) * stepH;
                float bottom = top + (durMin / (float)INTERVAL_MIN) * stepH;

                canvas.drawRect(
                        leftLabelWidth+2, top+2,
                        w-2,             bottom-2,
                        blockPaint
                );

                // 중앙에 텍스트
                textPaint.setColor(Color.BLACK);
                float textX = leftLabelWidth + 10;
                float textY = top + (bottom-top)/2 + textPaint.getTextSize()/2;
                canvas.drawText(d.getContent(), textX, textY, textPaint);
            }
        }
    }
}