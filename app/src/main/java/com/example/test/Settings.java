package com.example.test;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Settings extends AppCompatActivity {

    ImageView ivMyPage;
    Button btn_home;
    Button btn_logout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user =mAuth.getCurrentUser();

        ivMyPage = findViewById(R.id.ivMyPage);
        btn_home = findViewById(R.id.btn_home);
        btn_logout = findViewById(R.id.btn_logout);

        ivMyPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDocumentByUid(user.getUid());
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDocumentByUid(user.getUid());
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOutUser();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getDocumentByUid(String userId) {
        DocumentReference docRef = db.collection("info").document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                    String condition = document.getString("condition");
                    if ("dementia".equals(condition)) {
                        startActivity(new Intent(Settings.this, MainPage1.class));
                    } else if ("just".equals(condition)) {
                        startActivity(new Intent(Settings.this, MainPage2.class));
                    } else {
                        Log.w(TAG, "Unknown condition: " + condition);
                        Toast.makeText(this, "사용자 유형을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    finish(); // 액티비티 종료
                } else {
                    Log.d(TAG, "No such document for UID: " + userId);
                    Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "Firestore document fetch failed", task.getException());
                Toast.makeText(this, "서버 오류로 사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void signOutUser() {
        mAuth.signOut();
        // Update UI, maybe show the sign-in screen again
        Intent intent = new Intent(Settings.this, MainActivity.class);
        startActivity(intent);
    }
}