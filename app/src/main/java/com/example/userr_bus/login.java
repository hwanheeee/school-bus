package com.example.userr_bus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * 사용자 로그인 화면
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseRef;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private boolean doubleBackToExitPressedOnce = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFirebase();
        initializeViews();
        setupButtons();
        setupBackPressHandler();
    }

    // Firebase 인증 및 데이터베이스 초기화
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    // 레이아웃 요소 초기화
    private void initializeViews() {
        emailEditText = findViewById(R.id.school_num_input);
        passwordEditText = findViewById(R.id.number_input);
        loginButton = findViewById(R.id.btn_click);
        registerButton = findViewById(R.id.btn_JOIN);
    }

    // 버튼 클릭 리스너 설정
    private void setupButtons() {
        loginButton.setOnClickListener(view -> attemptLogin());
        registerButton.setOnClickListener(view -> navigateToRegister());
    }

    // 로그인 시도
    private void attemptLogin() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (validateInputs(email, password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            saveUserEmail(email);
                            navigateToRouteChoose();
                            Toast.makeText(this, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "로그인에 실패하였습니다. 이메일과 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // 입력값 유효성 검사
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.contains("@")) {
            Toast.makeText(this, "올바른 이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // 로그인 성공 시 이메일 저장
    private void saveUserEmail(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userEmail", email);
        editor.apply();
    }

    // routechoose 화면으로 이동
    private void navigateToRouteChoose() {
        Intent intent = new Intent(this, routechoose.class);
        startActivity(intent);
        finish();
    }

    // 회원가입 화면으로 이동
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    // 뒤로가기 두 번 누르면 앱 종료
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity(); // 앱 종료
                } else {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(LoginActivity.this, "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            }
        });
    }
}
