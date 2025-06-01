package com.example.health_advice_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

/**
 * AgreementActivity
 *  - 세 번째 화면: 개인정보 수집 동의
 *  - 체크박스를 선택해야 Continue 버튼이 활성화
 *  - Continue 클릭 시 OptimizationActivity로 이동
 */
public class AgreementActivity extends AppCompatActivity {

    private CheckBox cbAgree;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        cbAgree = findViewById(R.id.cb_agree);
        btnContinue = findViewById(R.id.btn_agree_continue);

        cbAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btnContinue.setEnabled(isChecked);
                if (isChecked) {
                    btnContinue.setBackgroundTintList(
                            getResources().getColorStateList(R.color.colorPrimary)
                    );
                } else {
                    btnContinue.setBackgroundTintList(
                            getResources().getColorStateList(R.color.disabledButton)
                    );
                }
            }
        });

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(AgreementActivity.this, OptimizationActivity.class);
            startActivity(intent);
            finish();
        });
    }
}