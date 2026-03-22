package com.example.bi1;

import android.content.Context;
<<<<<<< HEAD
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
=======
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
>>>>>>> 0d5c59f (22/3)
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
<<<<<<< HEAD
=======
import com.google.firebase.firestore.QueryDocumentSnapshot;
>>>>>>> 0d5c59f (22/3)
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
<<<<<<< HEAD
    private TextView txtTotal;
    private Button btnCheckout;
    private ImageButton btnBack;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
=======
    private TextView txtTotal, txtCustomerInfo, txtAddressDisplay;
    private Button btnCheckout, btnApplyVoucher;
    private ImageButton btnBack;
    private TextView btnChangeAddress;
    private EditText edtVoucherCode;
    private RadioGroup rgPaymentMethod;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    
    private String currentAddress = "";
    private String userPhone = "";
    private String userName = "";
    
    private double discountAmount = 0;
    private Promotion appliedPromotion = null;
>>>>>>> 0d5c59f (22/3)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
<<<<<<< HEAD
=======
        
        // Ánh xạ
>>>>>>> 0d5c59f (22/3)
        rvCart = findViewById(R.id.rvCart);
        txtTotal = findViewById(R.id.txtTotalCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBackCart);
<<<<<<< HEAD

        btnBack.setOnClickListener(v -> finish());

        cartItems = CartManager.getCartList();

=======
        txtCustomerInfo = findViewById(R.id.txtCustomerInfo);
        txtAddressDisplay = findViewById(R.id.txtAddressDisplay);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);
        
        // Ánh xạ phần mới
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);

        // Lấy thông tin người dùng hiện tại
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        userPhone = sp.getString("phone", "");
        userName = sp.getString("username", "Khách hàng");
        
        // Hiển thị thông tin mặc định
        txtCustomerInfo.setText(userName + " | " + userPhone);
        loadDefaultAddress();

        btnBack.setOnClickListener(v -> finish());
        
        btnChangeAddress.setOnClickListener(v -> showAddressDialog());

        cartItems = CartManager.getCartList();
>>>>>>> 0d5c59f (22/3)
        adapter = new CartAdapter(this, cartItems, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        updateTotal();

<<<<<<< HEAD
=======
        // Xử lý Voucher
        btnApplyVoucher.setOnClickListener(v -> {
            String code = edtVoucherCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
                return;
            }
            checkVoucher(code);
        });

>>>>>>> 0d5c59f (22/3)
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }
<<<<<<< HEAD
=======
            if (currentAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
                showAddressDialog();
                return;
            }
>>>>>>> 0d5c59f (22/3)
            showPaymentDialog();
        });
    }

<<<<<<< HEAD
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
=======
    private void checkVoucher(String code) {
        db.collection("promotions")
                .whereEqualTo("code", code)
                .whereEqualTo("status", 1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Mã voucher không tồn tại hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                        resetDiscount();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Promotion promo = doc.toObject(Promotion.class);
                        
                        // Kiểm tra ngày
                        Date now = new Date();
                        if (now.before(promo.getStartDate())) {
                            Toast.makeText(this, "Voucher chưa đến thời gian sử dụng", Toast.LENGTH_SHORT).show();
                            continue;
                        }
                        if (now.after(promo.getEndDate())) {
                            Toast.makeText(this, "Voucher đã hết hạn sử dụng", Toast.LENGTH_SHORT).show();
                            continue;
                        }

                        applyPromotion(promo);
                        return;
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kiểm tra voucher", Toast.LENGTH_SHORT).show());
    }

    private void applyPromotion(Promotion promo) {
        double subtotal = CartManager.getSubtotal();
        double discount = 0;

        if (promo.getType().equals("all")) {
            discount = subtotal * (promo.getDiscountValue() / 100.0);
        } else if (promo.getType().equals("category")) {
            for (CartItem item : cartItems) {
                if (item.isSelected() && item.getCategoryId() != null && item.getCategoryId().equals(promo.getTargetId())) {
                    discount += item.getTotalPrice() * (promo.getDiscountValue() / 100.0);
                }
            }
        } else if (promo.getType().equals("book")) {
            for (CartItem item : cartItems) {
                if (item.isSelected() && item.getBookId().equals(promo.getTargetId())) {
                    discount += item.getTotalPrice() * (promo.getDiscountValue() / 100.0);
                }
            }
        }

        if (discount > 0) {
            appliedPromotion = promo;
            discountAmount = discount;
            updateTotal();
            Toast.makeText(this, "Đã áp dụng mã giảm giá: " + String.format("%,.0f đ", discount), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Voucher không áp dụng cho các sản phẩm trong giỏ", Toast.LENGTH_SHORT).show();
            resetDiscount();
        }
    }

    private void resetDiscount() {
        appliedPromotion = null;
        discountAmount = 0;
        updateTotal();
    }

    private void loadDefaultAddress() {
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        currentAddress = sp.getString("last_address", "");
        if (!currentAddress.isEmpty()) {
            txtAddressDisplay.setText(currentAddress);
        } else {
            txtAddressDisplay.setText("Vui lòng cập nhật địa chỉ giao hàng");
        }
    }

    private void showAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Địa chỉ giao hàng");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_address, null);
        final EditText edtNewAddress = view.findViewById(R.id.edtNewAddress);
        edtNewAddress.setText(currentAddress);
        
        builder.setView(view);
        builder.setPositiveButton("XÁC NHẬN", (dialog, which) -> {
            String newAddr = edtNewAddress.getText().toString().trim();
            if (!newAddr.isEmpty()) {
                currentAddress = newAddr;
                txtAddressDisplay.setText(currentAddress);
                getSharedPreferences("auth", MODE_PRIVATE).edit().putString("last_address", currentAddress).apply();
            }
>>>>>>> 0d5c59f (22/3)
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

<<<<<<< HEAD
    private void processCheckout() {
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String userPhone = sp.getString("phone", "");

=======
    private void showPaymentDialog() {
        String paymentMethod = getSelectedPaymentMethod();
        double finalTotal = CartManager.getSubtotal() - discountAmount;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt hàng");
        builder.setMessage("Tổng tiền: " + String.format("%,.0f đ", finalTotal) + 
                (discountAmount > 0 ? "\n(Đã giảm: " + String.format("%,.0f đ", discountAmount) + ")" : "") +
                "\nĐịa chỉ: " + currentAddress + 
                "\nThanh toán: " + paymentMethod +
                "\n\nBạn có chắc chắn muốn đặt hàng?");

        builder.setPositiveButton("ĐẶT HÀNG", (dialog, which) -> {
            processCheckout(paymentMethod, finalTotal);
        });
        builder.setNegativeButton("QUAY LẠI", null);
        builder.show();
    }

    private String getSelectedPaymentMethod() {
        int id = rgPaymentMethod.getCheckedRadioButtonId();
        if (id == R.id.rbZaloPay) return "ZaloPay";
        if (id == R.id.rbMoMo) return "MoMo";
        return "Thanh toán khi nhận hàng (COD)";
    }

    private void processCheckout(String paymentMethod, double finalTotal) {
>>>>>>> 0d5c59f (22/3)
        if (userPhone.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

<<<<<<< HEAD
        // 1. Tạo mã đơn hàng duy nhất
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double total = CartManager.getTotalPrice();

        // 2. Tạo đối tượng Order
=======
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

>>>>>>> 0d5c59f (22/3)
        Order newOrder = new Order();
        newOrder.setId(orderId);
        newOrder.setUserId(userPhone);
        newOrder.setOrderDate(new Date());
<<<<<<< HEAD
        newOrder.setPaymentMethod("Chuyển khoản ACB");
        newOrder.setTotalAmount(total);
        newOrder.setStatus(0); // 0: Đã đặt

        // Sử dụng WriteBatch để lưu nhiều dữ liệu cùng lúc lên Firebase
        WriteBatch batch = db.batch();

        // Thêm đơn hàng vào collection "orders"
        batch.set(db.collection("orders").document(orderId), newOrder);

        // 3. Tạo và thêm các chi tiết đơn hàng vào collection "order_details"
=======
        newOrder.setPaymentMethod(paymentMethod);
        newOrder.setTotalAmount(finalTotal);
        newOrder.setStatus(0);

        WriteBatch batch = db.batch();
        batch.set(db.collection("orders").document(orderId), newOrder);

>>>>>>> 0d5c59f (22/3)
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

<<<<<<< HEAD
        // Thực thi lưu lên Firebase
=======
>>>>>>> 0d5c59f (22/3)
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
<<<<<<< HEAD
        updateTotal();
    }

    private void updateTotal() {
        double total = CartManager.getTotalPrice();
=======
        if (appliedPromotion != null) {
            applyPromotion(appliedPromotion);
        } else {
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = CartManager.getSubtotal() - discountAmount;
        if (total < 0) total = 0;
>>>>>>> 0d5c59f (22/3)
        txtTotal.setText(String.format("%,.0f đ", total));
    }
}
