package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.repository.SavedRecipeRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipesViewModel extends AndroidViewModel {

    private final SavedRecipeRepository savedRecipeRepository;
    private final LiveData<List<Recipe>> savedRecipesLiveData;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<Recipe>> displayRecipes = new MediatorLiveData<>();
    private final MutableLiveData<Recipe> selectedRecipe = new MutableLiveData<>();

    public RecipesViewModel(@NonNull Application application) {
        super(application);
        savedRecipeRepository = new SavedRecipeRepository(application);
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
    public LiveData<Recipe> getSelectedRecipe() { return selectedRecipe; }

    public void setSearchQuery(String query) { searchQuery.setValue(query); }

    public void selectRecipe(Recipe recipe) { selectedRecipe.setValue(recipe); }

    public LiveData<Recipe> getRecipeById(long id) { return savedRecipeRepository.getById(id); }

    public void saveRecipe(String title, List<String> ingredients, List<String> steps, String tips) {
        savedRecipeRepository.insert(title, ingredients, steps, tips, true);
    }

    public void updateRecipe(Recipe recipe) {
        savedRecipeRepository.update(recipe);
    }

    public void saveChatRecipe(Recipe recipe, java.util.function.Consumer<Long> onSaved) {
        savedRecipeRepository.insert(
                recipe.getTitle(), recipe.getIngredients(), recipe.getSteps(), recipe.getTips(),
                false, onSaved);
    }

    public void deleteById(long id) {
        savedRecipeRepository.deleteById(id);
    }

    public void deleteSavedRecipe(Recipe recipe) {
        savedRecipeRepository.delete(recipe);
    }
}
