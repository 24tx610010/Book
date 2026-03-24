package com.example.bi1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class AdminLoyalCustomersActivity extends AppCompatActivity {

    private RecyclerView rvLoyalCustomers;
    private LoyalCustomerAdapter adapter;
    private ArrayList<User> userList;
    private FirebaseFirestore db;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_loyal_customers);

        db = FirebaseFirestore.getInstance();
        rvLoyalCustomers = findViewById(R.id.rvLoyalCustomers);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        userList = new ArrayList<>();
        adapter = new LoyalCustomerAdapter(userList);
        rvLoyalCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvLoyalCustomers.setAdapter(adapter);

        loadLoyalCustomers();
    }

    private void loadLoyalCustomers() {
        // Lấy dữ liệu từ bảng loyal_customers
        db.collection("loyal_customers")
                .get()
                .addOnSuccessListener(loyalSnapshots -> {
                    userList.clear();
                    if (loyalSnapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Chưa có dữ liệu khách hàng thân thiết", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    for (QueryDocumentSnapshot loyalDoc : loyalSnapshots) {
                        // userId ở đây là số điện thoại dùng để link với document ID trong bảng users
                        String phone = loyalDoc.getString("userId"); 
                        Long points = loyalDoc.getLong("points");
                        final int intPoints = (points != null) ? points.intValue() : 0;

                        if (phone != null) {
                            // Truy vấn trực tiếp bằng document ID (vì document ID của bảng users là số điện thoại)
                            db.collection("users").document(phone).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            User user = documentSnapshot.toObject(User.class);
                                            if (user != null) {
                                                user.setLoyaltyPoints(intPoints);
                                                // Kiểm tra tránh trùng lặp do bất đồng bộ
                                                boolean exists = false;
                                                for(User u : userList) {
                                                    if(u.getPhone().equals(user.getPhone())) {
                                                        exists = true; break;
                                                    }
                                                }
                                                if(!exists) userList.add(user);
                                            }
                                        }
                                        
                                        // Sắp xếp và cập nhật UI sau mỗi lần lấy được user
                                        Collections.sort(userList, (u1, u2) -> Integer.compare(u2.getLoyaltyPoints(), u1.getLoyaltyPoints()));
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminLoyalCustomers", "Lỗi: " + e.getMessage());
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
