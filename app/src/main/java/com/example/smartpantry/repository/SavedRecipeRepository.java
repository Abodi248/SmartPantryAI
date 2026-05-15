package com.example.smartpantry.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.smartpantry.database.AppDatabase;
import com.example.smartpantry.database.SavedRecipeDao;
import com.example.smartpantry.database.SavedRecipeEntity;
import com.example.smartpantry.model.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedRecipeRepository {

    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    private final SavedRecipeDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();
    private final LiveData<List<Recipe>> savedRecipes;

    public SavedRecipeRepository(Application application) {
        dao = AppDatabase.getInstance(application).savedRecipeDao();
        savedRecipes = Transformations.map(dao.getAll(), entities -> {
            List<Recipe> list = new ArrayList<>();
            if (entities == null) return list;
            for (SavedRecipeEntity e : entities) {
                List<String> ingredients = parseJson(e.ingredientsJson);
                List<String> steps = parseJson(e.stepsJson);
                Recipe recipe = new Recipe(e.title, ingredients, steps, Collections.emptyList());
                recipe.setSavedId(e.id);
                recipe.setSaved(true);
                recipe.setUserCreated(e.isUserCreated == 1);
                list.add(recipe);
            }
            return list;
        });
    }

    public LiveData<List<Recipe>> getAll() { return savedRecipes; }

    public LiveData<Recipe> getById(long id) {
        return Transformations.map(dao.getById(id), e -> {
            if (e == null) return null;
            Recipe r = new Recipe(e.title, parseJson(e.ingredientsJson), parseJson(e.stepsJson),
                    Collections.emptyList());
            r.setSavedId(e.id);
            r.setSaved(true);
            r.setUserCreated(e.isUserCreated == 1);
            return r;
        });
    }

    public void insert(String title, List<String> ingredients, List<String> steps,
                       boolean isUserCreated) {
        executor.execute(() -> {
            SavedRecipeEntity e = new SavedRecipeEntity();
            e.title = title;
            e.ingredientsJson = gson.toJson(ingredients);
            e.stepsJson = gson.toJson(steps);
            e.isUserCreated = isUserCreated ? 1 : 0;
            dao.insert(e);
        });
    }

    public void delete(Recipe recipe) {
        executor.execute(() -> {
            SavedRecipeEntity e = new SavedRecipeEntity();
            e.id = recipe.getSavedId();
            e.title = recipe.getTitle();
            e.ingredientsJson = gson.toJson(recipe.getIngredients());
            e.stepsJson = gson.toJson(recipe.getSteps());
            dao.delete(e);
        });
    }

    private List<String> parseJson(String json) {
        try {
            List<String> result = gson.fromJson(json, STRING_LIST_TYPE);
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
