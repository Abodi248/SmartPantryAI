package com.example.smartpantry.model;

import java.util.Collections;
import java.util.List;

public class Recipe {

    private long savedId;
    private boolean saved;
    private boolean userCreated;
    private String title;
    private List<String> ingredients;
    private List<String> steps;
    private List<String> missingIngredients;

    public Recipe(String title, List<String> ingredients, List<String> steps,
                  List<String> missingIngredients) {
        this.title = title;
        this.ingredients = ingredients != null ? ingredients : Collections.emptyList();
        this.steps = steps != null ? steps : Collections.emptyList();
        this.missingIngredients = missingIngredients != null ? missingIngredients : Collections.emptyList();
    }

    public long getSavedId() { return savedId; }
    public void setSavedId(long savedId) { this.savedId = savedId; }
    public boolean isSaved() { return saved; }
    public void setSaved(boolean saved) { this.saved = saved; }
    public boolean isUserCreated() { return userCreated; }
    public void setUserCreated(boolean userCreated) { this.userCreated = userCreated; }
    private String tips = "";

    public String getTitle() { return title; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getSteps() { return steps; }
    public List<String> getMissingIngredients() { return missingIngredients; }
    public String getTips() { return tips != null ? tips : ""; }
    public void setTips(String tips) { this.tips = tips != null ? tips : ""; }
}