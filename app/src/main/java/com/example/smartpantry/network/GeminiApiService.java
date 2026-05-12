package com.example.smartpantry.network;

import com.example.smartpantry.network.dto.GeminiRequest;
import com.example.smartpantry.network.dto.GeminiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApiService {

    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    Call<GeminiResponse> generateContent(
            @Query("key") String apiKey,
            @Body GeminiRequest request
    );
}
