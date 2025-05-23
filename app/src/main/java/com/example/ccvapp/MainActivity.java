package com.example.ccvapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean boolean_login = true;

        // SharedPreferences로 최초 실행 여부 확인
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        if (isFirstRun) {
            // 👉 최초 실행: activity_main.xml 띄우기
            setContentView(R.layout.activity_main);

            // 최초 실행 상태를 false로 저장
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        } else {
            if(boolean_login){
                // 👉 이후 실행: main_page.xml 띄우기, 로그인 되어있으면
                setContentView(R.layout.main_page);
            }else{
                //로그인 안 되어있으면 activity_main.xml 띄우기
                setContentView(R.layout.activity_main);
            }
        }

        // 시스템 인셋 처리 (공통)
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
