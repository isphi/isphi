package com.budgetmanager.ui.article;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.budgetmanager.R;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.Article;
import com.budgetmanager.utils.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class AddEditArticleActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etAmount, etDate;
    private ImageView ivArticleImage;
    private BudgetDatabase database;
    private long categoryId;
    private long articleId = -1;
    private long selectedDate;
    private String imagePath;
    private Calendar dateCalendar;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveImageLocally(imageUri);
                    }
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission requise pour choisir une image", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_article);

        database = BudgetDatabase.getInstance(this);

        etTitle = findViewById(R.id.et_article_title);
        etDescription = findViewById(R.id.et_article_description);
        etAmount = findViewById(R.id.et_article_amount);
        etDate = findViewById(R.id.et_article_date);
        ivArticleImage = findViewById(R.id.iv_article_image);
        MaterialButton btnSave = findViewById(R.id.btn_save_article);
        MaterialButton btnPickImage = findViewById(R.id.btn_pick_article_image);

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

        btnPickImage.setOnClickListener(v -> checkPermissionAndPickImage());
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
                    if (article.getImagePath() != null) {
                        imagePath = article.getImagePath();
                        Glide.with(this).load(new File(imagePath)).into(ivArticleImage);
                    }
                });
            }
        });
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveImageLocally(Uri uri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream != null) {
                    File dir = new File(getExternalFilesDir(null), "article_images");
                    if (!dir.exists()) dir.mkdirs();

                    String fileName = "art_" + System.currentTimeMillis() + ".jpg";
                    File file = new File(dir, fileName);
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    inputStream.close();

                    imagePath = file.getAbsolutePath();
                    runOnUiThread(() -> Glide.with(this).load(file).into(ivArticleImage));
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show());
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
                    if (imagePath != null) article.setImagePath(imagePath);
                    database.articleDao().update(article);
                }
            } else {
                Article article = new Article();
                article.setTitle(title);
                article.setDescription(description);
                article.setAmount(amount);
                article.setDate(selectedDate);
                article.setCategoryId(categoryId);
                if (imagePath != null) article.setImagePath(imagePath);
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
