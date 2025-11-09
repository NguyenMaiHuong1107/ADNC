package com.example.qldb;

public class ReservationModel {
    public long id;
    public String date;   // "dd/MM/yyyy"
    public String time;   // "HH:mm"
    public int adult;
    public int child;
    public String phone;
    public String contactName;
    public ReservationStatus status;

    public ReservationModel(long id, String date, String time,
                            int adult, int child, String phone, String contactName,
                            ReservationStatus status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.adult = adult;
        this.child = child;
        this.phone = phone;
        this.contactName = contactName;
        this.status = status;
    }
}
