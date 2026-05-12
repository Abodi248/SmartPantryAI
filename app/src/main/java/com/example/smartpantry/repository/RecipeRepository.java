package com.example.smartpantry.repository;

import android.app.Application;
import com.example.smartpantry.BuildConfig;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.network.GeminiClient;
import com.example.smartpantry.network.dto.GeminiRequest;
import com.example.smartpantry.network.dto.GeminiResponse;
import com.example.smartpantry.network.dto.RecipeDto;
import com.example.smartpantry.utils.PromptBuilder;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RecipeRepository {

    public RecipeRepository(Application application) {}

    public void generateRecipes(List<Ingredient> ingredients, String restrictions,
                                Consumer<List<Recipe>> onSuccess, Consumer<String> onError) {
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

        // Strip markdown code fences (```json ... ``` or ``` ... ```)
        if (json.contains("```")) {
            int fenceEnd = json.indexOf('\n', json.indexOf("```"));
            int closeFence = json.lastIndexOf("```");
            if (fenceEnd > 0 && closeFence > fenceEnd) {
                json = json.substring(fenceEnd + 1, closeFence).trim();
            }
        }

        // Find the start of JSON content
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
}
