package com.budgetmanager.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetmanager.R;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.User;
import com.budgetmanager.utils.PasswordUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private TextInputEditText etSecurityQuestion, etSecurityAnswer;
    private BudgetDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = BudgetDatabase.getInstance(this);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etSecurityQuestion = findViewById(R.id.et_security_question);
        etSecurityAnswer = findViewById(R.id.et_security_answer);

        MaterialButton btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> performRegister());

        findViewById(R.id.tv_login).setOnClickListener(v -> finish());
    }

    private void performRegister() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);
        String securityQuestion = getText(etSecurityQuestion);
        String securityAnswer = getText(etSecurityAnswer);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Nom requis");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mot de passe requis");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Minimum 6 caractères");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            return;
        }
        if (TextUtils.isEmpty(securityQuestion)) {
            etSecurityQuestion.setError("Question de sécurité requise");
            return;
        }
        if (TextUtils.isEmpty(securityAnswer)) {
            etSecurityAnswer.setError("Réponse requise");
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(PasswordUtils.hashPassword(password));
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswer(securityAnswer.toLowerCase());

        Executors.newSingleThreadExecutor().execute(() -> {
            User existing = database.userDao().findByEmail(email);
            runOnUiThread(() -> {
                if (existing != null) {
                    etEmail.setError("Cet email est déjà utilisé");
                } else {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        database.userDao().insert(user);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Inscription réussie ! Connectez-vous.", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                }
            });
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
