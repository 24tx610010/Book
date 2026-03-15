package com.example.bi1;

import java.io.Serializable;

public class OrderDetail implements Serializable {
    private String id;
    private String orderId;
    private String bookId;
    private String bookName;
    private double unitPrice;
    private int quantity;
    private double subtotal;

    public OrderDetail() {}

    public OrderDetail(String id, String orderId, String bookId, String bookName, double unitPrice, int quantity, double subtotal) {
        this.id = id;
        this.orderId = orderId;
        this.bookId = bookId;
        this.bookName = bookName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
