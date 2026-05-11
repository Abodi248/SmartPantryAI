package com.example.smartpantry.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.repository.IngredientRepository;
import java.util.List;

public class PantryViewModel extends AndroidViewModel {

    private final IngredientRepository repository;
    private final LiveData<List<Ingredient>> ingredients;

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = new IngredientRepository(application);
        ingredients = repository.getAll();
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return ingredients;
    }

    public void addIngredient(String name, String quantity, String unit) {
        repository.insert(new Ingredient(name, quantity, unit));
    }

    public void updateIngredient(long id, String name, String quantity, String unit) {
        Ingredient ingredient = new Ingredient(name, quantity, unit);
        ingredient.setId(id);
        repository.update(ingredient);
    }

    public void deleteIngredient(Ingredient ingredient) {
        repository.delete(ingredient);
    }
}
