package com.example.health_advice_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.health_advice_app.Data.MyApp;
import com.example.health_advice_app.Data.MyData;
import com.example.health_advice_app.R;
import com.example.health_advice_app.views.HalfDayTimelineView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimetablePreviewActivity extends AppCompatActivity {

    private HalfDayTimelineView halfView;
    private Button             btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_preview);

        halfView = findViewById(R.id.halfDayTimelineView);
        btnOk    = findViewById(R.id.btnOk);

        MyApp app = (MyApp) getApplication();
        List<MyData> events = app.getMyDataList();

        // ▶ 빈 리스트면 더미 데이터 생성
        if (events == null || events.isEmpty()) {
            events = new ArrayList<>();
            Calendar c = Calendar.getInstance();
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            // base: 오늘 5시 0분
            c.set(Calendar.HOUR_OF_DAY, 5);
            c.set(Calendar.MINUTE, 0);
            long t5 = c.getTimeInMillis();

            // 5:00 ~ 8:30 Sleep (210분)
            events.add(new MyData(t5, 210, "Sleep"));

            // 9:00 ~ 10:15 In Class (75분)
            c.set(Calendar.HOUR_OF_DAY, 9);
            c.set(Calendar.MINUTE, 0);
            long t9 = c.getTimeInMillis();
            events.add(new MyData(t9, 75, "In Class"));

            // 10:30 ~ 12:00 Study (90분)
            c.set(Calendar.HOUR_OF_DAY, 10);
            c.set(Calendar.MINUTE, 30);
            long t1030 = c.getTimeInMillis();
            events.add(new MyData(t1030, 90, "Study"));

            // 12:30 ~ 13:00 Workout (30분)
            c.set(Calendar.HOUR_OF_DAY, 12);
            c.set(Calendar.MINUTE, 30);
            long t1230 = c.getTimeInMillis();
            events.add(new MyData(t1230, 30, "Workout"));
        }

        // 뷰에 이벤트 세팅
        halfView.setEvents(events);

        // OK 누르면 ReportActivity 로 이동
        btnOk.setOnClickListener(v -> {
            // 실제 앱에선 app.setMyDataList(events) 로 유지할 수도 있고…
            startActivity(new Intent(this, ReportActivity.class));
        });
    }
}