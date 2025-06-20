package com.example.health_advice_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.health_advice_app.Data.MyApp;
import com.example.health_advice_app.Data.MyData;
import com.example.health_advice_app.EstimateActivity;
import com.example.health_advice_app.R;
import com.example.health_advice_app.views.HalfDayTimelineView;
import com.example.health_advice_app.views.HalfDayTimelineView2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimetablePreviewActivity extends AppCompatActivity {

    private HalfDayTimelineView halfView;
    private HalfDayTimelineView2 halfView2;
    private Button             btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_preview);

        halfView = findViewById(R.id.halfDayTimelineView);
        halfView2 = findViewById(R.id.halfDayTimelineView2);
        btnOk    = findViewById(R.id.btnOk);

        MyApp app = (MyApp) getApplication();
        List<MyData> events = app.getMyDataList();

//        app.calDuration(18000, "Sleep");
//        app.calDuration(18060, "Sleep");
//        app.calDuration(18120, "Sleep");
//        app.calDuration(18180, "Sleep");
//
//        app.calDuration(18240, "Other");
//
//        app.calDuration(48000, "Sleep");
//        app.calDuration(48060, "Sleep");
//        app.calDuration(48120, "Sleep");
//        app.calDuration(48180, "Sleep");

        // 뷰에 이벤트 세팅
        halfView.setEvents(events);
        halfView2.setEvents(events);

        // OK 누르면 ReportActivity 로 이동
        btnOk.setOnClickListener(v -> {
            // 실제 앱에선 app.setMyDataList(events) 로 유지할 수도 있고…
            startActivity(new Intent(this, EstimateActivity.class));
        });
    }
}