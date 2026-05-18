package com.example.smartpantry.utils;

import android.util.Log;
import androidx.annotation.Nullable;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.network.dto.RecipeDto;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeExtractor {

    private static final String TAG = "RecipeExtractor";
    private static final Gson GSON = new Gson();

    private static final Pattern P_INGREDIENTS =
            Pattern.compile("(?i)\\bingredients?\\s*[:\\-]?", Pattern.MULTILINE);
    private static final Pattern P_STEPS =
            Pattern.compile("(?i)\\b(?:steps?|instructions?|directions?|method|preparation)\\s*[:\\-]?",
                    Pattern.MULTILINE);
    private static final Pattern P_TIPS =
            Pattern.compile("(?i)\\bTIPS?\\s*[:\\-]?", Pattern.MULTILINE);
    private static final Pattern P_RECIPE_HEADER =
            Pattern.compile("^(?:#{1,6}\\s*)?RECIPE:\\s*(.*)$", Pattern.MULTILINE);
    private static final Pattern P_BOLD_TITLE =
            Pattern.compile("\\*{1,3}([^*\n]{3,60})\\*{1,3}");
    private static final Pattern P_RECIPE_FOR =
            Pattern.compile("(?i)(?:recipe\\s+(?:for\\s+)?|dish[:\\s]+|meal[:\\s]+)\"?([A-Z][^\n\"!.]{2,60})");
    private static final Pattern P_BULLET =
            Pattern.compile("^[\\s]*(?:[•\\-*]|\\d+[.)])\\s*(.+)$", Pattern.MULTILINE);

    public static boolean isLikelyRecipe(String text) {
        if (text == null || text.length() < 80) return false;
        String lower = text.toLowerCase(Locale.ROOT);
        if ((lower.contains("\"title\"") || lower.contains("\"ingredients\""))
                && lower.contains("\"steps\"")) return true;
        boolean hasIngredients = lower.contains("ingredients");
        boolean hasSteps = lower.contains("steps") || lower.contains("instructions")
                || lower.contains("directions");
        Log.d(TAG, "isLikelyRecipe → ingredients=" + hasIngredients + " steps=" + hasSteps);
        return hasIngredients && hasSteps;
    }

    @Nullable
    public static Recipe extract(String text) {
        if (text == null || text.isEmpty()) return null;
        Log.d(TAG, "extract() text[0..200]: "
                + text.substring(0, Math.min(200, text.length())).replace('\n', '↵'));

        Recipe fromJson = tryJsonExtract(text);
        Log.d(TAG, "extract() tryJsonExtract → " + (fromJson != null ? "success" : "null"));
        if (fromJson != null) return fromJson;

        Recipe fromStructured = tryStructuredExtract(text);
        Log.d(TAG, "extract() tryStructuredExtract → " + (fromStructured != null ? "success" : "null"));
        if (fromStructured != null) return fromStructured;

        Recipe fromText = tryTextExtract(text);
        Log.d(TAG, "extract() tryTextExtract → " + (fromText != null ? "success" : "null"));
        return fromText;
    }


    @Nullable
    private static Recipe tryJsonExtract(String text) {
        String cleaned = stripMarkdownFences(text.trim());

        for (int i = 0; i < cleaned.length(); i++) {
            if (cleaned.charAt(i) != '{') continue;

            String block = extractBalancedBlock(cleaned, i);
            if (block != null) {
                try {
                    RecipeDto dto = GSON.fromJson(block, RecipeDto.class);
                    if (isUsable(dto)) return dtoToRecipe(dto);
                } catch (Exception ignored) {}
            }

            try {
                JsonReader reader = new JsonReader(new StringReader(cleaned.substring(i)));
                reader.setLenient(true);
                RecipeDto dto = GSON.fromJson(reader, RecipeDto.class);
                if (isUsable(dto)) return dtoToRecipe(dto);
            } catch (Exception ignored) {}
        }
        return null;
    }


    @Nullable
    private static Recipe tryStructuredExtract(String text) {
        String title;
        Matcher headerMatcher = P_RECIPE_HEADER.matcher(text);
        boolean headerFound = headerMatcher.find();
        Log.d(TAG, "tryStructuredExtract() P_RECIPE_HEADER matched=" + headerFound);

        if (headerFound) {
            title = headerMatcher.group(1).replaceAll("[*#]+", "").trim();

            String titleLower = title.toLowerCase(Locale.ROOT);
            boolean titleIsGreeting = isGreeting(title) || titleLower.startsWith("this is");
            if (!title.isEmpty() && titleIsGreeting) {
                Matcher recipeMatcher = P_RECIPE_FOR.matcher(title);
                if (recipeMatcher.find()) {
                    title = recipeMatcher.group(1).trim();
                } else {
                    title = "";
                }
            }

            if (title.isEmpty()) {
                String afterHeader = text.substring(headerMatcher.end()).trim();
                for (String line : afterHeader.split("\n", -1)) {
                    String candidate = line.replaceAll("[*#]+", "").trim();
                    if (candidate.isEmpty() || isGreeting(candidate)) continue;
                    if (P_INGREDIENTS.matcher(candidate).find()
                            || P_STEPS.matcher(candidate).find()) break;
                    title = candidate;
                    break;
                }
                if (title.isEmpty()) {
                    Log.d(TAG, "tryStructuredExtract() title empty after next-line scan → null");
                    return null;
                }
            }
        } else {
            Matcher ingMatcher = P_INGREDIENTS.matcher(text);
            if (!ingMatcher.find()) {
                Log.d(TAG, "tryStructuredExtract() no header and P_INGREDIENTS not found → null");
                return null;
            }
            title = extractTitle(text, ingMatcher.start());
        }

        List<String> ingredients = extractSection(text, P_INGREDIENTS, P_STEPS);
        List<String> steps = extractSection(text, P_STEPS, P_TIPS);
        Log.d(TAG, "tryStructuredExtract() title=\"" + title
                + "\" ingredients=" + ingredients.size() + " steps=" + steps.size());

        if (ingredients.isEmpty() && steps.isEmpty()) {
            Log.d(TAG, "tryStructuredExtract() both sections empty → null");
            return null;
        }

        Recipe r = new Recipe(title, ingredients, steps, Collections.emptyList());
        String tips = extractTipsText(text);
        if (tips != null && !tips.isEmpty()) r.setTips(tips);
        return r;
    }

    @Nullable
    private static String extractTipsText(String text) {
        Matcher m = P_TIPS.matcher(text);
        if (!m.find()) return null;
        String after = text.substring(m.end()).trim();
        int blank = after.indexOf("\n\n");
        return (blank != -1 ? after.substring(0, blank) : after).trim();
    }

    @Nullable
    private static Recipe tryTextExtract(String text) {
        Matcher ingMatcher = P_INGREDIENTS.matcher(text);
        if (!ingMatcher.find()) {
            Log.d(TAG, "tryTextExtract() P_INGREDIENTS not found → null");
            return null;
        }

        String title = extractTitle(text, ingMatcher.start());
        List<String> ingredients = extractSection(text, P_INGREDIENTS, P_STEPS);
        List<String> steps = extractSection(text, P_STEPS, null);

        if (ingredients.isEmpty()) return null;
        return new Recipe(title, ingredients, steps, Collections.emptyList());
    }

    private static String extractTitle(String text, int ingredientsHeaderStart) {
        Matcher boldMatcher = P_BOLD_TITLE.matcher(text);
        while (boldMatcher.find()) {
            if (boldMatcher.start() < ingredientsHeaderStart) {
                String candidate = boldMatcher.group(1).trim();
                if (!isGreeting(candidate)) return candidate;
            }
        }

        Matcher recipeForMatcher = P_RECIPE_FOR.matcher(text);
        if (recipeForMatcher.find() && recipeForMatcher.start() < ingredientsHeaderStart) {
            return recipeForMatcher.group(1).trim();
        }

        String before = text.substring(0, ingredientsHeaderStart);
        String[] lines = before.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim().replaceAll("[#*:\\-]+$", "").trim();
            if (!line.isEmpty() && line.length() >= 3 && line.length() <= 80
                    && !isGreeting(line)) {
                return line;
            }
        }

        for (String line : text.split("\n")) {
            String clean = line.trim().replaceAll("[#*]+", "").trim();
            if (!clean.isEmpty() && clean.length() >= 3 && clean.length() <= 80
                    && !isGreeting(clean)) {
                return clean;
            }
        }

        return "Recipe";
    }

    private static boolean isGreeting(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return lower.startsWith("sure") || lower.startsWith("here") || lower.startsWith("of course")
                || lower.startsWith("great") || lower.startsWith("i'll") || lower.startsWith("i will")
                || lower.startsWith("certainly") || lower.startsWith("absolutely")
                || lower.startsWith("let me") || lower.startsWith("below");
    }

    private static List<String> extractSection(String text, Pattern start, @Nullable Pattern end) {
        Matcher startMatcher = start.matcher(text);
        if (!startMatcher.find()) return Collections.emptyList();

        int contentStart = startMatcher.end();
        int contentEnd = text.length();

        if (end != null) {
            Matcher endMatcher = end.matcher(text);
            while (endMatcher.find()) {
                if (endMatcher.start() > contentStart) {
                    contentEnd = endMatcher.start();
                    break;
                }
            }
        }

        return extractBullets(text.substring(contentStart, contentEnd));
    }

    private static List<String> extractBullets(String section) {
        List<String> result = new ArrayList<>();
        Matcher m = P_BULLET.matcher(section);
        while (m.find()) {
            String item = m.group(1).trim();
            // Skip lines that are purely asterisks — closing ** from bold headers like **SECTION:**
            if (!item.isEmpty() && !item.matches("\\*+")) result.add(item);
        }
        if (result.isEmpty()) {
            for (String line : section.split("\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && trimmed.length() > 2) result.add(trimmed);
            }
        }
        return result;
    }

    private static boolean isUsable(@Nullable RecipeDto dto) {
        return dto != null && dto.title != null && !dto.title.isEmpty();
    }

    private static Recipe dtoToRecipe(RecipeDto dto) {
        return new Recipe(
                dto.title,
                dto.ingredients != null ? dto.ingredients : Collections.emptyList(),
                dto.steps != null ? dto.steps : Collections.emptyList(),
                dto.missing != null ? dto.missing : Collections.emptyList()
        );
    }

    @Nullable
    private static String extractBalancedBlock(String text, int start) {
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (c == '\\') i++;
                else if (c == '"') inString = false;
            } else {
                if (c == '"') inString = true;
                else if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private static String stripMarkdownFences(String text) {
        if (!text.contains("```")) return text;
        int fenceStart = text.indexOf("```");
        int newline = text.indexOf('\n', fenceStart);
        int closeFence = text.lastIndexOf("```");
        if (newline > 0 && closeFence > newline) {
            return text.substring(newline + 1, closeFence).trim();
        }
        return text;
    }
}
