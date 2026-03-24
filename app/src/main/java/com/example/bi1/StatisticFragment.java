package com.example.bi1;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private TextView txtTotalRevenue, lblRevenueTitle;
    private Spinner spnFilterTime;
    private RecyclerView rvSuccessOrders, rvBestSellers;
    private AdminOrderAdapter successAdapter;
    private BestSellerAdapter bestSellerAdapter;
    private ArrayList<Order> successList;
    private ArrayList<BestSellerItem> bestSellerList;
    private FirebaseFirestore db;
    private boolean isErrorShown = false;

    private final String[] filterOptions = {"Hôm nay", "7 ngày qua", "Tháng này", "Quý này", "Tất cả thời gian"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);

        db = FirebaseFirestore.getInstance();
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        txtTotalRevenue = view.findViewById(R.id.txtTotalRevenue);
        lblRevenueTitle = view.findViewById(R.id.lblRevenueTitle);
        spnFilterTime = view.findViewById(R.id.spnFilterTime);
        rvSuccessOrders = view.findViewById(R.id.rvSuccessOrders);
        rvBestSellers = view.findViewById(R.id.rvBestSellers);

        setupSpinner();
        setupRecyclerViews();
        setupChartStyle();
        setupPieChartStyle();

        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnFilterTime.setAdapter(adapter);

        spnFilterTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStatistics(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerViews() {
        successList = new ArrayList<>();
        successAdapter = new AdminOrderAdapter(getContext(), successList);
        rvSuccessOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSuccessOrders.setAdapter(successAdapter);

        bestSellerList = new ArrayList<>();
        bestSellerAdapter = new BestSellerAdapter(getContext(), bestSellerList);
        rvBestSellers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBestSellers.setAdapter(bestSellerAdapter);
    }

    private void setupChartStyle() {
        barChart.setNoDataText("Đang tải dữ liệu...");
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setExtraOffsets(5f, 10f, 5f, 15f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setTextColor(Color.DKGRAY);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000) return String.format(Locale.getDefault(), "%.0fK", value / 1000);
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });
        barChart.getAxisRight().setEnabled(false);
    }

    private void setupPieChartStyle() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
    }

    private void loadStatistics(int filterType) {
        final Date startDate;
        
        lblRevenueTitle.setText(String.format("Doanh thu %s", filterOptions[filterType].toLowerCase()));

        Map<String, Double> chartData = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar tempCal = Calendar.getInstance();

        switch (filterType) {
            case 0: // Hôm nay
                tempCal.set(Calendar.HOUR_OF_DAY, 0); tempCal.set(Calendar.MINUTE, 0); tempCal.set(Calendar.SECOND, 0);
                startDate = tempCal.getTime();
                chartData.put(sdf.format(new Date()), 0.0);
                break;
            case 1: // 7 ngày qua
                tempCal.add(Calendar.DAY_OF_YEAR, -6);
                tempCal.set(Calendar.HOUR_OF_DAY, 0); tempCal.set(Calendar.MINUTE, 0); tempCal.set(Calendar.SECOND, 0);
                startDate = tempCal.getTime();
                for (int i = 0; i < 7; i++) {
                    chartData.put(sdf.format(tempCal.getTime()), 0.0);
                    tempCal.add(Calendar.DAY_OF_YEAR, 1);
                }
                break;
            case 2: // Tháng này
                tempCal.set(Calendar.DAY_OF_MONTH, 1);
                tempCal.set(Calendar.HOUR_OF_DAY, 0); tempCal.set(Calendar.MINUTE, 0); tempCal.set(Calendar.SECOND, 0);
                startDate = tempCal.getTime();
                Calendar now = Calendar.getInstance();
                while (!tempCal.after(now)) {
                    chartData.put(sdf.format(tempCal.getTime()), 0.0);
                    tempCal.add(Calendar.DAY_OF_YEAR, 1);
                }
                break;
            case 3: // Quý này
                int currentMonth = tempCal.get(Calendar.MONTH);
                tempCal.set(Calendar.MONTH, (currentMonth / 3) * 3);
                tempCal.set(Calendar.DAY_OF_MONTH, 1);
                tempCal.set(Calendar.HOUR_OF_DAY, 0); tempCal.set(Calendar.MINUTE, 0); tempCal.set(Calendar.SECOND, 0);
                startDate = tempCal.getTime();
                Calendar nowQ = Calendar.getInstance();
                while (!tempCal.after(nowQ)) {
                    chartData.put(sdf.format(tempCal.getTime()), 0.0);
                    tempCal.add(Calendar.DAY_OF_YEAR, 1);
                }
                break;
            default: // Tất cả
                startDate = new Date(0);
                break;
        }

        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    isErrorShown = false;
                    double totalRevenue = 0;
                    Map<Integer, Integer> statusCount = new HashMap<>();
                    statusCount.put(0, 0); 
                    statusCount.put(1, 0); 
                    statusCount.put(2, 0); 

                    List<String> orderIds = new ArrayList<>();
                    successList.clear();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        Date date = order.getOrderDate();
                        
                        if (date != null && date.after(startDate)) {
                            int status = order.getStatus();
                            Integer currentCount = statusCount.get(status);
                            statusCount.put(status, (currentCount == null ? 0 : currentCount) + 1);

                            if (status == 1) {
                                successList.add(order);
                                orderIds.add(order.getId());
                                totalRevenue += order.getTotalAmount();

                                String dateKey = sdf.format(date);
                                if (filterType == 4) { 
                                    Double currentVal = chartData.get(dateKey);
                                    chartData.put(dateKey, (currentVal == null ? 0.0 : currentVal) + order.getTotalAmount());
                                } else if (chartData.containsKey(dateKey)) {
                                    Double currentVal = chartData.get(dateKey);
                                    chartData.put(dateKey, (currentVal == null ? 0.0 : currentVal) + order.getTotalAmount());
                                }
                            }
                        }
                    }

                    txtTotalRevenue.setText(String.format(Locale.getDefault(), "%,.0f đ", totalRevenue));
                    successAdapter.notifyDataSetChanged();
                    updateChart(chartData);
                    updatePieChart(statusCount);
                    calculateBestSellers(orderIds);
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null && !isErrorShown) {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu thống kê. Vui lòng kiểm tra Rules trên Firebase!", Toast.LENGTH_LONG).show();
                        isErrorShown = true;
                    }
                });
    }

    private void updatePieChart(Map<Integer, Integer> statusCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        
        Integer completed = statusCount.get(1);
        if (completed != null && completed > 0) entries.add(new PieEntry(completed, "Hoàn tất"));
        
        Integer pending = statusCount.get(0);
        if (pending != null && pending > 0) entries.add(new PieEntry(pending, "Tiếp nhận"));
        
        Integer cancelled = statusCount.get(2);
        if (cancelled != null && cancelled > 0) entries.add(new PieEntry(cancelled, "Huỷ bỏ"));

        if (entries.isEmpty()) {
            pieChart.clear();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#64B5F6"));
        colors.add(Color.parseColor("#90FE80"));
        colors.add(Color.parseColor("#FFB74D"));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        
        pieChart.setData(data);
        pieChart.animateY(1400, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    private void calculateBestSellers(List<String> orderIds) {
        if (orderIds.isEmpty()) {
            bestSellerList.clear();
            bestSellerAdapter.notifyDataSetChanged();
            return;
        }

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (int i = 0; i < orderIds.size(); i += 10) {
            int end = Math.min(i + 10, orderIds.size());
            List<String> subList = orderIds.subList(i, end);
            tasks.add(db.collection("order_details").whereIn("orderId", subList).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            Map<String, BestSellerItem> salesCount = new HashMap<>();
            for (Object result : results) {
                QuerySnapshot snapshots = (QuerySnapshot) result;
                for (QueryDocumentSnapshot doc : snapshots) {
                    OrderDetail detail = doc.toObject(OrderDetail.class);
                    String bookId = detail.getBookId();
                    if (bookId != null) {
                        if (salesCount.containsKey(bookId)) {
                            BestSellerItem item = salesCount.get(bookId);
                            if (item != null) {
                                item.setCount(item.getCount() + detail.getQuantity());
                            }
                        } else {
                            salesCount.put(bookId, new BestSellerItem(detail.getBookName(), detail.getQuantity()));
                        }
                    }
                }
            }

            List<BestSellerItem> sortedList = new ArrayList<>(salesCount.values());
            Collections.sort(sortedList, (o1, o2) -> Integer.compare(o2.getCount(), o1.getCount()));

            bestSellerList.clear();
            for (int i = 0; i < Math.min(5, sortedList.size()); i++) {
                bestSellerList.add(sortedList.get(i));
            }
            bestSellerAdapter.notifyDataSetChanged();
        });
    }

    private void updateChart(Map<String, Double> data) {
        if (data.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("Không có dữ liệu doanh thu");
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue().floatValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.parseColor("#E53935"));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.format(Locale.getDefault(), "%.0fK", value / 1000) : "";
            }
        });

        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < labels.size()) ? labels.get(index) : "";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        barChart.setData(barData);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    public static class BestSellerItem {
        private final String name;
        private int count;

        public BestSellerItem(String name, int count) {
            this.name = name;
            this.count = count;
        }

        public String getName() { return name; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    private static class BestSellerAdapter extends RecyclerView.Adapter<BestSellerAdapter.ViewHolder> {
        private final Context context;
        private final List<BestSellerItem> list;

        public BestSellerAdapter(Context context, List<BestSellerItem> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BestSellerItem item = list.get(position);
            holder.text1.setText(String.format(Locale.getDefault(), "%d. %s", position + 1, item.getName()));
            holder.text1.setTextColor(Color.parseColor("#E53935"));
            holder.text1.setTextSize(16);
            holder.text2.setText(String.format(Locale.getDefault(), "Đã bán: %d cuốn", item.getCount()));
            holder.text2.setPadding(30, 0, 0, 0);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
