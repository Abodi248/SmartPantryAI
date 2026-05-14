package com.example.smartpantry.utils;

import com.example.smartpantry.model.Ingredient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReceiptParser {

    public static List<Ingredient> parse(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        String jsonArray = extractJsonArray(text);
        if (jsonArray == null) return Collections.emptyList();
        try {
            JsonArray array = JsonParser.parseString(jsonArray).getAsJsonArray();
            List<Ingredient> result = new ArrayList<>();
            for (JsonElement el : array) {
                JsonObject obj = el.getAsJsonObject();
                String name = obj.has("name") ? obj.get("name").getAsString().trim() : "";
                String quantity = obj.has("quantity") ? obj.get("quantity").getAsString().trim() : "1";
                String unit = obj.has("unit") ? obj.get("unit").getAsString().trim() : "";
                if (!name.isEmpty()) {
                    result.add(new Ingredient(name, quantity, unit));
                }
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // Extracts the first [...] block from anywhere in the model response,
    // handling markdown fences and prose that surrounds the JSON array.
    private static String extractJsonArray(String text) {
        String s = text.trim();
        // Strip markdown code fences first
        if (s.contains("```")) {
            s = s.replaceAll("(?s)```(?:json)?\\s*", "").trim();
        }
        int start = s.indexOf('[');
        int end = s.lastIndexOf(']');
        if (start != -1 && end > start) {
            return s.substring(start, end + 1);
        }
        return null;
    }
}
