package com.example.test;

import static com.example.test.R.layout.activity_main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText etId, etPassword;
    private TextView tvTogglePassword, tv_signup;
    private View btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

        setContentView(activity_main);
        setupLoginUI();

        if (isFirstRun) {
            prefs.edit().putBoolean("isFirstRun", false).apply();
        }

        tv_signup = findViewById(R.id.tv_signup);
        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            getDocumentByUid(currentUser.getUid());
        }
    }

    private void setupLoginUI() {
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        tvTogglePassword = findViewById(R.id.tv_toggle_password);
        btnLogin = findViewById(R.id.btn_login);

        tvTogglePassword.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    tvTogglePassword.setText("클릭하여 비밀번호 보기");
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    tvTogglePassword.setText("클릭하여 비밀번호 숨기기");
                }
                isPasswordVisible = !isPasswordVisible;
                etPassword.setSelection(etPassword.getText().length());
            }
        });

        btnLogin.setOnClickListener(view -> {
            String email = etId.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            signInWithEmail(email, password);
        });
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Login successful. UID: " + user.getUid());
                            getDocumentByUid(user.getUid());
                        } else {
                            Log.w(TAG, "Login success but user is null.");
                        }
                    } else {
                        Toast.makeText(MainActivity.this,
                                "로그인 실패: " + (task.getException() != null ? task.getException().getMessage() : "알 수 없는 오류"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getDocumentByUid(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                    String condition = document.getString("condition");
                    if ("dementia".equals(condition)) {
                        startActivity(new Intent(MainActivity.this, MainPage1.class));
                    } else if ("just".equals(condition)) {
                        startActivity(new Intent(MainActivity.this, MainPage2.class));
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
}
