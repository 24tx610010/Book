package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

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

        // Trạng thái hiển thị
        updateStatusUI(holder, order.getStatus());

        // Nút DUYỆT
        holder.btnApprove.setOnClickListener(v -> updateOrderStatus(order.getId(), 1));

        // Nút HỦY ĐƠN
        holder.btnCancel.setOnClickListener(v -> updateOrderStatus(order.getId(), 2));

        // Nút CHI TIẾT
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderHistoryDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });
        
        // Ẩn nút Duyệt/Hủy nếu đơn đã được xử lý
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

    private void updateOrderStatus(String orderId, int newStatus) {
        FirebaseFirestore.getInstance().collection("orders").document(orderId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã cập nhật trạng thái đơn hàng", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtCode, txtUser, txtDate, txtStatus, txtTotal;
        Button btnApprove, btnCancel, btnDetail;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txtAdminOrderCode);
            txtUser = findViewById(R.id.txtAdminOrderUser);
            txtDate = itemView.findViewById(R.id.txtAdminOrderDate);
            txtStatus = itemView.findViewById(R.id.txtAdminOrderStatus);
            txtTotal = itemView.findViewById(R.id.txtAdminOrderTotal);
            btnApprove = itemView.findViewById(R.id.btnApproveOrder);
            btnCancel = itemView.findViewById(R.id.btnCancelOrder);
            btnDetail = itemView.findViewById(R.id.btnAdminOrderDetail);
        }
        
        private TextView findViewById(int id) {
            return itemView.findViewById(id);
        }
    }
}
