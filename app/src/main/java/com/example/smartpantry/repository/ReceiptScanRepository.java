package com.example.smartpantry.repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import com.example.smartpantry.BuildConfig;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.network.GeminiClient;
import com.example.smartpantry.network.LocalAiClient;
import com.example.smartpantry.network.dto.GeminiRequest;
import com.example.smartpantry.network.dto.GeminiResponse;
import com.example.smartpantry.utils.AiCapabilityChecker;
import com.example.smartpantry.utils.PromptBuilder;
import com.example.smartpantry.utils.ReceiptParser;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptScanRepository {

    private static final String TAG = "ReceiptScanRepository";

    private final Application application;
    private final LocalAiClient localAiClient;

    public ReceiptScanRepository(Application application) {
        this.application = application;
        boolean capable = AiCapabilityChecker.isDeviceCapable(application);
        boolean modelPresent = AiCapabilityChecker.isModelPresent(LocalAiClient.DEFAULT_MODEL_PATH);

        if (capable && modelPresent) {
            localAiClient = new LocalAiClient(
                    application,
                    LocalAiClient.DEFAULT_MODEL_PATH,
                    backend -> Log.i(TAG, "Receipt AI backend: " + backend.getLabel()));
        } else {
            localAiClient = null;
            Log.i(TAG, "Receipt AI: cloud (device not capable or model absent)");
        }
    }

    public void parseReceiptAsync(Uri imageUri,
                                   Consumer<List<Ingredient>> onSuccess,
                                   Consumer<String> onError) {
        InputImage image;
        try {
            image = InputImage.fromFilePath(application, imageUri);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load image: " + e.getMessage());
            onError.accept("Could not load receipt image");
            return;
        }

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String rawText = visionText.getText().trim();
                    Log.d(TAG, "OCR extracted " + rawText.length() + " chars");
                    if (rawText.isEmpty()) {
                        onError.accept("No text found in the receipt image");
                        return;
                    }
                    parseWithAi(rawText, onSuccess, onError);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "ML Kit OCR failed: " + e.getMessage());
                    onError.accept("Could not read receipt: " + e.getMessage());
                });
    }

    private void parseWithAi(String ocrText,
                              Consumer<List<Ingredient>> onSuccess,
                              Consumer<String> onError) {
        if (localAiClient != null && localAiClient.isReady()) {
            Log.d(TAG, "Parsing receipt on-device [" + localAiClient.getBackendType().getLabel() + "]");
            // Prompt ends with "[" — the model completes the array. Prepend "[" when parsing.
            String prompt = PromptBuilder.buildReceiptParsePromptLocal(ocrText);
            localAiClient.generateAsync(prompt,
                    response -> {
                        Log.d(TAG, "On-device raw response: " + response);
                        handleParsedResponse("[" + response, onSuccess, onError);
                    },
                    error -> {
                        Log.e(TAG, "On-device inference error: " + error);
                        onError.accept("On-device inference failed: " + error);
                    });
        } else {
            Log.d(TAG, "Parsing receipt via cloud");
            parseWithCloud(PromptBuilder.buildReceiptParsePrompt(ocrText), onSuccess, onError);
        }
    }

    private void handleParsedResponse(String json,
                                       Consumer<List<Ingredient>> onSuccess,
                                       Consumer<String> onError) {
        List<Ingredient> parsed = ReceiptParser.parse(json);
        if (parsed.isEmpty()) {
            onError.accept("No ingredients identified");
        } else {
            onSuccess.accept(parsed);
        }
    }

    private void parseWithCloud(String prompt,
                                 Consumer<List<Ingredient>> onSuccess,
                                 Consumer<String> onError) {
        GeminiClient.getInstance()
                .generateContent(BuildConfig.GEMINI_API_KEY, new GeminiRequest(prompt))
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call,
                                           Response<GeminiResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e(TAG, "Gemini error: HTTP " + response.code());
                            onError.accept("Could not parse receipt (error " + response.code() + ")");
                            return;
                        }
                        String json = response.body().getText();
                        Log.d(TAG, "Cloud receipt parse response: " + json);
                        handleParsedResponse(json, onSuccess, onError);
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        Log.e(TAG, "Gemini call failed: " + t.getMessage());
                        onError.accept("Network error: " + t.getMessage());
                    }
                });
    }

    public void shutdown() {
        if (localAiClient != null) localAiClient.close();
    }
}
