package com.budgetmanager;

import android.app.Application;

import com.budgetmanager.database.BudgetDatabase;

public class BudgetManagerApp extends Application {

    private BudgetDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = BudgetDatabase.getInstance(this);
    }

    public BudgetDatabase getDatabase() {
        return database;
    }
}
