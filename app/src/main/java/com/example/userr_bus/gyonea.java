package com.example.userr_bus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 교내 순환 예약 화면
 */
public class GyoneaActivity extends AppCompatActivity {

    private static final String TAG = "GyoneaActivity";

    private FloatingActionButton fab;
    private Spinner timeSpinner, placeSpinner;
    private Button reservationButton, backButton;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyonea);

        initializeFirebase();
        initializeViews();
        setupTimeSpinner();
        setupPlaceSpinner();
        setupButtons();
    }

    // Firebase 인스턴스 초기화
    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    // 레이아웃 요소 초기화
    private void initializeViews() {
        timeSpinner = findViewById(R.id.time_spinner);
        placeSpinner = findViewById(R.id.station_spinner);
        reservationButton = findViewById(R.id.reservation);
        backButton = findViewById(R.id.btn_back);
        fab = findViewById(R.id.fab);
    }

    // 시간 스피너 설정
    private void setupTimeSpinner() {
        String[] itemTime = {"시간을 선택하세요", "08:30", "08:35", "08:40", "08:55", "09:00",
                "09:30", "09:40", "09:50", "10:00", "10:10", "10:20", "10:30",
                "10:40", "10:50", "11:00"};

        List<String> availableTimes = getAvailableTimes(itemTime);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, availableTimes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);
    }

    // 현재 시간 이후만 리스트에 포함
    private List<String> getAvailableTimes(String[] itemTime) {
        List<String> timeList = new ArrayList<>();
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = currentTime.get(Calendar.MINUTE);

        for (String time : itemTime) {
            if (time.equals("시간을 선택하세요")) {
                timeList.add(time);
            } else {
                int hour = Integer.parseInt(time.substring(0, 2));
                int minute = Integer.parseInt(time.substring(3));
                if (hour > currentHour || (hour == currentHour && minute >= currentMinute)) {
                    timeList.add(time);
                }
            }
        }
        return timeList;
    }

    // 장소 스피너 설정
    private void setupPlaceSpinner() {
        String[] itemPlace = {"장소를 선택하세요", "정문", "B1", "C7", "C13", "D6", "A2(건너편)"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemPlace);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeSpinner.setAdapter(adapter);
    }

    // 버튼 리스너 설정
    private void setupButtons() {
        reservationButton.setOnClickListener(view -> handleReservation());
        backButton.setOnClickListener(view -> navigateToRouteChoose());
        fab.setOnClickListener(this::showPopupMenu);
    }

    // 예약 처리
    private void handleReservation() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인 상태가 아닙니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedTimePosition = timeSpinner.getSelectedItemPosition();
        int selectedPlacePosition = placeSpinner.getSelectedItemPosition();

        if (selectedTimePosition == 0 || selectedPlacePosition == 0) {
            if (selectedTimePosition == 0) {
                Toast.makeText(this, "유효한 시간을 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
            if (selectedPlacePosition == 0) {
                Toast.makeText(this, "유효한 장소를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("userId", currentUser.getUid());
        reservationData.put("route", "교내 순환");
        reservationData.put("time", timeSpinner.getSelectedItem().toString());
        reservationData.put("place", placeSpinner.getSelectedItem().toString());
        reservationData.put("reservationDate", new Timestamp(new Date()));

        firestore.collection("Reservation")
                .add(reservationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    Toast.makeText(this, "예약이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    navigateToSelectBusList();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(this, "예약에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    // routechoose로 이동
    private void navigateToRouteChoose() {
        startActivity(new Intent(this, routechoose.class));
        finish();
    }

    // selectbuslist로 이동
    private void navigateToSelectBusList() {
        startActivity(new Intent(this, selectbuslist.class));
        finish();
    }

    // 팝업 메뉴 표시
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            switch (item.getItemId()) {
                case R.id.menu_item_1:
                    openWebsite("https://www.cu.ac.kr/life/welfare/schoolbus");
                    return true;
                case R.id.menu_item_2:
                    navigateToSelectBusList();
                    return true;
                case R.id.menu_item_3:
                    if (auth != null) {
                        auth.signOut();
                        startActivity(new Intent(this, login.class));
                        finish();
                    } else {
                        Toast.makeText(this, "로그아웃에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // 웹사이트 이동
    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
