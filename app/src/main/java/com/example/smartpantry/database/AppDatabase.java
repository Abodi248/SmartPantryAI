package com.example.smartpantry.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
    entities = {IngredientEntity.class, MealPlanEntity.class, SavedRecipeEntity.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract IngredientDao ingredientDao();
    public abstract MealPlanDao mealPlanDao();
    public abstract SavedRecipeDao savedRecipeDao();

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "ALTER TABLE saved_recipes ADD COLUMN isUserCreated INTEGER NOT NULL DEFAULT 1"
            );
        }
    };

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS meal_plans ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "date TEXT NOT NULL,"
                + "recipeTitle TEXT NOT NULL,"
                + "notes TEXT NOT NULL)"
            );
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS saved_recipes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "title TEXT NOT NULL,"
                + "ingredientsJson TEXT NOT NULL,"
                + "stepsJson TEXT NOT NULL)"
            );
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smartpantry.db"
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
                }
            }
        }
        return instance;
    }
}
