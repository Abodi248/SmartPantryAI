package com.example.smartpantry.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.smartpantry.database.AppDatabase;
import com.example.smartpantry.database.MealPlanDao;
import com.example.smartpantry.database.MealPlanEntity;
import com.example.smartpantry.model.MealPlan;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealPlanRepository {

    private final MealPlanDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MealPlanRepository(Application application) {
        dao = AppDatabase.getInstance(application).mealPlanDao();
    }

    public LiveData<List<MealPlan>> getByDate(String date) {
        return Transformations.map(dao.getByDate(date), entities -> toModels(entities));
    }

    public LiveData<List<MealPlan>> getForDateRange(String startDate, String endDate) {
        return Transformations.map(dao.getForDateRange(startDate, endDate), entities -> toModels(entities));
    }

    public LiveData<List<String>> getDatesWithMealsInRange(String startDate, String endDate) {
        return dao.getDatesWithMealsInRange(startDate, endDate);
    }

    public void insert(String date, String recipeTitle, String notes) {
        executor.execute(() -> {
            MealPlanEntity e = new MealPlanEntity();
            e.date = date;
            e.recipeTitle = recipeTitle;
            e.notes = notes != null ? notes : "";
            dao.insert(e);
        });
    }

    public void delete(MealPlan mealPlan) {
        executor.execute(() -> {
            MealPlanEntity e = new MealPlanEntity();
            e.id = mealPlan.getId();
            e.date = mealPlan.getDate();
            e.recipeTitle = mealPlan.getRecipeTitle();
            e.notes = mealPlan.getNotes();
            dao.delete(e);
        });
    }

    private List<MealPlan> toModels(List<MealPlanEntity> entities) {
        List<MealPlan> list = new ArrayList<>();
        if (entities == null) return list;
        for (MealPlanEntity e : entities) {
            list.add(new MealPlan(e.id, e.date, e.recipeTitle, e.notes));
        }
        return list;
    }
}
