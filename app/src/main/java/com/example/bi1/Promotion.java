package com.example.bi1;

import java.util.Date;

public class Promotion {
    private String id;
    private String code;
    private double discountValue;
    private String type; // "all", "category", "book"
    private String targetId; // ID of category or book
    private Date startDate;
    private Date endDate;
    private int status; // 1: active, 0: disabled

    public Promotion() {}

    public Promotion(String id, String code, double discountValue, String type, String targetId, Date startDate, Date endDate) {
        this.id = id;
        this.code = code;
        this.discountValue = discountValue;
        this.type = type;
        this.targetId = targetId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = 1;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
