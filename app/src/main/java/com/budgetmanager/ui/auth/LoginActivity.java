package com.budgetmanager.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetmanager.MainActivity;
import com.budgetmanager.R;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.User;
import com.budgetmanager.utils.PasswordUtils;
import com.budgetmanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private SessionManager sessionManager;
    private BudgetDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        database = BudgetDatabase.getInstance(this);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> performLogin());

        findViewById(R.id.tv_register).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.tv_forgot_password).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void performLogin() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mot de passe requis");
            return;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);

        Executors.newSingleThreadExecutor().execute(() -> {
            User user = database.userDao().login(email, hashedPassword);
            runOnUiThread(() -> {
                if (user != null) {
                    sessionManager.createSession(user.getId(), user.getName(), user.getEmail());
                    navigateToMain();
                } else {
                    Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
