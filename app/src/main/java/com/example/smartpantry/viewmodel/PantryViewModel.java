package com.example.smartpantry.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smartpantry.model.Ingredient;
import java.util.List;

public class PantryViewModel extends ViewModel {

    private final MutableLiveData<List<Ingredient>> ingredients = new MutableLiveData<>();

    public LiveData<List<Ingredient>> getIngredients() {
        return ingredients;
    }

    // do later: add/update/delete methods backed by IngredientRepository
}