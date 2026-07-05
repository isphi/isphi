package com.budgetmanager.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.budgetmanager.database.dao.ArticleDao;
import com.budgetmanager.database.dao.CategoryDao;
import com.budgetmanager.database.dao.UserDao;
import com.budgetmanager.database.entity.Article;
import com.budgetmanager.database.entity.Category;
import com.budgetmanager.database.entity.User;

@Database(entities = {User.class, Category.class, Article.class}, version = 2, exportSchema = false)
public abstract class BudgetDatabase extends RoomDatabase {

    private static volatile BudgetDatabase INSTANCE;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE articles ADD COLUMN imagePath TEXT");
        }
    };

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
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
