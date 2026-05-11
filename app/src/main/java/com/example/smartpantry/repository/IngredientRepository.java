package com.example.smartpantry.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.smartpantry.database.AppDatabase;
import com.example.smartpantry.database.IngredientDao;
import com.example.smartpantry.database.IngredientEntity;
import com.example.smartpantry.model.Ingredient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientRepository {

    private final IngredientDao dao;
    private final LiveData<List<Ingredient>> ingredients;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public IngredientRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        dao = db.ingredientDao();
        ingredients = Transformations.map(dao.getAll(), entities -> {
            List<Ingredient> list = new ArrayList<>();
            if (entities == null) return list;
            for (IngredientEntity e : entities) {
                Ingredient ing = new Ingredient(e.name, e.quantity, e.unit);
                ing.setId(e.id);
                list.add(ing);
            }
            return list;
        });
    }

    public LiveData<List<Ingredient>> getAll() {
        return ingredients;
    }

    public void insert(Ingredient ingredient) {
        executor.execute(() -> {
            IngredientEntity e = new IngredientEntity();
            e.name = ingredient.getName();
            e.quantity = ingredient.getQuantity();
            e.unit = ingredient.getUnit();
            dao.insert(e);
        });
    }

    public void update(Ingredient ingredient) {
        executor.execute(() -> {
            IngredientEntity e = new IngredientEntity();
            e.id = ingredient.getId();
            e.name = ingredient.getName();
            e.quantity = ingredient.getQuantity();
            e.unit = ingredient.getUnit();
            dao.update(e);
        });
    }

    public void delete(Ingredient ingredient) {
        executor.execute(() -> {
            IngredientEntity e = new IngredientEntity();
            e.id = ingredient.getId();
            e.name = ingredient.getName();
            e.quantity = ingredient.getQuantity();
            e.unit = ingredient.getUnit();
            dao.delete(e);
        });
    }
}
