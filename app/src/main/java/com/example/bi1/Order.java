package com.example.bi1;

import java.io.Serializable;
import java.util.Date;

public class Order implements Serializable {
    private String id;
    private String userId; // Số điện thoại khách hàng
    private Date orderDate;
    private String paymentMethod;
    private double totalAmount;
    private int status; // 0: Đã đặt, 1: Đã duyệt, 2: Đã hủy

    public Order() {}

    public Order(String id, String userId, Date orderDate, String paymentMethod, double totalAmount, int status) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
