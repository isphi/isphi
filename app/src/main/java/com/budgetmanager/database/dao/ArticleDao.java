package com.budgetmanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import com.budgetmanager.database.entity.Article;

import java.util.List;

@Dao
public interface ArticleDao {

    @Insert
    long insert(Article article);

    @Update
    void update(Article article);

    @Delete
    void delete(Article article);

    @Query("SELECT * FROM articles WHERE categoryId = :categoryId ORDER BY date DESC")
    LiveData<List<Article>> getByCategoryOrderByDate(long categoryId);

    @Query("SELECT * FROM articles WHERE categoryId = :categoryId ORDER BY amount DESC")
    LiveData<List<Article>> getByCategoryOrderByAmount(long categoryId);

    @Query("SELECT * FROM articles WHERE categoryId = :categoryId ORDER BY title ASC")
    LiveData<List<Article>> getByCategoryOrderByTitle(long categoryId);

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    Article findById(long id);

    @Query("SELECT * FROM articles WHERE id = :id")
    LiveData<Article> findByIdLive(long id);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM articles a " +
           "INNER JOIN categories c ON a.categoryId = c.id " +
           "WHERE c.userId = :userId AND c.type = :type")
    LiveData<Double> getTotalByType(long userId, String type);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM articles a " +
           "INNER JOIN categories c ON a.categoryId = c.id " +
           "WHERE c.userId = :userId AND c.type = :type AND a.date >= :startDate AND a.date <= :endDate")
    LiveData<Double> getTotalByTypeAndDateRange(long userId, String type, long startDate, long endDate);

    @SuppressWarnings("RoomWarnings.CURSOR_MISMATCH")
    @Query("SELECT a.* FROM articles a " +
           "INNER JOIN categories c ON a.categoryId = c.id " +
           "WHERE c.userId = :userId AND a.date >= :startDate AND a.date <= :endDate " +
           "ORDER BY a.date DESC")
    LiveData<List<Article>> getByUserAndDateRange(long userId, long startDate, long endDate);
}
