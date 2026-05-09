package com.example.smartpantry.database;

import android.content.Context;

// do later: add @Database(entities = {IngredientEntity.class}, version = 1)
// and extend RoomDatabase
public abstract class AppDatabase {

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    // do later: instance = Room.databaseBuilder(context, AppDatabase.class, "smartpantry.db").build();
                }
            }
        }
        return instance;
    }

    // do later: public abstract IngredientDao ingredientDao();
}
