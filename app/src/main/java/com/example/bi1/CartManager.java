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
                qty,
                book.getHinhAnh(),
                book.getMoTa(),
                book.getTacGia(),
                book.getNhaXuatBan(),
                book.getNamXuatBan(),
                book.getNgonNgu()
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

    public static double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public static void clearCart() {
        cartList.clear();
    }
}
