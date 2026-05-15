package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.repository.IngredientRepository;
import com.example.smartpantry.repository.RecipeRepository;
import com.example.smartpantry.repository.SavedRecipeRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipesViewModel extends AndroidViewModel {

    private final RecipeRepository recipeRepository;
    private final SavedRecipeRepository savedRecipeRepository;
    private final LiveData<List<Ingredient>> pantryLiveData;
    private final Observer<List<Ingredient>> pantryObserver;
    private List<Ingredient> currentPantry = Collections.emptyList();

    private final LiveData<List<Recipe>> savedRecipesLiveData;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<Recipe>> displayRecipes = new MediatorLiveData<>();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // Set before navigating to RecipeDetailFragment
    private final MutableLiveData<Recipe> selectedRecipe = new MutableLiveData<>();

    public RecipesViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = new RecipeRepository(application);
        savedRecipeRepository = new SavedRecipeRepository(application);

        pantryLiveData = new IngredientRepository(application).getAll();
        pantryObserver = list -> currentPantry = list != null ? list : Collections.emptyList();
        pantryLiveData.observeForever(pantryObserver);

        savedRecipesLiveData = savedRecipeRepository.getAll();
        displayRecipes.addSource(savedRecipesLiveData, ignored -> rebuild());
        displayRecipes.addSource(searchQuery, ignored -> rebuild());
    }

    private void rebuild() {
        List<Recipe> all = savedRecipesLiveData.getValue();
        if (all == null) all = Collections.emptyList();
        String q = searchQuery.getValue();

        if (q == null || q.trim().isEmpty()) {
            displayRecipes.setValue(all);
            return;
        }

        String lower = q.trim().toLowerCase();
        List<Recipe> filtered = new ArrayList<>();
        for (Recipe r : all) {
            if (r.getTitle().toLowerCase().contains(lower)) { filtered.add(r); continue; }
            for (String ing : r.getIngredients()) {
                if (ing.toLowerCase().contains(lower)) { filtered.add(r); break; }
            }
        }
        displayRecipes.setValue(filtered);
    }

    public LiveData<List<Recipe>> getRecipes() { return displayRecipes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Recipe> getSelectedRecipe() { return selectedRecipe; }

    public void setSearchQuery(String query) { searchQuery.setValue(query); }

    public void selectRecipe(Recipe recipe) { selectedRecipe.setValue(recipe); }

    public void generateRecipes() {
        if (currentPantry.isEmpty()) {
            error.setValue("Add ingredients to your pantry first.");
            return;
        }
        isLoading.setValue(true);
        error.setValue(null);
        recipeRepository.generateRecipes(
                currentPantry,
                null,
                result -> {
                    isLoading.postValue(false);
                    // Auto-save every generated recipe so it persists across sessions
                    for (Recipe r : result) {
                        savedRecipeRepository.insert(
                                r.getTitle(), r.getIngredients(), r.getSteps(), false);
                    }
                },
                err -> {
                    isLoading.postValue(false);
                    error.postValue(err);
                }
        );
    }

    public void saveRecipe(String title, List<String> ingredients, List<String> steps) {
        savedRecipeRepository.insert(title, ingredients, steps, true);
    }

    public void deleteSavedRecipe(Recipe recipe) {
        savedRecipeRepository.delete(recipe);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        pantryLiveData.removeObserver(pantryObserver);
        recipeRepository.shutdown();
    }
}
