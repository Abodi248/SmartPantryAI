package com.example.smartpantry.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartpantry.model.ChatMessage;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.network.LocalAiClient;
import com.example.smartpantry.network.LocalAiClient.BackendType;
import com.example.smartpantry.utils.AiCapabilityChecker;
import com.example.smartpantry.utils.PromptBuilder;
import com.example.smartpantry.utils.SafetyFilter;
import java.util.List;
import java.util.function.Consumer;

public class ChatRepository {

    private static final String TAG = "ChatRepository";

    private final LocalAiClient localAiClient;
    private final boolean aiAvailable;
    private final MutableLiveData<String> backendLabel;
    private final MutableLiveData<Boolean> isInitializing = new MutableLiveData<>(false);

    public ChatRepository(Application application) {
        boolean capable = AiCapabilityChecker.isDeviceCapable(application);
        boolean modelPresent = AiCapabilityChecker.isModelPresent(LocalAiClient.DEFAULT_MODEL_PATH);

        if (capable && modelPresent) {
            backendLabel = new MutableLiveData<>("Initializing…");
            isInitializing.setValue(true);
            localAiClient = LocalAiClient.getInstance(application);
            localAiClient.addOnReadyListener(backend -> {
                Log.i(TAG, "AI backend resolved: " + backend.getLabel());
                backendLabel.postValue(backend.getLabel());
                isInitializing.postValue(false);
            });
            aiAvailable = true;
        } else {
            localAiClient = null;
            aiAvailable = false;
            backendLabel = new MutableLiveData<>(BackendType.NONE.getLabel());
            Log.w(TAG, "On-device AI unavailable — RAM insufficient or model file absent");
        }
    }

    public boolean isAiAvailable() { return aiAvailable; }

    public LiveData<String> getBackendLabel() { return backendLabel; }

    public LiveData<Boolean> getIsInitializing() { return isInitializing; }

    public void sendMessage(String userMessage, List<Ingredient> pantry,
                            List<ChatMessage> history,
                            Consumer<String> onResult, Consumer<String> onError) {
        if (!aiAvailable || localAiClient == null) {
            onError.accept("on_device_unavailable");
            return;
        }

        if (!SafetyFilter.isInputSafe(userMessage)) {
            onResult.accept(SafetyFilter.REFUSAL_MESSAGE);
            return;
        }

        String prompt = PromptBuilder.buildChatPrompt(pantry, history, userMessage);
        boolean recipeRequest = PromptBuilder.isRecipeIntent(userMessage);

        if (localAiClient.isReady()) {
            localAiClient.generateAsync(prompt, response -> {
                String text = response.trim();
                if (recipeRequest && !text.startsWith("RECIPE:")) {
                    text = "RECIPE: " + text;
                }
                Log.d(TAG, "Chat response (" + text.length() + " chars): "
                        + text.substring(0, Math.min(120, text.length())));
                onResult.accept(text);
            }, error -> {
                Log.e(TAG, "Local inference error: " + error);
                onError.accept(error);
            });
        } else {
            onError.accept("on_device_initializing");
        }
    }

}
