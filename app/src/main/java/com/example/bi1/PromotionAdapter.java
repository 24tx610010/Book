package com.example.bi1;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private Context context;
    private ArrayList<Promotion> promotionList;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PromotionAdapter(Context context, ArrayList<Promotion> promotionList) {
        this.context = context;
        this.promotionList = promotionList;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_promotion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promo = promotionList.get(position);

        if (promo.getCode() != null && !promo.getCode().isEmpty()) {
            holder.txtCode.setText("Mã Voucher: " + promo.getCode());
            holder.txtCode.setTextColor(Color.parseColor("#E91E63")); // Màu hồng cho Voucher
        } else {
            holder.txtCode.setText("Giảm giá trực tiếp");
            holder.txtCode.setTextColor(Color.parseColor("#4CAF50")); // Màu xanh cho giảm trực tiếp
        }

        holder.txtValue.setText("Giảm " + (int)promo.getDiscountValue() + "%");

        String scope = "Tất cả sản phẩm";
        if ("category".equals(promo.getType())) scope = "Theo danh mục";
        else if ("book".equals(promo.getType())) scope = "Theo sản phẩm";
        holder.txtScope.setText("Phạm vi: " + scope);

        if (promo.getStartDate() != null && promo.getEndDate() != null) {
            holder.txtTime.setText("Hạn dùng: " + sdf.format(promo.getStartDate()) + " - " + sdf.format(promo.getEndDate()));
        }

        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("promotions").document(promo.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa khuyến mãi", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    public static class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView txtCode, txtValue, txtScope, txtTime;
        Button btnDelete;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txtPromoCodeDisplay);
            txtValue = itemView.findViewById(R.id.txtPromoValueDisplay);
            txtScope = itemView.findViewById(R.id.txtPromoScope);
            txtTime = itemView.findViewById(R.id.txtPromoTime);
            btnDelete = itemView.findViewById(R.id.btnDeletePromo);
        }
    }
}
