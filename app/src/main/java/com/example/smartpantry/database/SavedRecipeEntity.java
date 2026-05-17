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
    public String ingredientsJson = "[]";

    @NonNull
    public String stepsJson = "[]";

    public int isUserCreated = 1;
}
