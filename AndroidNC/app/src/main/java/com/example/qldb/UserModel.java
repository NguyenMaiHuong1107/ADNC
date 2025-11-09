package com.example.qldb;

public class UserModel {
    public int id;
    public String fullName;
    public String phone;
    public UserModel(int id, String fullName, String phone) {
        this.id = id; this.fullName = fullName; this.phone = phone;
    }
}
