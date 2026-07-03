package com.budgetmanager.ui.budget;

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
import com.budgetmanager.utils.DateUtils;
import com.budgetmanager.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;

public class BudgetFragment extends Fragment {

    private TextView tvPeriodLabel, tvRevenue, tvExpense, tvBalance;
    private RecyclerView rvCategoryTotals;
    private CategoryTotalAdapter adapter;
    private BudgetDatabase database;
    private SessionManager sessionManager;

    private boolean isMonthlyView = true;
    private Calendar currentCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = BudgetDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());
        currentCalendar = Calendar.getInstance();

        tvPeriodLabel = view.findViewById(R.id.tv_period_label);
        tvRevenue = view.findViewById(R.id.tv_revenue);
        tvExpense = view.findViewById(R.id.tv_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        rvCategoryTotals = view.findViewById(R.id.rv_category_totals);

        adapter = new CategoryTotalAdapter();
        rvCategoryTotals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategoryTotals.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isMonthlyView = tab.getPosition() == 0;
                loadData();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        view.findViewById(R.id.btn_previous).setOnClickListener(v -> navigatePrevious());
        view.findViewById(R.id.btn_next).setOnClickListener(v -> navigateNext());

        loadData();
    }

    private void navigatePrevious() {
        if (isMonthlyView) {
            currentCalendar.add(Calendar.MONTH, -1);
        } else {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        loadData();
    }

    private void navigateNext() {
        if (isMonthlyView) {
            currentCalendar.add(Calendar.MONTH, 1);
        } else {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        loadData();
    }

    private void loadData() {
        long userId = sessionManager.getUserId();
        long currentTime = currentCalendar.getTimeInMillis();
        long startDate, endDate;

        if (isMonthlyView) {
            startDate = DateUtils.getStartOfMonth(currentTime);
            endDate = DateUtils.getEndOfMonth(currentTime);
            tvPeriodLabel.setText(DateUtils.formatMonth(currentTime));
        } else {
            startDate = DateUtils.getStartOfDay(currentTime);
            endDate = DateUtils.getEndOfDay(currentTime);
            tvPeriodLabel.setText(DateUtils.formatDate(currentTime));
        }

        database.articleDao().getTotalByTypeAndDateRange(userId, Category.TYPE_REVENUE,
                startDate, endDate).observe(getViewLifecycleOwner(), revenue -> {
            double rev = revenue != null ? revenue : 0;
            tvRevenue.setText(DateUtils.formatCurrency(rev));
            updateBalance();
        });

        database.articleDao().getTotalByTypeAndDateRange(userId, Category.TYPE_EXPENSE,
                startDate, endDate).observe(getViewLifecycleOwner(), expense -> {
            double exp = expense != null ? expense : 0;
            tvExpense.setText(DateUtils.formatCurrency(exp));
            updateBalance();
        });

        database.categoryDao().getCategoryTotalsByDateRange(userId, startDate, endDate)
                .observe(getViewLifecycleOwner(), totals -> adapter.setData(totals));
    }

    private void updateBalance() {
        String revStr = tvRevenue.getText().toString()
                .replaceAll("[^\\d,.]", "").replace(",", ".");
        String expStr = tvExpense.getText().toString()
                .replaceAll("[^\\d,.]", "").replace(",", ".");

        try {
            revStr = revStr.replace("\u00A0", "").replace(" ", "");
            expStr = expStr.replace("\u00A0", "").replace(" ", "");
            double revenue = revStr.isEmpty() ? 0 : Double.parseDouble(revStr);
            double expense = expStr.isEmpty() ? 0 : Double.parseDouble(expStr);
            double balance = revenue - expense;
            tvBalance.setText(DateUtils.formatCurrency(balance));
            tvBalance.setTextColor(balance >= 0 ?
                    getResources().getColor(android.R.color.holo_green_light) :
                    getResources().getColor(android.R.color.holo_red_light));
        } catch (NumberFormatException e) {
            tvBalance.setText(DateUtils.formatCurrency(0));
        }
    }
}
