package com.example.bi1;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PromotionActivity extends AppCompatActivity {

    private EditText edtPromoCode, edtPromoValue;
    private RadioGroup rgPromoType;
    private Spinner spnPromoTarget;
    private Button btnStartDate, btnEndDate, btnAddPromo;
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
        spnPromoTarget = findViewById(R.id.spnPromoTarget);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnAddPromo = findViewById(R.id.btnAddPromo);

        btnBack.setOnClickListener(v -> finish());

        // Mặc định ngày bắt đầu là hôm nay, kết thúc là 7 ngày sau
        btnStartDate.setText(sdf.format(startCalendar.getTime()));
        endCalendar.add(Calendar.DAY_OF_YEAR, 7);
        btnEndDate.setText(sdf.format(endCalendar.getTime()));

        // Setup Spinner
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, targetNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPromoTarget.setAdapter(spinnerAdapter);

        rgPromoType.setOnCheckedChangeListener((group, checkedId) -> {
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

    private void showDatePicker(boolean isStart) {
        Calendar calendar = isStart ? startCalendar : endCalendar;
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            if (isStart) btnStartDate.setText(sdf.format(calendar.getTime()));
            else btnEndDate.setText(sdf.format(calendar.getTime()));
            
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadCategories() {
        db.collection("categories").get().addOnSuccessListener(queryDocumentSnapshots -> {
            targetIds.clear();
            targetNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                targetIds.add(doc.getId());
                targetNames.add(doc.getString("tenLoai"));
            }
            spinnerAdapter.notifyDataSetChanged();
        });
    }

    private void loadBooks() {
        db.collection("books").get().addOnSuccessListener(queryDocumentSnapshots -> {
            targetIds.clear();
            targetNames.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                targetIds.add(doc.getId());
                targetNames.add(doc.getString("tenSach"));
            }
            spinnerAdapter.notifyDataSetChanged();
        });
    }

    private void savePromotion() {
        String code = edtPromoCode.getText().toString().trim().toUpperCase();
        String valStr = edtPromoValue.getText().toString().trim();
        
        if (code.isEmpty() || valStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // CHỈNH GIỜ: Bắt đầu lúc 00:00:00
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);

        // CHỈNH GIỜ: Kết thúc lúc 23:59:59
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);

        if (endCalendar.before(startCalendar)) {
            Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }

        double value = Double.parseDouble(valStr);
        String type = "all";
        String targetId = "";

        int checkedId = rgPromoType.getCheckedRadioButtonId();
        if (checkedId == R.id.rbByCategory) {
            type = "category";
            if (targetIds.isEmpty()) return;
            targetId = targetIds.get(spnPromoTarget.getSelectedItemPosition());
        } else if (checkedId == R.id.rbByBook) {
            type = "book";
            if (targetIds.isEmpty()) return;
            targetId = targetIds.get(spnPromoTarget.getSelectedItemPosition());
        }

        String id = UUID.randomUUID().toString();
        Promotion promo = new Promotion(id, code, value, type, targetId, startCalendar.getTime(), endCalendar.getTime());

        db.collection("promotions").document(id).set(promo)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã lưu khuyến mãi!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
