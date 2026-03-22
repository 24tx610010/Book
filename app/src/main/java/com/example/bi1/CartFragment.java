package com.example.bi1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
    private TextView txtTotal, txtSubtotal, txtDiscount, txtEmpty, txtSelectCount, txtBuyMoreInfo;
    private CheckBox cbSelectAll;
    private ProgressBar pbVoucher;
    private View layoutBottom, btnViewPromotions;
    private Button btnCheckout, btnBuyMore;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        db = FirebaseFirestore.getInstance();
        rvCart = view.findViewById(R.id.rvCartFragment);
        txtTotal = view.findViewById(R.id.txtTotalCartFragment);
        txtSubtotal = view.findViewById(R.id.txtSubtotalCart);
        txtDiscount = view.findViewById(R.id.txtDiscountCart);
        txtEmpty = view.findViewById(R.id.txtEmptyCart);
        txtSelectCount = view.findViewById(R.id.txtSelectCount);
        txtBuyMoreInfo = view.findViewById(R.id.txtBuyMoreInfo);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        pbVoucher = view.findViewById(R.id.pbVoucher);
        layoutBottom = view.findViewById(R.id.layoutBottomPay);
        btnCheckout = view.findViewById(R.id.btnCheckoutFragment);
        btnBuyMore = view.findViewById(R.id.btnBuyMore);
        btnViewPromotions = view.findViewById(R.id.btnViewPromotions);

        cartItems = CartManager.getCartList();
        adapter = new CartAdapter(getContext(), cartItems, this);
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(adapter);

        // Xử lý nút CHỌN TẤT CẢ
        if (cbSelectAll != null) {
            cbSelectAll.setOnClickListener(v -> {
                CartManager.toggleAll(cbSelectAll.isChecked());
                adapter.notifyDataSetChanged();
                updateTotal();
            });
        }

        // Nút mua thêm -> Quay về trang chủ
        View.OnClickListener buyMoreClick = v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).refreshToHome();
            }
        };
        if (btnBuyMore != null) btnBuyMore.setOnClickListener(buyMoreClick);
        if (txtEmpty != null) txtEmpty.setOnClickListener(buyMoreClick);

        // Xem khuyến mãi
        if (btnViewPromotions != null) {
            btnViewPromotions.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), PromotionActivity.class));
            });
        }

        updateTotal();

        btnCheckout.setOnClickListener(v -> {
            boolean hasSelection = false;
            for (CartItem item : cartItems) {
                if (item.isSelected()) {
                    hasSelection = true;
                    break;
                }
            }
            
            if (!hasSelection) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất 1 sản phẩm!", Toast.LENGTH_SHORT).show();
                return;
            }
            showPaymentDialog();
        });

        return view;
    }

    private void showPaymentDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thông tin thanh toán");
        builder.setMessage("Vui lòng chuyển khoản vào STK:\n\nACB: 31568367\nNGUYEN VAN A");
        builder.setPositiveButton("XÁC NHẬN", (dialog, which) -> processCheckout());
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void processCheckout() {
        if (getActivity() == null) return;
        String phone = getActivity().getSharedPreferences("auth", 0).getString("phone", "");
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        WriteBatch batch = db.batch();
        Order newOrder = new Order(orderId, phone, new Date(), "Chuyển khoản ACB", CartManager.getTotalPrice(), 0);
        batch.set(db.collection("orders").document(orderId), newOrder);

        for (int i = cartItems.size() - 1; i >= 0; i--) {
            CartItem item = cartItems.get(i);
            if (item.isSelected()) {
                String detailId = UUID.randomUUID().toString().substring(0, 10);
                OrderDetail detail = new OrderDetail(detailId, orderId, item.getBookId(), item.getBookName(), item.getUnitPrice(), item.getQuantity(), item.getTotalPrice());
                batch.set(db.collection("order_details").document(detailId), detail);
                cartItems.remove(i);
            }
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
            updateTotal();
            adapter.notifyDataSetChanged();
            if (getActivity() instanceof HomeActivity) ((HomeActivity) getActivity()).refreshCartBadge();
        });
    }

    @Override
    public void onCartChanged() {
        updateTotal();
    }

    private void updateTotal() {
        int selectedCount = 0;
        int totalQty = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedCount++;
                totalQty += item.getQuantity();
            }
        }
        
        if (txtSelectCount != null) {
            txtSelectCount.setText("Chọn tất cả ( " + selectedCount + " sản phẩm )");
        }
        
        if (cbSelectAll != null) {
            cbSelectAll.setChecked(selectedCount == cartItems.size() && !cartItems.isEmpty());
            cbSelectAll.setEnabled(!cartItems.isEmpty());
        }

        boolean isEmpty = cartItems.isEmpty();
        if (txtEmpty != null) txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (rvCart != null) rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (layoutBottom != null) layoutBottom.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (txtSubtotal != null) txtSubtotal.setText(String.format("%,.0f đ", CartManager.getSubtotal()));
        if (txtDiscount != null) txtDiscount.setText(String.format("-%,.0f đ", CartManager.getDiscountAmount()));
        if (txtTotal != null) txtTotal.setText(String.format("%,.0f đ", CartManager.getTotalPrice()));

        updateVoucherProgress(totalQty);
        
        if (getActivity() instanceof HomeActivity) ((HomeActivity) getActivity()).refreshCartBadge();
    }

    private void updateVoucherProgress(int totalQty) {
        if (pbVoucher == null || txtBuyMoreInfo == null) return;

        if (totalQty == 0) {
            pbVoucher.setProgress(0);
            txtBuyMoreInfo.setText("Mua thêm 2 sản phẩm để nhận ưu đãi 5%");
        } else if (totalQty == 1) {
            pbVoucher.setProgress(50);
            txtBuyMoreInfo.setText("Mua thêm 1 sản phẩm để nhận ưu đãi 5%");
        } else if (totalQty == 2) {
            pbVoucher.setProgress(100);
            txtBuyMoreInfo.setText("Bạn đã nhận ưu đãi 5%! Mua thêm 1 món để nhận 6%");
        } else {
            pbVoucher.setProgress(100);
            txtBuyMoreInfo.setText("Chúc mừng! Bạn đã nhận ưu đãi tối đa 6%");
        }
    }
}
