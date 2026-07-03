package com.budgetmanager.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.budgetmanager.database.dao.ArticleDao;
import com.budgetmanager.database.dao.CategoryDao;
import com.budgetmanager.database.dao.UserDao;
import com.budgetmanager.database.entity.Article;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.database.entity.User;

@Database(entities = {User.class, Category.class, Article.class}, version = 1, exportSchema = false)
public abstract class BudgetDatabase extends RoomDatabase {

    private static volatile BudgetDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ArticleDao articleDao();

    public static BudgetDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (BudgetDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            BudgetDatabase.class,
                            "budget_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
