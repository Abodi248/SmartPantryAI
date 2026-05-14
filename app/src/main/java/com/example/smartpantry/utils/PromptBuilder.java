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

    /**
     * Prompt for on-device model (Gemma). Ends with "[" to prime the model into
     * completing a JSON array directly, preventing prose responses.
     * Prepend "[" to the model's response before parsing.
     */
    public static String buildReceiptParsePromptLocal(String ocrText) {
        return "Extract every food or beverage item from the grocery receipt below.\n"
                + "Return a JSON array of objects with keys: name, quantity, unit.\n"
                + "Use empty string for unknown quantity or unit.\n"
                + "Receipt:\n"
                + ocrText + "\n"
                + "JSON:\n[";
    }

    /** Prompt for cloud (Gemini) — model follows instructions reliably without priming. */
    public static String buildReceiptParsePrompt(String ocrText) {
        return "You are a grocery receipt parser.\n"
                + "Extract all food and beverage items from the following receipt text.\n"
                + "For each item determine: name, quantity (number as string), and unit "
                + "(g, kg, ml, L, pcs, or empty string if unknown).\n"
                + "Respond with ONLY a JSON array — no explanation, no markdown:\n"
                + "[{\"name\": \"Milk\", \"quantity\": \"1\", \"unit\": \"L\"}, ...]\n"
                + "If quantity or unit cannot be determined use \"1\" and \"\" respectively.\n"
                + "Receipt text:\n"
                + ocrText;
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
