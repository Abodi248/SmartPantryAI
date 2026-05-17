package com.example.smartpantry.network;

import android.content.Context;
import android.util.Log;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class LocalAiClient {

    private static final String TAG = "LocalAiClient";

    public static final String DEFAULT_MODEL_PATH =
            "/data/local/tmp/gemma-2b-it-gpu-int4.bin";

    private static final int MAX_TOKENS = 1024;
    private static final int TOP_K = 40;
    private static final float TEMPERATURE = 0.8f;

    public enum BackendType {
        GPU("On-device (GPU)"),
        CPU("On-device (CPU)"),
        NONE("Unavailable");

        private final String label;

        BackendType(String label) { this.label = label; }

        public String getLabel() { return label; }
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LlmInference llmInference;
    private LlmInferenceSessionOptions sessionOptions;

    private volatile BackendType backendType = BackendType.NONE;
    private volatile boolean ready = false;

    public LocalAiClient(Context context, String modelPath, Consumer<BackendType> onReady) {
        Context appContext = context.getApplicationContext();
        executor.execute(() -> {
            sessionOptions = LlmInferenceSessionOptions.builder()
                    .setTopK(TOP_K)
                    .setTemperature(TEMPERATURE)
                    .build();

            BackendType result = tryInit(appContext, modelPath, LlmInference.Backend.GPU);
            if (result == BackendType.NONE) {
                result = tryInit(appContext, modelPath, LlmInference.Backend.CPU);
            }
            backendType = result;
            ready = (result != BackendType.NONE);
            Log.i(TAG, "Init complete — backend: " + result.getLabel());
            onReady.accept(result);
        });
    }

    public boolean isReady() { return ready; }

    public BackendType getBackendType() { return backendType; }

    // Backend init

    private BackendType tryInit(Context context, String modelPath, LlmInference.Backend backend) {
        BackendType target = (backend == LlmInference.Backend.GPU)
                ? BackendType.GPU : BackendType.CPU;
        Log.i(TAG, target.name() + " init start — " + modelPath);
        try {
            LlmInferenceOptions options = LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .setMaxTokens(MAX_TOKENS)
                    .setPreferredBackend(backend)
                    .build();
            llmInference = LlmInference.createFromOptions(context, options);
            Log.i(TAG, target.name() + " init success");
            return target;
        } catch (Exception e) {
            Log.w(TAG, target.name() + " init failed ("
                    + e.getClass().getSimpleName() + "): " + e.getMessage());
            releaseEngine();
            return BackendType.NONE;
        }
    }
    // Inference
    public void generateAsync(String prompt,
                               Consumer<String> onResult,
                               Consumer<String> onError) {
        if (!ready) {
            onError.accept("Local model not available");
            return;
        }
        executor.execute(() -> {
            LlmInferenceSession session = null;
            try {
                session = LlmInferenceSession.createFromOptions(llmInference, sessionOptions);
                session.addQueryChunk(prompt);
                String response = session.generateResponse();
                Log.d(TAG, "Inference complete (" + response.length() + " chars) ["
                        + backendType.name() + "]");
                onResult.accept(response);
            } catch (Exception e) {
                Log.e(TAG, "Inference error [" + backendType.name() + "]: "
                        + e.getMessage(), e);
                onError.accept("On-device inference failed: " + e.getMessage());
            } finally {
                if (session != null) {
                    try { session.close(); } catch (Exception ignored) {}
                }
            }
        });
    }
    public void close() {
        ready = false;
        executor.execute(this::releaseEngine);
        executor.shutdown();
    }

    private void releaseEngine() {
        if (llmInference != null) {
            try {
                llmInference.close();
                Log.i(TAG, "LlmInference released [" + backendType.name() + "]");
            } catch (Exception e) {
                Log.w(TAG, "Error releasing LlmInference: " + e.getMessage());
            } finally {
                llmInference = null;
            }
        }
    }
}
