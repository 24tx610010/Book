package com.example.bi1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
            if (btnAddBook != null) btnAddBook.setVisibility(View.VISIBLE);
            if (layoutCart != null) layoutCart.setVisibility(View.GONE);
            if (layoutAdminDrawer != null) layoutAdminDrawer.setVisibility(View.VISIBLE);
            if (layoutCategoryDrawer != null) layoutCategoryDrawer.setVisibility(View.GONE);
            if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
            
            txtToolbarTitle.setText("THỐNG KÊ DOANH THU");
            loadFragment(new StatisticFragment());
        } else {
            if (btnAddBook != null) btnAddBook.setVisibility(View.GONE);
            if (layoutCart != null) {
                layoutCart.setVisibility(View.VISIBLE);
                refreshCartBadge();
            }
            if (layoutAdminDrawer != null) layoutAdminDrawer.setVisibility(View.GONE);
            if (layoutCategoryDrawer != null) layoutCategoryDrawer.setVisibility(View.VISIBLE);
            if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);
            
            homeFragment = new HomeFragment();
            txtToolbarTitle.setText("BOOK STORE");
            loadFragment(homeFragment);
        }

        // 2. Bottom Navigation
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    txtToolbarTitle.setText("BOOK STORE");
                    if (homeFragment == null) homeFragment = new HomeFragment();
                    return loadFragment(homeFragment);
                }

                if (!isLoggedIn) {
                    Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return false;
                }

                Fragment fragment = null;
                if (itemId == R.id.nav_favorite) {
                    txtToolbarTitle.setText("YÊU THÍCH");
                    fragment = new FavoriteFragment();
                } else if (itemId == R.id.nav_orders) {
                    txtToolbarTitle.setText("ĐƠN HÀNG");
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
        if (btnAddBook != null) btnAddBook.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        
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

    public int getRoleId() {
        return roleId;
    }

    public void refreshToHome() {
        if (roleId == 1) {
            txtToolbarTitle.setText("BOOK STORE");
            if (homeFragment == null) homeFragment = new HomeFragment();
            loadFragment(homeFragment);
        } else {
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    private void setupSideMenu() {
        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        
        if (rvMenuCategories != null) {
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
            }).addOnFailureListener(e -> Log.e("Home", "Error load cat: " + e.getMessage()));
        }

        View menuHome = findViewById(R.id.menuHome);
        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                if (isLoggedIn && roleId == 1) {
                    txtToolbarTitle.setText("THỐNG KÊ DOANH THU");
                    loadFragment(new StatisticFragment());
                } else {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        // Admin Menu Click
        if (isLoggedIn && roleId == 1) {
            setupAdminMenuClick();
        }
        
        View menuLogout = findViewById(R.id.menuLogout);
        if (menuLogout != null) menuLogout.setOnClickListener(v -> logout());
    }

    private void setupAdminMenuClick() {
        View v1 = findViewById(R.id.menuAdminStatistic);
        if (v1 != null) v1.setOnClickListener(v -> { 
            txtToolbarTitle.setText("THỐNG KÊ DOANH THU");
            loadFragment(new StatisticFragment()); 
            drawerLayout.closeDrawer(GravityCompat.START); 
        });
        
        View v2 = findViewById(R.id.menuAdminBooks);
        if (v2 != null) v2.setOnClickListener(v -> { 
            txtToolbarTitle.setText("QUẢN LÝ SÁCH");
            if (homeFragment == null) homeFragment = new HomeFragment(); 
            loadFragment(homeFragment); 
            drawerLayout.closeDrawer(GravityCompat.START); 
        });
        
        View v3 = findViewById(R.id.menuAdminOrders);
        if (v3 != null) v3.setOnClickListener(v -> { 
            startActivity(new Intent(this, AdminOrderListActivity.class)); 
            drawerLayout.closeDrawer(GravityCompat.START); 
        });

        View v4 = findViewById(R.id.menuAdminUsers);
        if (v4 != null) v4.setOnClickListener(v -> { 
            startActivity(new Intent(this, AdminUserListActivity.class)); 
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        View vLoyal = findViewById(R.id.menuAdminLoyalCustomers);
        if (vLoyal != null) vLoyal.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminLoyalCustomersActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Bổ sung các chức năng còn thiếu
        View vPromo = findViewById(R.id.menuAdminPromotion);
        if (vPromo != null) vPromo.setOnClickListener(v -> {
            startActivity(new Intent(this, PromotionActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        View vCat = findViewById(R.id.menuAdminCategories);
        if (vCat != null) vCat.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryListActivity.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void logout() {
        SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
        sp.edit().clear().apply();
        isLoggedIn = false;
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null && !isFinishing()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
            return true;
        }
        return false;
    }

    public void refreshCartBadge() {
        if (txtCartBadge == null) return;
        int count = CartManager.getCartList().size();
        txtCartBadge.setText(String.valueOf(count));
        txtCartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoggedIn && roleId != 1) refreshCartBadge();
    }
}
