package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RelatedBooksAdapter extends RecyclerView.Adapter<RelatedBooksAdapter.RelatedViewHolder> {

    private Context context;
    private ArrayList<Book> relatedList;

    public RelatedBooksAdapter(Context context, ArrayList<Book> relatedList) {
        this.context = context;
        this.relatedList = relatedList;
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_horizontal, parent, false);
        return new RelatedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        Book book = relatedList.get(position);

        holder.txtName.setText(book.getTenSach());
        holder.txtPrice.setText(String.format("%,.0f đ", book.getGiaBan()));

        Glide.with(context)
                .load(book.getHinhAnh())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgBook);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            // TRUYỀN CẢ ĐỐI TƯỢNG SÁCH (Đã có ID)
            intent.putExtra("book", book);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return relatedList.size();
    }

    public static class RelatedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBook;
        TextView txtName, txtPrice;

        public RelatedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBook = itemView.findViewById(R.id.imgBookHorizontal);
            txtName = itemView.findViewById(R.id.txtBookNameHorizontal);
            txtPrice = itemView.findViewById(R.id.txtBookPriceHorizontal);
        }
    }
}
