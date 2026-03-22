package com.example.bi1;

import android.content.Context;
import android.content.Intent;
<<<<<<< HEAD
=======
import android.graphics.Paint;
>>>>>>> 0d5c59f (22/3)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
<<<<<<< HEAD
=======
import android.widget.CheckBox;
>>>>>>> 0d5c59f (22/3)
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
<<<<<<< HEAD
=======
import androidx.appcompat.app.AlertDialog;
>>>>>>> 0d5c59f (22/3)
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
<<<<<<< HEAD
        holder.txtUnitPrice.setText(String.format("Đơn giá: %,.0f đ", item.getUnitPrice()));
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));
        holder.txtSubtotal.setText(String.format("Thành tiền: %,.0f đ", item.getTotalPrice()));

        // Hiển thị ảnh trong giỏ hàng
=======
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

>>>>>>> 0d5c59f (22/3)
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
<<<<<<< HEAD
            } else {
                CartManager.removeFromCart(item.getBookId());
                notifyDataSetChanged();
=======
>>>>>>> 0d5c59f (22/3)
            }
            if (changeListener != null) changeListener.onCartChanged();
        });

<<<<<<< HEAD
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
=======
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
>>>>>>> 0d5c59f (22/3)
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
<<<<<<< HEAD
        TextView txtName, txtUnitPrice, txtQuantity, txtSubtotal, btnDetail;
        Button btnPlus, btnMinus;
        ImageButton btnDelete;
        ImageView imgThumb;
=======
        TextView txtName, txtUnitPrice, txtQuantity, txtSubtotal;
        Button btnPlus, btnMinus;
        ImageButton btnDelete;
        ImageView imgThumb;
        CheckBox cbSelect;
>>>>>>> 0d5c59f (22/3)

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCartBookName);
<<<<<<< HEAD
            txtUnitPrice = itemView.findViewById(R.id.txtCartUnitPrice);
            txtQuantity = itemView.findViewById(R.id.txtCartQuantity);
            txtSubtotal = itemView.findViewById(R.id.txtCartSubtotal);
            btnDetail = itemView.findViewById(R.id.btnCartDetail);
=======
            txtUnitPrice = itemView.findViewById(R.id.txtCartUnitPrice); // Giá gốc
            txtSubtotal = itemView.findViewById(R.id.txtCartSubtotal); // Giá bán
            txtQuantity = itemView.findViewById(R.id.txtCartQuantity);
>>>>>>> 0d5c59f (22/3)
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnDelete = itemView.findViewById(R.id.btnDeleteCartItem);
            imgThumb = itemView.findViewById(R.id.imgCartThumb);
<<<<<<< HEAD
=======
            cbSelect = itemView.findViewById(R.id.cbSelectItem);
>>>>>>> 0d5c59f (22/3)
        }
    }
}
