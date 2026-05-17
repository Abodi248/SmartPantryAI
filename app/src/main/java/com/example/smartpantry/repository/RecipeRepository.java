package com.example.smartpantry.repository;

import android.app.Application;
import android.util.Log;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.network.LocalAiClient;
import com.example.smartpantry.network.dto.RecipeDto;
import com.example.smartpantry.utils.AiCapabilityChecker;
import com.example.smartpantry.utils.PromptBuilder;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";

    private final LocalAiClient localAiClient;
    private final boolean aiAvailable;

    public RecipeRepository(Application application) {
        boolean capable = AiCapabilityChecker.isDeviceCapable(application);
        boolean modelPresent = AiCapabilityChecker.isModelPresent(LocalAiClient.DEFAULT_MODEL_PATH);
        if (capable && modelPresent) {
            localAiClient = new LocalAiClient(
                    application,
                    LocalAiClient.DEFAULT_MODEL_PATH,
                    backend -> Log.i(TAG, "Recipe AI backend: " + backend.getLabel())
            );
            aiAvailable = true;
        } else {
            localAiClient = null;
            aiAvailable = false;
            Log.w(TAG, "On-device AI unavailable — RAM insufficient or model file absent");
        }
    }

    public boolean isAiAvailable() { return aiAvailable; }

    public void generateRecipes(List<Ingredient> ingredients, String restrictions,
                                Consumer<List<Recipe>> onSuccess, Consumer<String> onError) {
        if (!aiAvailable || localAiClient == null) {
            onError.accept("on_device_unavailable");
            return;
        }

        if (!localAiClient.isReady()) {
            onError.accept("on_device_initializing");
            return;
        }

        Log.d(TAG, "Generating recipe on-device [" + localAiClient.getBackendType().getLabel() + "]");
        String prompt = PromptBuilder.buildRecipePrompt(ingredients, restrictions);
        localAiClient.generateAsync(
                prompt,
                response -> {
                    Log.d(TAG, "On-device raw response: " + response);
                    List<Recipe> recipes = parseRecipes("{" + response);
                    if (recipes.isEmpty()) {
                        onError.accept("Could not parse recipe from AI response. Try again.");
                    } else {
                        onSuccess.accept(recipes);
                    }
                },
                error -> {
                    Log.e(TAG, "On-device inference error: " + error);
                    onError.accept(error);
                });
    }

    private List<Recipe> parseRecipes(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        String json = text.trim();
        if (json.contains("```")) {
            int fenceEnd = json.indexOf('\n', json.indexOf("```"));
            int closeFence = json.lastIndexOf("```");
            if (fenceEnd > 0 && closeFence > fenceEnd) {
                json = json.substring(fenceEnd + 1, closeFence).trim();
            }
        }
        int objStart = json.indexOf('{');
        int arrStart = json.indexOf('[');
        if (objStart == -1 && arrStart == -1) return Collections.emptyList();
        Gson gson = new Gson();
        try {
            if (arrStart != -1 && (objStart == -1 || arrStart < objStart)) {
                RecipeDto[] dtos = gson.fromJson(json.substring(arrStart), RecipeDto[].class);
                List<Recipe> result = new ArrayList<>();
                for (RecipeDto dto : dtos) result.add(dtoToRecipe(dto));
                return result;
            } else {
                RecipeDto dto = gson.fromJson(json.substring(objStart), RecipeDto.class);
                return Collections.singletonList(dtoToRecipe(dto));
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Recipe dtoToRecipe(RecipeDto dto) {
        return new Recipe(
                dto.title != null ? dto.title : "Untitled Recipe",
                dto.ingredients != null ? dto.ingredients : Collections.emptyList(),
                dto.steps != null ? dto.steps : Collections.emptyList(),
                dto.missing != null ? dto.missing : Collections.emptyList()
        );
    }

    public void shutdown() {
        if (localAiClient != null) localAiClient.close();
    }
}
