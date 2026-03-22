package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AccountFragment extends Fragment {

    private TextView txtUsername, txtPhone, txtRole, txtHistoryTitle;
    private Button btnLogout, btnManageCategories, btnManageOrders, btnManageUsers, btnManageBooks;
    private View layoutAdminTools;
    private RecyclerView rvOrderHistory;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> orderList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        db = FirebaseFirestore.getInstance();

        txtUsername = view.findViewById(R.id.txtProfileUsername);
        txtPhone = view.findViewById(R.id.txtProfilePhone);
        txtRole = view.findViewById(R.id.txtProfileRole);
        txtHistoryTitle = view.findViewById(R.id.txtHistoryTitle);
        rvOrderHistory = view.findViewById(R.id.rvOrderHistory);
        btnLogout = view.findViewById(R.id.btnLogout);
        layoutAdminTools = view.findViewById(R.id.layoutAdminTools);
        btnManageCategories = view.findViewById(R.id.btnManageCategories);
        btnManageOrders = view.findViewById(R.id.btnManageOrders);
        btnManageUsers = view.findViewById(R.id.btnManageUsers);
        btnManageBooks = view.findViewById(R.id.btnManageBooks);

        SharedPreferences sp = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String username = sp.getString("username", "Chưa cập nhật");
        String phone = sp.getString("phone", "");
        int roleId = sp.getInt("roleid", 2);

        txtUsername.setText(username);
        txtPhone.setText(phone);

        if (roleId == 1) {
            txtRole.setText("Quản trị viên (Admin)");
            layoutAdminTools.setVisibility(View.VISIBLE);
            txtHistoryTitle.setVisibility(View.GONE);
            rvOrderHistory.setVisibility(View.GONE);
        } else {
            txtRole.setText("Khách hàng (User)");
            layoutAdminTools.setVisibility(View.GONE);
            txtHistoryTitle.setVisibility(View.VISIBLE);
            rvOrderHistory.setVisibility(View.VISIBLE);
            setupOrderHistory(phone);
        }

        btnManageCategories.setOnClickListener(v -> startActivity(new Intent(getContext(), CategoryListActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), AdminOrderListActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(getContext(), AdminUserListActivity.class)));
        
        // SỬA LỖI: Gọi hàm chuyển trang một cách an toàn
        btnManageBooks.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).refreshToHome();
            }
        });

        btnLogout.setOnClickListener(v -> {
            sp.edit().putBoolean("logged_in", false).apply();
            Toast.makeText(getContext(), "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void setupOrderHistory(String phone) {
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(getContext(), orderList);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
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
