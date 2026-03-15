package com.example.bi1;

public class Category {
    private String id;
    private String tenLoai;

    public Category() {}

    public Category(String id, String tenLoai) {
        this.id = id;
        this.tenLoai = tenLoai;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }

    // Ghi đè toString để hiển thị tên trong Spinner
    @Override
    public String toString() {
        return tenLoai;
    }
}
