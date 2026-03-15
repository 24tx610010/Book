package com.example.bi1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CategoryListActivity extends AppCompatActivity {

    private RecyclerView rvCategory;
    private Button btnAdd;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private ArrayList<Category> categoryList;
    private CategoryAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        db = FirebaseFirestore.getInstance();
        rvCategory = findViewById(R.id.rvCategoryAdmin);
        btnAdd = findViewById(R.id.btnAddCategory);
        btnBack = findViewById(R.id.btnBackCategory);

        btnBack.setOnClickListener(v -> finish());

        categoryList = new ArrayList<>();
        adapter = new CategoryAdminAdapter(this, categoryList);
        rvCategory.setLayoutManager(new LinearLayoutManager(this));
        rvCategory.setAdapter(adapter);

        listenToCategories();

        btnAdd.setOnClickListener(v -> showAddDialog());
    }

    private void listenToCategories() {
        db.collection("categories")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        categoryList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Category cat = doc.toObject(Category.class);
                            cat.setId(doc.getId());
                            categoryList.add(cat);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm loại sách mới");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        final EditText edtName = view.findViewById(R.id.edtNewCategoryName);
        builder.setView(view);

        builder.setPositiveButton("THÊM", (dialog, which) -> {
            String name = edtName.getText().toString().trim();
            if (!name.isEmpty()) {
                Map<String, Object> cat = new HashMap<>();
                cat.put("tenLoai", name);
                db.collection("categories").add(cat)
                        .addOnSuccessListener(ref -> Toast.makeText(this, "Đã thêm loại sách", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }
}
