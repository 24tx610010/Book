package com.example.bi1;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static List<CartItem> cartList = new ArrayList<>();

    public static List<CartItem> getCartList() {
        return cartList;
    }

    public static void addToCart(Book book, int qty) {
        for (CartItem item : cartList) {
            if (item.getBookId().equals(book.getId())) {
                item.setQuantity(item.getQuantity() + qty);
                return;
            }
        }
        cartList.add(new CartItem(
                book.getId(),
                book.getTenSach(),
                book.getGiaBan(),
                book.getGiaGoc(),
                qty,
                book.getHinhAnh(),
                book.getMoTa(),
                book.getTacGia(),
                book.getNhaXuatBan(),
                book.getNamXuatBan(),
                book.getNgonNgu(),
                book.getMaLoaiSach()
        ));
    }

    public static void updateQuantity(String bookId, int newQty) {
        for (CartItem item : cartList) {
            if (item.getBookId().equals(bookId)) {
                item.setQuantity(newQty);
                return;
            }
        }
    }

    public static void removeFromCart(String bookId) {
        for (int i = 0; i < cartList.size(); i++) {
            if (cartList.get(i).getBookId().equals(bookId)) {
                cartList.remove(i);
                return;
            }
        }
    }

    // TỔNG TIỀN HÀNG (Dựa trên GIÁ BÁN hiện tại của sách)
    public static double getSubtotal() {
        double total = 0;
        for (CartItem item : cartList) {
            if (item.isSelected()) {
                total += item.getUnitPrice() * item.getQuantity();
            }
        }
        return total;
    }

    // TỔNG SỐ TIỀN ĐƯỢC GIẢM THÊM (Ví dụ: giảm theo số lượng món đồ trong giỏ)
    public static double getDiscountAmount() {
        int totalQty = 0;
        double subtotal = 0;

        for (CartItem item : cartList) {
            if (item.isSelected()) {
                totalQty += item.getQuantity();
                subtotal += item.getUnitPrice() * item.getQuantity();
            }
        }

        // Giảm giá tự động theo số lượng (5% cho 2 món, 6% cho >=3 món)
        double qtyDiscount = 0;
        if (totalQty == 2) {
            qtyDiscount = subtotal * 0.05;
        } else if (totalQty >= 3) {
            qtyDiscount = subtotal * 0.06;
        }

        return qtyDiscount;
    }

    // TỔNG TIỀN CUỐI CÙNG
    public static double getTotalPrice() {
        double finalTotal = getSubtotal() - getDiscountAmount();
        return Math.max(0, finalTotal);
    }

    public static void clearCart() {
        cartList.clear();
    }
    
    public static void toggleAll(boolean selected) {
        for (CartItem item : cartList) {
            item.setSelected(selected);
        }
    }
}
