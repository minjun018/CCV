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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

public class MainPage2 extends AppCompatActivity {
    ImageView ivSettings;
    LinearLayout co_layout, beat_layout;
    TextView tv_co_value, tv_hr_value;

    private DatabaseReference gasDataRef, heartDataRef;
    private ValueEventListener gasValueListener, heartValueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page2);

        ivSettings = findViewById(R.id.ivSettings);
        co_layout = findViewById(R.id.co_layout);
        beat_layout = findViewById(R.id.beat_layout);
        tv_co_value = findViewById(R.id.tv_co_value);
        tv_hr_value = findViewById(R.id.tv_hr_value);

        ivSettings.setOnClickListener(view -> {
            Intent intent = new Intent(MainPage2.this, Settings.class);
            startActivity(intent);
        });

        co_layout.setOnClickListener(view -> {
            String title = "일산화탄소 농도 위험!!";
            String msg = "일산화탄소 농도가 일정치를 초과했습니다.";
            createNotification(title, msg);
        });

        beat_layout.setOnClickListener(view -> {
            String title = "심박수 위험!!";
            String msg = "심박수가 일정치를 초과했습니다.";
            createNotification(title, msg);
        });

        // Firebase 참조 설정
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gasDataRef = database.getReference("gassensor_data");
        heartDataRef = database.getReference("hreartsensor_data");

        // 일산화탄소 데이터 가져오기
        Query lastGasValueQuery = gasDataRef.orderByKey().limitToLast(1);
        gasValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Object val = child.getValue();
                        if (val instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) val;
                            Object coValue = map.get("value");
                            tv_co_value.setText(coValue != null ? String.valueOf(coValue) : "CO 없음");
                        } else {
                            tv_co_value.setText("형식 오류");
                        }
                        break;
                    }
                } else {
                    tv_co_value.setText("데이터 없음");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tv_co_value.setText("오류: " + error.getMessage());
            }
        };
        lastGasValueQuery.addValueEventListener(gasValueListener);

        // 심박수 데이터 가져오기
        Query lastHeartValueQuery = heartDataRef.orderByKey().limitToLast(1);
        heartValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Object val = child.getValue();
                        if (val instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) val;
                            Object hrValue = map.get("value");
                            tv_hr_value.setText(hrValue != null ? String.valueOf(hrValue) : "HR 없음");
                        } else {
                            tv_hr_value.setText("형식 오류");
                        }
                        break;
                    }
                } else {
                    tv_hr_value.setText("데이터 없음");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tv_hr_value.setText("오류: " + error.getMessage());
            }
        };
        lastHeartValueQuery.addValueEventListener(heartValueListener);

        // 시스템 인셋 처리
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
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT)
            );
        }
        notificationManager.notify(1, builder.build());
    }

    // 리스너 제거 (onStop에서 메모리 누수 방지)
    @Override
    protected void onStop() {
        super.onStop();
        if (gasValueListener != null) gasDataRef.removeEventListener(gasValueListener);
        if (heartValueListener != null) heartDataRef.removeEventListener(heartValueListener);
    }
}
