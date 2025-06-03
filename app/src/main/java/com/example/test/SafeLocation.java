package com.example.test;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth; // Firebase Auth 임포트
import com.google.firebase.auth.FirebaseUser;import com.google.firebase.firestore.FirebaseFirestore;import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import androidx.annotation.NonNull;import com.google.firebase.firestore.DocumentReference;import java.util.HashMap;
import java.util.Map;import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;import com.google.firebase.firestore.SetOptions;

public class SafeLocation extends AppCompatActivity {
    EditText start_atitude,start_longitude;
    Button btn_yes;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_safe_location);

        start_atitude = findViewById(R.id.start_atitude);
        start_longitude = findViewById(R.id.start_longitude);
        btn_yes = findViewById(R.id.btn_yes);// Firebase 인스턴스 초기화
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 초기화

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String atitude = start_atitude.getText().toString();
                String longitude = start_longitude.getText().toString();

                Map<String, Object> user = new HashMap<>();
                user.put("atitude", atitude);
                user.put("longitude", longitude);
                FirebaseUser users = firebaseAuth.getCurrentUser();
                // 예를 들어, 사용자 고유 ID(UID)를 문서 ID로 사용하고 싶다고 해봅시다.
                String userId = users.getUid();; // 실제 사용자 ID로 대체하세요.

                db.collection("users").document(userId)
                        .set(user, SetOptions.merge()) // SetOptions.merge()를 사용해서 병합합니다.
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                System.out.println("Document merged successfully!");
                                Intent intent = new Intent(SafeLocation.this, MainPage1.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                System.err.println("Error merging document: " + e);
                                Log.e("SafeLocation", "Error merging document: ", e);
                                Toast.makeText(SafeLocation.this, "위치 저장 실패 : "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}