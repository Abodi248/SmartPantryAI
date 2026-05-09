package com.example.smartpantry.utils;

import java.util.Arrays;
import java.util.List;

public class SafetyFilter {

    public static final String REFUSAL_MESSAGE =
            "I can only help with cooking and recipes. Please ask me something food-related.";

    private static final List<String> BLOCKED_KEYWORDS = Arrays.asList(
            "self-harm", "suicide", "explosive", "poison", "drug synthesis",
            "methamphetamine", "weapon", "kill", "murder"
    );

    public static boolean isInputSafe(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        String lower = input.toLowerCase();
        for (String keyword : BLOCKED_KEYWORDS) {
            if (lower.contains(keyword)) return false;
        }
        return true;
    }
}
