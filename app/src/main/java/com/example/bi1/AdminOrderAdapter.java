package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {

    private Context context;
    private ArrayList<Order> orderList;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AdminOrderAdapter(Context context, ArrayList<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_admin, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.txtCode.setText("Mã đơn: " + order.getId());
        holder.txtUser.setText("Khách hàng: " + order.getUserId());
        holder.txtTotal.setText(String.format("%,.0f đ", order.getTotalAmount()));
        if (order.getOrderDate() != null) {
            holder.txtDate.setText("Ngày mua: " + sdf.format(order.getOrderDate()));
        }

        // --- HIỂN THỊ ĐIỂM TÍCH LŨY ---
        if (order.getUserId() != null) {
            FirebaseFirestore.getInstance().collection("users").document(order.getUserId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Long points = doc.getLong("LoyaltyPoints");
                            holder.txtLoyalty.setText("Điểm tích lũy: " + (points != null ? points : 0));
                        } else {
                            holder.txtLoyalty.setText("Điểm tích lũy: 0");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("AdminOrder", "Lỗi lấy điểm: " + e.getMessage()));
        }

        updateStatusUI(holder, order.getStatus());

        holder.btnApprove.setOnClickListener(v -> updateOrderStatus(order, 1));
        holder.btnCancel.setOnClickListener(v -> updateOrderStatus(order, 2));

        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderHistoryDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });
        
        if (order.getStatus() != 0) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatusUI(AdminOrderViewHolder holder, int status) {
        switch (status) {
            case 0:
                holder.txtStatus.setText("Chờ duyệt");
                holder.txtStatus.setTextColor(Color.parseColor("#FF9800"));
                break;
            case 1:
                holder.txtStatus.setText("Đã duyệt");
                holder.txtStatus.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case 2:
                holder.txtStatus.setText("Đã hủy");
                holder.txtStatus.setTextColor(Color.parseColor("#F44336"));
                break;
        }
    }

    private void updateOrderStatus(Order order, int newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference orderRef = db.collection("orders").document(order.getId());

        // Cập nhật trạng thái đơn hàng trước
        orderRef.update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    if (newStatus == 1) {
                        processStockAndPoints(order);
                        Toast.makeText(context, "Đã duyệt đơn hàng", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                    order.setStatus(newStatus);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminOrder", "Lỗi cập nhật đơn: " + e.getMessage());
                    Toast.makeText(context, "Lỗi: " + e.getMessage() + ". Kiểm tra Security Rules trên Firebase!", Toast.LENGTH_LONG).show();
                });
    }

    private void processStockAndPoints(Order order) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // 1. Trừ kho và tăng lượt bán
        db.collection("order_details")
                .whereEqualTo("orderId", order.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String bookId = doc.getString("bookId");
                        Long qty = doc.getLong("quantity");
                        if (bookId != null && qty != null) {
                            updateBookStock(bookId, qty);
                        }
                    }
                });

        // 2. Cộng điểm tích lũy cho User
        if (order.getUserId() != null) {
            DocumentReference userRef = db.collection("users").document(order.getUserId());
            userRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Long currentPoints = doc.getLong("LoyaltyPoints");
                    long nextPoints = (currentPoints != null ? currentPoints : 0) + 1;
                    userRef.update("LoyaltyPoints", nextPoints >= 10 ? nextPoints - 10 : nextPoints);
                }
            });
        }
    }

    private void updateBookStock(String bookId, long qty) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookRef = db.collection("books").document(bookId);
        bookRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                long stock = doc.contains("SoLuong") ? doc.getLong("SoLuong") : 0;
                long sales = doc.contains("luotBan") ? doc.getLong("luotBan") : 0;
                bookRef.update("SoLuong", Math.max(0, stock - qty), "luotBan", sales + qty);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtCode, txtUser, txtDate, txtStatus, txtTotal, txtLoyalty;
        Button btnApprove, btnCancel, btnDetail;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txtAdminOrderCode);
            txtUser = itemView.findViewById(R.id.txtAdminOrderUser);
            txtLoyalty = itemView.findViewById(R.id.txtAdminOrderLoyalty);
            txtDate = itemView.findViewById(R.id.txtAdminOrderDate);
            txtStatus = itemView.findViewById(R.id.txtAdminOrderStatus);
            txtTotal = itemView.findViewById(R.id.txtAdminOrderTotal);
            btnApprove = itemView.findViewById(R.id.btnApproveOrder);
            btnCancel = itemView.findViewById(R.id.btnCancelOrder);
            btnDetail = itemView.findViewById(R.id.btnAdminOrderDetail);
        }
    }
}
