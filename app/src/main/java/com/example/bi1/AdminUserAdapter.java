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

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private Context context;
    private ArrayList<User> userList;

    public AdminUserAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.txtFullName.setText(user.getHoTen());
        holder.txtPhone.setText("SĐT: " + user.getPhone());
        holder.txtRole.setText(user.getRoleid() == 1 ? "ADMIN" : "USER");

        holder.btnEdit.setOnClickListener(v -> showEditUserDialog(user));
        holder.btnDelete.setOnClickListener(v -> showDeleteConfirm(user));
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sửa thông tin người dùng");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_user, null);
        EditText edtName = view.findViewById(R.id.edtEditUserFullName);
        EditText edtRole = view.findViewById(R.id.edtEditUserRole);
        
        edtName.setText(user.getHoTen());
        edtRole.setText(String.valueOf(user.getRoleid()));
        
        builder.setView(view);

        builder.setPositiveButton("CẬP NHẬT", (dialog, which) -> {
            String newName = edtName.getText().toString().trim();
            String roleStr = edtRole.getText().toString().trim();

            if (!newName.isEmpty() && !roleStr.isEmpty()) {
                try {
                    int newRole = Integer.parseInt(roleStr);
                    FirebaseFirestore.getInstance().collection("users").document(user.getPhone())
                            .update("hoTen", newName, "roleid", newRole)
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã cập nhật", Toast.LENGTH_SHORT).show());
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Vai trò phải là số!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void showDeleteConfirm(User user) {
        if (user.getPhone().equals("admin")) {
            Toast.makeText(context, "Không thể xóa tài khoản admin hệ thống!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng '" + user.getHoTen() + "' không?")
                .setPositiveButton("XÓA", (dialog, which) -> {
                    FirebaseFirestore.getInstance().collection("users").document(user.getPhone())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa người dùng", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("HỦY", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtFullName, txtPhone, txtRole;
        ImageButton btnEdit, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFullName = itemView.findViewById(R.id.txtAdminUserFullName);
            txtPhone = itemView.findViewById(R.id.txtAdminUserPhone);
            txtRole = itemView.findViewById(R.id.txtAdminUserRole);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
