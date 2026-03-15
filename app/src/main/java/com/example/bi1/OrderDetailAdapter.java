package com.example.bi1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.DetailViewHolder> {

    private ArrayList<OrderDetail> detailList;

    public OrderDetailAdapter(ArrayList<OrderDetail> detailList) {
        this.detailList = detailList;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_detail, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        OrderDetail detail = detailList.get(position);
        holder.txtName.setText(detail.getBookName());
        holder.txtPriceQty.setText(String.format("%,.0f đ x %d", detail.getUnitPrice(), detail.getQuantity()));
        holder.txtSubtotal.setText(String.format("%,.0f đ", detail.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }

    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPriceQty, txtSubtotal;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtDetailBookName);
            txtPriceQty = itemView.findViewById(R.id.txtDetailPriceQty);
            txtSubtotal = itemView.findViewById(R.id.txtDetailSubtotal);
        }
    }
}
