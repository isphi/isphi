package com.budgetmanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.budgetmanager.database.entity.Category;
import com.budgetmanager.database.entity.CategoryTotal;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    LiveData<List<Category>> getAllByUser(long userId);

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type ORDER BY name ASC")
    LiveData<List<Category>> getByUserAndType(long userId, String type);

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    Category findById(long id);

    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> findByIdLive(long id);

    @Query("SELECT c.name AS categoryName, c.type AS type, COALESCE(SUM(a.amount), 0) AS total " +
           "FROM categories c LEFT JOIN articles a ON c.id = a.categoryId " +
           "WHERE c.userId = :userId GROUP BY c.id ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getCategoryTotals(long userId);

    @Query("SELECT c.name AS categoryName, c.type AS type, COALESCE(SUM(a.amount), 0) AS total " +
           "FROM categories c LEFT JOIN articles a ON c.id = a.categoryId " +
           "WHERE c.userId = :userId AND a.date >= :startDate AND a.date <= :endDate " +
           "GROUP BY c.id ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getCategoryTotalsByDateRange(long userId, long startDate, long endDate);
}
