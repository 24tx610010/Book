package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
        holder.txtUnitPrice.setText(String.format("Đơn giá: %,.0f đ", item.getUnitPrice()));
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));
        holder.txtSubtotal.setText(String.format("Thành tiền: %,.0f đ", item.getTotalPrice()));

        // Hiển thị ảnh trong giỏ hàng
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
                CartManager.removeFromCart(item.getBookId());
                notifyDataSetChanged();
            }
            if (changeListener != null) changeListener.onCartChanged();
        });

        // Nút xóa sản phẩm
        holder.btnDelete.setOnClickListener(v -> {
            CartManager.removeFromCart(item.getBookId());
            notifyDataSetChanged();
            if (changeListener != null) changeListener.onCartChanged();
        });

        // Bấm vào "Xem chi tiết" để mở trang DetailActivity
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("name", item.getBookName());
            intent.putExtra("price", String.valueOf(item.getUnitPrice()));
            intent.putExtra("desc", item.getDescription());
            intent.putExtra("image", item.getImage());
            intent.putExtra("author", item.getAuthor());
            intent.putExtra("publisher", item.getPublisher());
            intent.putExtra("year", item.getYear());
            intent.putExtra("language", item.getLanguage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtUnitPrice, txtQuantity, txtSubtotal, btnDetail;
        Button btnPlus, btnMinus;
        ImageButton btnDelete;
        ImageView imgThumb;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCartBookName);
            txtUnitPrice = itemView.findViewById(R.id.txtCartUnitPrice);
            txtQuantity = itemView.findViewById(R.id.txtCartQuantity);
            txtSubtotal = itemView.findViewById(R.id.txtCartSubtotal);
            btnDetail = itemView.findViewById(R.id.btnCartDetail);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDeleteCartItem);
            imgThumb = itemView.findViewById(R.id.imgCartThumb);
        }
    }
}
