package com.example.health_advice_app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.health_advice_app.Data.MyData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

/**
 * WeekTimetableView
 *
 * - 위쪽에 요일 레이블(7개), 왼쪽에 시간 레이블(72개), 오른쪽에 7×72 그리드를 한 번에 그리는 커스텀 뷰
 * - 셀 크기(cellSizeDp), 레이블 크기(leftLabelDp, topLabelDp)는 dp 단위로 정의 가능
 * - 터치/드래그로 셀 선택/해제(toggle) 시 색상이 바뀜
 * - exportToCsv()를 호출하면 내부 저장소("timetable.csv")에 7×72 배치(0/1)로 저장
 */
public class WeekTimetableView extends View {

    // 7일 × 72행(6:00 AM ~ 12:00 AM, 15분 단위)
    private static final int DAYS = 7;
    private static final int ROWS = 72;

    // 기본 dp 값
    private static final float DEFAULT_CELL_DP = 40f;      // 셀 하나 크기
    private static final float DEFAULT_LEFT_LABEL_DP = 60f; // 왼쪽 시간 레이블 너비
    private static final float DEFAULT_TOP_LABEL_DP = 40f;  // 위쪽 요일 레이블 높이

    private final int leftLabelPx;  // 왼쪽 레이블 너비 (px)
    private final int topLabelPx;   // 위 레이블 높이 (px)
    private final int cellSizePx;   // 셀 크기 (px)

    private Paint textPaint;        // 레이블 텍스트 그리기용
    private Paint gridPaint;        // 그리드 선용
    private Paint selectedPaint;    // 선택된 셀 채우기용
    private Paint cellBgPaint;      // 일반 셀 배경용

    // 셀 선택 여부 저장: [row][day]
    private boolean[][] cellSelected = new boolean[ROWS][DAYS];

    // 드래그 시 마지막 터치한 행/열 (중복 토글 방지용)
    private int lastTouchedRow = -1;
    private int lastTouchedDay = -1;

    public void setEvents(List<MyData> list) {
    }

    // 선택 상태 변경 리스너
    public interface OnSelectionChangeListener {
        void onSelectionChanged();
    }
    private OnSelectionChangeListener selectionChangeListener;
    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    public WeekTimetableView(Context context) {
        this(context, null);
    }

    public WeekTimetableView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekTimetableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // dp → px 변환
        leftLabelPx = dpToPx(DEFAULT_LEFT_LABEL_DP);
        topLabelPx = dpToPx(DEFAULT_TOP_LABEL_DP);
        cellSizePx = dpToPx(DEFAULT_CELL_DP);
        initPaints();
    }

    private void initPaints() {
        // 레이블 텍스트용 페인트
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(dpToPx(12)); // 약 12sp 크기
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 그리드 선용 페인트
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dpToPx(1) / 2f);

        // 선택된 셀 배경용 페인트
        selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedPaint.setColor(Color.parseColor("#6FC3B5"));
        selectedPaint.setStyle(Paint.Style.FILL);

        // 기본 셀 배경용 (연한 회색)
        cellBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellBgPaint.setColor(Color.parseColor("#E5F0EE"));
        cellBgPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 전체 뷰 크기: (왼쪽 레이블 너비 + 7*셀크기) × (위 레이블 높이 + 72*셀크기)
        int measuredWidth = leftLabelPx + DAYS * cellSizePx;
        int measuredHeight = topLabelPx + ROWS * cellSizePx;
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDayLabels(canvas);
        drawTimeLabels(canvas);
        drawCells(canvas);
        drawGridLines(canvas);
    }

    private void drawDayLabels(Canvas canvas) {
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        float halfCell = cellSizePx / 2f;
        for (int d = 0; d < DAYS; d++) {
            float cx = leftLabelPx + d * cellSizePx + halfCell;
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textHeight = fm.descent - fm.ascent;
            float cy = (topLabelPx / 2f) + (textHeight / 2f) - fm.descent;
            canvas.drawText(days[d], cx, cy, textPaint);
        }
    }

    private void drawTimeLabels(Canvas canvas) {
        float cx = leftLabelPx / 2f;
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;

        for (int row = 0; row < ROWS; row++) {
            int totalMinutes = 6 * 60 + row * 15; // 6:00 AM + 15분 단위
            int hour = (totalMinutes / 60) % 24;
            int minute = totalMinutes % 60;
            String ampm = (hour < 12) ? "AM" : "PM";
            int hour12 = hour % 12;
            if (hour12 == 0) hour12 = 12;
            String timeLabel = String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, ampm);

            float cy = topLabelPx + row * cellSizePx + (cellSizePx / 2f)
                    + (textHeight / 2f) - fm.descent;
            canvas.drawText(timeLabel, cx, cy, textPaint);
        }
    }

    private void drawCells(Canvas canvas) {
        for (int row = 0; row < ROWS; row++) {
            for (int day = 0; day < DAYS; day++) {
                float left = leftLabelPx + day * cellSizePx;
                float top = topLabelPx + row * cellSizePx;
                float right = left + cellSizePx;
                float bottom = top + cellSizePx;
                // 기본 배경
                canvas.drawRect(left, top, right, bottom, cellBgPaint);
                // 선택된 셀 채우기
                if (cellSelected[row][day]) {
                    canvas.drawRect(left, top, right, bottom, selectedPaint);
                }
            }
        }
    }

    private void drawGridLines(Canvas canvas) {
        // 가로선
        for (int r = 0; r <= ROWS; r++) {
            float y = topLabelPx + r * cellSizePx;
            canvas.drawLine(leftLabelPx, y, leftLabelPx + DAYS * cellSizePx, y, gridPaint);
        }
        // 세로선
        for (int d = 0; d <= DAYS; d++) {
            float x = leftLabelPx + d * cellSizePx;
            canvas.drawLine(x, topLabelPx, x, topLabelPx + ROWS * cellSizePx, gridPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        // 레이블 영역(위/왼쪽)을 벗어난, 실제 셀 영역만 처리
        if (x < leftLabelPx || y < topLabelPx) {
            return true;
        }
        int day = (int) ((x - leftLabelPx) / cellSizePx);
        int row = (int) ((y - topLabelPx) / cellSizePx);
        if (day < 0 || day >= DAYS || row < 0 || row >= ROWS) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (lastTouchedRow != row || lastTouchedDay != day) {
                    toggleCell(row, day);
                    lastTouchedRow = row;
                    lastTouchedDay = day;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                lastTouchedRow = -1;
                lastTouchedDay = -1;
                break;
        }
        return true;
    }

    private void toggleCell(int row, int day) {
        cellSelected[row][day] = !cellSelected[row][day];
        invalidate();
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged();
        }
    }

    /**
     * CSV 출력: 내부 저장소(getFilesDir())에 "timetable.csv"로 저장
     * 각 행마다 7개 값(0/1) → 줄바꿈
     */
    public void exportToCsv(Context context) {
        try {
            File dir = context.getFilesDir();
            File csvFile = new File(dir, "timetable.csv");
            FileOutputStream fos = new FileOutputStream(csvFile, false);
            OutputStreamWriter writer = new OutputStreamWriter(fos);

            for (int r = 0; r < ROWS; r++) {
                StringBuilder sb = new StringBuilder();
                for (int d = 0; d < DAYS; d++) {
                    sb.append(cellSelected[r][d] ? "1" : "0");
                    if (d < DAYS - 1) sb.append(",");
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
            writer.flush();
            writer.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // dp → px 변환
    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    /**  외부에서 선택 여부 확인할 때 사용 */
    public boolean hasAnySelection() {
        for (int r = 0; r < ROWS; r++) {
            for (int d = 0; d < DAYS; d++) {
                if (cellSelected[r][d]) return true;
            }
        }
        return false;
    }
}