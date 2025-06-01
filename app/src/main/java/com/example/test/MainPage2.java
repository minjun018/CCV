package com.example.test;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.api.Distribution;

public class MainPage2 extends AppCompatActivity {
    ImageView ivSettings;
    LinearLayout co_layout, beat_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page2);

        ivSettings=findViewById(R.id.ivSettings);
        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage2.this, Settings.class);
                startActivity(intent);
            }
        });
        co_layout = findViewById(R.id.co_layout);
        beat_layout = findViewById(R.id.beat_layout);

        co_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "일산화탄소 농도 위험!!";
                String msg = "일산화탄소 농도가 일정치를 초과했습니다.";
                createNotification(title, msg);
            }
        });
        beat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "심박수 위험!!";
                String msg = "심박수가 일정치를 초과했습니다.";
                createNotification(title, msg);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void createNotification(String title, String msg) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.drawable.home_24px);
        builder.setContentTitle(title);
        builder.setContentText(msg);

        builder.setColor(Color.RED);
        // 사용자가 탭을 클릭하면 자동 제거
        builder.setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }
}