package com.example.userr_bus;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * 사용자의 예약 목록을 표시하는 화면
 */
public class SelectBusListActivity extends AppCompatActivity {

    private static final String TAG = "SelectBusListActivity";

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CustomAdapter adapter;
    private ArrayList<Reservation> reservationList;

    private FloatingActionButton fab;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectbuslist);

        initializeViews();
        setupRecyclerView();
        loadReservationList();
        setupButtons();
        setupBackButtonHandler();
    }

    private void initializeViews() {
        firestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        fab = findViewById(R.id.fab);
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationList = new ArrayList<>();
        adapter = new CustomAdapter(reservationList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DecorationItem(9)); // 리스트 간 여백 설정
    }

    private void loadReservationList() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firestore.collection("Reservation")
                    .whereEqualTo("userId", currentUser.getUid())
                    .orderBy("reservationDate", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            reservationList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reservation reservation = document.toObject(Reservation.class);
                                reservation.setDocumentId(document.getId());
                                reservationList.add(reservation);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Error getting reservations", task.getException());
                        }
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }

    private void setupButtons() {
        Button homeButton = findViewById(R.id.home_img);
        Button homeButton2 = findViewById(R.id.btn_home);

        View.OnClickListener moveHomeListener = view -> {
            Intent intent = new Intent(this, RouteChooseActivity.class);
            startActivity(intent);
            finish();
        };

        homeButton.setOnClickListener(moveHomeListener);
        homeButton2.setOnClickListener(moveHomeListener);

        fab.setOnClickListener(this::showPopupMenu);
    }

    private void setupBackButtonHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SelectBusListActivity.this, RouteChooseActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            switch (item.getItemId()) {
                case R.id.menu_item_1:
                    openWebsite("https://www.cu.ac.kr/life/welfare/schoolbus");
                    return true;
                case R.id.menu_item_2:
                    loadReservationList();
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
