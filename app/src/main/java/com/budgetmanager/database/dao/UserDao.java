package com.budgetmanager.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.budgetmanager.database.entity.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User findById(long id);

    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> findByIdLive(long id);

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    void updatePassword(long userId, String newPassword);

    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    void resetPassword(String email, String newPassword);
}
