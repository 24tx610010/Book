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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminOrderListActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private AdminOrderAdapter adapter;
    private ArrayList<Order> orderList;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;
    private boolean isErrorShown = false;

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
        if (orderListener != null) orderListener.remove();
        
        orderListener = db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AdminOrderList", "Firestore Listen Error: " + error.getMessage());
                        // Chỉ hiện Toast một lần duy nhất nếu bị lỗi quyền
                        if (!isErrorShown) {
                            Toast.makeText(this, "Lỗi truy cập dữ liệu. Vui lòng kiểm tra Rules trên Firebase Console!", Toast.LENGTH_LONG).show();
                            isErrorShown = true;
                        }
                        return;
                    }

                    if (value != null) {
                        isErrorShown = false; // Reset cờ khi tải thành công
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Order order = doc.toObject(Order.class);
                            order.setId(doc.getId());
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) orderListener.remove();
    }
}
