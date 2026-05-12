package com.example.smartpantry.network;

import com.example.smartpantry.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class GeminiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static volatile GeminiApiService instance;

    public static GeminiApiService getInstance() {
        if (instance == null) {
            synchronized (GeminiClient.class) {
                if (instance == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.NONE);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .addInterceptor(logging)
                            .build();

                    instance = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                            .create(GeminiApiService.class);
                }
            }
        }
        return instance;
    }
}
