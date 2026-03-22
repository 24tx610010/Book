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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatisticFragment extends Fragment {

    private BarChart barChart;
    private TextView txtTotalRevenue, txtBestSeller;
    private RecyclerView rvSuccessOrders, rvCancelledOrders;
    private AdminOrderAdapter successAdapter, cancelledAdapter;
    private ArrayList<Order> successList, cancelledList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);

        db = FirebaseFirestore.getInstance();
        barChart = view.findViewById(R.id.barChart);
        txtTotalRevenue = view.findViewById(R.id.txtTotalRevenue);
        txtBestSeller = view.findViewById(R.id.txtBestSeller);
        
        rvSuccessOrders = view.findViewById(R.id.rvSuccessOrders);
        rvCancelledOrders = view.findViewById(R.id.rvCancelledOrders);

        setupRecyclerViews();
        setupChartStyle();
        loadStatistics();
        loadBestSeller();
        loadOrdersByStatus();

        return view;
    }

    private void setupRecyclerViews() {
        successList = new ArrayList<>();
        cancelledList = new ArrayList<>();

        successAdapter = new AdminOrderAdapter(getContext(), successList);
        cancelledAdapter = new AdminOrderAdapter(getContext(), cancelledList);

        rvSuccessOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuccessOrders.setAdapter(successAdapter);

        rvCancelledOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCancelledOrders.setAdapter(cancelledAdapter);
    }

    private void setupChartStyle() {
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.setMaxVisibleValueCount(60);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) return String.format("%.1fM", value / 1000000);
                if (value >= 1000) return String.format("%.0fK", value / 1000);
                return super.getFormattedValue(value);
            }
        });

        barChart.getAxisRight().setEnabled(false);
    }

    private void loadStatistics() {
        db.collection("orders")
                .whereEqualTo("status", 1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRevenue = 0;
                    // Dùng TreeMap để tự động sắp xếp theo ngày
                    Map<String, Double> dailyRevenue = new TreeMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        totalRevenue += order.getTotalAmount();

                        if (order.getOrderDate() != null) {
                            String dateKey = sdf.format(order.getOrderDate());
                            dailyRevenue.put(dateKey, dailyRevenue.getOrDefault(dateKey, 0.0) + order.getTotalAmount());
                        }
                    }

                    txtTotalRevenue.setText(String.format("%,.0f đ", totalRevenue));
                    updateChart(dailyRevenue);
                });
    }

    private void updateChart(Map<String, Double> dailyRevenue) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, Double> entry : dailyRevenue.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue().floatValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        // Màu gradient xanh dương cho hiện đại
        dataSet.setColor(Color.parseColor("#42A5F5"));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                if (value >= 1000) return String.format("%.0fK", value/1000);
                return String.format("%.0f", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        barChart.setData(barData);
        barChart.animateY(1200);
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
                        txtBestSeller.setText(book.getTenSach() + " (" + book.getLuotBan() + " lượt bán)");
                    }
                });
    }

    private void loadOrdersByStatus() {
        db.collection("orders")
                .whereEqualTo("status", 1)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        successList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            successList.add(doc.toObject(Order.class));
                        }
                        successAdapter.notifyDataSetChanged();
                    }
                });

        db.collection("orders")
                .whereEqualTo("status", 2)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        cancelledList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            cancelledList.add(doc.toObject(Order.class));
                        }
                        cancelledAdapter.notifyDataSetChanged();
                    }
                });
    }
}
