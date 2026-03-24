package com.example.bi1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderHistoryDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHANGE_ADDRESS = 202;
    private TextView txtOrderId, txtDate, txtStatus, txtPayment, txtTotal;
    private TextView txtReceiver, txtAddress;
    private TextView btnEditAddress;
    private RecyclerView rvItems;
    private OrderDetailAdapter adapter;
    private ArrayList<OrderDetail> detailList;
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private Order currentOrder;

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
        txtReceiver = findViewById(R.id.txtDetailReceiver);
        txtAddress = findViewById(R.id.txtDetailAddress);
        btnEditAddress = findViewById(R.id.btnEditShippingAddress);
        rvItems = findViewById(R.id.rvOrderDetailItems);
        ImageButton btnBack = findViewById(R.id.btnBackOrderDetail);

        btnBack.setOnClickListener(v -> finish());

        // Lấy dữ liệu đơn hàng từ Intent
        currentOrder = (Order) getIntent().getSerializableExtra("order");
        if (currentOrder != null) {
            displayOrderInfo(currentOrder);
            loadOrderDetails(currentOrder.getId());
        }

        // Sự kiện thay đổi địa chỉ
        btnEditAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressListActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CHANGE_ADDRESS);
        });

        // Cấu hình RecyclerView
        detailList = new ArrayList<>();
        adapter = new OrderDetailAdapter(detailList);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHANGE_ADDRESS && resultCode == RESULT_OK && data != null) {
            UserAddress selectedAddr = (UserAddress) data.getSerializableExtra("selected_address");
            if (selectedAddr != null) {
                updateOrderAddress(selectedAddr);
            }
        }
    }

    private void updateOrderAddress(UserAddress newAddr) {
        if (currentOrder == null) return;

        db.collection("orders").document(currentOrder.getId())
                .update("receiverName", newAddr.getReceiverName(),
                        "receiverPhone", newAddr.getReceiverPhone(),
                        "shippingAddress", newAddr.getDetailAddress())
                .addOnSuccessListener(aVoid -> {
                    currentOrder.setReceiverName(newAddr.getReceiverName());
                    currentOrder.setReceiverPhone(newAddr.getReceiverPhone());
                    currentOrder.setShippingAddress(newAddr.getDetailAddress());
                    
                    txtReceiver.setText(currentOrder.getReceiverName() + " | " + currentOrder.getReceiverPhone());
                    txtAddress.setText(currentOrder.getShippingAddress());
                    
                    Toast.makeText(this, "Đã cập nhật địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void displayOrderInfo(Order order) {
        txtOrderId.setText("Mã đơn: " + order.getId());
        txtDate.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
        txtPayment.setText("Thanh toán: " + order.getPaymentMethod());
        txtTotal.setText(String.format("%,.0f đ", order.getTotalAmount()));
        
        txtReceiver.setText(order.getReceiverName() + " | " + order.getReceiverPhone());
        txtAddress.setText(order.getShippingAddress());

        // Chỉ cho phép sửa địa chỉ nếu đơn hàng đang "Chờ duyệt" (status 0)
        if (order.getStatus() == 0) {
            btnEditAddress.setVisibility(View.VISIBLE);
        } else {
            btnEditAddress.setVisibility(View.GONE);
        }

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
