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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private ArrayList<Order> orderList;
    private FirebaseFirestore db;
    private String userPhone;
    private TextView txtEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rvOrderHistoryFragment);
        txtEmpty = view.findViewById(R.id.txtEmptyOrderHistory);

        SharedPreferences sp = getActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        userPhone = sp.getString("phone", "");

        orderList = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), orderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (!userPhone.isEmpty()) {
            loadOrderHistory();
        }

        return view;
    }

    private void loadOrderHistory() {
        db.collection("orders")
                .whereEqualTo("userId", userPhone)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            orderList.add(doc.toObject(Order.class));
                        }
                        adapter.notifyDataSetChanged();
                        txtEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }
}
