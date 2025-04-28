package com.example.userr_bus;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

/**
 * 사용자 노선 선택 화면
 */
public class RouteChooseActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private TextView schoolNumTextView;

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routechoose);

        initializeFirebase();
        initializeViews();
        setupFabButton();
        setupRouteButtons();
        setupBackButtonHandler();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    private void initializeViews() {
        // SharedPreferences에서 사용자 이메일 가져와서 표시
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "No Email Found");

        schoolNumTextView = findViewById(R.id.scholl_num);
        schoolNumTextView.setText(userEmail);

        fab = findViewById(R.id.fab);
    }

    private void setupFabButton() {
        fab.setOnClickListener(this::showPopupMenu);
    }

    private void setupRouteButtons() {
        setupMoveButton(R.id.gyonea, GyoneaActivity.class);
        setupMoveButton(R.id.hayang, HayangActivity.class);
        setupMoveButton(R.id.ansim, AnsimStationActivity.class);
        setupMoveButton(R.id.sawel, SawelActivity.class);
        setupMoveButton(R.id.sawel_ansim, AnsimSawelActivity.class);
        setupMoveButton(R.id.btn_back, LoginActivity.class);
    }

    private void setupMoveButton(int buttonId, Class<?> destinationActivity) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(this, destinationActivity);
            startActivity(intent);
            finish();
        });
    }

    private void setupBackButtonHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity(); // 앱 종료
                } else {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(RouteChooseActivity.this, "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            }
        });
    }

    // 팝업 메뉴 표시
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_item_1:
                    openWebsite("https://www.cu.ac.kr/life/welfare/schoolbus");
                    return true;
                case R.id.menu_item_2:
                    startActivity(new Intent(this, SelectBusListActivity.class));
                    finish();
                    return true;
                case R.id.menu_item_3:
                    logoutUser();
                    return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void logoutUser() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "로그아웃에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
