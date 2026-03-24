package com.example.bi1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class LoyalCustomerAdapter extends RecyclerView.Adapter<LoyalCustomerAdapter.ViewHolder> {
    private ArrayList<User> userList;

    public LoyalCustomerAdapter(ArrayList<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loyal_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.txtName.setText(user.getHoTen());
        holder.txtPhone.setText("SĐT: " + user.getPhone());
        holder.txtPoints.setText(String.valueOf(user.getLoyaltyPoints()));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone, txtPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCustomerName);
            txtPhone = itemView.findViewById(R.id.txtCustomerPhone);
            txtPoints = itemView.findViewById(R.id.txtLoyaltyPoints);
        }
    }
}
