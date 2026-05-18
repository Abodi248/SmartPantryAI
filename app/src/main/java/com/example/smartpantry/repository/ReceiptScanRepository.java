package com.example.smartpantry.repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.network.LocalAiClient;
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

public class ReceiptScanRepository {

    private static final String TAG = "ReceiptScanRepository";

    private final Application application;
    private final LocalAiClient localAiClient;
    private final boolean aiAvailable;
    private final MutableLiveData<Boolean> isInitializing = new MutableLiveData<>(false);

    public ReceiptScanRepository(Application application) {
        this.application = application;
        boolean capable = AiCapabilityChecker.isDeviceCapable(application);
        boolean modelPresent = AiCapabilityChecker.isModelPresent(LocalAiClient.DEFAULT_MODEL_PATH);

        if (capable && modelPresent) {
            isInitializing.setValue(true);
            localAiClient = LocalAiClient.getInstance(application);
            localAiClient.addOnReadyListener(backend -> {
                Log.i(TAG, "Receipt AI backend: " + backend.getLabel());
                isInitializing.postValue(false);
            });
            aiAvailable = true;
        } else {
            localAiClient = null;
            aiAvailable = false;
            Log.w(TAG, "On-device AI unavailable — RAM insufficient or model file absent");
        }
    }

    public boolean isAiAvailable() { return aiAvailable; }

    public LiveData<Boolean> getIsInitializing() { return isInitializing; }

    public void parseReceiptAsync(Uri imageUri,
                                   Consumer<List<Ingredient>> onSuccess,
                                   Consumer<String> onError) {
        if (!aiAvailable || localAiClient == null) {
            onError.accept("on_device_unavailable");
            return;
        }

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
        if (!localAiClient.isReady()) {
            onError.accept("on_device_initializing");
            return;
        }

        Log.d(TAG, "Parsing receipt on-device [" + localAiClient.getBackendType().getLabel() + "]");
        String prompt = PromptBuilder.buildReceiptParsePrompt(ocrText);
        localAiClient.generateAsync(
                prompt,
                response -> {
                    Log.d(TAG, "On-device raw response: " + response);
                    List<Ingredient> parsed = ReceiptParser.parse("[" + response);
                    if (parsed.isEmpty()) {
                        onError.accept("No ingredients identified from receipt");
                    } else {
                        onSuccess.accept(parsed);
                    }
                },
                error -> {
                    Log.e(TAG, "On-device inference error: " + error);
                    onError.accept("Receipt parsing failed: " + error);
                });
    }

}
