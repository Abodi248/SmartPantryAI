package com.example.smartpantry.model;

import java.util.List;

public class Recipe {

    private String title;
    private List<String> ingredients;
    private List<String> steps;
    private List<String> missingIngredients;

    public Recipe(String title, List<String> ingredients, List<String> steps,
                  List<String> missingIngredients) {
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.missingIngredients = missingIngredients;
    }

    public String getTitle() { return title; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getSteps() { return steps; }
    public List<String> getMissingIngredients() { return missingIngredients; }
}