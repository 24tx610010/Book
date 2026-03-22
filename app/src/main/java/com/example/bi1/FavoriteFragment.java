package com.example.bi1;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private ArrayList<Book> favoriteBooks;
    private FirebaseFirestore db;
    private String userPhone;
    private TextView txtEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rvFavoriteBooks);
        txtEmpty = view.findViewById(R.id.txtEmptyFavorites);

        SharedPreferences sp = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        userPhone = sp.getString("phone", "");
        int roleId = sp.getInt("roleid", 2);

        favoriteBooks = new ArrayList<>();
        adapter = new BookAdapter(getContext(), favoriteBooks, roleId);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

        if (!userPhone.isEmpty()) {
            loadFavorites();
        }

        return view;
    }

    private void loadFavorites() {
        db.collection("favorites").document(userPhone).collection("items")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        Set<String> favoriteIds = new HashSet<>();
                        for (QueryDocumentSnapshot doc : value) {
                            favoriteIds.add(doc.getId());
                        }
                        
                        if (favoriteIds.isEmpty()) {
                            favoriteBooks.clear();
                            adapter.notifyDataSetChanged();
                            if (txtEmpty != null) txtEmpty.setVisibility(View.VISIBLE);
                        } else {
                            if (txtEmpty != null) txtEmpty.setVisibility(View.GONE);
                            fetchBookDetails(favoriteIds);
                        }
                    }
                });
    }

    private void fetchBookDetails(Set<String> favoriteIds) {
        db.collection("books").addSnapshotListener((value, error) -> {
            if (value != null) {
                favoriteBooks.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Book book = doc.toObject(Book.class);
                    book.setId(doc.getId());
                    if (favoriteIds.contains(book.getId())) {
                        favoriteBooks.add(book);
                    }
                }
                adapter.notifyDataSetChanged();
                if (txtEmpty != null) txtEmpty.setVisibility(favoriteBooks.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
}
