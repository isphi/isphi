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
import com.budgetmanager.database.entity.Article;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.utils.DateUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private List<Article> articles = new ArrayList<>();
    private final OnArticleClickListener listener;
    private final String categoryType;

    public interface OnArticleClickListener {
        void onArticleEdit(Article article);
        void onArticleDelete(Article article);
    }

    public ArticleAdapter(OnArticleClickListener listener, String categoryType) {
        this.listener = listener;
        this.categoryType = categoryType;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.tvTitle.setText(article.getTitle());
        holder.tvDescription.setText(article.getDescription());
        holder.tvAmount.setText(DateUtils.formatCurrency(article.getAmount()));
        holder.tvDate.setText(DateUtils.formatDate(article.getDate()));

        if (article.getImagePath() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(new File(article.getImagePath()))
                    .placeholder(R.drawable.ic_category_placeholder)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_category_placeholder);
        }

        if (Category.TYPE_REVENUE.equals(categoryType)) {
            holder.tvAmount.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_light));
        } else {
            holder.tvAmount.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_red_light));
        }

        holder.btnEdit.setOnClickListener(v -> listener.onArticleEdit(article));
        holder.btnDelete.setOnClickListener(v -> listener.onArticleDelete(article));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvAmount, tvDate;
        ImageView ivImage;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_article_image);
            tvTitle = itemView.findViewById(R.id.tv_article_title);
            tvDescription = itemView.findViewById(R.id.tv_article_description);
            tvAmount = itemView.findViewById(R.id.tv_article_amount);
            tvDate = itemView.findViewById(R.id.tv_article_date);
            btnEdit = itemView.findViewById(R.id.btn_edit_article);
            btnDelete = itemView.findViewById(R.id.btn_delete_article);
        }
    }
}
