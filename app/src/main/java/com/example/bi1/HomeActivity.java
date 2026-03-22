package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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
        
        rvMenuCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryList = new ArrayList<>();
        categoryMenuAdapter = new CategoryMenuAdapter(this, categoryList, category -> {
            txtToolbarTitle.setText(category.getTenLoai().toUpperCase());
            if (!isLoggedIn || roleId != 1) {
                if (bottomNavigationView != null) bottomNavigationView.setSelectedItemId(R.id.nav_home);
                if (homeFragment != null) homeFragment.filterByCategory(category.getId());
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        });
        rvMenuCategories.setAdapter(categoryMenuAdapter);

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
        txtCartBadge.setText(String.valueOf(count));
        txtCartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoggedIn && roleId != 1) updateCartBadge();
    }
}
