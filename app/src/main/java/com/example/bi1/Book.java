package com.example.bi1;

import java.io.Serializable;
<<<<<<< HEAD
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> 0d5c59f (22/3)

public class Book implements Serializable {
    private String Id;
    private String TenSach;
<<<<<<< HEAD
    private double GiaBan;
    private String HinhAnh;
=======
    private double GiaGoc; // Giá gốc ban đầu
    private double GiaBan; // Giá đã khuyến mãi
    private int khuyenMai; // Phần trăm khuyến mãi (ví dụ: 10)
    private String HinhAnh;
    private List<String> hinhAnhChiTiet;
>>>>>>> 0d5c59f (22/3)
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

<<<<<<< HEAD
    public Book() {}
=======
    public Book() {
        hinhAnhChiTiet = new ArrayList<>();
    }
>>>>>>> 0d5c59f (22/3)

    public String getId() { return Id; }
    public void setId(String id) { Id = id; }
    public String getTenSach() { return TenSach; }
    public void setTenSach(String tenSach) { TenSach = tenSach; }
<<<<<<< HEAD
    public double getGiaBan() { return GiaBan; }
    public void setGiaBan(double giaBan) { GiaBan = giaBan; }
    public String getHinhAnh() { return HinhAnh; }
    public void setHinhAnh(String hinhAnh) { HinhAnh = hinhAnh; }
=======
    
    public double getGiaGoc() { return GiaGoc; }
    public void setGiaGoc(double giaGoc) { GiaGoc = giaGoc; }
    
    public double getGiaBan() { return GiaBan; }
    public void setGiaBan(double giaBan) { GiaBan = giaBan; }

    public int getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(int khuyenMai) { this.khuyenMai = khuyenMai; }
    
    public String getHinhAnh() { return HinhAnh; }
    public void setHinhAnh(String hinhAnh) { HinhAnh = hinhAnh; }
    public List<String> getHinhAnhChiTiet() { return hinhAnhChiTiet; }
    public void setHinhAnhChiTiet(List<String> hinhAnhChiTiet) { this.hinhAnhChiTiet = hinhAnhChiTiet; }
>>>>>>> 0d5c59f (22/3)
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
<<<<<<< HEAD
=======

    // Tính phần trăm giảm giá (ưu tiên lấy từ thuộc tính khuyenMai nếu có, nếu không tính từ GiaGoc/GiaBan)
    public int getDiscountPercent() {
        if (khuyenMai > 0) return khuyenMai;
        if (GiaGoc > 0 && GiaGoc > GiaBan) {
            return (int) (((GiaGoc - GiaBan) / GiaGoc) * 100);
        }
        return 0;
    }
>>>>>>> 0d5c59f (22/3)
}
