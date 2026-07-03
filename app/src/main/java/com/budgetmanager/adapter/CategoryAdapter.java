package com.budgetmanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.budgetmanager.R;
import com.budgetmanager.database.entity.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
        void onCategoryEdit(Category category);
        void onCategoryDelete(Category category);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());
        holder.tvType.setText(category.getType().equals(Category.TYPE_REVENUE) ? "Revenu" : "Dépense");

        if (category.getImagePath() != null && !category.getImagePath().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(new File(category.getImagePath()))
                    .placeholder(R.drawable.ic_category_placeholder)
                    .circleCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_category_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
        holder.btnEdit.setOnClickListener(v -> listener.onCategoryEdit(category));
        holder.btnDelete.setOnClickListener(v -> listener.onCategoryDelete(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvType;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_category_image);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvType = itemView.findViewById(R.id.tv_category_type);
            btnEdit = itemView.findViewById(R.id.btn_edit_category);
            btnDelete = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
