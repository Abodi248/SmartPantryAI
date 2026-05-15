package com.example.smartpantry.viewmodel;

import android.app.Application;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.repository.IngredientRepository;
import com.example.smartpantry.repository.ReceiptScanRepository;
import java.util.List;

public class PantryViewModel extends AndroidViewModel {

    private final IngredientRepository repository;
    private final ReceiptScanRepository receiptScanRepository;
    private final LiveData<List<Ingredient>> ingredients;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<Ingredient>> filteredIngredients = new MediatorLiveData<>();

    private final MutableLiveData<List<Ingredient>> scannedIngredients = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isScanLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> scanError = new MutableLiveData<>();

    public PantryViewModel(@NonNull Application application) {
        super(application);
        repository = new IngredientRepository(application);
        receiptScanRepository = new ReceiptScanRepository(application);
        ingredients = repository.getAll();

        filteredIngredients.addSource(ingredients, list -> applyFilter(list, searchQuery.getValue()));
        filteredIngredients.addSource(searchQuery, q -> applyFilter(ingredients.getValue(), q));
    }

    private void applyFilter(List<Ingredient> list, String query) {
        if (list == null) { filteredIngredients.setValue(java.util.Collections.emptyList()); return; }
        if (query == null || query.trim().isEmpty()) { filteredIngredients.setValue(list); return; }
        String lower = query.trim().toLowerCase();
        List<Ingredient> result = new java.util.ArrayList<>();
        for (Ingredient i : list) {
            if (i.getName().toLowerCase().contains(lower)) result.add(i);
        }
        filteredIngredients.setValue(result);
    }

    public LiveData<List<Ingredient>> getIngredients() { return filteredIngredients; }

    public void setSearchQuery(String query) { searchQuery.setValue(query); }

    public LiveData<List<Ingredient>> getScannedIngredients() { return scannedIngredients; }

    public LiveData<Boolean> getIsScanLoading() { return isScanLoading; }

    public LiveData<String> getScanError() { return scanError; }

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

    public void addAllIngredients(List<Ingredient> ingredients) {
        repository.insertAll(ingredients);
    }

    public void scanReceipt(Uri imageUri) {
        isScanLoading.setValue(true);
        scanError.setValue(null);
        receiptScanRepository.parseReceiptAsync(
                imageUri,
                parsed -> {
                    isScanLoading.postValue(false);
                    scannedIngredients.postValue(parsed);
                },
                error -> {
                    isScanLoading.postValue(false);
                    scanError.postValue(error);
                }
        );
    }

    public void clearScannedIngredients() {
        scannedIngredients.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        receiptScanRepository.shutdown();
    }
}
