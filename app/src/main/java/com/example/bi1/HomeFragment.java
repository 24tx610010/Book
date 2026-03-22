package com.example.bi1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private ArrayList<Book> bookList;
    private FirebaseFirestore db;
    private EditText edtSearch;
    private int roleId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rvHomeBooks);
        edtSearch = view.findViewById(R.id.edtSearchHome);

        if (getActivity() instanceof HomeActivity) {
            roleId = ((HomeActivity) getActivity()).getRoleId();
        }

        bookList = new ArrayList<>();
        adapter = new BookAdapter(getContext(), bookList, roleId);
        
        // SỬA THÀNH GRIDLAYOUTMANAGER VỚI 2 CỘT
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        loadAllBooks();

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

    private void loadAllBooks() {
        db.collection("books").addSnapshotListener((value, error) -> {
            if (value != null) {
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
    
    public void filterByCategory(String categoryId) {
        db.collection("books").whereEqualTo("MaLoaiSach", categoryId).get().addOnSuccessListener(value -> {
            bookList.clear();
            for (QueryDocumentSnapshot doc : value) {
                Book book = doc.toObject(Book.class);
                book.setId(doc.getId());
                bookList.add(book);
            }
            adapter.updateList(bookList);
        });
    }
}
