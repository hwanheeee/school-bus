package com.example.userr_bus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    private ArrayList<Reservation> reservationsList;
    private Context context;
    private FirebaseFirestore db;
    private static final String TAG = "CustomAdapter";

    public CustomAdapter(ArrayList<Reservation> reservationsList, Context context) {
        this.reservationsList = reservationsList;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Reservation reservation = reservationsList.get(position);
        holder.bind(reservation);
    }

    @Override
    public int getItemCount() {
        return (reservationsList != null ? reservationsList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView routeTextView, placeTextView, timeTextView, reservationDateTextView;
        private Button cancelButton;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            routeTextView = itemView.findViewById(R.id.tv_route);
            placeTextView = itemView.findViewById(R.id.tv_place);
            timeTextView = itemView.findViewById(R.id.tv_time);
            reservationDateTextView = itemView.findViewById(R.id.tv_reservationDate);
            cancelButton = itemView.findViewById(R.id.btnCancel_1);

            db = FirebaseFirestore.getInstance();
        }

        // 각 아이템에 데이터 바인딩
        public void bind(Reservation reservation) {
            routeTextView.setText(reservation.getRoute());
            placeTextView.setText(reservation.getPlace());
            timeTextView.setText(reservation.getTime());

            // 날짜 포맷팅
            Timestamp reservationTimestamp = reservation.getReservationDate();
            if (reservationTimestamp != null) {
                Date date = reservationTimestamp.toDate();
                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                reservationDateTextView.setText(formattedDate);
            } else {
                reservationDateTextView.setText("날짜 정보 없음");
            }

            // 버튼 활성/비활성 여부 설정
            setCancelButtonState(reservation);

            // 버튼 클릭 리스너
            cancelButton.setOnClickListener(view -> {
                if (cancelButton.isEnabled()) {
                    showCancelConfirmationDialog(reservation);
                } else {
                    Toast.makeText(context, "시간이 지나 취소할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 예약 취소 가능 여부에 따라 버튼 설정
        private void setCancelButtonState(Reservation reservation) {
            Calendar now = Calendar.getInstance();
            Calendar reservationDateTime = Calendar.getInstance();
            reservationDateTime.setTime(reservation.getReservationDate().toDate());

            String[] timeParts = reservation.getTime().split(":");
            int reservationHour = Integer.parseInt(timeParts[0]);
            int reservationMinute = Integer.parseInt(timeParts[1]);
            reservationDateTime.set(Calendar.HOUR_OF_DAY, reservationHour);
            reservationDateTime.set(Calendar.MINUTE, reservationMinute);

            boolean canCancel = now.before(reservationDateTime);

            cancelButton.setEnabled(canCancel);
            if (!canCancel) {
                cancelButton.setBackgroundResource(R.drawable.btn_cancel_cannot);
            }
        }

        // 취소 확인 다이얼로그 표시
        private void showCancelConfirmationDialog(Reservation reservation) {
            new AlertDialog.Builder(context)
                    .setTitle("예약 취소 확인")
                    .setMessage("정말 예약을 취소하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> deleteReservation(reservation))
                    .setNegativeButton("아니오", null)
                    .show();
        }

        // Firebase에서 예약 삭제
        private void deleteReservation(Reservation reservation) {
            db.collection("Reservation")
                    .document(reservation.getDocumentId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            reservationsList.remove(position);
                            notifyItemRemoved(position);
                        }
                        Toast.makeText(context, "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "예약 취소에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting reservation: " + e.getMessage());
                    });
        }
    }
}
