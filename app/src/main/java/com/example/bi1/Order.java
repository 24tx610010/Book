package com.example.bi1;

import java.io.Serializable;
import java.util.Date;

public class Order implements Serializable {
    private String id;
    private String userId; // Số điện thoại khách hàng
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
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

    public Order(String id, String userId, String receiverName, String receiverPhone, String shippingAddress, Date orderDate, String paymentMethod, double totalAmount, int status) {
        this.id = id;
        this.userId = userId;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.shippingAddress = shippingAddress;
        this.orderDate = orderDate;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
