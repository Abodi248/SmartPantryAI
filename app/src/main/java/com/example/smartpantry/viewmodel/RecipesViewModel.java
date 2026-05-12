package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.repository.IngredientRepository;
import com.example.smartpantry.repository.RecipeRepository;
import java.util.Collections;
import java.util.List;

public class RecipesViewModel extends AndroidViewModel {

    private final RecipeRepository recipeRepository;
    private final LiveData<List<Ingredient>> pantryLiveData;
    private final Observer<List<Ingredient>> pantryObserver;
    private List<Ingredient> currentPantry = Collections.emptyList();

    private final MutableLiveData<List<Recipe>> recipes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public RecipesViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = new RecipeRepository(application);
        pantryLiveData = new IngredientRepository(application).getAll();
        pantryObserver = list -> currentPantry = list != null ? list : Collections.emptyList();
        pantryLiveData.observeForever(pantryObserver);
    }

    public LiveData<List<Recipe>> getRecipes() { return recipes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

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
                    recipes.postValue(result);
                },
                err -> {
                    isLoading.postValue(false);
                    error.postValue(err);
                }
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        pantryLiveData.removeObserver(pantryObserver);
    }
}
