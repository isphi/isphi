package com.budgetmanager.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetmanager.R;
import com.budgetmanager.ui.calendar.CalendarDay;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    private static final int COLOR_WEEKEND = Color.parseColor("#3A2E4D");
    private static final int COLOR_HOLIDAY = Color.parseColor("#FF6584");
    private static final int COLOR_NORMAL = Color.parseColor("#2A2A2A");

    private List<CalendarDay> days = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setDays(List<CalendarDay> days) {
        this.days = days;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        if (day.isEmpty()) {
            holder.tvDayNumber.setText("");
            holder.dot.setVisibility(View.GONE);
            holder.root.getBackground().mutate().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP);
            holder.root.setOnClickListener(null);
            holder.root.setClickable(false);
            return;
        }

        holder.tvDayNumber.setText(String.valueOf(day.getDay()));

        int bgColor = COLOR_NORMAL;
        if (day.isHoliday()) {
            bgColor = COLOR_HOLIDAY;
        } else if (day.isWeekend()) {
            bgColor = COLOR_WEEKEND;
        }
        holder.root.getBackground().mutate().setColorFilter(bgColor, PorterDuff.Mode.SRC_ATOP);

        holder.dot.setVisibility(day.hasTransactions() ? View.VISIBLE : View.GONE);

        holder.root.setClickable(true);
        holder.root.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView tvDayNumber;
        View dot;

        ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.day_root);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            dot = itemView.findViewById(R.id.dot_transaction);
        }
    }
}
