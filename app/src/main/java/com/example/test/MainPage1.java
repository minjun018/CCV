package com.example.test;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainPage1 extends AppCompatActivity {
    LinearLayout location_field, co_layout, beat_layout;
    ImageView ivSettings;
    TextView fill_hr, fill_co, location;

    private DatabaseReference gpsDataRef, gasDataRef, heartDataRef;
    private ValueEventListener gpsValueListener, gasValueListener, heartValueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page1);

        // View 초기화
        location_field = findViewById(R.id.location_field);
        ivSettings = findViewById(R.id.ivSettings);
        fill_hr = findViewById(R.id.fill_hr);
        fill_co = findViewById(R.id.fill_co);
        location = findViewById(R.id.location);
        co_layout = findViewById(R.id.co_layout);
        beat_layout = findViewById(R.id.beat_layout);

        // Firebase Realtime Database 참조
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gpsDataRef = database.getReference("GPSsensor_data");
        gasDataRef = database.getReference("gassensor_data");
        heartDataRef = database.getReference("hreartsensor_data");

        // MainPage1.java - onCreate()에 추가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 설정 아이콘 클릭 시 Settings 화면으로 이동
        ivSettings.setOnClickListener(view -> {
            Intent intent = new Intent(MainPage1.this, Settings.class);
            startActivity(intent);
        });

        // 안전 장소 이탈 알림
        location_field.setOnClickListener(view -> {
            String title = "사용자 안전 장소 이탈!!";
            String msg = "사용자가 2시간 이상 안전 장소를 이탈했습니다.";
            createNotification(title, msg);
        });

        // 일산화탄소 알림
        co_layout.setOnClickListener(view -> {
            String title = "일산화탄소 농도 위험!!";
            String msg = "일산화탄소 농도가 일정치를 초과했습니다.";
            createNotification(title, msg);
        });

        // 심박수 알림
        beat_layout.setOnClickListener(view -> {
            String title = "심박수 위험 수치";
            String msg = "심박수가 위험 수치에 도달했습니다.";
            createNotification(title, msg);
        });

        // 심박수 데이터 가져오기
        Query lastHeartValueQuery = heartDataRef.orderByKey().limitToLast(1);
        heartValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Object childValue = childSnapshot.getValue();
                        if (childValue instanceof Map) {
                            Map<String, Object> data = (Map<String, Object>) childValue;
                            Object hrValue = data.get("value");
                            fill_hr.setText(hrValue != null ? String.valueOf(hrValue) : "HR value null");
                        } else {
                            fill_hr.setText("HR format error");
                        }
                        break;
                    }
                } else {
                    fill_hr.setText("No HR data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fill_hr.setText("HR error: " + error.getMessage());
            }
        };
        lastHeartValueQuery.addValueEventListener(heartValueListener);

        // 일산화탄소 데이터 가져오기
        Query lastGasValueQuery = gasDataRef.orderByKey().limitToLast(1);
        gasValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Object childValue = childSnapshot.getValue();
                        if (childValue instanceof Map) {
                            Map<String, Object> data = (Map<String, Object>) childValue;
                            Object coValue = data.get("value");
                            fill_co.setText(coValue != null ? String.valueOf(coValue) : "CO value null");
                        } else {
                            fill_co.setText("CO format error");
                        }
                        break;
                    }
                } else {
                    fill_co.setText("No CO data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fill_co.setText("CO error: " + error.getMessage());
            }
        };
        lastGasValueQuery.addValueEventListener(gasValueListener);



        // 시스템 인셋 처리 (소프트 키보드 등)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 알림 생성 함수
    private void createNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "default_channel_id";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Default Channel Description");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.home_24px) // 알림 아이콘 설정
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    // 리스너 해제 (메모리 누수 방지)
    @Override
    protected void onStop() {
        super.onStop();
        if (heartValueListener != null) heartDataRef.removeEventListener(heartValueListener);
        if (gasValueListener != null) gasDataRef.removeEventListener(gasValueListener);
        if (gpsValueListener != null) gpsDataRef.removeEventListener(gpsValueListener);
    }
}
