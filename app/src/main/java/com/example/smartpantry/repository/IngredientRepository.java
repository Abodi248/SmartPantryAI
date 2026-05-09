package com.example.smartpantry.repository;

import android.app.Application;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IngredientRepository {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public IngredientRepository(Application application) {
        // do later: initialise AppDatabase and IngredientDao here
    }

    // do later: implement getAll(), insert(), update(), delete() using Room + LiveData
}
