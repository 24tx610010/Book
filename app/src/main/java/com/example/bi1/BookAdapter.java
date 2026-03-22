package com.example.bi1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
<<<<<<< HEAD
=======
import android.graphics.Paint;
>>>>>>> 0d5c59f (22/3)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private Context context;
    private ArrayList<Book> bookList;
    private ArrayList<Book> bookListFull;
    private int roleId;
    private FirebaseFirestore db;
    private String userPhone;
    private Set<String> favoriteIds = new HashSet<>();

    public BookAdapter(Context context, ArrayList<Book> bookList, int roleId) {
        this.context = context;
        this.bookList = bookList;
        this.bookListFull = new ArrayList<>(bookList);
        this.roleId = roleId;
        this.db = FirebaseFirestore.getInstance();
        
        SharedPreferences sp = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        this.userPhone = sp.getString("phone", "");
        
        if (!userPhone.isEmpty()) {
            loadFavorites();
        }
    }

    private void loadFavorites() {
        db.collection("favorites").document(userPhone).collection("items")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        favoriteIds.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            favoriteIds.add(doc.getId());
                        }
                        notifyDataSetChanged();
                    }
                });
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.txtName.setText(book.getTenSach());
        holder.txtPrice.setText(String.format("%,.0f đ", book.getGiaBan()));
<<<<<<< HEAD
        holder.txtStock.setText("Còn lại: " + book.getSoLuong());
        
        // HIỂN THỊ DÃY SAO THAY VÌ CON SỐ
        holder.ratingBar.setRating(book.getRating());

=======
        holder.txtStock.setText("Tồn: " + book.getSoLuong());
        holder.txtSold.setText("Bán: " + book.getLuotBan());
        holder.ratingBar.setRating(book.getRating());

        // HIỂN THỊ KHUYẾN MÃI VỚI GẠCH NGANG MÀU ĐỎ
        if (book.getGiaGoc() > book.getGiaBan()) {
            holder.txtOriginalPrice.setVisibility(View.VISIBLE);
            holder.txtOriginalPrice.setText(String.format("%,.0f đ", book.getGiaGoc()));
            
            // Gạch ngang chữ
            holder.txtOriginalPrice.setPaintFlags(holder.txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            // Đổi màu chữ giá gốc sang màu xám đậm hoặc đỏ tùy ý, ở đây mình dùng màu xám để nổi bật gạch ngang nếu muốn
            holder.txtOriginalPrice.setTextColor(android.graphics.Color.GRAY);
            
            holder.txtDiscountLabel.setVisibility(View.VISIBLE);
            holder.txtDiscountLabel.setText("-" + book.getDiscountPercent() + "%");
        } else {
            holder.txtOriginalPrice.setVisibility(View.GONE);
            holder.txtDiscountLabel.setVisibility(View.GONE);
        }

>>>>>>> 0d5c59f (22/3)
        if (book.getHinhAnh() != null && !book.getHinhAnh().isEmpty()) {
            Glide.with(context).load(book.getHinhAnh()).placeholder(R.mipmap.ic_launcher).into(holder.imgBookThumb);
        }

<<<<<<< HEAD
        // XỬ LÝ DẤU GẠCH ĐỎ KHI HẾT HÀNG (SỐ LƯỢNG = 0)
=======
        // XỬ LÝ HẾT HÀNG
>>>>>>> 0d5c59f (22/3)
        if (book.getSoLuong() <= 0) {
            holder.viewSoldOut.setVisibility(View.VISIBLE);
            holder.txtSoldOut.setVisibility(View.VISIBLE);
        } else {
            holder.viewSoldOut.setVisibility(View.GONE);
            holder.txtSoldOut.setVisibility(View.GONE);
        }

<<<<<<< HEAD
=======
        // Xử lý nút Trái tim
>>>>>>> 0d5c59f (22/3)
        if (favoriteIds.contains(book.getId())) {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_outline);
        }
<<<<<<< HEAD

        holder.btnFavorite.setOnClickListener(v -> toggleFavorite(book));

        if (roleId == 1) { // ADMIN
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.layoutQuickActions.setVisibility(View.GONE);
            holder.btnFavorite.setVisibility(View.GONE);
        } else { // USER
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
=======
        holder.btnFavorite.setOnClickListener(v -> toggleFavorite(book));

        // PHÂN QUYỀN
        if (roleId == 1) { // ADMIN
            holder.layoutAdminActions.setVisibility(View.VISIBLE);
            holder.layoutQuickActions.setVisibility(View.GONE);
            holder.btnFavorite.setVisibility(View.GONE);
        } else { // USER
            holder.layoutAdminActions.setVisibility(View.GONE);
>>>>>>> 0d5c59f (22/3)
            holder.layoutQuickActions.setVisibility(View.VISIBLE);
            holder.btnFavorite.setVisibility(View.VISIBLE);
        }

        holder.btnDetail.setOnClickListener(v -> openDetail(book));
<<<<<<< HEAD
        holder.btnQuickAddToCart.setOnClickListener(v -> CartManager.addToCart(book, 1));
        holder.btnQuickBuy.setOnClickListener(v -> {
            CartManager.addToCart(book, 1);
            context.startActivity(new Intent(context, CartActivity.class));
        });

        // Admin Buttons
=======
        
        holder.btnQuickAddToCart.setOnClickListener(v -> {
            if (book.getSoLuong() > 0) CartManager.addToCart(book, 1);
            else Toast.makeText(context, "Hết hàng!", Toast.LENGTH_SHORT).show();
        });

        holder.btnQuickBuy.setOnClickListener(v -> {
            if (book.getSoLuong() > 0) {
                CartManager.addToCart(book, 1);
                context.startActivity(new Intent(context, CartActivity.class));
            } else Toast.makeText(context, "Hết hàng!", Toast.LENGTH_SHORT).show();
        });

>>>>>>> 0d5c59f (22/3)
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddBookActivity.class);
            intent.putExtra("bookId", book.getId());
            intent.putExtra("name", book.getTenSach());
            intent.putExtra("price", book.getGiaBan());
<<<<<<< HEAD
=======
            intent.putExtra("originalPrice", book.getGiaGoc());
>>>>>>> 0d5c59f (22/3)
            intent.putExtra("stock", book.getSoLuong());
            intent.putExtra("image", book.getHinhAnh());
            intent.putExtra("author", book.getTacGia());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context).setTitle("Xóa").setMessage("Xóa cuốn này?")
                    .setPositiveButton("Xóa", (d, w) -> db.collection("books").document(book.getId()).delete()).show();
        });
    }

    private void toggleFavorite(Book book) {
<<<<<<< HEAD
        if (userPhone.isEmpty()) {
            Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }
=======
        if (userPhone.isEmpty()) return;
>>>>>>> 0d5c59f (22/3)
        String bookId = book.getId();
        if (favoriteIds.contains(bookId)) {
            db.collection("favorites").document(userPhone).collection("items").document(bookId).delete();
        } else {
            java.util.Map<String, Object> fav = new java.util.HashMap<>();
            fav.put("bookName", book.getTenSach());
            db.collection("favorites").document(userPhone).collection("items").document(bookId).set(fav);
        }
    }

    private void openDetail(Book book) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("book", book);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() { return bookList.size(); }

    public void updateList(ArrayList<Book> newList) {
        this.bookList = newList;
        this.bookListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        bookList.clear();
        if (text.isEmpty()) { bookList.addAll(bookListFull); }
        else {
            text = text.toLowerCase();
            for (Book item : bookListFull) {
                if (item.getTenSach().toLowerCase().contains(text)) bookList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
<<<<<<< HEAD
        TextView txtName, txtPrice, txtStock, txtSoldOut;
=======
        TextView txtName, txtPrice, txtOriginalPrice, txtStock, txtSold, txtDiscountLabel, txtSoldOut;
>>>>>>> 0d5c59f (22/3)
        RatingBar ratingBar;
        ImageView imgBookThumb;
        ImageButton btnFavorite, btnQuickAddToCart;
        Button btnDetail, btnEdit, btnDelete, btnQuickBuy;
<<<<<<< HEAD
        View layoutQuickActions, viewSoldOut;
=======
        View layoutQuickActions, layoutAdminActions, viewSoldOut;
>>>>>>> 0d5c59f (22/3)

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtBookName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
<<<<<<< HEAD
            txtStock = itemView.findViewById(R.id.txtStock);
=======
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            txtStock = itemView.findViewById(R.id.txtStock);
            txtSold = itemView.findViewById(R.id.txtSoldQuantity);
            txtDiscountLabel = itemView.findViewById(R.id.txtDiscountLabel);
>>>>>>> 0d5c59f (22/3)
            txtSoldOut = itemView.findViewById(R.id.txtSoldOut);
            ratingBar = itemView.findViewById(R.id.itemRatingBar);
            imgBookThumb = itemView.findViewById(R.id.imgBookThumb);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnQuickAddToCart = itemView.findViewById(R.id.btnQuickAddToCart);
            btnQuickBuy = itemView.findViewById(R.id.btnQuickBuy);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutQuickActions = itemView.findViewById(R.id.layoutUserQuickActions);
<<<<<<< HEAD
=======
            layoutAdminActions = itemView.findViewById(R.id.layoutAdminActions);
>>>>>>> 0d5c59f (22/3)
            viewSoldOut = itemView.findViewById(R.id.viewSoldOut);
        }
    }
}
