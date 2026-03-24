package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class AccountFragment extends Fragment {

    private TextView txtUsername, txtPhone, txtRole, txtLoyaltyPoints, txtLoyaltyStatus;
    private Button btnLogout, btnManageCategories, btnManageOrders, btnManageUsers, btnManageBooks, btnRedeemGift, btnLoyalCustomers;
    private View layoutAdminTools, cardLoyalty;
    private FirebaseFirestore db;
    private String userPhone;
    private ListenerRegistration loyaltyListener;
    private boolean isErrorShown = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        txtUsername = view.findViewById(R.id.txtProfileUsername);
        txtPhone = view.findViewById(R.id.txtProfilePhone);
        txtRole = view.findViewById(R.id.txtProfileRole);
        txtLoyaltyPoints = view.findViewById(R.id.txtLoyaltyPoints);
        txtLoyaltyStatus = view.findViewById(R.id.txtLoyaltyStatus);
        
        cardLoyalty = view.findViewById(R.id.cardLoyalty);
        btnRedeemGift = view.findViewById(R.id.btnRedeemGift);
        btnLogout = view.findViewById(R.id.btnLogout);
        layoutAdminTools = view.findViewById(R.id.layoutAdminTools);
        
        btnManageCategories = view.findViewById(R.id.btnManageCategories);
        btnManageOrders = view.findViewById(R.id.btnManageOrders);
        btnManageUsers = view.findViewById(R.id.btnManageUsers);
        btnManageBooks = view.findViewById(R.id.btnManageBooks);
        btnLoyalCustomers = view.findViewById(R.id.btnLoyalCustomers);

        // Lấy thông tin từ SharedPreferences
        SharedPreferences sp = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String username = sp.getString("username", "Chưa cập nhật");
        userPhone = sp.getString("phone", "");
        int roleId = sp.getInt("roleid", 2);

        txtUsername.setText(username);
        txtPhone.setText(userPhone.isEmpty() ? "Đăng nhập Google/FB" : userPhone);

        if (roleId == 1) {
            txtRole.setText("Quản trị viên (Admin)");
            layoutAdminTools.setVisibility(View.VISIBLE);
            cardLoyalty.setVisibility(View.GONE);
        } else {
            txtRole.setText("Khách hàng (User)");
            layoutAdminTools.setVisibility(View.GONE);
            cardLoyalty.setVisibility(View.VISIBLE);
            loadLoyaltyPoints();
        }

        // Sự kiện các nút Admin
        btnManageCategories.setOnClickListener(v -> startActivity(new Intent(getContext(), CategoryListActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(getContext(), AdminOrderListActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(getContext(), AdminUserListActivity.class)));
        btnManageBooks.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).refreshToHome();
            }
        });
        
        if (btnLoyalCustomers != null) {
            btnLoyalCustomers.setOnClickListener(v -> startActivity(new Intent(getContext(), AdminLoyalCustomersActivity.class)));
        }

        btnRedeemGift.setOnClickListener(v -> redeemGift());

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

    private void loadLoyaltyPoints() {
        if (userPhone.isEmpty()) return;

        if (loyaltyListener != null) loyaltyListener.remove();

        // Lấy điểm từ bảng loyal_customers theo userId (phone)
        loyaltyListener = db.collection("loyal_customers").whereEqualTo("userId", userPhone).limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AccountFragment", "Lỗi tải điểm: " + error.getMessage());
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        Long points = value.getDocuments().get(0).getLong("points");
                        if (points == null) points = 0L;

                        txtLoyaltyPoints.setText(points + " điểm");

                        if (points >= 10) {
                            txtLoyaltyStatus.setText("Chúc mừng! Bạn đã đủ điểm đổi quà.");
                            btnRedeemGift.setVisibility(View.VISIBLE);
                        } else {
                            txtLoyaltyStatus.setText("Bạn chưa đủ điểm, vui lòng mua sách để tích lũy");
                            btnRedeemGift.setVisibility(View.GONE);
                        }
                    } else {
                        txtLoyaltyPoints.setText("0 điểm");
                        txtLoyaltyStatus.setText("Bạn chưa đủ điểm, vui lòng mua sách để tích lũy");
                        btnRedeemGift.setVisibility(View.GONE);
                    }
                });
    }

    private void redeemGift() {
        if (userPhone.isEmpty()) return;

        db.collection("loyal_customers").whereEqualTo("userId", userPhone).limit(1).get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        DocumentReference loyalRef = snapshots.getDocuments().get(0).getReference();
                        db.runTransaction(transaction -> {
                            Long currentPoints = transaction.get(loyalRef).getLong("points");
                            if (currentPoints != null && currentPoints >= 10) {
                                transaction.update(loyalRef, "points", currentPoints - 10);
                                return true;
                            }
                            return false;
                        }).addOnSuccessListener(success -> {
                            if (success) {
                                Toast.makeText(getContext(), "Đổi quà thành công! Tủ đựng sách sẽ được gửi đến bạn.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Không đủ điểm để đổi quà!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loyaltyListener != null) loyaltyListener.remove();
    }
}
