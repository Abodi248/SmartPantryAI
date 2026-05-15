package com.example.smartpantry.repository;

import android.app.Application;
import android.util.Log;
import com.example.smartpantry.BuildConfig;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.network.GeminiClient;
import com.example.smartpantry.network.LocalAiClient;
import com.example.smartpantry.network.dto.GeminiRequest;
import com.example.smartpantry.network.dto.GeminiResponse;
import com.example.smartpantry.network.dto.RecipeDto;
import com.example.smartpantry.utils.AiCapabilityChecker;
import com.example.smartpantry.utils.PromptBuilder;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RecipeRepository {

    private static final String TAG = "RecipeRepository";

    private final LocalAiClient localAiClient;

    public RecipeRepository(Application application) {
        boolean capable = AiCapabilityChecker.isDeviceCapable(application);
        boolean modelPresent = AiCapabilityChecker.isModelPresent(LocalAiClient.DEFAULT_MODEL_PATH);
        if (capable && modelPresent) {
            localAiClient = new LocalAiClient(
                    application,
                    LocalAiClient.DEFAULT_MODEL_PATH,
                    backend -> Log.i(TAG, "Recipe AI backend: " + backend.getLabel())
            );
        } else {
            localAiClient = null;
            Log.i(TAG, "Recipe AI backend: CLOUD (device not capable or model absent)");
        }
    }

    public void generateRecipes(List<Ingredient> ingredients, String restrictions,
                                Consumer<List<Recipe>> onSuccess, Consumer<String> onError) {
        if (localAiClient != null && localAiClient.isReady()) {
            Log.d(TAG, "Generating recipe on-device [" + localAiClient.getBackendType().getLabel() + "]");
            String prompt = PromptBuilder.buildRecipePromptLocal(ingredients, restrictions);
            localAiClient.generateAsync(prompt,
                    response -> {
                        Log.d(TAG, "On-device raw response: " + response);
                        List<Recipe> recipes = parseRecipes("{" + response);
                        if (recipes.isEmpty()) {
                            Log.w(TAG, "On-device parse failed, falling back to cloud");
                            generateViaCloud(ingredients, restrictions, onSuccess, onError);
                        } else {
                            onSuccess.accept(recipes);
                        }
                    },
                    error -> {
                        Log.w(TAG, "On-device inference failed, falling back to cloud: " + error);
                        generateViaCloud(ingredients, restrictions, onSuccess, onError);
                    });
        } else {
            generateViaCloud(ingredients, restrictions, onSuccess, onError);
        }
    }

    private void generateViaCloud(List<Ingredient> ingredients, String restrictions,
                                  Consumer<List<Recipe>> onSuccess, Consumer<String> onError) {
        Log.d(TAG, "Generating recipe via cloud");
        String prompt = PromptBuilder.buildRecipePrompt(ingredients, restrictions);
        GeminiClient.getInstance()
                .generateContent(BuildConfig.GEMINI_API_KEY, new GeminiRequest(prompt))
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call,
                                           Response<GeminiResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            onError.accept("API error " + response.code());
                            return;
                        }
                        String text = response.body().getText();
                        List<Recipe> recipes = parseRecipes(text);
                        if (recipes.isEmpty()) {
                            onError.accept("Could not parse recipe response. Try again.");
                        } else {
                            onSuccess.accept(recipes);
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        onError.accept(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
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
                java.util.List<Recipe> result = new java.util.ArrayList<>();
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
