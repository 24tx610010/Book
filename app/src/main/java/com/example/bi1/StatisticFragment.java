package com.example.bi1;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticFragment extends Fragment {

    private BarChart barChart;
    private TextView txtTotalRevenue, txtBestSeller;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);

        db = FirebaseFirestore.getInstance();
        barChart = view.findViewById(R.id.barChart);
        txtTotalRevenue = view.findViewById(R.id.txtTotalRevenue);
        txtBestSeller = view.findViewById(R.id.txtBestSeller);

        loadStatistics();
        loadBestSeller();

        return view;
    }

    private void loadStatistics() {
        db.collection("orders")
                .whereEqualTo("status", 1) // Chỉ tính đơn đã duyệt
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRevenue = 0;
                    Map<String, Double> dailyRevenue = new HashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        totalRevenue += order.getTotalAmount();

                        String dateKey = sdf.format(order.getOrderDate());
                        dailyRevenue.put(dateKey, dailyRevenue.getOrDefault(dateKey, 0.0) + order.getTotalAmount());
                    }

                    txtTotalRevenue.setText(String.format("%,.0f đ", totalRevenue));
                    updateChart(dailyRevenue);
                });
    }

    private void updateChart(Map<String, Double> dailyRevenue) {
        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (Double value : dailyRevenue.values()) {
            entries.add(new BarEntry(i++, value.floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu theo ngày");
        dataSet.setColor(Color.parseColor("#9575CD")); // Màu tím chủ đạo
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate(); 
    }

    private void loadBestSeller() {
        db.collection("books")
                .orderBy("luotBan", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Book book = queryDocumentSnapshots.getDocuments().get(0).toObject(Book.class);
                        txtBestSeller.setText(book.getTenSach() + " (" + book.getLuotBan() + " cuốn)");
                    }
                });
    }
}
