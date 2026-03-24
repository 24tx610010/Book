package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtUsername, txtPhone, txtRole, txtHistoryTitle, txtLoyalty;
    private Button btnLogout, btnManageCategories, btnManageOrders, btnManageUsers;
    private ImageButton btnBack;
    private LinearLayout layoutAdminTools;
    private CardView cardLoyalty;
    private RecyclerView rvOrderHistory;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> orderList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        txtUsername = findViewById(R.id.txtProfileUsername);
        txtPhone = findViewById(R.id.txtProfilePhone);
        txtRole = findViewById(R.id.txtProfileRole);
        txtLoyalty = findViewById(R.id.txtProfileLoyalty);
        cardLoyalty = findViewById(R.id.cardLoyalty);
        txtHistoryTitle = findViewById(R.id.txtHistoryTitle);
        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);
        
        layoutAdminTools = findViewById(R.id.layoutAdminTools);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        
        btnBack = findViewById(R.id.btnBackProfile);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Lấy thông tin từ SharedPreferences
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String username = sp.getString("username", "Người dùng");
        String phone = sp.getString("phone", "");
        int roleId = sp.getInt("roleid", 2);

        // Hiển thị thông tin
        txtUsername.setText(username);
        if (phone.contains("@")) {
            txtPhone.setText("Email: " + phone);
        } else {
            txtPhone.setText("SĐT: " + (phone.isEmpty() ? "Chưa cập nhật" : phone));
        }
        
        if (roleId == 1) {
            txtRole.setText("Quản trị viên (Admin)");
            layoutAdminTools.setVisibility(View.VISIBLE);
            txtHistoryTitle.setVisibility(View.GONE);
            rvOrderHistory.setVisibility(View.GONE);
            cardLoyalty.setVisibility(View.GONE); // Admin không cần thẻ điểm
        } else {
            txtRole.setText("Khách hàng");
            layoutAdminTools.setVisibility(View.GONE);
            txtHistoryTitle.setVisibility(View.VISIBLE);
            rvOrderHistory.setVisibility(View.VISIBLE);
            cardLoyalty.setVisibility(View.VISIBLE);
            
            loadLoyaltyPoints(phone);
            setupOrderHistory(phone);
        }

        btnManageCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoryListActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(this, AdminOrderListActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUserListActivity.class)));

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadLoyaltyPoints(String phone) {
        if (phone.isEmpty()) return;
        
        db.collection("users").document(phone)
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        Long points = value.getLong("LoyaltyPoints");
                        txtLoyalty.setText((points != null ? points : 0) + " điểm");
                        
                        TextView txtNote = findViewById(R.id.txtLoyaltyNote);
                        if (points != null && points >= 10) {
                            txtNote.setText("Bạn đã đủ 10 điểm! Đã được reset để nhận ưu đãi.");
                        } else {
                            txtNote.setText("Vui lòng mua thêm sách để tích lũy đủ 10 điểm.");
                        }
                    }
                });
    }

    private void setupOrderHistory(String phone) {
        if (phone.isEmpty()) return;
        
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(orderAdapter);

        db.collection("orders")
                .whereEqualTo("userId", phone)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        }
                        orderAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
