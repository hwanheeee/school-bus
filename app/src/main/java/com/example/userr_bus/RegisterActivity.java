package com.example.userr_bus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * 사용자 회원가입 화면
 */
public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private EditText emailEditText, passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFirebase();
        initializeViews();
        setupButton();
    }

    // Firebase 인증 및 DB 객체 초기화
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("userr_bus");
    }

    // 레이아웃 요소 초기화
    private void initializeViews() {
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_pwd);
        registerButton = findViewById(R.id.btn_register);
    }

    // 회원가입 버튼 리스너 설정
    private void setupButton() {
        registerButton.setOnClickListener(view -> attemptRegister());
    }

    // 회원가입 시도
    private void attemptRegister() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (!validateInputs(email, password)) {
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToDatabase(password);
                    } else {
                        Toast.makeText(this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 입력값 유효성 검사
    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            emailEditText.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    // 회원가입 성공 후 데이터베이스에 저장
    private void saveUserToDatabase(String password) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            UsesrAccount account = new UsesrAccount();  // ⭐️ 여기 클래스명 나중에 UserAccount로 수정 추천
            account.setIdToken(firebaseUser.getUid());
            account.setEmailId(firebaseUser.getEmail());
            account.setPassword(password);

            databaseRef.child("UsesrAccount").child(firebaseUser.getUid()).setValue(account)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "회원 데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // 로그인 화면으로 이동
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
