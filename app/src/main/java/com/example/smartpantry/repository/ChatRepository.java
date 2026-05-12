package com.example.smartpantry.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartpantry.BuildConfig;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.network.GeminiClient;
import com.example.smartpantry.network.LocalAiClient;
import com.example.smartpantry.network.LocalAiClient.BackendType;
import com.example.smartpantry.network.dto.GeminiRequest;
import com.example.smartpantry.network.dto.GeminiResponse;
import com.example.smartpantry.utils.AiCapabilityChecker;
import com.example.smartpantry.utils.PromptBuilder;
import com.example.smartpantry.utils.SafetyFilter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.function.Consumer;


public class ChatRepository {

    private static final String TAG = "ChatRepository";

    private final LocalAiClient localAiClient;

    private final MutableLiveData<String> backendLabel =
            new MutableLiveData<>(BackendType.NONE.getLabel());

    public ChatRepository(Application application) {
        boolean capable = AiCapabilityChecker.isDeviceCapable(application);
        boolean modelPresent = AiCapabilityChecker.isModelPresent(LocalAiClient.DEFAULT_MODEL_PATH);

        if (capable && modelPresent) {
            localAiClient = new LocalAiClient(
                    application,
                    LocalAiClient.DEFAULT_MODEL_PATH,
                    backend -> {
                        Log.i(TAG, "AI backend resolved: " + backend.getLabel());
                        backendLabel.postValue(backend.getLabel());
                    }
            );
        } else {
            localAiClient = null;
            Log.i(TAG, "AI backend: CLOUD (device not capable or model absent)");
        }
    }

    public LiveData<String> getBackendLabel() { return backendLabel; }

    public void sendMessage(String userMessage, List<Ingredient> pantry,
                            Consumer<String> onResult, Consumer<String> onError) {

        if (!SafetyFilter.isInputSafe(userMessage)) {
            onResult.accept(SafetyFilter.REFUSAL_MESSAGE);
            return;
        }

        String prompt = PromptBuilder.buildChatPrompt(pantry, userMessage);

        if (localAiClient != null && localAiClient.isReady()) {
            localAiClient.generateAsync(prompt, onResult, fallbackError -> {
                Log.w(TAG, "Local inference failed, falling back to cloud: " + fallbackError);
                sendViaCloud(prompt, onResult, onError);
            });
        } else {
            sendViaCloud(prompt, onResult, onError);
        }
    }

    private void sendViaCloud(String prompt, Consumer<String> onResult, Consumer<String> onError) {
        GeminiClient.getInstance()
                .generateContent(BuildConfig.GEMINI_API_KEY, new GeminiRequest(prompt))
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call,
                                           Response<GeminiResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            onError.accept("Cloud API error " + response.code());
                            return;
                        }
                        String text = response.body().getText();
                        onResult.accept(text != null ? text : "No response from AI.");
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        onError.accept(t.getMessage() != null ? t.getMessage() : "Network error");
                    }
                });
    }

    public void shutdown() {
        if (localAiClient != null) {
            localAiClient.close();
        }
    }
}
