package com.example.qldb.ActiVity;

public class Reservation {
    private String restaurantName;
    private String date;
    private String time;
    private String status;
    private int imageResId;

    public Reservation(String restaurantName, String date, String time, String status, int imageResId) {
        this.restaurantName = restaurantName;
        this.date = date;
        this.time = time;
        this.status = status;
        this.imageResId = imageResId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public int getImageResId() {
        return imageResId;
    }
}
