package com.example.bi1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
    private TextView txtTotal, txtCustomerInfo, txtAddressDisplay;
    private Button btnCheckout, btnApplyVoucher;
    private ImageButton btnBack;
    private TextView btnChangeAddress;
    private EditText edtVoucherCode;
    private RadioGroup rgPaymentMethod;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;
    
    private String currentReceiverName = "";
    private String currentReceiverPhone = "";
    private String currentDetailAddress = "";
    private String userPhone = "";
    
    private double discountAmount = 0;
    private Promotion appliedPromotion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        
        // Ánh xạ
        rvCart = findViewById(R.id.rvCart);
        txtTotal = findViewById(R.id.txtTotalCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBackCart);
        txtCustomerInfo = findViewById(R.id.txtCustomerInfo);
        txtAddressDisplay = findViewById(R.id.txtAddressDisplay);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);
        
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);

        // Lấy thông tin người dùng hiện tại
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        userPhone = sp.getString("phone", "");
        currentReceiverName = sp.getString("username", "");
        currentReceiverPhone = userPhone;
        
        loadSavedAddress();

        btnBack.setOnClickListener(v -> finish());
        btnChangeAddress.setOnClickListener(v -> showAddressDialog());

        cartItems = CartManager.getCartList();
        adapter = new CartAdapter(this, cartItems, this);
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        updateTotal();

        btnApplyVoucher.setOnClickListener(v -> {
            String code = edtVoucherCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
                return;
            }
            checkVoucher(code);
        });

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentDetailAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
                showAddressDialog();
                return;
            }
            showPaymentDialog();
        });
    }

    private void loadSavedAddress() {
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        currentReceiverName = sp.getString("ship_name", currentReceiverName);
        currentReceiverPhone = sp.getString("ship_phone", currentReceiverPhone);
        currentDetailAddress = sp.getString("ship_address", "");

        updateAddressUI();
    }

    private void updateAddressUI() {
        txtCustomerInfo.setText(currentReceiverName + " | " + currentReceiverPhone);
        if (!currentDetailAddress.isEmpty()) {
            txtAddressDisplay.setText(currentDetailAddress);
        } else {
            txtAddressDisplay.setText("Vui lòng cập nhật địa chỉ giao hàng");
        }
    }

    private void showAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_address, null);
        
        final EditText edtName = view.findViewById(R.id.edtReceiverName);
        final EditText edtPhone = view.findViewById(R.id.edtReceiverPhone);
        final EditText edtAddress = view.findViewById(R.id.edtNewAddress);

        edtName.setText(currentReceiverName);
        edtPhone.setText(currentReceiverPhone);
        edtAddress.setText(currentDetailAddress);
        
        builder.setView(view);
        builder.setPositiveButton("XÁC NHẬN", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String addr = edtAddress.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            currentReceiverName = name;
            currentReceiverPhone = phone;
            currentDetailAddress = addr;

            updateAddressUI();

            // Lưu lại cho lần sau
            getSharedPreferences("auth", MODE_PRIVATE).edit()
                    .putString("ship_name", currentReceiverName)
                    .putString("ship_phone", currentReceiverPhone)
                    .putString("ship_address", currentDetailAddress)
                    .apply();
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

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

    private void showPaymentDialog() {
        String paymentMethod = getSelectedPaymentMethod();
        double finalTotal = CartManager.getSubtotal() - discountAmount;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt hàng");
        builder.setMessage("Người nhận: " + currentReceiverName + " (" + currentReceiverPhone + ")" +
                "\nĐịa chỉ: " + currentDetailAddress + 
                "\nTổng tiền: " + String.format("%,.0f đ", finalTotal) + 
                (discountAmount > 0 ? "\n(Đã giảm: " + String.format("%,.0f đ", discountAmount) + ")" : "") +
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
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order newOrder = new Order();
        newOrder.setId(orderId);
        newOrder.setUserId(userPhone);
        newOrder.setReceiverName(currentReceiverName);
        newOrder.setReceiverPhone(currentReceiverPhone);
        newOrder.setShippingAddress(currentDetailAddress);
        newOrder.setOrderDate(new Date());
        newOrder.setPaymentMethod(paymentMethod);
        newOrder.setTotalAmount(finalTotal);
        newOrder.setStatus(0);

        WriteBatch batch = db.batch();
        batch.set(db.collection("orders").document(orderId), newOrder);

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
        if (appliedPromotion != null) {
            applyPromotion(appliedPromotion);
        } else {
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = CartManager.getSubtotal() - discountAmount;
        if (total < 0) total = 0;
        txtTotal.setText(String.format("%,.0f đ", total));
    }
}
