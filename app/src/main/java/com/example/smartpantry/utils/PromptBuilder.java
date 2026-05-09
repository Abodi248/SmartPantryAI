package com.example.smartpantry.utils;

import com.example.smartpantry.model.Ingredient;
import java.util.List;

public class PromptBuilder {

    public static String buildRecipePrompt(List<Ingredient> ingredients, String dietaryRestrictions) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful cooking assistant.\n");
        sb.append("The user has these ingredients: ");
        sb.append(formatIngredients(ingredients)).append(".\n");
        sb.append("Dietary restrictions: ")
          .append(dietaryRestrictions == null || dietaryRestrictions.isEmpty()
                  ? "none" : dietaryRestrictions)
          .append(".\n");
        sb.append("Suggest a recipe using primarily these ingredients.\n");
        sb.append("Respond in this JSON format:\n");
        sb.append("{\"title\": \"...\", \"ingredients\": [...], \"steps\": [...], \"missing\": [...]}");
        return sb.toString();
    }

    public static String buildChatPrompt(List<Ingredient> pantry, String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are SmartPantry AI, a cooking assistant.\n");
        sb.append("Current pantry: ").append(formatIngredients(pantry)).append(".\n");
        sb.append("Answer the user's question about cooking, recipes, or food.\n");
        sb.append("If asked about unrelated topics, politely redirect to cooking.\n");
        sb.append("Refuse any requests for harmful content.\n");
        sb.append("User: ").append(userMessage);
        return sb.toString();
    }

    private static String formatIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) return "none";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ing = ingredients.get(i);
            sb.append(ing.getQuantity()).append(" ").append(ing.getUnit())
              .append(" ").append(ing.getName());
            if (i < ingredients.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }
}
