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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtUsername, txtPhone, txtRole, txtHistoryTitle;
<<<<<<< HEAD
    private Button btnLogout, btnManageCategories, btnManageOrders;
=======
    private Button btnLogout, btnManageCategories, btnManageOrders, btnManageUsers;
>>>>>>> 0d5c59f (22/3)
    private ImageButton btnBack;
    private LinearLayout layoutAdminTools;
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
        txtHistoryTitle = findViewById(R.id.txtHistoryTitle);
        rvOrderHistory = findViewById(R.id.rvOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);
        
        layoutAdminTools = findViewById(R.id.layoutAdminTools);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        btnManageOrders = findViewById(R.id.btnManageOrders);
<<<<<<< HEAD
=======
        btnManageUsers = findViewById(R.id.btnManageUsers);
>>>>>>> 0d5c59f (22/3)
        
        btnBack = findViewById(R.id.btnBackProfile);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Lấy thông tin từ SharedPreferences
        SharedPreferences sp = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String username = sp.getString("username", "Chưa cập nhật");
        String phone = sp.getString("phone", "");
        int roleId = sp.getInt("roleid", 2);

        // Hiển thị thông tin
        txtUsername.setText(username);
        txtPhone.setText(phone);
        
        if (roleId == 1) {
            txtRole.setText("Quản trị viên (Admin)");
<<<<<<< HEAD
            // Hiện công cụ Admin, ẩn lịch sử khách hàng
=======
>>>>>>> 0d5c59f (22/3)
            layoutAdminTools.setVisibility(View.VISIBLE);
            txtHistoryTitle.setVisibility(View.GONE);
            rvOrderHistory.setVisibility(View.GONE);
        } else {
            txtRole.setText("Khách hàng (User)");
<<<<<<< HEAD
            // Ẩn công cụ Admin, hiện lịch sử khách hàng
            layoutAdminTools.setVisibility(View.GONE);
            txtHistoryTitle.setVisibility(View.VISIBLE);
            rvOrderHistory.setVisibility(View.VISIBLE);
            
            setupOrderHistory(phone);
        }

        // Mở trang quản lý loại sách
        btnManageCategories.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryListActivity.class));
        });

        // Mở trang quản lý đơn hàng
        btnManageOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminOrderListActivity.class));
        });

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("logged_in", false);
            editor.apply();

            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();

=======
            layoutAdminTools.setVisibility(View.GONE);
            txtHistoryTitle.setVisibility(View.VISIBLE);
            rvOrderHistory.setVisibility(View.VISIBLE);
            setupOrderHistory(phone);
        }

        btnManageCategories.setOnClickListener(v -> startActivity(new Intent(this, CategoryListActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(this, AdminOrderListActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUserListActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sp.edit().putBoolean("logged_in", false).apply();
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
>>>>>>> 0d5c59f (22/3)
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupOrderHistory(String phone) {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(orderAdapter);

        db.collection("orders")
                .whereEqualTo("userId", phone)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
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
}
