package com.example.health_advice_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SignInActivity
 *  - 첫 번째 화면: Sign In 버튼 클릭 시 UserInfoActivity로 이동
 */
public class SignInActivity extends AppCompatActivity {

    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnSignIn = findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, UserInfoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}