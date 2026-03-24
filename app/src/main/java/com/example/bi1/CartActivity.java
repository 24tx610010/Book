package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private static final int REQUEST_CODE_ADDRESS = 101;
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
    private String currentAddressId = "";
    
    private double discountAmount = 0;
    private Promotion appliedPromotion = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        
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

        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        userPhone = sp.getString("phone", "");
        currentReceiverName = sp.getString("username", "");
        currentReceiverPhone = userPhone;
        
        loadSavedAddress();

        btnBack.setOnClickListener(v -> finish());
        
        // CẬP NHẬT: Mở màn hình Sổ địa chỉ thay vì hiện Dialog
        btnChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressListActivity.class);
            intent.putExtra("selected_id", currentAddressId);
            startActivityForResult(intent, REQUEST_CODE_ADDRESS);
        });

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
            if (currentDetailAddress.isEmpty() || currentDetailAddress.equals("Vui lòng cập nhật địa chỉ")) {
                Toast.makeText(this, "Vui lòng chọn địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
                btnChangeAddress.performClick();
                return;
            }
            showPaymentDialog();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADDRESS && resultCode == RESULT_OK && data != null) {
            UserAddress selectedAddr = (UserAddress) data.getSerializableExtra("selected_address");
            if (selectedAddr != null) {
                currentReceiverName = selectedAddr.getReceiverName();
                currentReceiverPhone = selectedAddr.getReceiverPhone();
                currentDetailAddress = selectedAddr.getDetailAddress();
                currentAddressId = selectedAddr.getId();
                updateAddressUI();
                
                // Lưu tạm vào SP để lần sau mở lại vẫn thấy
                getSharedPreferences("auth", MODE_PRIVATE).edit()
                        .putString("ship_name", currentReceiverName)
                        .putString("ship_phone", currentReceiverPhone)
                        .putString("ship_address", currentDetailAddress).apply();
            }
        }
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
        txtAddressDisplay.setText(currentDetailAddress.isEmpty() ? "Vui lòng cập nhật địa chỉ" : currentDetailAddress);
    }

    private void showPaymentDialog() {
        double finalTotal = CartManager.getSubtotal() - discountAmount;
        String method = rgPaymentMethod.getCheckedRadioButtonId() == R.id.rbZaloPay ? "ZaloPay" : 
                       (rgPaymentMethod.getCheckedRadioButtonId() == R.id.rbMoMo ? "MoMo" : "COD");
        
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage("Tổng tiền: " + String.format("%,.0f đ", finalTotal) + "\nĐịa chỉ: " + currentDetailAddress)
                .setPositiveButton("ĐẶT HÀNG", (dialog, which) -> processCheckout(method, finalTotal))
                .setNegativeButton("HỦY", null).show();
    }

    private void processCheckout(String paymentMethod, double finalTotal) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order newOrder = new Order(orderId, userPhone, new Date(), paymentMethod, finalTotal, 0);
        newOrder.setReceiverName(currentReceiverName);
        newOrder.setReceiverPhone(currentReceiverPhone);
        newOrder.setShippingAddress(currentDetailAddress);

        WriteBatch batch = db.batch();
        batch.set(db.collection("orders").document(orderId), newOrder);

        for (CartItem cartItem : cartItems) {
            String detailId = UUID.randomUUID().toString().substring(0, 10);
            OrderDetail detail = new OrderDetail(detailId, orderId, cartItem.getBookId(), cartItem.getBookName(), 
                                                 cartItem.getUnitPrice(), cartItem.getQuantity(), cartItem.getTotalPrice());
            batch.set(db.collection("order_details").document(detailId), detail);
        }

        // Tích lũy điểm vào bảng LoyalCustomers
        if (finalTotal >= 200000) {
            db.collection("loyal_customers").whereEqualTo("userId", userPhone).limit(1).get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        DocumentReference loyalRef = snapshots.getDocuments().get(0).getReference();
                        db.runTransaction(transaction -> {
                            Long currentPoints = transaction.get(loyalRef).getLong("points");
                            if (currentPoints == null) currentPoints = 0L;
                            transaction.update(loyalRef, "points", currentPoints + 1);
                            return null;
                        });
                    } else {
                        String loyalId = "LOY-" + UUID.randomUUID().toString().substring(0, 8);
                        LoyalCustomer lc = new LoyalCustomer(loyalId, userPhone, 1);
                        db.collection("loyal_customers").document(loyalId).set(lc);
                    }
                });
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đặt hàng thành công! Mã: " + orderId, Toast.LENGTH_LONG).show();
            CartManager.clearCart();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override public void onCartChanged() { updateTotal(); }

    private void updateTotal() {
        double total = Math.max(0, CartManager.getSubtotal() - discountAmount);
        txtTotal.setText(String.format("%,.0f đ", total));
    }
}
