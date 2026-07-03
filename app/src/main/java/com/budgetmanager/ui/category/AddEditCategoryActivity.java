package com.budgetmanager.ui.category;

import android.Manifest;
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
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class AddEditCategoryActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private ImageView ivCategoryImage;
    private BudgetDatabase database;
    private SessionManager sessionManager;
    private String imagePath;
    private long categoryId = -1;
    private String categoryType;

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
        setContentView(R.layout.activity_add_edit_category);

        database = BudgetDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        etName = findViewById(R.id.et_category_name);
        ivCategoryImage = findViewById(R.id.iv_category_image);
        MaterialButton btnSave = findViewById(R.id.btn_save_category);
        MaterialButton btnPickImage = findViewById(R.id.btn_pick_image);

        categoryType = getIntent().getStringExtra("type");
        categoryId = getIntent().getLongExtra("category_id", -1);

        if (categoryId != -1) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Modifier la catégorie");
            }
            loadCategory();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Nouvelle catégorie");
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnPickImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnSave.setOnClickListener(v -> saveCategory());
    }

    private void loadCategory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Category category = database.categoryDao().findById(categoryId);
            if (category != null) {
                runOnUiThread(() -> {
                    etName.setText(category.getName());
                    categoryType = category.getType();
                    if (category.getImagePath() != null) {
                        imagePath = category.getImagePath();
                        Glide.with(this).load(new File(imagePath)).into(ivCategoryImage);
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
                    File dir = new File(getExternalFilesDir(null), "category_images");
                    if (!dir.exists()) dir.mkdirs();

                    String fileName = "cat_" + System.currentTimeMillis() + ".jpg";
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
                    runOnUiThread(() -> Glide.with(this).load(file).into(ivCategoryImage));
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveCategory() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            etName.setError("Nom de catégorie requis");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            if (categoryId != -1) {
                Category category = database.categoryDao().findById(categoryId);
                if (category != null) {
                    category.setName(name);
                    if (imagePath != null) category.setImagePath(imagePath);
                    database.categoryDao().update(category);
                }
            } else {
                Category category = new Category();
                category.setName(name);
                category.setType(categoryType);
                category.setUserId(sessionManager.getUserId());
                if (imagePath != null) category.setImagePath(imagePath);
                database.categoryDao().insert(category);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Catégorie sauvegardée", Toast.LENGTH_SHORT).show();
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
