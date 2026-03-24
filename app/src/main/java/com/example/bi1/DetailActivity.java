package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DetailActivity extends AppCompatActivity {

    private RecyclerView rvBookImages, rvReviews;
    private RecyclerView rvSameAuthor, rvSameCategory;
    
    private RelatedBooksAdapter sameAuthorAdapter, sameCategoryAdapter;
    private ReviewAdapter reviewAdapter;
    
    private ArrayList<Book> sameAuthorList, sameCategoryList;
    private ArrayList<Review> reviewList;
    
    private FirebaseFirestore db;
    private Book currentBook;
    private String userPhone, userName;
    private TextView txtName, txtPrice, txtDesc, txtBookDetails, txtOriginalPrice, txtDiscountLabel;
    private View layoutSameAuthor, layoutSameCategory;
    private TextView txtSameCategoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = FirebaseFirestore.getInstance();
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        userPhone = sp.getString("phone", "");
        userName = sp.getString("username", "Khách");

        // Ánh xạ View
        ImageButton btnBack = findViewById(R.id.btnBack);
        rvBookImages = findViewById(R.id.rvBookImages);
        txtName = findViewById(R.id.txtName);
        txtPrice = findViewById(R.id.txtPrice);
        txtOriginalPrice = findViewById(R.id.txtOriginalPriceDetail);
        txtDiscountLabel = findViewById(R.id.txtDiscountLabelDetail);
        txtDesc = findViewById(R.id.txtDesc);
        txtBookDetails = findViewById(R.id.txtBookDetails);
        
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText edtComment = findViewById(R.id.edtComment);
        Button btnSubmitReview = findViewById(R.id.btnSubmitReview);
        
        View layoutUserActions = findViewById(R.id.layoutUserActions);
        View layoutReview = findViewById(R.id.layoutReview);
        
        // --- CẤU HÌNH SÁCH CÙNG TÁC GIẢ ---
        layoutSameAuthor = findViewById(R.id.layoutSameAuthor);
        rvSameAuthor = findViewById(R.id.rvSameAuthor);
        sameAuthorList = new ArrayList<>();
        sameAuthorAdapter = new RelatedBooksAdapter(this, sameAuthorList);
        rvSameAuthor.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSameAuthor.setAdapter(sameAuthorAdapter);

        // --- CẤU HÌNH SÁCH CÙNG THỂ LOẠI (MÃ LOẠI) ---
        layoutSameCategory = findViewById(R.id.layoutSameCategory);
        txtSameCategoryTitle = findViewById(R.id.txtSameCategoryTitle);
        rvSameCategory = findViewById(R.id.rvSameCategory);
        sameCategoryList = new ArrayList<>();
        sameCategoryAdapter = new RelatedBooksAdapter(this, sameCategoryList);
        rvSameCategory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSameCategory.setAdapter(sameCategoryAdapter);

        // Reviews
        rvReviews = findViewById(R.id.rvReviews);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        // Lấy dữ liệu từ Intent
        currentBook = (Book) getIntent().getSerializableExtra("book");
        if (currentBook != null) {
            displayBookInfo();
            loadRelatedData();
        }

        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btnAddToCart).setOnClickListener(v -> {
            CartManager.addToCart(currentBook, 1);
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.btnBuyNow).setOnClickListener(v -> {
            if (currentBook.getGiaBan() < 200000) {
                checkLoyaltyForFreePurchase();
            } else {
                addToCartAndGo();
            }
        });

        btnSubmitReview.setOnClickListener(v -> {
            String comment = edtComment.getText().toString().trim();
            if (comment.isEmpty()) return;
            Review newReview = new Review(UUID.randomUUID().toString(), currentBook.getId(), userPhone, userName, ratingBar.getRating(), comment, new Date());
            db.collection("reviews").add(newReview).addOnSuccessListener(ref -> {
                edtComment.setText("");
                Toast.makeText(this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void checkLoyaltyForFreePurchase() {
        if (userPhone.isEmpty()) {
            addToCartAndGo();
            return;
        }

        // Đọc từ bảng loyal_customers
        db.collection("loyal_customers").whereEqualTo("userId", userPhone).limit(1).get()
            .addOnSuccessListener(snapshots -> {
                if (!snapshots.isEmpty()) {
                    long points = snapshots.getDocuments().get(0).getLong("points") != null ? 
                                 snapshots.getDocuments().get(0).getLong("points") : 0;
                    
                    if (points >= 10) {
                        new AlertDialog.Builder(this)
                            .setTitle("Dùng điểm tích lũy")
                            .setMessage("Bạn có " + points + " điểm tích lũy. Bạn có muốn dùng 10 điểm để nhận miễn phí cuốn sách này không?")
                            .setPositiveButton("DÙNG ĐIỂM", (dialog, which) -> processFreePurchase(snapshots.getDocuments().get(0).getReference()))
                            .setNegativeButton("MUA BÌNH THƯỜNG", (dialog, which) -> addToCartAndGo())
                            .show();
                    } else {
                        addToCartAndGo();
                    }
                } else {
                    addToCartAndGo();
                }
            });
    }

    private void processFreePurchase(DocumentReference loyalRef) {
        db.runTransaction(transaction -> {
            Long currentPoints = transaction.get(loyalRef).getLong("points");
            if (currentPoints != null && currentPoints >= 10) {
                transaction.update(loyalRef, "points", currentPoints - 10);
                return true;
            }
            return false;
        }).addOnSuccessListener(success -> {
            if (success) {
                Toast.makeText(this, "Chúc mừng! Bạn đã đổi 10 điểm lấy sách thành công. Đơn hàng 0đ sẽ được xử lý.", Toast.LENGTH_LONG).show();
                createFreeOrder();
            } else {
                Toast.makeText(this, "Không đủ điểm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFreeOrder() {
        String orderId = "FREE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order newOrder = new Order(orderId, userPhone, new Date(), "Loyalty Points", 0, 0);
        newOrder.setReceiverName(userName);
        newOrder.setReceiverPhone(userPhone);
        newOrder.setShippingAddress("Nhận tại cửa hàng (Đổi điểm tích lũy)");

        db.collection("orders").document(orderId).set(newOrder).addOnSuccessListener(aVoid -> {
            OrderDetail detail = new OrderDetail(UUID.randomUUID().toString().substring(0, 10), orderId, 
                    currentBook.getId(), currentBook.getTenSach(), 0, 1, 0);
            db.collection("order_details").add(detail);
            Toast.makeText(this, "Đã tạo đơn hàng miễn phí!", Toast.LENGTH_SHORT).show();
        });
    }

    private void addToCartAndGo() {
        CartManager.addToCart(currentBook, 1);
        startActivity(new Intent(this, CartActivity.class));
    }

    private void displayBookInfo() {
        txtName.setText(currentBook.getTenSach());
        txtPrice.setText(String.format("%,.0f đ", currentBook.getGiaBan()));
        txtDesc.setText(currentBook.getMoTa());
        
        if (currentBook.getGiaGoc() > currentBook.getGiaBan()) {
            txtOriginalPrice.setVisibility(View.VISIBLE);
            txtOriginalPrice.setText(String.format("%,.0f đ", currentBook.getGiaGoc()));
            txtOriginalPrice.setPaintFlags(txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        List<String> images = new ArrayList<>();
        if (currentBook.getHinhAnh() != null) images.add(currentBook.getHinhAnh());
        if (currentBook.getHinhAnhChiTiet() != null) images.addAll(currentBook.getHinhAnhChiTiet());
        rvBookImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvBookImages.setAdapter(new ImageSliderAdapter(images));

        // Tải tên loại sách
        if (currentBook.getMaLoaiSach() != null) {
            db.collection("categories").document(currentBook.getMaLoaiSach()).get().addOnSuccessListener(doc -> {
                String catName = doc.exists() ? doc.getString("tenLoai") : "Thể loại khác";
                txtBookDetails.setText("Tác giả: " + currentBook.getTacGia() + "\nThể loại: " + catName);
                if (txtSameCategoryTitle != null) txtSameCategoryTitle.setText("Sách cùng loại " + catName);
            });
        }
    }

    private void loadRelatedData() {
        // Sách cùng tác giả
        if (currentBook.getTacGia() != null) {
            db.collection("books").whereEqualTo("TacGia", currentBook.getTacGia()).limit(10).get()
                .addOnSuccessListener(snapshots -> {
                    sameAuthorList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Book b = doc.toObject(Book.class);
                        b.setId(doc.getId());
                        if (!b.getId().equals(currentBook.getId())) sameAuthorList.add(b);
                    }
                    layoutSameAuthor.setVisibility(sameAuthorList.isEmpty() ? View.GONE : View.VISIBLE);
                    sameAuthorAdapter.notifyDataSetChanged();
                });
        }

        // Sách cùng mã loại
        if (currentBook.getMaLoaiSach() != null) {
            db.collection("books").whereEqualTo("MaLoaiSach", currentBook.getMaLoaiSach()).limit(10).get()
                .addOnSuccessListener(snapshots -> {
                    sameCategoryList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Book b = doc.toObject(Book.class);
                        b.setId(doc.getId());
                        if (!b.getId().equals(currentBook.getId())) sameCategoryList.add(b);
                    }
                    layoutSameCategory.setVisibility(sameCategoryList.isEmpty() ? View.GONE : View.VISIBLE);
                    sameCategoryAdapter.notifyDataSetChanged();
                });
        }

        loadReviews(currentBook.getId());
    }

    private void loadReviews(String bookId) {
        db.collection("reviews").whereEqualTo("bookId", bookId).orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener((v, e) -> {
                if (v != null) {
                    reviewList.clear();
                    for (QueryDocumentSnapshot d : v) reviewList.add(d.toObject(Review.class));
                    reviewAdapter.notifyDataSetChanged();
                }
            });
    }
}
