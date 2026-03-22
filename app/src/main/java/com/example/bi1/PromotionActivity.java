package com.example.bi1;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class PromotionActivity extends AppCompatActivity {

    private EditText edtPromoCode, edtPromoValue;
    private RadioGroup rgPromoType, rgApplyMethod;
    private Spinner spnPromoTarget;
    private Button btnStartDate, btnEndDate, btnAddPromo;
    private RecyclerView rvPromotions;
    private PromotionAdapter promoAdapter;
    private ArrayList<Promotion> promotionList;
    private FirebaseFirestore db;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private ArrayList<String> targetIds = new ArrayList<>();
    private ArrayList<String> targetNames = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        ImageButton btnBack = findViewById(R.id.btnBackPromotion);
        edtPromoCode = findViewById(R.id.edtPromoCode);
        edtPromoValue = findViewById(R.id.edtPromoValue);
        rgPromoType = findViewById(R.id.rgPromoType);
        rgApplyMethod = findViewById(R.id.rgApplyMethod);
        spnPromoTarget = findViewById(R.id.spnPromoTarget);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnAddPromo = findViewById(R.id.btnAddPromo);
        
        rvPromotions = findViewById(R.id.rvPromotions);

        setupRecyclerViews();
        loadData();

        btnBack.setOnClickListener(v -> finish());

        rgApplyMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbVoucherCode) {
                edtPromoCode.setVisibility(View.VISIBLE);
            } else {
                edtPromoCode.setVisibility(View.GONE);
            }
        });

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, targetNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPromoTarget.setAdapter(spinnerAdapter);

        rgPromoType.setOnCheckedChangeListener((group, checkedId) -> {
            targetIds.clear();
            targetNames.clear();
            spinnerAdapter.notifyDataSetChanged();
            
            if (checkedId == R.id.rbAll) {
                spnPromoTarget.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbByCategory) {
                spnPromoTarget.setVisibility(View.VISIBLE);
                loadCategories();
            } else if (checkedId == R.id.rbByBook) {
                spnPromoTarget.setVisibility(View.VISIBLE);
                loadBooks();
            }
        });

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnAddPromo.setOnClickListener(v -> savePromotion());
    }

    private void setupRecyclerViews() {
        promotionList = new ArrayList<>();
        promoAdapter = new PromotionAdapter(this, promotionList);
        rvPromotions.setLayoutManager(new LinearLayoutManager(this));
        rvPromotions.setAdapter(promoAdapter);
    }

    private void loadData() {
        // Load danh sách khuyến mãi (Voucher + Giảm trực tiếp)
        db.collection("promotions")
                .orderBy("startDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        promotionList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            promotionList.add(doc.toObject(Promotion.class));
                        }
                        promoAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showDatePicker(boolean isStart) {
        Calendar calendar = isStart ? startCalendar : endCalendar;
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            if (isStart) btnStartDate.setText(sdf.format(calendar.getTime()));
            else btnEndDate.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            targetIds.clear(); targetNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                targetIds.add(doc.getId());
                targetNames.add(doc.getString("tenLoai"));
            }
            spinnerAdapter.notifyDataSetChanged();
            if (!targetNames.isEmpty()) spnPromoTarget.setSelection(0);
        });
    }

    private void loadBooks() {
        db.collection("books").get().addOnSuccessListener(queryDocumentSnapshots -> {
            targetIds.clear(); targetNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                targetIds.add(doc.getId());
                targetNames.add(doc.getString("tenSach"));
            }
            spinnerAdapter.notifyDataSetChanged();
            if (!targetNames.isEmpty()) spnPromoTarget.setSelection(0);
        });
    }

    private void savePromotion() {
        String valStr = edtPromoValue.getText().toString().trim();
        if (valStr.isEmpty()) { Toast.makeText(this, "Nhập giá trị giảm", Toast.LENGTH_SHORT).show(); return; }

        int percent = Integer.parseInt(valStr);
        int applyMethodId = rgApplyMethod.getCheckedRadioButtonId();
        int promoTypeId = rgPromoType.getCheckedRadioButtonId();
        
        String type = "all";
        String targetId = "";
        
        if (promoTypeId != R.id.rbAll) {
            if (targetIds.isEmpty() || spnPromoTarget.getSelectedItemPosition() == -1) {
                Toast.makeText(this, "Vui lòng chọn đối tượng áp dụng", Toast.LENGTH_SHORT).show();
                return;
            }
            if (promoTypeId == R.id.rbByCategory) {
                type = "category";
                targetId = targetIds.get(spnPromoTarget.getSelectedItemPosition());
            } else if (promoTypeId == R.id.rbByBook) {
                type = "book";
                targetId = targetIds.get(spnPromoTarget.getSelectedItemPosition());
            }
        }

        String id = UUID.randomUUID().toString();
        String code = "";
        
        if (applyMethodId == R.id.rbVoucherCode) {
            code = edtPromoCode.getText().toString().trim().toUpperCase();
            if (code.isEmpty()) { Toast.makeText(this, "Nhập mã voucher", Toast.LENGTH_SHORT).show(); return; }
        } else {
            // Giảm trực tiếp: Cập nhật giá sách ngay
            updateBooksDirectly(type, targetId, percent);
        }

        Promotion promo = new Promotion(id, code, percent, type, targetId, startCalendar.getTime(), endCalendar.getTime());
        db.collection("promotions").document(id).set(promo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã lưu chương trình khuyến mãi!", Toast.LENGTH_SHORT).show();
                    edtPromoCode.setText("");
                    edtPromoValue.setText("");
                });
    }

    private void updateBooksDirectly(String type, String targetId, int percent) {
        Query query;
        if (type.equals("book")) query = db.collection("books").whereEqualTo("Id", targetId);
        else if (type.equals("category")) query = db.collection("books").whereEqualTo("MaLoaiSach", targetId);
        else query = db.collection("books");

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Double giaGoc = doc.getDouble("GiaGoc");
                if (giaGoc == null) giaGoc = doc.getDouble("GiaBan");
                if (giaGoc != null) {
                    double giaMoi = giaGoc * (1 - (percent / 100.0));
                    batch.update(doc.getReference(), "GiaBan", giaMoi, "khuyenMai", percent, "GiaGoc", giaGoc);
                }
            }
            batch.commit();
        });
    }
}
