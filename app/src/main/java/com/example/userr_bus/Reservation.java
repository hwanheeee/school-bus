package com.example.userr_bus;

import com.google.firebase.Timestamp;

/**
 * 예약 정보를 담는 모델 클래스
 */
public class Reservation implements Comparable<Reservation> {

    private String userId;             // 예약한 사용자 ID
    private String route;              // 노선명
    private String place;              // 승하차 장소
    private String time;               // 예약 시간
    private Timestamp reservationDate; // 예약한 날짜 (서버 시간 기준)
    private String documentId;         // Firestore 문서 ID

    // 기본 생성자
    public Reservation() {}

    // Getter, Setter 메소드
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Timestamp getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Timestamp reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * 예약일 기준으로 정렬하기 위한 비교 메소드
     */
    @Override
    public int compareTo(Reservation other) {
        return this.reservationDate.compareTo(other.reservationDate);
    }
}
