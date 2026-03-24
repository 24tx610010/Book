package com.example.bi1;

import java.io.Serializable;

public class LoyalCustomer implements Serializable {
    private String id; // Khóa chính (Mã bảng khách hàng thân thiết)
    private String userId; // Khóa ngoại liên kết đến bảng User (User ID)
    private int points; // Số điểm tích lũy

    public LoyalCustomer() {}

    public LoyalCustomer(String id, String userId, int points) {
        this.id = id;
        this.userId = userId;
        this.points = points;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}
