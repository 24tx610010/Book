package com.example.bi1;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String phone;
    private String hoTen;
    private String password;
    private int roleid; // 1: Admin, 2: User

    public User() {}

    public User(String id, String phone, String hoTen, String password, int roleid) {
        this.id = id;
        this.phone = phone;
        this.hoTen = hoTen;
        this.password = password;
        this.roleid = roleid;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getRoleid() { return roleid; }
    public void setRoleid(int roleid) { this.roleid = roleid; }
}
