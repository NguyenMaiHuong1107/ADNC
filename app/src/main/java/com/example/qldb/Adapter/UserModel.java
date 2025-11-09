package com.example.qldb.Adapter;

public class UserModel {
    public int userId;
    public String fullName;
    public String phone;

    public UserModel(int userId, String fullName, String phone) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
    }
}
