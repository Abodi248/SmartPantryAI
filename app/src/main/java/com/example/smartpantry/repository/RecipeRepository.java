package com.example.smartpantry.repository;

import android.app.Application;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeRepository {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RecipeRepository(Application application) {
        // do later: initialise GeminiClient here
    }

    // do later: generateRecipes(List<Ingredient>, callback) → calls Gemini API via Retrofit
}
