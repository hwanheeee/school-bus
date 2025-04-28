package com.example.userr_bus;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * 앱 시작 시 로고를 보여주는 스플래시 화면
 */
public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 스플래시 화면 표시 시간 (3초)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moveToLoginAfterDelay();
    }

    // 일정 시간 후 로그인 화면으로 이동
    private void moveToLoginAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
