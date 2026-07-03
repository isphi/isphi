package com.budgetmanager.ui.dashboard;

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

import com.budgetmanager.R;
import com.budgetmanager.adapter.CategoryTotalAdapter;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.database.entity.CategoryTotal;
import com.budgetmanager.utils.DateUtils;
import com.budgetmanager.utils.SessionManager;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private TextView tvTotalRevenue, tvTotalExpense, tvBalance;
    private PieChart pieChartExpense, pieChartRevenue;
    private RecyclerView rvCategoryTotals;
    private CategoryTotalAdapter adapter;
    private BudgetDatabase database;
    private SessionManager sessionManager;

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
        rvCategoryTotals = view.findViewById(R.id.rv_category_totals);

        adapter = new CategoryTotalAdapter();
        rvCategoryTotals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategoryTotals.setAdapter(adapter);

        setupPieChart(pieChartExpense, "Dépenses");
        setupPieChart(pieChartRevenue, "Revenus");

        observeData(userId);
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

    private void observeData(long userId) {
        database.articleDao().getTotalByType(userId, Category.TYPE_REVENUE).observe(
                getViewLifecycleOwner(), revenue -> {
                    double rev = revenue != null ? revenue : 0;
                    tvTotalRevenue.setText(DateUtils.formatCurrency(rev));
                    updateBalance();
                });

        database.articleDao().getTotalByType(userId, Category.TYPE_EXPENSE).observe(
                getViewLifecycleOwner(), expense -> {
                    double exp = expense != null ? expense : 0;
                    tvTotalExpense.setText(DateUtils.formatCurrency(exp));
                    updateBalance();
                });

        database.categoryDao().getCategoryTotals(userId).observe(
                getViewLifecycleOwner(), totals -> {
                    adapter.setData(totals);
                    updateCharts(totals);
                });
    }

    private void updateBalance() {
        String revStr = tvTotalRevenue.getText().toString()
                .replaceAll("[^\\d,.]", "").replace(",", ".");
        String expStr = tvTotalExpense.getText().toString()
                .replaceAll("[^\\d,.]", "").replace(",", ".");

        try {
            // Parse removing thousands separator (space in French format)
            revStr = revStr.replace("\u00A0", "").replace(" ", "");
            expStr = expStr.replace("\u00A0", "").replace(" ", "");
            double revenue = revStr.isEmpty() ? 0 : Double.parseDouble(revStr);
            double expense = expStr.isEmpty() ? 0 : Double.parseDouble(expStr);
            double balance = revenue - expense;
            tvBalance.setText(DateUtils.formatCurrency(balance));
            tvBalance.setTextColor(balance >= 0 ?
                    Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));
        } catch (NumberFormatException e) {
            tvBalance.setText(DateUtils.formatCurrency(0));
        }
    }

    private void updateCharts(List<CategoryTotal> totals) {
        List<PieEntry> expenseEntries = new ArrayList<>();
        List<PieEntry> revenueEntries = new ArrayList<>();

        for (CategoryTotal ct : totals) {
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
