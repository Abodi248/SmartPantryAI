package com.example.smartpantry.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smartpantry.model.Recipe;
import java.util.List;

public class RecipesViewModel extends ViewModel {

    private final MutableLiveData<List<Recipe>> recipes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<Recipe>> getRecipes() { return recipes; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    // do later: generateRecipes(List<Ingredient>) via RecipeRepository → Gemini API
}