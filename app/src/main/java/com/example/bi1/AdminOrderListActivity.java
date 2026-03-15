package com.example.bi1;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminOrderListActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private AdminOrderAdapter adapter;
    private ArrayList<Order> orderList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_list);

        db = FirebaseFirestore.getInstance();
        rvOrders = findViewById(R.id.rvAdminOrderList);
        ImageButton btnBack = findViewById(R.id.btnBackAdminOrder);

        btnBack.setOnClickListener(v -> finish());

        orderList = new ArrayList<>();
        adapter = new AdminOrderAdapter(this, orderList);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        listenToAllOrders();
    }

    private void listenToAllOrders() {
        // Admin xem tất cả các đơn hàng, sắp xếp theo thời gian mới nhất
        db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
