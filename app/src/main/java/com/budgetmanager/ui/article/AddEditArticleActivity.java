package com.budgetmanager.ui.article;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetmanager.R;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.Article;
import com.budgetmanager.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.concurrent.Executors;

public class AddEditArticleActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etAmount, etDate;
    private BudgetDatabase database;
    private long categoryId;
    private long articleId = -1;
    private long selectedDate;
    private Calendar dateCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_article);

        database = BudgetDatabase.getInstance(this);

        etTitle = findViewById(R.id.et_article_title);
        etDescription = findViewById(R.id.et_article_description);
        etAmount = findViewById(R.id.et_article_amount);
        etDate = findViewById(R.id.et_article_date);
        MaterialButton btnSave = findViewById(R.id.btn_save_article);

        categoryId = getIntent().getLongExtra("category_id", -1);
        articleId = getIntent().getLongExtra("article_id", -1);

        dateCalendar = Calendar.getInstance();
        selectedDate = dateCalendar.getTimeInMillis();
        etDate.setText(DateUtils.formatDate(selectedDate));

        etDate.setOnClickListener(v -> showDatePicker());
        etDate.setFocusable(false);

        if (articleId != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Modifier l'article");
            }
            loadArticle();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Nouvel article");
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSave.setOnClickListener(v -> saveArticle());
    }

    private void loadArticle() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Article article = database.articleDao().findById(articleId);
            if (article != null) {
                runOnUiThread(() -> {
                    etTitle.setText(article.getTitle());
                    etDescription.setText(article.getDescription());
                    etAmount.setText(String.valueOf(article.getAmount()));
                    selectedDate = article.getDate();
                    etDate.setText(DateUtils.formatDate(selectedDate));
                });
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dateCalendar.set(year, month, dayOfMonth);
            selectedDate = dateCalendar.getTimeInMillis();
            etDate.setText(DateUtils.formatDate(selectedDate));
        }, dateCalendar.get(Calendar.YEAR), dateCalendar.get(Calendar.MONTH),
                dateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveArticle() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Titre requis");
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Montant requis");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Montant invalide");
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Le montant doit être positif");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            if (articleId != -1) {
                Article article = database.articleDao().findById(articleId);
                if (article != null) {
                    article.setTitle(title);
                    article.setDescription(description);
                    article.setAmount(amount);
                    article.setDate(selectedDate);
                    database.articleDao().update(article);
                }
            } else {
                Article article = new Article();
                article.setTitle(title);
                article.setDescription(description);
                article.setAmount(amount);
                article.setDate(selectedDate);
                article.setCategoryId(categoryId);
                database.articleDao().insert(article);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Article sauvegardé", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
