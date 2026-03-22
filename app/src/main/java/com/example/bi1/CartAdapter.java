package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartChangeListener changeListener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.changeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.txtName.setText(item.getBookName());
        holder.txtSubtotal.setText(String.format("%,.0f đ", item.getUnitPrice())); // Giá bán hiện tại
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));
        
        // Hiển thị giá gốc gạch ngang nếu có khuyến mãi
        if (item.getOriginalPrice() > item.getUnitPrice()) {
            holder.txtUnitPrice.setVisibility(View.VISIBLE);
            holder.txtUnitPrice.setText(String.format("%,.0f đ", item.getOriginalPrice()));
            holder.txtUnitPrice.setPaintFlags(holder.txtUnitPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.txtUnitPrice.setVisibility(View.GONE);
        }

        // Checkbox chọn sản phẩm
        holder.cbSelect.setChecked(item.isSelected());
        holder.cbSelect.setOnClickListener(v -> {
            item.setSelected(holder.cbSelect.isChecked());
            if (changeListener != null) changeListener.onCartChanged();
        });

        Glide.with(context)
                .load(item.getImage())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgThumb);

        // Nút tăng số lượng
        holder.btnPlus.setOnClickListener(v -> {
            CartManager.updateQuantity(item.getBookId(), item.getQuantity() + 1);
            notifyItemChanged(position);
            if (changeListener != null) changeListener.onCartChanged();
        });

        // Nút giảm số lượng
        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                CartManager.updateQuantity(item.getBookId(), item.getQuantity() - 1);
                notifyItemChanged(position);
            } else {
                // Tùy chọn: Xóa khỏi giỏ hàng nếu số lượng về 0
                new AlertDialog.Builder(context)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            CartManager.removeFromCart(item.getBookId());
                            notifyDataSetChanged();
                            if (changeListener != null) changeListener.onCartChanged();
                        })
                        .setNegativeButton("Không", null)
                        .show();
            }
            if (changeListener != null) changeListener.onCartChanged();
        });

        // Nút xóa sản phẩm - Có hộp thoại xác nhận
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm '" + item.getBookName() + "' khỏi giỏ hàng không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        CartManager.removeFromCart(item.getBookId());
                        notifyDataSetChanged();
                        if (changeListener != null) changeListener.onCartChanged();
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtUnitPrice, txtQuantity, txtSubtotal;
        Button btnPlus, btnMinus;
        ImageButton btnDelete;
        ImageView imgThumb;
        CheckBox cbSelect;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCartBookName);
            txtUnitPrice = itemView.findViewById(R.id.txtCartUnitPrice); // Giá gốc
            txtSubtotal = itemView.findViewById(R.id.txtCartSubtotal); // Giá bán
            txtQuantity = itemView.findViewById(R.id.txtCartQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDeleteCartItem);
            imgThumb = itemView.findViewById(R.id.imgCartThumb);
            cbSelect = itemView.findViewById(R.id.cbSelectItem);
        }
    }
}
