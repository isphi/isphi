package com.budgetmanager.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetmanager.R;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.User;
import com.budgetmanager.utils.PasswordUtils;
import com.budgetmanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private BudgetDatabase database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        database = BudgetDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        MaterialButton btnChangePassword = findViewById(R.id.btn_change_password);
        btnChangePassword.setOnClickListener(v -> changePassword());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Changer le mot de passe");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void changePassword() {
        String currentPassword = getText(etCurrentPassword);
        String newPassword = getText(etNewPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Mot de passe actuel requis");
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Nouveau mot de passe requis");
            return;
        }
        if (newPassword.length() < 6) {
            etNewPassword.setError("Minimum 6 caractères");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            User user = database.userDao().findById(sessionManager.getUserId());
            runOnUiThread(() -> {
                if (user != null && PasswordUtils.verifyPassword(currentPassword, user.getPassword())) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.userDao().updatePassword(user.getId(),
                                PasswordUtils.hashPassword(newPassword));
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Mot de passe modifié avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                } else {
                    etCurrentPassword.setError("Mot de passe actuel incorrect");
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
