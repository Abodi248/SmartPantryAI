package com.example.smartpantry.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecipeDto {

    @SerializedName("title")
    public String title;

    @SerializedName("ingredients")
    public List<String> ingredients;

    @SerializedName("steps")
    public List<String> steps;

    @SerializedName("missing")
    public List<String> missing;

    @SerializedName("tips")
    public String tips;
}
