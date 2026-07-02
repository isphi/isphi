package com.budgetmanager.ui.article;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetmanager.R;
import com.budgetmanager.adapter.ArticleAdapter;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.Article;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.Executors;

public class ArticleListActivity extends AppCompatActivity implements ArticleAdapter.OnArticleClickListener {

    private RecyclerView rvArticles;
    private ArticleAdapter adapter;
    private BudgetDatabase database;
    private long categoryId;
    private String categoryName;
    private String categoryType;
    private int currentSortOrder = 0; // 0=date, 1=amount, 2=title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        database = BudgetDatabase.getInstance(this);

        categoryId = getIntent().getLongExtra("category_id", -1);
        categoryName = getIntent().getStringExtra("category_name");
        categoryType = getIntent().getStringExtra("category_type");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvArticles = findViewById(R.id.rv_articles);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ArticleAdapter(this, categoryType);
        rvArticles.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_article);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditArticleActivity.class);
            intent.putExtra("category_id", categoryId);
            intent.putExtra("category_type", categoryType);
            startActivity(intent);
        });

        loadArticles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadArticles();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_sort_date) {
            currentSortOrder = 0;
            loadArticles();
            return true;
        } else if (itemId == R.id.action_sort_amount) {
            currentSortOrder = 1;
            loadArticles();
            return true;
        } else if (itemId == R.id.action_sort_title) {
            currentSortOrder = 2;
            loadArticles();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadArticles() {
        LiveData<List<Article>> articlesLiveData;

        switch (currentSortOrder) {
            case 1:
                articlesLiveData = database.articleDao().getByCategoryOrderByAmount(categoryId);
                break;
            case 2:
                articlesLiveData = database.articleDao().getByCategoryOrderByTitle(categoryId);
                break;
            default:
                articlesLiveData = database.articleDao().getByCategoryOrderByDate(categoryId);
                break;
        }

        articlesLiveData.observe(this, articles -> adapter.setArticles(articles));
    }

    @Override
    public void onArticleEdit(Article article) {
        Intent intent = new Intent(this, AddEditArticleActivity.class);
        intent.putExtra("article_id", article.getId());
        intent.putExtra("category_id", categoryId);
        intent.putExtra("category_type", categoryType);
        startActivity(intent);
    }

    @Override
    public void onArticleDelete(Article article) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer l'article")
                .setMessage("Voulez-vous vraiment supprimer \"" + article.getTitle() + "\" ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() ->
                            database.articleDao().delete(article));
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
