package com.example.bi1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private List<UserAddress> addressList;
    private String selectedAddressId;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(UserAddress address);
        void onEditAddress(UserAddress address);
    }

    public AddressAdapter(List<UserAddress> addressList, String selectedAddressId, OnAddressSelectedListener listener) {
        this.addressList = addressList;
        this.selectedAddressId = selectedAddressId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserAddress address = addressList.get(position);
        holder.txtName.setText(address.getReceiverName());
        holder.txtPhone.setText(address.getReceiverPhone());
        holder.txtDetail.setText(address.getDetailAddress());
        
        holder.rbSelected.setChecked(address.getId().equals(selectedAddressId));
        holder.txtDefaultBadge.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            selectedAddressId = address.getId();
            notifyDataSetChanged();
            listener.onAddressSelected(address);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEditAddress(address));
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone, txtDetail, txtDefaultBadge;
        RadioButton rbSelected;
        ImageButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtItemReceiverName);
            txtPhone = itemView.findViewById(R.id.txtItemReceiverPhone);
            txtDetail = itemView.findViewById(R.id.txtItemDetailAddress);
            txtDefaultBadge = itemView.findViewById(R.id.txtDefaultBadge);
            rbSelected = itemView.findViewById(R.id.rbSelected);
            btnEdit = itemView.findViewById(R.id.btnEditAddress);
        }
    }
}
