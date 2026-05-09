package com.example.smartpantry.network;

// do later: singleton Retrofit instance pointed at the Gemini base URL
public class GeminiClient {

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/";

    private static GeminiApiService instance;

    public static GeminiApiService getInstance() {
        if (instance == null) {
            // do later: build Retrofit with GsonConverterFactory + HttpLoggingInterceptor
        }
        return instance;
    }
}
