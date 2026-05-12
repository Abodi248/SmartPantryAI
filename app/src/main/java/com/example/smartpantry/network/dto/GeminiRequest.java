package com.example.smartpantry.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class GeminiRequest {

    @SerializedName("contents")
    public List<GeminiContent> contents;

    public GeminiRequest(String prompt) {
        this.contents = Collections.singletonList(new GeminiContent(prompt));
    }
}
