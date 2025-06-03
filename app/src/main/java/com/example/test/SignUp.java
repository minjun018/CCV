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
import androidx.annotation.NonNull;import com.google.firebase.firestore.DocumentReference;

public class SignUp extends AppCompatActivity {

    // UI 요소 변수 선언
    EditText name, new_phonenumber, new_id, new_password, confirm_password;
    private Spinner dementia_buttons; // Spinner를 클래스 멤버 변수로 선언
    TextView go_back, tv_toggle_password, tv_toggle_confirm_password;
    Button btn_signup;

    // Firebase 관련 변수 선언
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db; // Firestore 인스턴스도 멤버 변수로 선언

    // 디버깅 태그
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 엣지투엣지 활성화
        setContentView(R.layout.activity_sign_up); // 레이아웃 설정

        // UI 요소 연결 (findViewById)
        name = findViewById(R.id.name);
        new_phonenumber = findViewById(R.id.new_phonenumber);
        new_id = findViewById(R.id.new_id); // 이메일 입력 필드
        new_password = findViewById(R.id.new_password); // 비밀번호 입력 필드
        confirm_password = findViewById(R.id.confirm_password); // 비밀번호 확인 필드
        go_back = findViewById(R.id.go_back); // 뒤로가기 TextView
        dementia_buttons = findViewById(R.id.dementia_buttons); // 치매 여부 Spinner
        tv_toggle_password = findViewById(R.id.tv_toggle_password); // 비밀번호 보기/숨기기 TextView
        tv_toggle_confirm_password = findViewById(R.id.tv_toggle_confirm_password); // 비밀번호 확인 보기/숨기기 TextView
        btn_signup = findViewById(R.id.btn_signup); // 회원가입 버튼

        // Firebase 인스턴스 초기화
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 초기화

        // Spinner 항목 설정
        String[] items = {"선택하세요", "유", "무"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item, // 기본 스피너 아이템 레이아웃
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // 드롭다운 목록 레이아웃
        dementia_buttons.setAdapter(adapter);

        // 스피너 아이템 선택 리스너 (선택 변경 시 토스트 메시지 표시 등 부가 기능용)
        // ***주의: Firestore 저장 로직은 여기에 넣지 않습니다!***
        dementia_buttons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                // 선택 변경 시 사용자에게 피드백을 주고 싶다면 여기에 코드를 추가
                // Log.d(TAG, "Spinner selected: " + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때 (초기 상태 등)
                // Log.d(TAG, "Spinner nothing selected");
            }
        });


        // 비밀번호 가시성 토글 설정
        tv_toggle_password.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // 비밀번호 숨기기
                    new_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    tv_toggle_password.setText("클릭하여 비밀번호 보기");
                } else {
                    // 비밀번호 보기
                    new_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    tv_toggle_password.setText("클릭하여 비밀번호 숨기기");
                }
                // 커서를 텍스트 끝으로 이동하여 자연스럽게 보이도록 함
                new_password.setSelection(new_password.getText().length());
                isPasswordVisible = !isPasswordVisible; // 상태 반전
            }
        });

        // 비밀번호 확인 가시성 토글 설정
        tv_toggle_confirm_password.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // 비밀번호 확인 숨기기
                    confirm_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    tv_toggle_confirm_password.setText("클릭하여 비밀번호 보기");
                } else {
                    // 비밀번호 확인 보기
                    confirm_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    tv_toggle_confirm_password.setText("클릭하여 비밀번호 숨기기");
                }
                // 커서를 텍스트 끝으로 이동
                confirm_password.setSelection(confirm_password.getText().length());
                isPasswordVisible = !isPasswordVisible; // 상태 반전
            }
        });

        // 뒤로가기 버튼 클릭 리스너
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });

        // 회원가입 버튼 클릭 리스너 (핵심 로직)
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 입력 필드 값 가져오기
                String email = new_id.getText().toString().trim();
                String password = new_password.getText().toString().trim();
                String c_password = confirm_password.getText().toString().trim();
                String name_d = name.getText().toString().trim();
                String new_phonenumber_d = new_phonenumber.getText().toString().trim();
                // 스피너에서 현재 선택된 아이템 가져오기 (버튼 클릭 시점의 값)
                String dementiaCondition = dementia_buttons.getSelectedItem().toString();

                // 입력값 유효성 검사
                if (email.isEmpty() || password.isEmpty() || name_d.isEmpty() || new_phonenumber_d.isEmpty()) {
                    Toast.makeText(SignUp.this, "이름, 연락처, 이메일, 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 함수 종료
                }

                if (dementiaCondition.equals("선택하세요")) {
                    Toast.makeText(SignUp.this, "노인의 치매 여부를 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return; // 함수 종료
                }

                // 비밀번호 일치 확인
                if (!password.equals(c_password)){
                    Toast.makeText(SignUp.this, "비밀번호와 비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return; // 함수 종료
                }

                // Firebase Authentication 회원가입 시도
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 회원가입 성공!
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = firebaseAuth.getCurrentUser(); // 새로 가입된 사용자 정보 가져오기

                                    // TODO: 회원가입 성공 후 Firestore에 사용자 정보 저장
                                    if (user != null) {
                                        String uid = user.getUid(); // 사용자의 고유 UID 가져오기

                                        // Firestore에 저장할 데이터 Map 생성
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("name", name_d);
                                        userData.put("phone", new_phonenumber_d);

                                        userData.put("email", user.getEmail()); // 가입된 이메일도 저장 (선택 사항)
                                        userData.put("createdAt", Timestamp.now()); // 계정 생성 시간 타임스탬프 (선택 사항)

                                        if(dementiaCondition.equals("유")){
                                            userData.put("condition", "dementia"); // 스피너에서 가져온 값 저장
                                        }else if (dementiaCondition.equals("무")){
                                            userData.put("condition", "just");
                                        }else{
                                            Log.w(TAG, "Unexpected condition value:  error");
                                        }

                                        // "users" 컬렉션 아래에 사용자의 UID를 문서 ID로 사용하여 문서 생성/업데이트
                                        db.collection("users").document(uid) // <-- UID를 문서 ID로 사용
                                                .set(userData) // set() 메서드로 데이터 저장 (문서 없으면 생성, 있으면 덮어쓰기)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "Firestore UID 기반 문서 성공적으로 작성됨! UID: " + uid);
                                                        Toast.makeText(SignUp.this, "회원가입 및 정보 저장 성공!", Toast.LENGTH_SHORT).show();

                                                        // TODO: Firestore 저장 성공 후 다음 화면으로 이동
                                                        // 저장된 Map에서 "condition" 값을 다시 가져와서 비교
                                                        String conditionValue = (String) userData.get("condition");

                                                        if("dementia".equals(conditionValue)) {
                                                            Intent intent = new Intent(SignUp.this, SafeLocation.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else if("just".equals(conditionValue)) {
                                                            Intent intent = new Intent(SignUp.this, MainPage2.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Log.w(TAG, "Unexpected condition value: " + conditionValue);
                                                            Intent intent = new Intent(SignUp.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error writing UID document to Firestore", e);
                                                        Toast.makeText(SignUp.this, "사용자 정보 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                        // Firestore 저장 실패 시 어떻게 처리할지 결정해야 합니다.
                                                        // - 사용자에게 알리고 다시 시도하게 할 수도 있고,
                                                        // - Authentication 계정은 삭제하고 처음부터 다시 가입하게 할 수도 있습니다.
                                                        // - 여기서는 일단 실패 알림 후 다음 화면으로 이동하지 않습니다.
                                                    }
                                                });

                                    } else {
                                        // 회원가입은 성공했으나 FirebaseUser 객체를 가져오지 못한 경우 (매우 드물지만 대비)
                                        Log.w(TAG, "User is null after successful registration.");
                                        Toast.makeText(SignUp.this, "회원가입은 성공했으나 사용자 정보를 가져오는데 실패했습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
                                        // 로그인 화면으로 이동 등을 고려
                                        Intent intent = new Intent(SignUp.this, MainActivity.class); // 로그인 화면으로 가정
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    // 회원가입 실패
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());

                                    // 오류 메시지를 사용자에게 보여주기 (오류 종류에 따라 다르게 처리 가능)
                                    String errorMessage = "회원가입 실패.";
                                    if (task.getException() != null) {
                                        errorMessage += " " + task.getException().getMessage();
                                    }
                                    Toast.makeText(SignUp.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        // 시스템 바 인셋 적용 (기존 코드 유지)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
} // onCreate 메소드 끝
// SignUp 클래스 끝
