package com.example.smartpantry.model;

public class MealPlan {
    private long id;
    private String date;
    private String recipeTitle;
    private String notes;

    public MealPlan(long id, String date, String recipeTitle, String notes) {
        this.id = id;
        this.date = date;
        this.recipeTitle = recipeTitle;
        this.notes = notes != null ? notes : "";
    }

    public long getId() { return id; }
    public String getDate() { return date; }
    public String getRecipeTitle() { return recipeTitle; }
    public String getNotes() { return notes; }
}
