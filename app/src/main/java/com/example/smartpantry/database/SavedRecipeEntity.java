package com.example.smartpantry.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saved_recipes")
public class SavedRecipeEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title = "";

    @NonNull
    public String ingredientsJson = "[]";   // Gson-serialised List<String>

    @NonNull
    public String stepsJson = "[]";         // Gson-serialised List<String>

    public int isUserCreated = 1;
}
