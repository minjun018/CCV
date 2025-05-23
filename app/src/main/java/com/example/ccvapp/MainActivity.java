package com.example.ccvapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;  // ← 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean boolean_login = false;
        mAuth = FirebaseAuth.getInstance();  // ← 초기화

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            // 최초 실행
            setContentView(R.layout.activity_main);

            // 최초 실행 상태 저장
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        } else {
            if(boolean_login){
                // 로그인 O
                setContentView(R.layout.main_page);
            } else {
                // 로그인 X
                setContentView(R.layout.activity_main);
            }
        }
        EditText etId = findViewById(R.id.et_id);
        EditText etPassword = findViewById(R.id.et_password);
        TextView tvTogglePassword = findViewById(R.id.tv_toggle_password);

        tvTogglePassword.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // 비밀번호 숨기기
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    tvTogglePassword.setText("클릭하여 비밀번호 보기");
                } else {
                    // 비밀번호 보이기
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    tvTogglePassword.setText("클릭하여 비밀번호 숨기기");
                }
                isPasswordVisible = !isPasswordVisible;

                // 커서가 끝으로 이동하도록
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        // ❗ setContentView 이후에 호출해야 함
        View btn_login = findViewById(R.id.btn_login);

        if (btn_login != null) {
            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = etId.getText().toString();
                    String password = etPassword.getText().toString();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent intent = new Intent(MainActivity.this, MainPage.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            });
        } else {
            Log.e("MainActivity", "btn_login is null. Check if the correct layout is loaded.");
        }


        // 인셋 설정
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            EdgeToEdge.enable(MainActivity.this);
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

}
