package com.budgetmanager.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetmanager.R;
import com.budgetmanager.adapter.CategoryTotalAdapter;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.database.entity.CategoryTotal;
import com.budgetmanager.utils.DateUtils;
import com.budgetmanager.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DashboardFragment extends Fragment {

    private static final int SORT_AMOUNT_DESC = 0;
    private static final int SORT_AMOUNT_ASC = 1;
    private static final int SORT_NAME = 2;

    private TextView tvTotalRevenue, tvTotalExpense, tvBalance;
    private PieChart pieChartExpense, pieChartRevenue;
    private BarChart barChartSummary;
    private Spinner spinnerSort;
    private RecyclerView rvCategoryTotals;
    private CategoryTotalAdapter adapter;
    private BudgetDatabase database;
    private SessionManager sessionManager;

    private double totalRevenue = 0;
    private double totalExpense = 0;
    private int sortMode = SORT_AMOUNT_DESC;
    private List<CategoryTotal> categoryTotals = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = BudgetDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());
        long userId = sessionManager.getUserId();

        tvTotalRevenue = view.findViewById(R.id.tv_total_revenue);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        pieChartExpense = view.findViewById(R.id.pie_chart_expense);
        pieChartRevenue = view.findViewById(R.id.pie_chart_revenue);
        barChartSummary = view.findViewById(R.id.bar_chart_summary);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        rvCategoryTotals = view.findViewById(R.id.rv_category_totals);

        adapter = new CategoryTotalAdapter();
        rvCategoryTotals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategoryTotals.setAdapter(adapter);

        setupSortSpinner();
        setupPieChart(pieChartExpense, "Dépenses");
        setupPieChart(pieChartRevenue, "Revenus");
        setupBarChart();

        observeData(userId);
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Montant ↓", "Montant ↑", "Nom"});
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortMode = position;
                applySortAndDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupPieChart(PieChart chart, String label) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setCenterText(label);
        chart.setCenterTextSize(14f);
        chart.setCenterTextColor(Color.WHITE);
        chart.setHoleRadius(45f);
        chart.setTransparentCircleRadius(50f);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setNoDataText("Aucune donnée");
        chart.setNoDataTextColor(Color.WHITE);
    }

    private void setupBarChart() {
        barChartSummary.getDescription().setEnabled(false);
        barChartSummary.setDrawGridBackground(false);
        barChartSummary.setFitBars(true);
        barChartSummary.getLegend().setEnabled(false);
        barChartSummary.setNoDataText("Aucune donnée");
        barChartSummary.setNoDataTextColor(Color.WHITE);

        XAxis xAxis = barChartSummary.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Revenus", "Dépenses", "Solde"}));

        barChartSummary.getAxisLeft().setTextColor(Color.WHITE);
        barChartSummary.getAxisRight().setEnabled(false);
    }

    private void observeData(long userId) {
        database.articleDao().getTotalByType(userId, Category.TYPE_REVENUE).observe(
                getViewLifecycleOwner(), revenue -> {
                    totalRevenue = revenue != null ? revenue : 0;
                    tvTotalRevenue.setText(DateUtils.formatCurrency(totalRevenue));
                    updateBalance();
                });

        database.articleDao().getTotalByType(userId, Category.TYPE_EXPENSE).observe(
                getViewLifecycleOwner(), expense -> {
                    totalExpense = expense != null ? expense : 0;
                    tvTotalExpense.setText(DateUtils.formatCurrency(totalExpense));
                    updateBalance();
                });

        database.categoryDao().getCategoryTotals(userId).observe(
                getViewLifecycleOwner(), totals -> {
                    categoryTotals = totals != null ? totals : new ArrayList<>();
                    applySortAndDisplay();
                    updatePieCharts();
                });
    }

    private void updateBalance() {
        double balance = totalRevenue - totalExpense;
        tvBalance.setText(DateUtils.formatCurrency(balance));
        tvBalance.setTextColor(balance >= 0 ?
                Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
        updateBarChart();
    }

    private void updateBarChart() {
        double balance = totalRevenue - totalExpense;
        if (totalRevenue == 0 && totalExpense == 0) {
            barChartSummary.clear();
            barChartSummary.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) totalRevenue));
        entries.add(new BarEntry(1, (float) totalExpense));
        entries.add(new BarEntry(2, (float) balance));

        BarDataSet dataSet = new BarDataSet(entries, "Résumé");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#F44336"),
                balance >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        barChartSummary.setData(data);
        barChartSummary.setFitBars(true);
        barChartSummary.invalidate();
    }

    private void applySortAndDisplay() {
        List<CategoryTotal> sorted = new ArrayList<>(categoryTotals);
        switch (sortMode) {
            case SORT_AMOUNT_ASC:
                Collections.sort(sorted, Comparator.comparingDouble(CategoryTotal::getTotal));
                break;
            case SORT_NAME:
                Collections.sort(sorted, (a, b) -> {
                    String nameA = a.getCategoryName() != null ? a.getCategoryName() : "";
                    String nameB = b.getCategoryName() != null ? b.getCategoryName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                });
                break;
            case SORT_AMOUNT_DESC:
            default:
                Collections.sort(sorted,
                        (a, b) -> Double.compare(b.getTotal(), a.getTotal()));
                break;
        }
        adapter.setData(sorted);
    }

    private void updatePieCharts() {
        List<PieEntry> expenseEntries = new ArrayList<>();
        List<PieEntry> revenueEntries = new ArrayList<>();

        for (CategoryTotal ct : categoryTotals) {
            if (ct.getTotal() > 0) {
                if (Category.TYPE_EXPENSE.equals(ct.getType())) {
                    expenseEntries.add(new PieEntry((float) ct.getTotal(), ct.getCategoryName()));
                } else {
                    revenueEntries.add(new PieEntry((float) ct.getTotal(), ct.getCategoryName()));
                }
            }
        }

        updatePieChart(pieChartExpense, expenseEntries, "Dépenses");
        updatePieChart(pieChartRevenue, revenueEntries, "Revenus");
    }

    private void updatePieChart(PieChart chart, List<PieEntry> entries, String label) {
        if (entries.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, label);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }
}
