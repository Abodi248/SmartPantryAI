package com.example.smartpantry.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MealPlanDao {

    @Insert
    long insert(MealPlanEntity entity);

    @Delete
    void delete(MealPlanEntity entity);

    @Query("SELECT * FROM meal_plans WHERE date = :date ORDER BY id ASC")
    LiveData<List<MealPlanEntity>> getByDate(String date);

    @Query("SELECT * FROM meal_plans WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, id ASC")
    LiveData<List<MealPlanEntity>> getForDateRange(String startDate, String endDate);

    @Query("SELECT DISTINCT date FROM meal_plans WHERE date >= :startDate AND date <= :endDate")
    LiveData<List<String>> getDatesWithMealsInRange(String startDate, String endDate);
}
