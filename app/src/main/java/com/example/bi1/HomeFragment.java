package com.example.bi1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private ArrayList<Book> bookList;
    private FirebaseFirestore db;
    private EditText edtSearch;
    private int roleId;
    private ListenerRegistration bookListener, bestSellerListener;
    private boolean isErrorShown = false;

    // UI cho Sách bán chạy
    private View layoutBestSeller;
    private ImageView imgBestSeller;
    private TextView txtBestSellerName, txtBestSellerSold, txtBestSellerPrice;
    private Button btnViewBestSeller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rvHomeBooks);
        edtSearch = view.findViewById(R.id.edtSearchHome);

        // Ánh xạ phần bán chạy
        layoutBestSeller = view.findViewById(R.id.layoutBestSeller);
        imgBestSeller = view.findViewById(R.id.imgBestSeller);
        txtBestSellerName = view.findViewById(R.id.txtBestSellerName);
        txtBestSellerSold = view.findViewById(R.id.txtBestSellerSold);
        txtBestSellerPrice = view.findViewById(R.id.txtBestSellerPrice);
        btnViewBestSeller = view.findViewById(R.id.btnViewBestSeller);

        if (getActivity() instanceof HomeActivity) {
            roleId = ((HomeActivity) getActivity()).getRoleId();
        }

        bookList = new ArrayList<>();
        adapter = new BookAdapter(getContext(), bookList, roleId);
        
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        // Load dữ liệu
        loadBestSellerBook();
        loadBooks(null);

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

        return view;
    }

    public void filterByCategory(String categoryId) {
        if (edtSearch != null) edtSearch.setText("");
        loadBooks(categoryId);
    }

    private void loadBestSellerBook() {
        if (bestSellerListener != null) bestSellerListener.remove();
        bestSellerListener = db.collection("books")
                .orderBy("luotBan", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("HomeFragment", "Lỗi Sách bán chạy: " + error.getMessage());
                        // Nếu lỗi do thiếu Index (thường gặp khi orderBy), link tạo index sẽ hiện ở Logcat
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        layoutBestSeller.setVisibility(View.VISIBLE);
                        Book book = value.getDocuments().get(0).toObject(Book.class);
                        if (book != null) {
                            book.setId(value.getDocuments().get(0).getId());
                            txtBestSellerName.setText(book.getTenSach());
                            txtBestSellerSold.setText("Đã bán: " + book.getLuotBan());
                            txtBestSellerPrice.setText(String.format("%,.0f đ", book.getGiaBan()));

                            if (book.getHinhAnh() != null && !book.getHinhAnh().isEmpty()) {
                                Glide.with(this).load(book.getHinhAnh()).placeholder(R.mipmap.ic_launcher).into(imgBestSeller);
                            }

                            btnViewBestSeller.setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), DetailActivity.class);
                                intent.putExtra("book", book);
                                startActivity(intent);
                            });
                        }
                    } else {
                        layoutBestSeller.setVisibility(View.GONE);
                    }
                });
    }

    private void loadBooks(@Nullable String categoryId) {
        if (bookListener != null) bookListener.remove();

        Query query = db.collection("books");
        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereEqualTo("MaLoaiSach", categoryId);
        }

        bookListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("HomeFragment", "Lỗi Danh sách sách: " + error.getMessage());
                if (!isErrorShown) {
                    Toast.makeText(getContext(), "[Trang chủ] Lỗi tải sách. Kiểm tra Rules bảng 'books'!", Toast.LENGTH_LONG).show();
                    isErrorShown = true;
                }
                return;
            }
            if (value != null) {
                isErrorShown = false;
                bookList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Book book = doc.toObject(Book.class);
                    book.setId(doc.getId());
                    bookList.add(book);
                }
                adapter.updateList(bookList);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bookListener != null) bookListener.remove();
        if (bestSellerListener != null) bestSellerListener.remove();
    }
}
