package com.budgetmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.budgetmanager.ui.auth.ChangePasswordActivity;
import com.budgetmanager.ui.auth.LoginActivity;
import com.budgetmanager.ui.budget.BudgetFragment;
import com.budgetmanager.ui.category.CategoryListFragment;
import com.budgetmanager.ui.dashboard.DashboardFragment;
import com.budgetmanager.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Budget Manager");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.nav_budget) {
                fragment = new BudgetFragment();
            } else if (itemId == R.id.nav_categories) {
                fragment = new CategoryListFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
            return true;
        } else if (itemId == R.id.action_logout) {
            sessionManager.logout();
            navigateToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
