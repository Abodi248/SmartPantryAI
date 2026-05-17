package com.example.smartpantry.utils;

import com.example.smartpantry.model.Ingredient;
import java.util.List;

public class PromptBuilder {


    public static String buildRecipePrompt(List<Ingredient> ingredients,
                                           String dietaryRestrictions) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a cooking assistant. Suggest one recipe.\n");
        sb.append("Ingredients available: ").append(formatIngredients(ingredients)).append(".\n");
        if (dietaryRestrictions != null && !dietaryRestrictions.isEmpty()) {
            sb.append("Dietary restrictions: ").append(dietaryRestrictions).append(".\n");
        }
        sb.append("Output a JSON object with keys: title (string), ingredients (array of strings),\n");
        sb.append("steps (array of strings), missing (array of strings for any needed ingredients).\n");
        sb.append("JSON:\n{");
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


    public static String buildReceiptParsePrompt(String ocrText) {
        return "Extract every food or beverage item from the grocery receipt below.\n"
                + "Return a JSON array of objects with keys: name, quantity, unit.\n"
                + "Use empty string for unknown quantity or unit.\n"
                + "Receipt:\n"
                + ocrText + "\n"
                + "JSON:\n[";
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
