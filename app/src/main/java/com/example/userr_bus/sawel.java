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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사월역 출발 예약 화면
 */
public class SawelActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private static final String TAG = "SawelActivity";

    private Spinner timeSpinner, placeSpinner;
    private Button reservationButton, backButton;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sawel);

        initializeViews();
        setupSpinners();
        setupButtons();
    }

    private void initializeViews() {
        timeSpinner = findViewById(R.id.time_spinner);
        placeSpinner = findViewById(R.id.station_spinner);
        reservationButton = findViewById(R.id.reservation);
        backButton = findViewById(R.id.btn_back);
        fab = findViewById(R.id.fab);

        firestore = FirebaseFirestore.getInstance();
    }

    private void setupSpinners() {
        String[] times = {"시간을 선택하세요", "08:00", "08:05", "08:20", "08:40", "09:00", "09:30", "09:50", "10:00"};
        String[] places = {"장소를 선택하세요", "사월역(3번출구)", "정문", "B1", "C7", "C13", "D6", "A2(건너편)"};

        timeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getAvailableTimes(times)));
        placeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, places));
    }

    private List<String> getAvailableTimes(String[] times) {
        List<String> availableTimes = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        for (String time : times) {
            if (time.equals("시간을 선택하세요")) {
                availableTimes.add(time);
            } else {
                int hour = Integer.parseInt(time.substring(0, 2));
                int minute = Integer.parseInt(time.substring(3));

                if (hour > currentHour || (hour == currentHour && minute >= currentMinute)) {
                    availableTimes.add(time);
                }
            }
        }
        return availableTimes;
    }

    private void setupButtons() {
        reservationButton.setOnClickListener(view -> attemptReservation());
        backButton.setOnClickListener(view -> {
            startActivity(new Intent(this, RouteChooseActivity.class));
            finish();
        });
        fab.setOnClickListener(this::showPopupMenu);
    }

    private void attemptReservation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
        reservationData.put("route", "사월역 출발");
        reservationData.put("time", timeSpinner.getSelectedItem().toString());
        reservationData.put("place", placeSpinner.getSelectedItem().toString());
        reservationData.put("reservationDate", new Timestamp(new Date()));

        firestore.collection("Reservation")
                .add(reservationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Reservation successful with ID: " + documentReference.getId());
                    Toast.makeText(this, "예약이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    navigateToSelectBusList();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding reservation", e);
                    Toast.makeText(this, "예약에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToSelectBusList() {
        startActivity(new Intent(this, SelectBusListActivity.class));
        finish();
    }

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
                        startActivity(new Intent(this, LoginActivity.class));
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

    private void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
