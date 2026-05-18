package com.example.smartpantry.network;

import android.content.Context;
import android.util.Log;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession;
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession.LlmInferenceSessionOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class LocalAiClient {

    private static final String TAG = "LocalAiClient";

    public static final String DEFAULT_MODEL_PATH = "/data/local/tmp/gemma-2b-it-gpu-int4.bin";

    private static final int MAX_TOKENS = 4096;
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

    private static volatile LocalAiClient instance;

    public static LocalAiClient getInstance(Context context) {
        if (instance == null) {
            synchronized (LocalAiClient.class) {
                if (instance == null) {
                    instance = new LocalAiClient(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Object listenerLock = new Object();
    private final List<Consumer<BackendType>> pendingListeners = new ArrayList<>();

    private volatile BackendType backendType = BackendType.NONE;
    private volatile boolean ready = false;

    private LlmInference llmInference;
    private LlmInferenceSessionOptions sessionOptions;

    private LocalAiClient(Context context) {
        executor.execute(() -> initInBackground(context));
    }

    private void initInBackground(Context context) {
        sessionOptions = LlmInferenceSessionOptions.builder()
                .setTopK(TOP_K)
                .setTemperature(TEMPERATURE)
                .build();

        BackendType result = tryInit(context, DEFAULT_MODEL_PATH, LlmInference.Backend.GPU);
        if (result == BackendType.NONE) {
            result = tryInit(context, DEFAULT_MODEL_PATH, LlmInference.Backend.CPU);
        }

        final BackendType finalResult = result;
        List<Consumer<BackendType>> toNotify;
        synchronized (listenerLock) {
            backendType = finalResult;
            ready = (finalResult != BackendType.NONE);
            toNotify = new ArrayList<>(pendingListeners);
            pendingListeners.clear();
        }
        Log.i(TAG, "Init complete — backend: " + finalResult.getLabel());
        for (Consumer<BackendType> cb : toNotify) {
            cb.accept(finalResult);
        }
    }

    public void addOnReadyListener(Consumer<BackendType> listener) {
        boolean alreadyReady;
        synchronized (listenerLock) {
            alreadyReady = ready;
            if (!alreadyReady) {
                pendingListeners.add(listener);
            }
        }
        if (alreadyReady) {
            executor.execute(() -> listener.accept(backendType));
        }
    }

    public boolean isReady() { return ready; }

    public BackendType getBackendType() { return backendType; }

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
