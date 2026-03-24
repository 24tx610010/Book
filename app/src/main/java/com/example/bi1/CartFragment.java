package com.example.bi1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCart;
    private TextView txtTotal, txtSubtotal, txtDiscount, txtEmpty, txtSelectCount, txtBuyMoreInfo;
    private TextView txtCustomerInfo, txtAddressDisplay;
    private CheckBox cbSelectAll;
    private ProgressBar pbVoucher;
    private View layoutBottom;
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;

    // Loyalty components
    private View cardLoyalty;
    private TextView txtLoyaltyInfo;
    private CheckBox cbUseLoyalty;
    private int userPoints = 0;
    private double loyaltyDiscount = 0;

    private String currentReceiverName = "";
    private String currentReceiverPhone = "";
    private String currentDetailAddress = "";
    private String userPhone = "";

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
        Button btnCheckout = view.findViewById(R.id.btnCheckoutFragment);
        Button btnBuyMore = view.findViewById(R.id.btnBuyMore);
        View btnViewPromotions = view.findViewById(R.id.btnViewPromotions);
        
        txtCustomerInfo = view.findViewById(R.id.txtCustomerInfo);
        txtAddressDisplay = view.findViewById(R.id.txtAddressDisplay);
        TextView btnChangeAddress = view.findViewById(R.id.btnChangeAddress);

        // Loyalty UI
        cardLoyalty = view.findViewById(R.id.cardLoyalty);
        txtLoyaltyInfo = view.findViewById(R.id.txtLoyaltyInfo);
        cbUseLoyalty = view.findViewById(R.id.cbUseLoyalty);

        // Lấy thông tin người dùng hiện tại
        if (getActivity() != null) {
            SharedPreferences sp = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
            userPhone = sp.getString("phone", "");
            currentReceiverName = sp.getString("username", "");
            currentReceiverPhone = userPhone;
            loadSavedAddress();
            fetchUserLoyaltyPoints();
        }

        if (btnChangeAddress != null) btnChangeAddress.setOnClickListener(v -> showAddressDialog());

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

        if (cbUseLoyalty != null) {
            cbUseLoyalty.setOnCheckedChangeListener((buttonView, isChecked) -> updateTotal());
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

            if (currentDetailAddress == null || currentDetailAddress.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập địa chỉ nhận hàng!", Toast.LENGTH_SHORT).show();
                showAddressDialog();
                return;
            }

            showPaymentDialog();
        });

        return view;
    }

    private void fetchUserLoyaltyPoints() {
        if (userPhone.isEmpty()) return;
        db.collection("users").whereEqualTo("Phone", userPhone).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            userPoints = user.getLoyaltyPoints();
                            updateLoyaltyUI();
                        }
                    }
                });
    }

    private void updateLoyaltyUI() {
        if (cardLoyalty == null) return;
        cardLoyalty.setVisibility(View.VISIBLE);
        txtLoyaltyInfo.setText("Điểm hiện tại: " + userPoints + " (Đạt 10 điểm để được FREE 1 sách)");
        if (userPoints >= 10) {
            cbUseLoyalty.setVisibility(View.VISIBLE);
        } else {
            cbUseLoyalty.setVisibility(View.GONE);
            cbUseLoyalty.setChecked(false);
        }
    }

    private void loadSavedAddress() {
        if (getActivity() == null) return;
        SharedPreferences sp = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        currentReceiverName = sp.getString("ship_name", currentReceiverName);
        currentReceiverPhone = sp.getString("ship_phone", currentReceiverPhone);
        currentDetailAddress = sp.getString("ship_address", "");

        updateAddressUI();
    }

    private void updateAddressUI() {
        if (txtCustomerInfo != null) txtCustomerInfo.setText(String.format("%s | %s", currentReceiverName, currentReceiverPhone));
        if (txtAddressDisplay != null) {
            if (currentDetailAddress != null && !currentDetailAddress.isEmpty()) {
                txtAddressDisplay.setText(currentDetailAddress);
            } else {
                txtAddressDisplay.setText("Vui lòng cập nhật địa chỉ giao hàng");
            }
        }
    }

    private void showAddressDialog() {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_address, null);
        
        final EditText edtName = view.findViewById(R.id.edtReceiverName);
        final EditText edtPhone = view.findViewById(R.id.edtReceiverPhone);
        final EditText edtAddress = view.findViewById(R.id.edtNewAddress);

        edtName.setText(currentReceiverName);
        edtPhone.setText(currentReceiverPhone);
        edtAddress.setText(currentDetailAddress);
        
        builder.setView(view);
        builder.setPositiveButton("XÁC NHẬN", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String addr = edtAddress.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            currentReceiverName = name;
            currentReceiverPhone = phone;
            currentDetailAddress = addr;

            updateAddressUI();

            // Lưu lại cho lần sau
            if (getActivity() != null) {
                getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE).edit()
                        .putString("ship_name", currentReceiverName)
                        .putString("ship_phone", currentReceiverPhone)
                        .putString("ship_address", currentDetailAddress)
                        .apply();
            }
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void showPaymentDialog() {
        if (getContext() == null) return;
        double finalTotal = CartManager.getTotalPrice() - loyaltyDiscount;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận đặt hàng");
        builder.setMessage("Người nhận: " + currentReceiverName + " (" + currentReceiverPhone + ")" +
                "\nĐịa chỉ: " + currentDetailAddress + 
                "\nTổng thanh toán: " + String.format(Locale.getDefault(), "%,.0f đ", finalTotal) + 
                "\n\nBạn có chắc chắn muốn đặt hàng?");

        builder.setPositiveButton("XÁC NHẬN", (dialog, which) -> processCheckout());
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void processCheckout() {
        if (getActivity() == null) return;
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double finalAmount = CartManager.getTotalPrice() - loyaltyDiscount;
        
        WriteBatch batch = db.batch();
        
        Order newOrder = new Order(
                orderId, 
                userPhone, 
                currentReceiverName, 
                currentReceiverPhone, 
                currentDetailAddress, 
                new Date(), 
                "COD", 
                finalAmount, 
                0
        );

        batch.set(db.collection("orders").document(orderId), newOrder);

        for (int i = cartItems.size() - 1; i >= 0; i--) {
            CartItem item = cartItems.get(i);
            if (item.isSelected()) {
                String detailId = UUID.randomUUID().toString().substring(0, 10);
                double itemPrice = item.getUnitPrice();
                // Nếu đang dùng điểm và đây là món hàng được giảm (ví dụ món đầu tiên hoặc món rẻ nhất)
                // Đơn giản nhất là trừ thẳng vào tổng bill, nhưng để lưu chi tiết chính xác:
                if (loyaltyDiscount > 0 && itemPrice >= loyaltyDiscount) {
                    // Trừ discount vào món này (chỉ áp dụng 1 lần)
                    OrderDetail detail = new OrderDetail(detailId, orderId, item.getBookId(), item.getBookName(), itemPrice, item.getQuantity(), (itemPrice * item.getQuantity()) - loyaltyDiscount);
                    batch.set(db.collection("order_details").document(detailId), detail);
                    loyaltyDiscount = 0; // Reset để không trừ tiếp vào món sau
                } else {
                    OrderDetail detail = new OrderDetail(detailId, orderId, item.getBookId(), item.getBookName(), itemPrice, item.getQuantity(), item.getTotalPrice());
                    batch.set(db.collection("order_details").document(detailId), detail);
                }
                cartItems.remove(i);
            }
        }

        // Cập nhật điểm tích lũy
        db.collection("users").whereEqualTo("Phone", userPhone).limit(1).get().addOnSuccessListener(snapshots -> {
            if (!snapshots.isEmpty()) {
                DocumentSnapshot userDoc = snapshots.getDocuments().get(0);
                int currentP = userDoc.getLong("LoyaltyPoints") != null ? userDoc.getLong("LoyaltyPoints").intValue() : 0;
                int newP = currentP;
                
                // Nếu dùng 10đ
                if (cbUseLoyalty.isChecked()) {
                    newP -= 10;
                }
                
                // Nếu đơn trên 200k, cộng 1đ
                if (finalAmount > 200000) {
                    newP += 1;
                }
                
                db.collection("users").document(userDoc.getId()).update("LoyaltyPoints", newP);
            }
        });

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
            cbUseLoyalty.setChecked(false);
            fetchUserLoyaltyPoints(); // Refresh points
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
        double cheapestPrice = -1;

        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedCount++;
                totalQty += item.getQuantity();
                if (cheapestPrice == -1 || item.getUnitPrice() < cheapestPrice) {
                    cheapestPrice = item.getUnitPrice();
                }
            }
        }
        
        if (txtSelectCount != null) {
            txtSelectCount.setText(String.format(Locale.getDefault(), "Chọn tất cả ( %d sản phẩm )", selectedCount));
        }
        
        if (cbSelectAll != null) {
            cbSelectAll.setChecked(selectedCount == cartItems.size() && !cartItems.isEmpty());
            cbSelectAll.setEnabled(!cartItems.isEmpty());
        }

        boolean isEmpty = cartItems.isEmpty();
        if (txtEmpty != null) txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (rvCart != null) rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (layoutBottom != null) layoutBottom.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        double subtotal = CartManager.getSubtotal();
        double voucherDiscount = CartManager.getDiscountAmount();
        
        loyaltyDiscount = 0;
        if (cbUseLoyalty.isChecked() && selectedCount > 0) {
            loyaltyDiscount = cheapestPrice; // Miễn phí 1 cuốn (chọn cuốn rẻ nhất trong những cuốn đã chọn)
        }

        if (txtSubtotal != null) txtSubtotal.setText(String.format(Locale.getDefault(), "%,.0f đ", subtotal));
        
        double totalDiscount = voucherDiscount + loyaltyDiscount;
        if (txtDiscount != null) txtDiscount.setText(String.format(Locale.getDefault(), "-%,.0f đ", totalDiscount));
        
        double finalTotal = subtotal - totalDiscount;
        if (txtTotal != null) txtTotal.setText(String.format(Locale.getDefault(), "%,.0f đ", Math.max(0, finalTotal)));

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
