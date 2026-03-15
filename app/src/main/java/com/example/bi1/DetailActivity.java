package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class DetailActivity extends AppCompatActivity {

    private RecyclerView rvRelated, rvReviews;
    private RelatedBooksAdapter relatedAdapter;
    private ReviewAdapter reviewAdapter;
    private ArrayList<Book> relatedList;
    private ArrayList<Review> reviewList;
    private FirebaseFirestore db;
    private Book currentBook;
    private String userPhone, userName;
    private TextView txtName, txtPrice, txtDesc, txtBookDetails;

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
        ImageView imgBook = findViewById(R.id.imgBook);
        txtName = findViewById(R.id.txtName);
        txtPrice = findViewById(R.id.txtPrice);
        txtDesc = findViewById(R.id.txtDesc);
        txtBookDetails = findViewById(R.id.txtBookDetails);
        
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText edtComment = findViewById(R.id.edtComment);
        Button btnSubmitReview = findViewById(R.id.btnSubmitReview);
        
        View layoutUserActions = findViewById(R.id.layoutUserActions);
        View layoutReview = findViewById(R.id.layoutReview);
        View dividerReview = findViewById(R.id.dividerReview);
        
        Button btnAddToCart = findViewById(R.id.btnAddToCart);
        Button btnBuyNow = findViewById(R.id.btnBuyNow);

        // Lấy dữ liệu từ Intent
        currentBook = (Book) getIntent().getSerializableExtra("book");
        if (currentBook == null) {
            currentBook = new Book();
            currentBook.setId(getIntent().getStringExtra("bookId"));
            currentBook.setTenSach(getIntent().getStringExtra("name"));
            currentBook.setGiaBan(Double.parseDouble(getIntent().getStringExtra("price") != null ? getIntent().getStringExtra("price") : "0"));
            currentBook.setMoTa(getIntent().getStringExtra("desc"));
            currentBook.setHinhAnh(getIntent().getStringExtra("image"));
            currentBook.setTacGia(getIntent().getStringExtra("author"));
            currentBook.setNhaXuatBan(getIntent().getStringExtra("publisher"));
            currentBook.setNamXuatBan(getIntent().getStringExtra("year"));
            currentBook.setNgonNgu(getIntent().getStringExtra("language"));
        }

        displayBookInfo(imgBook);

        // Cấu hình Recycler Related
        rvRelated = findViewById(R.id.rvRelatedBooks);
        relatedList = new ArrayList<>();
        relatedAdapter = new RelatedBooksAdapter(this, relatedList);
        rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRelated.setAdapter(relatedAdapter);

        // Cấu hình Recycler Reviews
        rvReviews = findViewById(R.id.rvReviews);
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);

        // Load dữ liệu
        if (currentBook.getTacGia() != null && !currentBook.getTacGia().isEmpty()) {
            loadRelatedBooks(currentBook.getTacGia());
        }
        if (currentBook.getId() != null) {
            loadReviews(currentBook.getId());
        }

        // Phân quyền Admin
        int roleId = sp.getInt("roleid", 2);
        if (roleId == 1) {
            layoutUserActions.setVisibility(View.GONE);
            layoutReview.setVisibility(View.GONE);
            dividerReview.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());
        btnAddToCart.setOnClickListener(v -> CartManager.addToCart(currentBook, 1));
        btnBuyNow.setOnClickListener(v -> {
            CartManager.addToCart(currentBook, 1);
            startActivity(new Intent(this, CartActivity.class));
        });

        btnSubmitReview.setOnClickListener(v -> {
            String comment = edtComment.getText().toString().trim();
            float rating = ratingBar.getRating();
            if (comment.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show();
                return;
            }
            Review newReview = new Review(UUID.randomUUID().toString(), currentBook.getId(), userPhone, userName, rating, comment, new Date());
            db.collection("reviews").add(newReview).addOnSuccessListener(ref -> {
                Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                edtComment.setText("");
                ratingBar.setRating(5);
            });
        });
    }

    private void displayBookInfo(ImageView imgBook) {
        txtName.setText(currentBook.getTenSach());
        txtDesc.setText(currentBook.getMoTa() != null ? currentBook.getMoTa() : "Chưa có mô tả.");
        txtPrice.setText(String.format("%,.0f đ", currentBook.getGiaBan()));
        Glide.with(this).load(currentBook.getHinhAnh()).placeholder(R.mipmap.ic_launcher).into(imgBook);
        
        StringBuilder details = new StringBuilder();
        details.append("Tác giả: ").append(currentBook.getTacGia() != null ? currentBook.getTacGia() : "Đang cập nhật").append("\n");
        details.append("Nhà xuất bản: ").append(currentBook.getNhaXuatBan() != null ? currentBook.getNhaXuatBan() : "Đang cập nhật").append("\n");
        details.append("Năm xuất bản: ").append(currentBook.getNamXuatBan() != null ? currentBook.getNamXuatBan() : "Đang cập nhật").append("\n");
        details.append("Ngôn ngữ: ").append(currentBook.getNgonNgu() != null ? currentBook.getNgonNgu() : "Tiếng Việt");
        txtBookDetails.setText(details.toString());
    }

    private void loadReviews(String bookId) {
        db.collection("reviews")
                .whereEqualTo("bookId", bookId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        reviewList.clear();
                        float totalRating = 0;
                        for (QueryDocumentSnapshot doc : value) {
                            Review r = doc.toObject(Review.class);
                            reviewList.add(r);
                            totalRating += r.getRatingValue();
                        }
                        reviewAdapter.notifyDataSetChanged();
                        
                        if (!reviewList.isEmpty()) {
                            float avg = totalRating / reviewList.size();
                            updateAverageRating(bookId, avg);
                        }
                    }
                });
    }

    private void updateAverageRating(String bookId, float avg) {
        db.collection("books").document(bookId).update("rating", avg);
    }

    private void loadRelatedBooks(String author) {
        db.collection("books")
                .whereEqualTo("TacGia", author)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    relatedList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Book b = doc.toObject(Book.class);
                        b.setId(doc.getId());
                        // Chỉ hiện sách cùng tác giả nhưng không trùng với cuốn đang xem
                        if (!b.getTenSach().equalsIgnoreCase(currentBook.getTenSach())) {
                            relatedList.add(b);
                        }
                    }
                    relatedAdapter.notifyDataSetChanged();
                    
                    View relatedTitle = findViewById(R.id.txtRelatedTitle);
                    if (relatedList.isEmpty()) {
                        if (relatedTitle != null) relatedTitle.setVisibility(View.GONE);
                        rvRelated.setVisibility(View.GONE);
                    } else {
                        if (relatedTitle != null) relatedTitle.setVisibility(View.VISIBLE);
                        rvRelated.setVisibility(View.VISIBLE);
                    }
                });
    }
}
