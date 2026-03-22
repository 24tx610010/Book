package com.example.bi1;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddBookActivity extends AppCompatActivity {

    private EditText edtName, edtPrice, edtOriginalPrice, edtBookDesc, edtBookImage, edtAuthor, edtPublisher, edtYear, edtLanguage, edtStock;
    private Spinner spCategory;
    private Button btnSave;
    private ImageButton btnBack;
    private TextView txtTitle;
    private FirebaseFirestore db;
    
    private String bookId = null;
    private String oldCategoryId = null;
    private ArrayList<Category> categoryList;
    private ArrayAdapter<Category> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        btnBack = findViewById(R.id.btnBack);
        edtName = findViewById(R.id.edtBookName);
        edtOriginalPrice = findViewById(R.id.edtOriginalPrice);
        edtPrice = findViewById(R.id.edtBookPrice);
        edtStock = findViewById(R.id.edtStock);
        edtAuthor = findViewById(R.id.edtAuthor);
        edtPublisher = findViewById(R.id.edtPublisher);
        edtYear = findViewById(R.id.edtYear);
        edtLanguage = findViewById(R.id.edtLanguage);
        edtBookImage = findViewById(R.id.edtBookImage);
        edtBookDesc = findViewById(R.id.edtBookDesc);
        spCategory = findViewById(R.id.spCategory);
        btnSave = findViewById(R.id.btnSaveBook);
        txtTitle = findViewById(R.id.txtAddTitle);

        // Thiết lập Spinner cho Loại sách
        categoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Kiểm tra xem là thêm mới hay sửa
        if (getIntent().hasExtra("BOOK_DATA")) {
            Book book = (Book) getIntent().getSerializableExtra("BOOK_DATA");
            if (book != null) {
                bookId = book.getId();
                oldCategoryId = book.getMaLoaiSach();
                txtTitle.setText("CẬP NHẬT SÁCH");
                
                edtName.setText(book.getTenSach());
                edtOriginalPrice.setText(String.valueOf(book.getGiaGoc()));
                edtPrice.setText(String.valueOf(book.getGiaBan()));
                edtStock.setText(String.valueOf(book.getSoLuong()));
                edtAuthor.setText(book.getTacGia());
                edtPublisher.setText(book.getNhaXuatBan());
                edtYear.setText(book.getNamXuatBan());
                edtLanguage.setText(book.getNgonNgu());
                edtBookImage.setText(book.getHinhAnh());
                edtBookDesc.setText(book.getMoTa());
                
                btnSave.setText("CẬP NHẬT NGAY");
            }
        }

        // Tải danh sách loại sách từ Firebase
        loadCategories();

        btnSave.setOnClickListener(v -> saveBook());
    }

    private void loadCategories() {
        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Category cat = doc.toObject(Category.class);
                        cat.setId(doc.getId());
                        categoryList.add(cat);
                    }
                    categoryAdapter.notifyDataSetChanged();
                    
                    // Nếu là sửa, chọn lại đúng loại sách cũ
                    if (oldCategoryId != null) {
                        for (int i = 0; i < categoryList.size(); i++) {
                            if (categoryList.get(i).getId().equals(oldCategoryId)) {
                                spCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải loại sách", Toast.LENGTH_SHORT).show());
    }

    private void saveBook() {
        String name = edtName.getText().toString().trim();
        String originalPriceStr = edtOriginalPrice.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String stockStr = edtStock.getText().toString().trim();
        String author = edtAuthor.getText().toString().trim();
        String publisher = edtPublisher.getText().toString().trim();
        String year = edtYear.getText().toString().trim();
        String language = edtLanguage.getText().toString().trim();
        String desc = edtBookDesc.getText().toString().trim();
        String image = edtBookImage.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ tên, giá và số lượng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm loại sách trên Firebase trước", Toast.LENGTH_SHORT).show();
            return;
        }

        double price, originalPrice;
        int stock;
        try {
            price = Double.parseDouble(priceStr);
            originalPrice = originalPriceStr.isEmpty() ? price : Double.parseDouble(originalPriceStr);
            stock = Integer.parseInt(stockStr);
        } catch (Exception e) {
            Toast.makeText(this, "Giá hoặc số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy ID loại sách từ Spinner
        Category selectedCat = (Category) spCategory.getSelectedItem();
        String categoryId = selectedCat.getId();

        Map<String, Object> book = new HashMap<>();
        book.put("TenSach", name);
        book.put("GiaGoc", originalPrice);
        book.put("GiaBan", price);
        book.put("SoLuong", stock);
        book.put("TacGia", author);
        book.put("NhaXuatBan", publisher);
        book.put("NamXuatBan", year);
        book.put("NgonNgu", language);
        book.put("MoTa", desc);
        book.put("HinhAnh", image);
        book.put("MaLoaiSach", categoryId);

        if (bookId == null) {
            // Khởi tạo các giá trị mặc định cho sách mới
            book.put("luotBan", 0);
            book.put("rating", 5.0f);
            book.put("isNoiBat", false);
            book.put("khuyenMai", 0);
            
            db.collection("books").add(book)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("books").document(bookId).update(book)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
