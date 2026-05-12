package com.example.smartpantry.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeminiResponse {

    @SerializedName("candidates")
    public List<GeminiCandidate> candidates;

    public String getText() {
        if (candidates == null || candidates.isEmpty()) return null;
        GeminiCandidate c = candidates.get(0);
        if (c.content == null || c.content.parts == null || c.content.parts.isEmpty()) return null;
        return c.content.parts.get(0).text;
    }
}
