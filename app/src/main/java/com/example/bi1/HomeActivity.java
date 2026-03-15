package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView, rvMenuCategories;
    private TextView txtWelcome, txtToolbarTitle, menuHome, txtCartBadge;
    private Button btnAddBook;
    private ImageButton btnProfile, btnMenu;
    private View layoutCart;
    private EditText edtSearch;

    private ArrayList<Book> bookList;
    private BookAdapter adapter;
    private FirebaseFirestore firestore;

    private ArrayList<Category> categoryList;
    private CategoryMenuAdapter categoryMenuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ánh xạ view
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);
        recyclerView = findViewById(R.id.recyclerView);
        rvMenuCategories = findViewById(R.id.rvMenuCategories);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        menuHome = findViewById(R.id.menuHome);
        btnProfile = findViewById(R.id.btnProfile);
        btnAddBook = findViewById(R.id.btnAddBook);
        layoutCart = findViewById(R.id.layoutCart);
        txtCartBadge = findViewById(R.id.txtCartBadge);
        edtSearch = findViewById(R.id.edtSearch);

        firestore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        int roleId = sp.getInt("roleid", 2);
        String user = sp.getString("username", "User");
        txtWelcome.setText("Xin chào " + user + " 👋");

        adapter = new BookAdapter(this, bookList, roleId);
        recyclerView.setAdapter(adapter);

        rvMenuCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryList = new ArrayList<>();
        categoryMenuAdapter = new CategoryMenuAdapter(this, categoryList, category -> {
            txtToolbarTitle.setText(category.getTenLoai().toUpperCase());
            loadBooksByCategory(category.getId());
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        rvMenuCategories.setAdapter(categoryMenuAdapter);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        menuHome.setOnClickListener(v -> {
            txtToolbarTitle.setText("BOOK STORE");
            loadAllBooks();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        loadCategories();
        loadAllBooks();
        
        // Cập nhật số lượng giỏ hàng ban đầu
        refreshCartBadge();

        setupFeatures(roleId);
    }

    // Hàm cập nhật số lượng hiển thị trên biểu tượng giỏ hàng từ Session
    private void refreshCartBadge() {
        int count = 0;
        for (CartItem item : CartManager.getCartList()) {
            count += item.getQuantity();
        }
        txtCartBadge.setText(String.valueOf(count));
        txtCartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại số lượng mỗi khi quay lại trang chủ (từ trang Chi tiết hoặc Giỏ hàng)
        refreshCartBadge();
    }

    private void loadCategories() {
        firestore.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            categoryList.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Category cat = doc.toObject(Category.class);
                cat.setId(doc.getId());
                categoryList.add(cat);
            }
            categoryMenuAdapter.notifyDataSetChanged();
        });
    }

    private void loadAllBooks() {
        firestore.collection("books")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        ArrayList<Book> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            newList.add(book);
                        }
                        adapter.updateList(newList);
                    }
                });
    }

    private void loadBooksByCategory(String categoryId) {
        firestore.collection("books")
                .whereEqualTo("MaLoaiSach", categoryId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        ArrayList<Book> newList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            Book book = document.toObject(Book.class);
                            book.setId(document.getId());
                            newList.add(book);
                        }
                        adapter.updateList(newList);
                    }
                });
    }

    private void setupFeatures(int roleId) {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (roleId != 1) {
            btnAddBook.setVisibility(View.GONE);
            layoutCart.setVisibility(View.VISIBLE);
        } else {
            btnAddBook.setVisibility(View.VISIBLE);
            layoutCart.setVisibility(View.GONE);
        }

        layoutCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnAddBook.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }
}
