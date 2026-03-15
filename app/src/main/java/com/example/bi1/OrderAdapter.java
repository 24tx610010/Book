package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private ArrayList<Order> orderList;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public OrderAdapter(Context context, ArrayList<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.txtCode.setText("Mã đơn: " + order.getId());
        holder.txtTotal.setText(String.format("%,.0f đ", order.getTotalAmount()));
        
        if (order.getOrderDate() != null) {
            holder.txtDate.setText("Ngày mua: " + sdf.format(order.getOrderDate()));
        }

        switch (order.getStatus()) {
            case 0:
                holder.txtStatus.setText("Đã đặt");
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

        // KHI BẤM VÀO NÚT CHI TIẾT ĐƠN HÀNG
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderHistoryDetailActivity.class);
            intent.putExtra("order", order); // Truyền toàn bộ đối tượng đơn hàng sang trang chi tiết
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtCode, txtDate, txtStatus, txtTotal;
        Button btnDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txtOrderCode);
            txtDate = itemView.findViewById(R.id.txtOrderDate);
            txtStatus = itemView.findViewById(R.id.txtOrderStatus);
            txtTotal = itemView.findViewById(R.id.txtOrderTotal);
            btnDetail = itemView.findViewById(R.id.btnOrderDetail);
        }
    }
}
