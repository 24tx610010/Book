package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
<<<<<<< HEAD
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
=======
import android.view.View;
>>>>>>> 0d5c59f (22/3)
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
<<<<<<< HEAD
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

=======
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
>>>>>>> 0d5c59f (22/3)
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
<<<<<<< HEAD
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
=======
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton btnAddBook;
    private ImageButton btnMenu;
    private TextView txtToolbarTitle, txtCartBadge;
    private View layoutCart, layoutAdminDrawer, layoutCategoryDrawer;
    
    private FirebaseFirestore db;
    private ArrayList<Category> categoryList;
    private CategoryMenuAdapter categoryMenuAdapter;
    private RecyclerView rvMenuCategories;
    
    private int roleId;
    private boolean isLoggedIn;
    private HomeFragment homeFragment;
>>>>>>> 0d5c59f (22/3)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

<<<<<<< HEAD
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

=======
        db = FirebaseFirestore.getInstance();
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        
        isLoggedIn = sp.getBoolean("logged_in", false);
        roleId = sp.getInt("roleid", 2);

        // Ánh xạ
        drawerLayout = findViewById(R.id.drawerLayout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnAddBook = findViewById(R.id.btnAddBook);
        btnMenu = findViewById(R.id.btnMenu);
        txtToolbarTitle = findViewById(R.id.txtToolbarTitle);
        rvMenuCategories = findViewById(R.id.rvMenuCategories);
        layoutCart = findViewById(R.id.layoutCart);
        txtCartBadge = findViewById(R.id.txtCartBadge);
        layoutAdminDrawer = findViewById(R.id.layoutAdminDrawer);
        layoutCategoryDrawer = findViewById(R.id.layoutCategoryDrawer);

        // 1. Phân quyền khởi tạo
        if (isLoggedIn && roleId == 1) {
            btnAddBook.setVisibility(View.VISIBLE);
            if (layoutCart != null) layoutCart.setVisibility(View.GONE);
            if (layoutAdminDrawer != null) layoutAdminDrawer.setVisibility(View.VISIBLE);
            if (layoutCategoryDrawer != null) layoutCategoryDrawer.setVisibility(View.GONE);
            if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
            
            txtToolbarTitle.setText("THỐNG KÊ DOANH THU");
            loadFragment(new StatisticFragment());
        } else {
            btnAddBook.setVisibility(View.GONE);
            if (layoutCart != null) {
                layoutCart.setVisibility(View.VISIBLE);
                updateCartBadge();
            }
            if (layoutAdminDrawer != null) layoutAdminDrawer.setVisibility(View.GONE);
            if (layoutCategoryDrawer != null) layoutCategoryDrawer.setVisibility(View.VISIBLE);
            if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);
            
            homeFragment = new HomeFragment();
            txtToolbarTitle.setText("BOOK STORE");
            loadFragment(homeFragment);
        }

        // 2. Bottom Navigation với check đăng nhập
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                
                if (itemId == R.id.nav_home) {
                    txtToolbarTitle.setText("BOOK STORE");
                    if (homeFragment == null) homeFragment = new HomeFragment();
                    return loadFragment(homeFragment);
                }

                if (!isLoggedIn) {
                    Toast.makeText(this, "Vui lòng đăng nhập để sử dụng!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }

                Fragment fragment = null;
                if (itemId == R.id.nav_favorite) {
                    txtToolbarTitle.setText("YÊU THÍCH");
                    fragment = new FavoriteFragment();
                } else if (itemId == R.id.nav_orders) {
                    txtToolbarTitle.setText("ĐƠN HÀNG CỦA BẠN");
                    fragment = new OrderHistoryFragment();
                } else if (itemId == R.id.nav_cart) {
                    txtToolbarTitle.setText("GIỎ HÀNG");
                    fragment = new CartFragment();
                } else if (itemId == R.id.nav_account) {
                    txtToolbarTitle.setText("TÀI KHOẢN");
                    fragment = new AccountFragment();
                }
                return loadFragment(fragment);
            });
        }

        setupSideMenu();

        btnAddBook.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        if (layoutCart != null) {
            layoutCart.setOnClickListener(v -> {
                if (!isLoggedIn) {
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    bottomNavigationView.setSelectedItemId(R.id.nav_cart);
                }
            });
        }
    }

    // THÊM HÀM NÀY ĐỂ TRẢ VỀ ROLEID CHO FRAGMENT
    public int getRoleId() {
        return roleId;
    }

    private void setupSideMenu() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        
>>>>>>> 0d5c59f (22/3)
        rvMenuCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryList = new ArrayList<>();
        categoryMenuAdapter = new CategoryMenuAdapter(this, categoryList, category -> {
            txtToolbarTitle.setText(category.getTenLoai().toUpperCase());
<<<<<<< HEAD
            loadBooksByCategory(category.getId());
=======
            if (!isLoggedIn || roleId != 1) {
                if (bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.nav_home);
                if (homeFragment != null) homeFragment.filterByCategory(category.getId());
            }
>>>>>>> 0d5c59f (22/3)
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        rvMenuCategories.setAdapter(categoryMenuAdapter);

<<<<<<< HEAD
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
=======
        db.collection("categories").get().addOnSuccessListener(snapshots -> {
            categoryList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                categoryList.add(doc.toObject(Category.class));
            }
            categoryMenuAdapter.notifyDataSetChanged();
        });

        findViewById(R.id.menuHome).setOnClickListener(v -> {
            if (isLoggedIn && roleId == 1) {
                txtToolbarTitle.setText("THỐNG KÊ DOANH THU");
                loadFragment(new StatisticFragment());
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Nút Admin Drawer
        if (isLoggedIn && roleId == 1) {
            findViewById(R.id.menuAdminStatistic).setOnClickListener(v -> {
                txtToolbarTitle.setText("THỐNG KÊ DOANH THU");
                loadFragment(new StatisticFragment());
                drawerLayout.closeDrawer(GravityCompat.START);
            });
            findViewById(R.id.menuAdminPromotion).setOnClickListener(v -> {
                startActivity(new Intent(this, PromotionActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            });
            findViewById(R.id.menuAdminBooks).setOnClickListener(v -> {
                txtToolbarTitle.setText("QUẢN LÝ SÁCH");
                if (homeFragment == null) homeFragment = new HomeFragment();
                loadFragment(homeFragment);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
            findViewById(R.id.menuAdminCategories).setOnClickListener(v -> {
                startActivity(new Intent(this, CategoryListActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            });
            findViewById(R.id.menuAdminOrders).setOnClickListener(v -> {
                startActivity(new Intent(this, AdminOrderListActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            });
            findViewById(R.id.menuAdminUsers).setOnClickListener(v -> {
                startActivity(new Intent(this, AdminUserListActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }
        findViewById(R.id.menuLogout).setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        sp.edit().putBoolean("logged_in", false).apply();
        isLoggedIn = false;
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
            return true;
        }
        return false;
    }

    public void refreshToHome() {
        if (bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    public void refreshCartBadge() { updateCartBadge(); }

    private void updateCartBadge() {
        if (txtCartBadge == null) return;
        int count = CartManager.getCartList().size();
>>>>>>> 0d5c59f (22/3)
        txtCartBadge.setText(String.valueOf(count));
        txtCartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
<<<<<<< HEAD
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
=======
        if (isLoggedIn && roleId != 1) updateCartBadge();
>>>>>>> 0d5c59f (22/3)
    }
}
