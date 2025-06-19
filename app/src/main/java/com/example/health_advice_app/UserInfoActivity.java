package com.example.health_advice_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.health_advice_app.views.WeekTimetableView;

/**
 * UserInfoActivity
 *  - 두 번째 화면: 이름, 생년월일, 학교, 키, 몸무게 입력
 *  - WeekTimetableView 로 시간표를 터치/드래그하여 지정
 *  - 모든 입력이 완료되고 시간표에서 최소 하나 선택되면 Continue 버튼 활성화
 *  - Continue 클릭 시 CSV 저장 후 AgreementActivity 로 이동
 */
public class UserInfoActivity extends AppCompatActivity {

    // 여기에 뷰를 참조할 수 있도록 변수(필드)를 선언해야 합니다.
    private EditText etName;
    private EditText etBirthday;
    private EditText etSchool;
    private EditText etHeight;
    private EditText etWeight;
    private WeekTimetableView weekTimetableView;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 반드시 이 순서: (1) 레이아웃을 먼저 inflate → (2) findViewById
        setContentView(R.layout.activity_user_info);

        // (2) 레이아웃 뷰 바인딩
        etName = findViewById(R.id.et_name);
        etBirthday = findViewById(R.id.et_birthday);
        etSchool = findViewById(R.id.et_school);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        weekTimetableView = findViewById(R.id.weekTimetableView);
        btnContinue = findViewById(R.id.btn_userinfo_continue);

        // 모든 EditText에 TextWatcher를 달아서 텍스트가 바뀔 때마다 체크
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkAllInputs();
            }
        };
        etName.addTextChangedListener(watcher);
        etBirthday.addTextChangedListener(watcher);
        etSchool.addTextChangedListener(watcher);
        etHeight.addTextChangedListener(watcher);
        etWeight.addTextChangedListener(watcher);

        // WeekTimetableView 에도 선택 변경 리스너 등록
        weekTimetableView.setOnSelectionChangeListener(new WeekTimetableView.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged() {
                checkAllInputs();
            }
        });

        // Continue 버튼 클릭 시 CSV 저장하고 다음 화면으로 이동
        btnContinue.setOnClickListener(v -> {
            weekTimetableView.exportToCsv(UserInfoActivity.this);
            Intent intent = new Intent(UserInfoActivity.this, com.example.health_advice_app.AgreementActivity.class);
            startActivity(intent);
            finish();
        });

        // 최초 실행 시에는 버튼 비활성화 상태로 시작
        checkAllInputs();
    }

    /**
     * 이름, 생년월일, 학교, 키, 몸무게가 모두 비어 있지 않고
     * WeekTimetableView 에서 하나라도 셀이 선택되면 Continue 버튼 활성화
     */
    private void checkAllInputs() {
        String name = etName.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String school = etSchool.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();

        boolean allFilled = !name.isEmpty()
                && !birthday.isEmpty()
                && !school.isEmpty()
                && !height.isEmpty()
                && !weight.isEmpty();

        boolean timetableSelected = weekTimetableView.hasAnySelection();

        if (allFilled && timetableSelected) {
            btnContinue.setEnabled(true);
            btnContinue.setBackgroundTintList(
                    getResources().getColorStateList(R.color.colorPrimary)
            );
        } else {
            btnContinue.setEnabled(false);
            btnContinue.setBackgroundTintList(
                    getResources().getColorStateList(R.color.disabledButton)
            );
        }
    }
}