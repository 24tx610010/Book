package com.example.bi1;

import java.io.Serializable;

public class Book implements Serializable {
    private String Id;
    private String TenSach;
    private double GiaBan;
    private String HinhAnh;
    private String MaLoaiSach;
    private String MoTa;
    private int SoLuong;
    private String TacGia;
    private String NhaXuatBan;
    private String NamXuatBan;
    private String NgonNgu;
    private int luotBan; 
    private float rating;
    private boolean isNoiBat;

    public Book() {}

    public String getId() { return Id; }
    public void setId(String id) { Id = id; }
    public String getTenSach() { return TenSach; }
    public void setTenSach(String tenSach) { TenSach = tenSach; }
    public double getGiaBan() { return GiaBan; }
    public void setGiaBan(double giaBan) { GiaBan = giaBan; }
    public String getHinhAnh() { return HinhAnh; }
    public void setHinhAnh(String hinhAnh) { HinhAnh = hinhAnh; }
    public String getMaLoaiSach() { return MaLoaiSach; }
    public void setMaLoaiSach(String maLoaiSach) { MaLoaiSach = maLoaiSach; }
    public String getMoTa() { return MoTa; }
    public void setMoTa(String moTa) { MoTa = moTa; }
    public int getSoLuong() { return SoLuong; }
    public void setSoLuong(int soLuong) { SoLuong = soLuong; }
    public String getTacGia() { return TacGia; }
    public void setTacGia(String tacGia) { this.TacGia = tacGia; }
    public String getNhaXuatBan() { return NhaXuatBan; }
    public void setNhaXuatBan(String nhaXuatBan) { this.NhaXuatBan = nhaXuatBan; }
    public String getNamXuatBan() { return NamXuatBan; }
    public void setNamXuatBan(String namXuatBan) { this.NamXuatBan = namXuatBan; }
    public String getNgonNgu() { return NgonNgu; }
    public void setNgonNgu(String ngonNgu) { this.NgonNgu = ngonNgu; }
    public int getLuotBan() { return luotBan; }
    public void setLuotBan(int luotBan) { this.luotBan = luotBan; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public boolean isNoiBat() { return isNoiBat; }
    public void setNoiBat(boolean noiBat) { isNoiBat = noiBat; }
}
