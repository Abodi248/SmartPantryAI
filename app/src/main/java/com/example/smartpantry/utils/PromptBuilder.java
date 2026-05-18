package com.example.smartpantry.utils;

import com.example.smartpantry.model.ChatMessage;
import com.example.smartpantry.model.Ingredient;
import java.util.List;
import java.util.Locale;

public class PromptBuilder {


    public static String buildRecipePrompt(List<Ingredient> ingredients,
                                           String dietaryRestrictions) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a cooking assistant. Suggest one recipe using the ingredients listed.\n");
        sb.append("Ingredients available: ").append(formatIngredients(ingredients)).append(".\n");
        if (dietaryRestrictions != null && !dietaryRestrictions.isEmpty()) {
            sb.append("Dietary restrictions: ").append(dietaryRestrictions).append(".\n");
        }
        sb.append("Output ONLY valid JSON. No preamble, no explanation, no markdown.\n");
        sb.append("Format exactly:\n");
        sb.append("{\"title\":\"Name\",\"ingredients\":[\"item\"],\"steps\":[\"step\"],\"missing\":[]}\n");
        sb.append("JSON:\n{");
        return sb.toString();
    }

    public static String buildChatPrompt(List<Ingredient> pantry, List<ChatMessage> history,
                                          String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are SmartPantry AI, a kitchen and meal planning assistant.\n");
        sb.append("Current pantry: ").append(formatIngredients(pantry)).append(".\n\n");
        sb.append("When the user asks for a recipe (keywords: recipe, make, cook, how do I, give me),\n");
        sb.append("respond using EXACTLY this structure — no deviations:\n");
        sb.append("RECIPE: [just the dish name]\n");
        sb.append("INGREDIENTS:\n- [ingredient 1]\n- [ingredient 2]\n");
        sb.append("STEPS:\n1. [step 1]\n2. [step 2]\n");
        sb.append("TIPS:\n[tip text]\n\n");
        sb.append("For all other questions, respond in plain conversational text.\n");
        sb.append("Refuse any requests for harmful content.\n\n");

        if (history != null) {
            for (ChatMessage msg : history) {
                sb.append(msg.isUser() ? "User: " : "Assistant: ");
                sb.append(msg.getText()).append("\n");
            }
        }

        sb.append("User: ").append(userMessage).append("\nAssistant:");
        if (isRecipeIntent(userMessage)) {
            sb.append(" RECIPE: ");
        }
        return sb.toString();
    }

    public static boolean isRecipeIntent(String message) {
        if (message == null || message.trim().isEmpty()) return false;
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains("recipe") || lower.contains("cook")
                || lower.contains("make") || lower.contains("prepare")
                || lower.contains("how do i") || lower.contains("give me")
                || lower.contains("what can i");
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
