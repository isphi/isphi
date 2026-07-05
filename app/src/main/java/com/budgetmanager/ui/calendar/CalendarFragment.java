package com.budgetmanager.ui.calendar;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetmanager.R;
import com.budgetmanager.adapter.CalendarAdapter;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.database.entity.DailyTotal;
import com.budgetmanager.utils.DateUtils;
import com.budgetmanager.utils.HolidayUtils;
import com.budgetmanager.utils.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnDayClickListener {

    private TextView tvMonthLabel, tvDayDetail;
    private LinearLayout llHolidays;
    private RecyclerView rvCalendar;
    private CalendarAdapter adapter;
    private BudgetDatabase database;
    private SessionManager sessionManager;

    private Calendar currentCalendar;
    private List<CalendarDay> days = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = BudgetDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());
        currentCalendar = Calendar.getInstance();

        tvMonthLabel = view.findViewById(R.id.tv_month_label);
        tvDayDetail = view.findViewById(R.id.tv_day_detail);
        llHolidays = view.findViewById(R.id.ll_holidays);
        rvCalendar = view.findViewById(R.id.rv_calendar);

        adapter = new CalendarAdapter(this);
        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        rvCalendar.setAdapter(adapter);

        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            buildMonth();
        });
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            buildMonth();
        });

        buildMonth();
    }

    private void buildMonth() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);

        tvMonthLabel.setText(DateUtils.formatMonth(currentCalendar.getTimeInMillis()));
        tvDayDetail.setText("Sélectionnez un jour");

        Map<Integer, String> holidays = HolidayUtils.getHolidaysForMonth(year, month);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, 1);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Décalage pour une grille commençant le lundi.
        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDow - Calendar.MONDAY + 7) % 7;

        days = new ArrayList<>();
        for (int i = 0; i < offset; i++) {
            days.add(new CalendarDay(0, false, false, null));
        }
        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            boolean weekend = dow == Calendar.SATURDAY || dow == Calendar.SUNDAY;
            String holidayName = holidays.get(d);
            days.add(new CalendarDay(d, weekend, holidayName != null, holidayName));
        }

        adapter.setDays(days);
        renderHolidayList(holidays);
        observeTransactions(year, month);
    }

    private void observeTransactions(int year, int month) {
        long userId = sessionManager.getUserId();
        long start = DateUtils.getStartOfMonth(currentCalendar.getTimeInMillis());
        long end = DateUtils.getEndOfMonth(currentCalendar.getTimeInMillis());

        database.articleDao().getDailyTotals(userId, start, end)
                .observe(getViewLifecycleOwner(), totals -> {
                    for (CalendarDay day : days) {
                        day.setRevenue(0);
                        day.setExpense(0);
                    }
                    if (totals != null) {
                        for (DailyTotal t : totals) {
                            CalendarDay day = findDay(t.getDay());
                            if (day == null) continue;
                            if (Category.TYPE_REVENUE.equals(t.getType())) {
                                day.setRevenue(t.getTotal());
                            } else {
                                day.setExpense(t.getTotal());
                            }
                        }
                    }
                    adapter.setDays(days);
                });
    }

    @Nullable
    private CalendarDay findDay(int dayOfMonth) {
        for (CalendarDay day : days) {
            if (day.getDay() == dayOfMonth) return day;
        }
        return null;
    }

    private void renderHolidayList(Map<Integer, String> holidays) {
        llHolidays.removeAllViews();
        if (holidays.isEmpty()) {
            TextView tv = new TextView(requireContext());
            tv.setText("Aucun jour férié ce mois-ci");
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tv.setPadding(0, 4, 0, 4);
            llHolidays.addView(tv);
            return;
        }
        // Trié par jour.
        TreeMap<Integer, String> sorted = new TreeMap<>(holidays);
        for (Map.Entry<Integer, String> entry : sorted.entrySet()) {
            TextView tv = new TextView(requireContext());
            tv.setText(String.format("%02d — %s", entry.getKey(), entry.getValue()));
            tv.setTextColor(getResources().getColor(android.R.color.white));
            tv.setTextSize(14f);
            tv.setPadding(0, 8, 0, 8);
            llHolidays.addView(tv);
        }
    }

    @Override
    public void onDayClick(CalendarDay day) {
        StringBuilder sb = new StringBuilder();
        sb.append(day.getDay()).append(" ")
                .append(DateUtils.formatMonth(currentCalendar.getTimeInMillis()));
        if (day.isHoliday()) {
            sb.append("  •  ").append(day.getHolidayName());
        } else if (day.isWeekend()) {
            sb.append("  •  Week-end");
        }
        sb.append("\n\nRevenus : ").append(DateUtils.formatCurrency(day.getRevenue()));
        sb.append("\nDépenses : ").append(DateUtils.formatCurrency(day.getExpense()));
        sb.append("\nSolde : ").append(DateUtils.formatCurrency(day.getRevenue() - day.getExpense()));
        tvDayDetail.setText(sb.toString());
        tvDayDetail.setGravity(Gravity.START);
    }
}
