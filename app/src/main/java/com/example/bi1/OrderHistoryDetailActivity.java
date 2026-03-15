package com.example.bi1;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderHistoryDetailActivity extends AppCompatActivity {

    private TextView txtOrderId, txtDate, txtStatus, txtPayment, txtTotal;
    private RecyclerView rvItems;
    private OrderDetailAdapter adapter;
    private ArrayList<OrderDetail> detailList;
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history_detail);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        txtOrderId = findViewById(R.id.txtDetailOrderId);
        txtDate = findViewById(R.id.txtDetailOrderDate);
        txtStatus = findViewById(R.id.txtDetailOrderStatus);
        txtPayment = findViewById(R.id.txtDetailPayment);
        txtTotal = findViewById(R.id.txtDetailOrderTotal);
        rvItems = findViewById(R.id.rvOrderDetailItems);
        ImageButton btnBack = findViewById(R.id.btnBackOrderDetail);

        btnBack.setOnClickListener(v -> finish());

        // Lấy dữ liệu đơn hàng từ Intent
        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            displayOrderInfo(order);
            loadOrderDetails(order.getId());
        }

        // Cấu hình RecyclerView
        detailList = new ArrayList<>();
        adapter = new OrderDetailAdapter(detailList);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);
    }

    private void displayOrderInfo(Order order) {
        txtOrderId.setText("Mã đơn: " + order.getId());
        txtDate.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
        txtPayment.setText("Thanh toán: " + order.getPaymentMethod());
        txtTotal.setText(String.format("%,.0f đ", order.getTotalAmount()));

        switch (order.getStatus()) {
            case 0: txtStatus.setText("Trạng thái: Đang chờ duyệt"); break;
            case 1: txtStatus.setText("Trạng thái: Đã duyệt"); break;
            case 2: txtStatus.setText("Trạng thái: Đã hủy"); break;
        }
    }

    private void loadOrderDetails(String orderId) {
        db.collection("order_details")
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    detailList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        detailList.add(doc.toObject(OrderDetail.class));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
