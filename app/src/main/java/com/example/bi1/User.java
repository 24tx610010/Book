package com.example.bi1;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String phone;
    private String hoTen;
    private String password;
    private int roleid; // 1: Admin, 2: User
    private int loyaltyPoints; // Điểm tích lũy

    public User() {}

    public User(String id, String phone, String hoTen, String password, int roleid) {
        this.id = id;
        this.phone = phone;
        this.hoTen = hoTen;
        this.password = password;
        this.roleid = roleid;
        this.loyaltyPoints = 0;
    }

    @PropertyName("Id")
    public String getId() { return id; }
    @PropertyName("Id")
    public void setId(String id) { this.id = id; }

    @PropertyName("Phone")
    public String getPhone() { return phone; }
    @PropertyName("Phone")
    public void setPhone(String phone) { this.phone = phone; }

    @PropertyName("HoTen")
    public String getHoTen() { return hoTen; }
    @PropertyName("HoTen")
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    @PropertyName("Password")
    public String getPassword() { return password; }
    @PropertyName("Password")
    public void setPassword(String password) { this.password = password; }

    @PropertyName("RoleID")
    public int getRoleid() { return roleid; }
    @PropertyName("RoleID")
    public void setRoleid(int roleid) { this.roleid = roleid; }

    @PropertyName("LoyaltyPoints")
    public int getLoyaltyPoints() { return loyaltyPoints; }
    @PropertyName("LoyaltyPoints")
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
}
