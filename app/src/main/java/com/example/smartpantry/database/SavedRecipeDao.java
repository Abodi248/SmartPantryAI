package com.example.smartpantry.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SavedRecipeDao {

    @Insert
    void insert(SavedRecipeEntity entity);

    @Delete
    void delete(SavedRecipeEntity entity);

    @Query("SELECT * FROM saved_recipes ORDER BY id DESC")
    LiveData<List<SavedRecipeEntity>> getAll();

    @Query("SELECT * FROM saved_recipes WHERE id = :id LIMIT 1")
    LiveData<SavedRecipeEntity> getById(long id);
}
