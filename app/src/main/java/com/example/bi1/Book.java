package com.example.bi1;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable {
    private String Id;
    private String TenSach;
    private double GiaGoc; 
    private double GiaBan; 
    private int khuyenMai; 
    private String HinhAnh;
    private List<String> hinhAnhChiTiet;
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

    public Book() {
        hinhAnhChiTiet = new ArrayList<>();
    }

    public String getId() { return Id; }
    public void setId(String id) { Id = id; }

    @PropertyName("TenSach")
    public String getTenSach() { return TenSach; }
    @PropertyName("TenSach")
    public void setTenSach(String tenSach) { TenSach = tenSach; }
    
    @PropertyName("GiaGoc")
    public double getGiaGoc() { return GiaGoc; }
    @PropertyName("GiaGoc")
    public void setGiaGoc(double giaGoc) { GiaGoc = giaGoc; }
    
    @PropertyName("GiaBan")
    public double getGiaBan() { return GiaBan; }
    @PropertyName("GiaBan")
    public void setGiaBan(double giaBan) { GiaBan = giaBan; }

    public int getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(int khuyenMai) { this.khuyenMai = khuyenMai; }
    
    @PropertyName("HinhAnh")
    public String getHinhAnh() { return HinhAnh; }
    @PropertyName("HinhAnh")
    public void setHinhAnh(String hinhAnh) { HinhAnh = hinhAnh; }

    public List<String> getHinhAnhChiTiet() { return hinhAnhChiTiet; }
    public void setHinhAnhChiTiet(List<String> hinhAnhChiTiet) { this.hinhAnhChiTiet = hinhAnhChiTiet; }

    @PropertyName("MaLoaiSach")
    public String getMaLoaiSach() { return MaLoaiSach; }
    @PropertyName("MaLoaiSach")
    public void setMaLoaiSach(String maLoaiSach) { MaLoaiSach = maLoaiSach; }

    @PropertyName("MoTa")
    public String getMoTa() { return MoTa; }
    @PropertyName("MoTa")
    public void setMoTa(String moTa) { MoTa = moTa; }

    @PropertyName("SoLuong")
    public int getSoLuong() { return SoLuong; }
    @PropertyName("SoLuong")
    public void setSoLuong(int soLuong) { SoLuong = soLuong; }

    @PropertyName("TacGia")
    public String getTacGia() { return TacGia; }
    @PropertyName("TacGia")
    public void setTacGia(String tacGia) { this.TacGia = tacGia; }

    @PropertyName("NhaXuatBan")
    public String getNhaXuatBan() { return NhaXuatBan; }
    @PropertyName("NhaXuatBan")
    public void setNhaXuatBan(String nhaXuatBan) { this.NhaXuatBan = nhaXuatBan; }

    @PropertyName("NamXuatBan")
    public String getNamXuatBan() { return NamXuatBan; }
    @PropertyName("NamXuatBan")
    public void setNamXuatBan(String namXuatBan) { this.NamXuatBan = namXuatBan; }

    @PropertyName("NgonNgu")
    public String getNgonNgu() { return NgonNgu; }
    @PropertyName("NgonNgu")
    public void setNgonNgu(String ngonNgu) { this.NgonNgu = ngonNgu; }

    public int getLuotBan() { return luotBan; }
    public void setLuotBan(int luotBan) { this.luotBan = luotBan; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public boolean isNoiBat() { return isNoiBat; }
    public void setNoiBat(boolean noiBat) { isNoiBat = noiBat; }

    public int getDiscountPercent() {
        if (khuyenMai > 0) return khuyenMai;
        if (GiaGoc > 0 && GiaGoc > GiaBan) {
            return (int) (((GiaGoc - GiaBan) / GiaGoc) * 100);
        }
        return 0;
    }
}
