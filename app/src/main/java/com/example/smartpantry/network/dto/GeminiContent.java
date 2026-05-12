package com.example.smartpantry.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class GeminiContent {

    @SerializedName("parts")
    public List<GeminiPart> parts;

    public GeminiContent(String text) {
        this.parts = Collections.singletonList(new GeminiPart(text));
    }

    public GeminiContent() {}
}
