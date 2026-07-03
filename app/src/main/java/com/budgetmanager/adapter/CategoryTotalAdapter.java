package com.budgetmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetmanager.R;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.database.entity.CategoryTotal;
import com.budgetmanager.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class CategoryTotalAdapter extends RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder> {

    private List<CategoryTotal> data = new ArrayList<>();

    public void setData(List<CategoryTotal> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_total, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryTotal item = data.get(position);
        holder.tvCategoryName.setText(item.getCategoryName());
        holder.tvTotal.setText(DateUtils.formatCurrency(item.getTotal()));

        boolean isRevenue = Category.TYPE_REVENUE.equals(item.getType());
        holder.tvTypeLabel.setText(isRevenue ? "Revenu" : "Dépense");
        holder.tvTotal.setTextColor(holder.itemView.getContext().getResources().getColor(
                isRevenue ? android.R.color.holo_green_light : android.R.color.holo_red_light));
        holder.tvTypeLabel.setTextColor(holder.itemView.getContext().getResources().getColor(
                isRevenue ? android.R.color.holo_green_light : android.R.color.holo_red_light));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvTotal, tvTypeLabel;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_cat_total_name);
            tvTotal = itemView.findViewById(R.id.tv_cat_total_amount);
            tvTypeLabel = itemView.findViewById(R.id.tv_cat_total_type);
        }
    }
}
