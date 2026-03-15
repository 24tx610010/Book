package com.example.bi1;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CategoryAdminAdapter extends RecyclerView.Adapter<CategoryAdminAdapter.CategoryViewHolder> {

    private Context context;
    private ArrayList<Category> categoryList;

    public CategoryAdminAdapter(Context context, ArrayList<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_admin, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category cat = categoryList.get(position);
        holder.txtName.setText(cat.getTenLoai());

        holder.btnEdit.setOnClickListener(v -> showEditDialog(cat));
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirm(cat));
    }

    private void showEditDialog(Category cat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sửa tên loại sách");
        
        final EditText input = new EditText(context);
        input.setText(cat.getTenLoai());
        builder.setView(input);

        builder.setPositiveButton("CẬP NHẬT", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                FirebaseFirestore.getInstance().collection("categories").document(cat.getId())
                        .update("tenLoai", newName)
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã cập nhật", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void showDeleteConfirm(Category cat) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa loại sách")
                .setMessage("Bạn có chắc chắn muốn xóa '" + cat.getTenLoai() + "' không?")
                .setPositiveButton("XÓA", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection("categories").document(cat.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        ImageButton btnEdit, btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtCategoryNameAdmin);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
