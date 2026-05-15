package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.MealPlan;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.repository.IngredientRepository;
import com.example.smartpantry.repository.MealPlanRepository;
import com.example.smartpantry.repository.SavedRecipeRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeViewModel extends AndroidViewModel {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final IngredientRepository ingredientRepository;
    private final SavedRecipeRepository savedRecipeRepository;
    private final MealPlanRepository mealPlanRepository;

    private final MutableLiveData<String> selectedDate = new MutableLiveData<>(todayIso());

    // Pantry item count
    private final LiveData<Integer> pantryCount;

    // Saved-recipe count where all ingredients are available in pantry
    private final MediatorLiveData<Integer> readyRecipeCount = new MediatorLiveData<>();

    // Meals for the selected day (for the week strip detail section)
    private LiveData<List<MealPlan>> mealsForSelectedDate;

    // Upcoming meal-plan entries for the next 7 days (used by Home summary)
    private final LiveData<List<MealPlan>> upcomingMeals;

    // Dates that have at least one meal planned in the current week window
    private final LiveData<List<String>> datesWithMeals;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        ingredientRepository = new IngredientRepository(application);
        savedRecipeRepository = new SavedRecipeRepository(application);
        mealPlanRepository = new MealPlanRepository(application);

        pantryCount = Transformations.map(ingredientRepository.getAll(),
                list -> list != null ? list.size() : 0);

        // "Ready" = saved recipe whose every ingredient name matches at least one pantry item
        LiveData<List<Ingredient>> pantryLive = ingredientRepository.getAll();
        LiveData<List<Recipe>> savedLive = savedRecipeRepository.getAll();
        readyRecipeCount.addSource(pantryLive, ignored -> recalcReady(pantryLive.getValue(), savedLive.getValue()));
        readyRecipeCount.addSource(savedLive, ignored -> recalcReady(pantryLive.getValue(), savedLive.getValue()));

        String todayStr = todayIso();
        String weekEnd = LocalDate.now().plusDays(6).format(ISO);
        upcomingMeals = mealPlanRepository.getForDateRange(todayStr, weekEnd);
        datesWithMeals = mealPlanRepository.getDatesWithMealsInRange(todayStr, weekEnd);

        // Default meals view to today
        mealsForSelectedDate = Transformations.switchMap(selectedDate,
                date -> mealPlanRepository.getByDate(date));
    }

    // Exposed LiveData
    public LiveData<Integer> getPantryCount() { return pantryCount; }
    public LiveData<Integer> getReadyRecipeCount() { return readyRecipeCount; }
    public LiveData<List<Recipe>> getSavedRecipes() { return savedRecipeRepository.getAll(); }
    public LiveData<List<MealPlan>> getUpcomingMeals() { return upcomingMeals; }
    public LiveData<List<MealPlan>> getMealsForSelectedDate() { return mealsForSelectedDate; }
    public LiveData<List<String>> getDatesWithMeals() { return datesWithMeals; }
    public LiveData<String> getSelectedDate() { return selectedDate; }

    public void selectDate(String isoDate) { selectedDate.setValue(isoDate); }

    public void addMeal(String recipeTitle, String notes) {
        String date = selectedDate.getValue();
        if (date == null) date = todayIso();
        mealPlanRepository.insert(date, recipeTitle, notes);
    }

    public void deleteMeal(MealPlan mealPlan) {
        mealPlanRepository.delete(mealPlan);
    }

    // Helpers

    private void recalcReady(List<Ingredient> pantry, List<Recipe> saved) {
        if (pantry == null || saved == null) {
            readyRecipeCount.setValue(0);
            return;
        }
        Set<String> pantryNames = new HashSet<>();
        for (Ingredient i : pantry) pantryNames.add(i.getName().toLowerCase());

        int count = 0;
        for (Recipe recipe : saved) {
            List<String> recipeIngredients = recipe.getIngredients();
            if (recipeIngredients.isEmpty()) { count++; continue; }
            boolean allFound = true;
            for (String ri : recipeIngredients) {
                String riLower = ri.toLowerCase();
                boolean found = false;
                for (String pn : pantryNames) {
                    if (pn.contains(riLower) || riLower.contains(pn)) { found = true; break; }
                }
                if (!found) { allFound = false; break; }
            }
            if (allFound) count++;
        }
        readyRecipeCount.setValue(count);
    }

    private static String todayIso() {
        return LocalDate.now().format(ISO);
    }
}
