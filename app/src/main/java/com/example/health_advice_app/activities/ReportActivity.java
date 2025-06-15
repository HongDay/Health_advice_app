package com.example.health_advice_app.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.health_advice_app.Data.MyApp;
import com.example.health_advice_app.Data.MyData;
import com.example.health_advice_app.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // 1) 저장된 MyData 꺼내오기
        MyApp app = (MyApp) getApplication();
        List<MyData> dataList = new ArrayList<>(app.getMyDataList());

        // 2) 비어 있으면 테스트용 더미 데이터 생성
        if (dataList.isEmpty()) {
            long todayStart = System.currentTimeMillis() - (System.currentTimeMillis() % (24 * 3600_000L));
            // 05:00 ~ 08:30 (3.5h = 210m)
            dataList.add(new MyData(
                    todayStart + 5 * 3600_000L,
                    210,
                    "Sleep"
            ));
            // 09:00 ~ 10:15 (1.25h = 75m)
            dataList.add(new MyData(
                    todayStart + 9 * 3600_000L,
                    75,
                    "In Class"
            ));
            // 10:30 ~ 12:00 (1.5h = 90m)
            dataList.add(new MyData(
                    todayStart + 10 * 3600_000L + 30 * 60_000L,
                    90,
                    "Study"
            ));
            // 12:30 ~ 13:00 (0.5h = 30m)
            dataList.add(new MyData(
                    todayStart + 12 * 3600_000L + 30 * 60_000L,
                    30,
                    "Workout"
            ));
        }

        // 3) 활동별 총 분 집계
        Map<String, Integer> sumsMin = new HashMap<>();
        for (MyData d : dataList) {
            sumsMin.put(
                    d.getContent(),
                    sumsMin.getOrDefault(d.getContent(), 0) + d.getDurationMinutes()
            );
        }

        // 4) DataPoint[] 로 변환 (Y 값은 “시간” 단위로)
        String[] labels = sumsMin.keySet().toArray(new String[0]);
        DataPoint[] userPoints = new DataPoint[labels.length];
        for (int i = 0; i < labels.length; i++) {
            double hours = sumsMin.get(labels[i]) / 60.0;  // 분 → 시간
            userPoints[i] = new DataPoint(i, hours);
        }

        BarGraphSeries<DataPoint> userSeries = new BarGraphSeries<>(userPoints);
        userSeries.setColor(getColor(R.color.purple_200));
        userSeries.setTitle("You");

        // 5) 평균 더미 (시간 단위)
        double[] avgH = {150/60.0, 210/60.0, 75/60.0, 30/60.0};
        DataPoint[] avgPoints = new DataPoint[labels.length];
        for (int i = 0; i < labels.length; i++) {
            avgPoints[i] = new DataPoint(i, avgH[i % avgH.length]);
        }
        BarGraphSeries<DataPoint> avgSeries = new BarGraphSeries<>(avgPoints);
        avgSeries.setColor(getColor(R.color.teal_200));
        avgSeries.setTitle("20s Univ. Avg");

        // 6) 그래프 세팅
        GraphView graph = findViewById(R.id.barGraph);
        graph.addSeries(userSeries);
        graph.addSeries(avgSeries);

        graph.getGridLabelRenderer().setHorizontalAxisTitle("Activity");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Hours");

        // X축에 활동명 라벨만
        StaticLabelsFormatter formatter = new StaticLabelsFormatter(graph);
        formatter.setHorizontalLabels(labels);
        graph.getGridLabelRenderer().setLabelFormatter(formatter);
        graph.getGridLabelRenderer().setNumHorizontalLabels(labels.length);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);

        // 7) 아래 OK 버튼 → 종료
        Button btnOk = findViewById(R.id.btnOkReport);
        btnOk.setOnClickListener(v -> finish());
    }
}