package com.budgetmanager.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetmanager.R;
import com.budgetmanager.database.BudgetDatabase;
import com.budgetmanager.database.entity.User;
import com.budgetmanager.utils.PasswordUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.concurrent.Executors;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etSecurityAnswer, etNewPassword, etConfirmPassword;
    private MaterialTextView tvSecurityQuestion;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private BudgetDatabase database;
    private User foundUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        database = BudgetDatabase.getInstance(this);

        etEmail = findViewById(R.id.et_email);
        etSecurityAnswer = findViewById(R.id.et_security_answer);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvSecurityQuestion = findViewById(R.id.tv_security_question);

        layoutStep1 = findViewById(R.id.layout_step1);
        layoutStep2 = findViewById(R.id.layout_step2);
        layoutStep3 = findViewById(R.id.layout_step3);

        MaterialButton btnFindAccount = findViewById(R.id.btn_find_account);
        MaterialButton btnVerifyAnswer = findViewById(R.id.btn_verify_answer);
        MaterialButton btnResetPassword = findViewById(R.id.btn_reset_password);

        btnFindAccount.setOnClickListener(v -> findAccount());
        btnVerifyAnswer.setOnClickListener(v -> verifyAnswer());
        btnResetPassword.setOnClickListener(v -> resetPassword());

        findViewById(R.id.tv_back_login).setOnClickListener(v -> finish());
    }

    private void findAccount() {
        String email = getText(etEmail);
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            foundUser = database.userDao().findByEmail(email);
            runOnUiThread(() -> {
                if (foundUser != null) {
                    tvSecurityQuestion.setText(foundUser.getSecurityQuestion());
                    layoutStep1.setVisibility(View.GONE);
                    layoutStep2.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "Aucun compte trouvé avec cet email", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void verifyAnswer() {
        String answer = getText(etSecurityAnswer);
        if (TextUtils.isEmpty(answer)) {
            etSecurityAnswer.setError("Réponse requise");
            return;
        }

        if (answer.toLowerCase().equals(foundUser.getSecurityAnswer())) {
            layoutStep2.setVisibility(View.GONE);
            layoutStep3.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Réponse incorrecte", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetPassword() {
        String newPassword = getText(etNewPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Mot de passe requis");
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
            database.userDao().resetPassword(foundUser.getEmail(),
                    PasswordUtils.hashPassword(newPassword));
            runOnUiThread(() -> {
                Toast.makeText(this, "Mot de passe réinitialisé avec succès", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
