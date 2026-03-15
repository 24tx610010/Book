package com.example.bi1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
    private TextView txtTotal;
    private Button btnCheckout;
    private ImageButton btnBack;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        rvCart = findViewById(R.id.rvCart);
        txtTotal = findViewById(R.id.txtTotalCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBackCart);

        btnBack.setOnClickListener(v -> finish());

        cartItems = CartManager.getCartList();

        adapter = new CartAdapter(this, cartItems, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        updateTotal();

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentDialog();
        });
    }

    private void showPaymentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông tin thanh toán");
        builder.setMessage("Vui lòng chuyển khoản tổng tiền vào số tài khoản sau:\n\n" +
                "Ngân hàng: ACB\n" +
                "Số tài khoản: 31568367\n" +
                "Chủ TK: NGUYEN VAN A\n\n" +
                "Nội dung: [Số điện thoại] Thanh toan don hang");

        builder.setPositiveButton("XÁC NHẬN ĐÃ CHUYỂN", (dialog, which) -> {
            processCheckout();
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void processCheckout() {
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String userPhone = sp.getString("phone", "");

        if (userPhone.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo mã đơn hàng duy nhất
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double total = CartManager.getTotalPrice();

        // 2. Tạo đối tượng Order
        Order newOrder = new Order();
        newOrder.setId(orderId);
        newOrder.setUserId(userPhone);
        newOrder.setOrderDate(new Date());
        newOrder.setPaymentMethod("Chuyển khoản ACB");
        newOrder.setTotalAmount(total);
        newOrder.setStatus(0); // 0: Đã đặt

        // Sử dụng WriteBatch để lưu nhiều dữ liệu cùng lúc lên Firebase
        WriteBatch batch = db.batch();

        // Thêm đơn hàng vào collection "orders"
        batch.set(db.collection("orders").document(orderId), newOrder);

        // 3. Tạo và thêm các chi tiết đơn hàng vào collection "order_details"
        for (CartItem cartItem : cartItems) {
            String detailId = UUID.randomUUID().toString().substring(0, 10);
            OrderDetail detail = new OrderDetail(
                    detailId,
                    orderId,
                    cartItem.getBookId(),
                    cartItem.getBookName(),
                    cartItem.getUnitPrice(),
                    cartItem.getQuantity(),
                    cartItem.getTotalPrice()
            );
            batch.set(db.collection("order_details").document(detailId), detail);
        }

        // Thực thi lưu lên Firebase
        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đặt hàng thành công! Mã đơn: " + orderId, Toast.LENGTH_LONG).show();
            CartManager.clearCart();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCartChanged() {
        updateTotal();
    }

    private void updateTotal() {
        double total = CartManager.getTotalPrice();
        txtTotal.setText(String.format("%,.0f đ", total));
    }
}
