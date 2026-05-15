package com.example.smartpantry.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_plans")
public class MealPlanEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String date = "";        // ISO "yyyy-MM-dd"

    @NonNull
    public String recipeTitle = "";

    @NonNull
    public String notes = "";
}
