package com.example.health_advice_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * OptimizationActivity
 *  - 네 번째 화면: 3일 최적화 기간 동의
 *  - 체크박스를 선택해야 Continue 버튼 활성화
 *  - Continue 클릭 시 화면 종료
 */
public class OptimizationActivity extends AppCompatActivity {

    private CheckBox cbOptAgree;
    private Button btnOptContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optimization);

        cbOptAgree = findViewById(R.id.cb_opt_agree);
        btnOptContinue = findViewById(R.id.btn_opt_continue);

        cbOptAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btnOptContinue.setEnabled(isChecked);
                if (isChecked) {
                    btnOptContinue.setBackgroundTintList(
                            getResources().getColorStateList(R.color.colorPrimary)
                    );
                } else {
                    btnOptContinue.setBackgroundTintList(
                            getResources().getColorStateList(R.color.disabledButton)
                    );
                }
            }
        });

        btnOptContinue.setOnClickListener(v -> {
            // 최종 완료 후 단순 종료
            Intent intent = new Intent(OptimizationActivity.this, com.example.health_advice_app.MainActivity.class);
            startActivity(intent);
        });
    }
}