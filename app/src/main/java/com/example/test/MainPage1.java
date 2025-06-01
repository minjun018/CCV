package com.example.test;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
// Import is already here
// import androidx.annotation.NonNull; // Redundant import, can be removed
// import android.util.Log; // Redundant import, can be removed
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query; // Query 사용을 위해 추가
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import java.util.Map; // Map 사용을 위해 추가

import java.util.Map;
public class MainPage1 extends AppCompatActivity {
    LinearLayout location_field,co_layout,beat_layout;
    ImageView ivSettings;
    TextView fill_hr; // Declared here
    private ValueEventListener sensorValueListener;
    private DatabaseReference sensorDataRef; // sensor_data 경로를 가리킬 Reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page1);

        // --- Realtime Database setup ---
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // 클래스 멤버 변수인 sensorDataRef를 초기화합니다.
        sensorDataRef = database.getReference("sensor_data");
        // --- End Realtime Database setup ---

        // ... (알림 권한, View 초기화, 클릭 리스너 등 기존 코드 유지) ...

        location_field = findViewById(R.id.location_field);
        ivSettings = findViewById(R.id.ivSettings);
        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage1.this, Settings.class);
                startActivity(intent);
            }
        });
        location_field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "사용자 안전 장소 이탈!!";
                String msg = "사용자가 2시간 이상 안전 장소를 이탈했습니다.";
                createNotification(title, msg);
            }
        });

        co_layout = findViewById(R.id.co_layout);
        co_layout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                String title = "일산화탄소 농도 위험!!";
                String msg = "일산화탄소 농도가 일정치를 초과했습니다.";
                createNotification(title, msg);
            }
        });
        beat_layout = findViewById(R.id.beat_layout);
        beat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "심박수 위험 수치";
                String msg = "심박수가 위험 수치에 도달했습니다.";
                createNotification(title, msg);
            }
        });


        fill_hr = findViewById(R.id.fill_hr); // TextView 초기화

        // --- Realtime Database Listener Setup ---
        // ValueEventListener 객체를 정의합니다. (이 부분은 이미 작성하셨던 코드)
        sensorValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("MY_APP", "onDataChange triggered.");
                Object dataSnapshotValue = dataSnapshot.getValue();
                Log.d("MY_APP", "DataSnapshot.getValue() result: " + dataSnapshotValue);

                if (dataSnapshot.hasChildren()) {
                    Log.d("MY_APP", "DataSnapshot has children.");
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String childKey = childSnapshot.getKey();
                        Object childValue = childSnapshot.getValue();

                        Log.d("MY_APP", "Processing child key: " + childKey);
                        Log.d("MY_APP", "Child data (value below random key): " + childValue);

                        if (childValue instanceof Map) {
                            Log.d("MY_APP", "Child data is a Map.");
                            Map<String, Object> data = (Map<String, Object>) childValue;

                            if (data.containsKey("value")) { // 키 이름이 'value'라고 가정
                                Object finalValueObj = data.get("value");
                                Log.d("MY_APP", "Found 'value' key. Its object value is: " + finalValueObj);

                                if (finalValueObj != null) {
                                    String finalValue = String.valueOf(finalValueObj);
                                    Log.d("MY_APP", "Successfully converted to String: " + finalValue);
                                    fill_hr.setText(finalValue); // <-- TextView 업데이트
                                    Log.d("MY_APP", "TextView updated with: " + finalValue);
                                } else {
                                    Log.d("MY_APP", "'value' key found, but its value is null. Setting TextView.");
                                    fill_hr.setText("Value is null.");
                                }
                            } else {
                                Log.d("MY_APP", "Child data is a Map, but does not contain the key 'value'. Setting TextView.");
                                fill_hr.setText("'value' key missing under " + childKey);
                            }
                        } else if (childValue != null) {
                            Log.d("MY_APP", "Child data is NOT a Map, but not null. Type: " + childValue.getClass().getName() + ". Setting TextView.");
                            fill_hr.setText("Format error under " + childKey);
                        }
                        else {
                            Log.d("MY_APP", "Child data is null under key: " + childKey + ". Setting TextView.");
                            fill_hr.setText("No data under " + childKey);
                        }
                        break; // Process only the last child
                    }
                } else {
                    Log.d("MY_APP", "DataSnapshot has no children at sensor_data path. Setting TextView.");
                    fill_hr.setText("No data available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("MY_APP", "Failed to read sensor data.", error.toException());
                fill_hr.setText("Error loading data: " + error.getMessage());
            }
        };

        // !!! 중요: 여기서 쿼리를 만들고 리스너를 연결해야 합니다 !!!
        // sensor_data 경로에서 키 순서대로 정렬하고 마지막 1개만 가져오는 쿼리
        Query lastSensorValueQuery = sensorDataRef.orderByKey().limitToLast(1);

        // 쿼리에 ValueEventListener를 연결합니다. 이 코드가 빠져있었습니다.
        lastSensorValueQuery.addValueEventListener(sensorValueListener);
        Log.d("MY_APP", "ValueEventListener added to lastSensorValueQuery.");

        // --- End Realtime Database Listener Setup ---


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets )-> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티 종료 시 리스너를 반드시 제거해야 메모리 누수를 막을 수 있습니다.
        // sensorDataRef와 sensorValueListener가 null이 아닌 경우에만 제거합니다.
        if (sensorDataRef != null && sensorValueListener != null) {
            // 리스너를 제거합니다.
            sensorDataRef.removeEventListener(sensorValueListener);
            Log.d("MY_APP", "Sensor value listener removed in onDestroy.");
        } else {
            Log.d("MY_APP", "sensorDataRef or sensorValueListener was null in onDestroy. Listener not removed.");
        }
    }


    private void createNotification(String title, String msg) {
        // ... (알림 생성 코드는 그대로 유지) ...
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.drawable.home_24px);
        builder.setContentTitle(title);
        builder.setContentText(msg);

        builder.setColor(Color.RED);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        notificationManager.notify(1, builder.build());
    }
}
