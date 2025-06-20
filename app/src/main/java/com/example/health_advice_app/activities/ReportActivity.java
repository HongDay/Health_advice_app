package com.example.health_advice_app.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.health_advice_app.Data.MyApp;
import com.example.health_advice_app.Data.MyData;
import com.example.health_advice_app.R;
import com.example.health_advice_app.databinding.ActivityReportBinding;
import com.example.health_advice_app.databinding.ItemCardBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private ActivityReportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 저장된 MyData 꺼내오기
        MyApp app = (MyApp) getApplication();
        List<MyData> dataList = new ArrayList<>(app.getMyDataList());

        // Sleep 리포트 화면 구성
        binding.cardSleep.titleText.setText("Sleep");
        float standardSleep = 7 * 60; // 7시간
        // 총 숙면시간 계산
        float totalSleep = 0;
        for (MyData data : dataList) {
            if("Sleep".equals(data.getContent())){
                totalSleep += data.getDurationMinutes();
            }
        }
        float finalTotalSleep = totalSleep;
        binding.cardSleep.graphContainer.post(() -> {
            int containerWidth = binding.cardSleep.graphContainer.getWidth();
            float ratio = finalTotalSleep / standardSleep;
            int progressWidth = (int)(containerWidth * ratio);

            ViewGroup.LayoutParams params = binding.cardSleep.progressBar.getLayoutParams();
            params.width = progressWidth;
            binding.cardSleep.progressBar.setLayoutParams(params);
        });
        // 기준에 맞춘 조언 메세지
        if (totalSleep < 0.6 * standardSleep) {
            binding.cardSleep.tvExplain.setText(getString(R.string.Sleep_txt1));
            binding.cardSleep.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_red));
            binding.cardSleep.qualityText.setText("Too Lack");
        } else if (totalSleep < 0.9 * standardSleep) {
            binding.cardSleep.tvExplain.setText(getString(R.string.Sleep_txt2));
            binding.cardSleep.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_yellow));
            binding.cardSleep.qualityText.setText("Still Not Enough");
        } else{
            binding.cardSleep.tvExplain.setText(getString(R.string.Sleep_txt3));
            binding.cardSleep.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_green));
            binding.cardSleep.qualityText.setText("Enough");
        }

        // Study 리포트 화면 구성
        binding.cardStudy.titleText.setText("Study");
        float standardStudy = 4 * 60; // 4시간
        // 총 숙면시간 계산
        float totalStudy = 0;
        for (MyData data : dataList) {
            if("Study".equals(data.getContent())){
                totalStudy += data.getDurationMinutes();
            }
        }
        float finalTotalStudy = totalStudy;
        binding.cardStudy.graphContainer.post(() -> {
            int containerWidth = binding.cardStudy.graphContainer.getWidth();
            float ratio = finalTotalStudy / standardStudy;
            int progressWidth = (int)(containerWidth * ratio);

            ViewGroup.LayoutParams params = binding.cardStudy.progressBar.getLayoutParams();
            params.width = progressWidth;
            binding.cardStudy.progressBar.setLayoutParams(params);
        });
        // 기준에 맞춘 조언 메세지
        if (totalStudy < 0.6 * standardStudy) {
            binding.cardStudy.tvExplain.setText(getString(R.string.Study_txt1));
            binding.cardStudy.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_red));
            binding.cardStudy.qualityText.setText("Too Lack");
        } else if (totalStudy < 0.9 * standardStudy) {
            binding.cardStudy.tvExplain.setText(getString(R.string.Study_txt2));
            binding.cardStudy.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_yellow));
            binding.cardStudy.qualityText.setText("Still Not Enough");
        } else{
            binding.cardStudy.tvExplain.setText(getString(R.string.Study_txt3));
            binding.cardStudy.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_green));
            binding.cardStudy.qualityText.setText("Enough");
        }

        // WorkOut 리포트 화면 구성
        binding.cardWorkout.titleText.setText("WorkOut");
        float standardWork = 90; // 1시간 30분
        // 총 운동시간 계산
        float totalWork = 0;
        for (MyData data : dataList) {
            if("Workout".equals(data.getContent())){
                totalWork += data.getDurationMinutes();
            }
        }
        float finalTotalWork = totalWork;
        binding.cardWorkout.graphContainer.post(() -> {
            int containerWidth = binding.cardWorkout.graphContainer.getWidth();
            float ratio = finalTotalWork / standardWork;
            int progressWidth = (int)(containerWidth * ratio);

            ViewGroup.LayoutParams params = binding.cardWorkout.progressBar.getLayoutParams();
            params.width = progressWidth;
            binding.cardWorkout.progressBar.setLayoutParams(params);
        });
        // 기준에 맞춘 조언 메세지
        if (totalWork < 0.6 * standardWork) {
            binding.cardWorkout.tvExplain.setText(getString(R.string.Work_txt1));
            binding.cardWorkout.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_red));
            binding.cardWorkout.qualityText.setText("Too Lack");
        } else if (totalWork < 0.85 * standardWork) {
            binding.cardWorkout.tvExplain.setText(getString(R.string.Work_txt2));
            binding.cardWorkout.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_yellow));
            binding.cardWorkout.qualityText.setText("Still Not Enough");
        } else{
            binding.cardWorkout.tvExplain.setText(getString(R.string.Work_txt3));
            binding.cardWorkout.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_green));
            binding.cardWorkout.qualityText.setText("Enough");
        }

        // Class 리포트 화면 구성
        binding.cardClass.titleText.setText("Class");
        float standardClass = 180; // 3시간. 임의 설정.
        // 총 운동시간 계산
        float totalClass = 0;
        for (MyData data : dataList) {
            if("In Class".equals(data.getContent())){
                totalClass += data.getDurationMinutes();
            }
        }
        float finalTotalClass = totalClass;
        binding.cardClass.graphContainer.post(() -> {
            int containerWidth = binding.cardClass.graphContainer.getWidth();
            float ratio = finalTotalClass / standardClass;
            int progressWidth = (int)(containerWidth * ratio);

            ViewGroup.LayoutParams params = binding.cardClass.progressBar.getLayoutParams();
            params.width = progressWidth;
            binding.cardClass.progressBar.setLayoutParams(params);
        });
        // 기준에 맞춘 조언 메세지
        if (totalClass < 0.6 * standardClass) {
            binding.cardClass.tvExplain.setText(getString(R.string.Class_txt1));
            binding.cardClass.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_red));
            binding.cardClass.qualityText.setText("Too Lack");
        } else if (totalClass < 0.9 * standardClass) {
            binding.cardClass.tvExplain.setText(getString(R.string.Class_txt2));
            binding.cardClass.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_yellow));
            binding.cardClass.qualityText.setText("Still Not Enough");
        } else{
            binding.cardClass.tvExplain.setText(getString(R.string.Class_txt3));
            binding.cardClass.colorDot.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_green));
            binding.cardClass.qualityText.setText("Enough");
        }


        // 7) 아래 OK 버튼 → 종료
        Button btnOk = findViewById(R.id.btnOkReport);
        btnOk.setOnClickListener(v -> finish());
    }
}