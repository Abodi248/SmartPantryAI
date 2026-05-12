package com.example.smartpantry.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.io.File;

public class AiCapabilityChecker {

    private static final String TAG = "AiCapabilityChecker";

    // Gemma 2B INT4 needs roughly 1.5 GB model + runtime overhead
    private static final long MIN_RAM_BYTES = 3_500_000_000L; // 3.5 GB

    public static boolean isDeviceCapable(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return false;

        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);

        long totalRam = memInfo.totalMem;
        Log.d(TAG, "Total RAM: " + totalRam / 1_000_000 + " MB");

        if (totalRam < MIN_RAM_BYTES) {
            Log.w(TAG, "Device RAM insufficient for on-device model (" +
                    totalRam / 1_000_000 + " MB < " + MIN_RAM_BYTES / 1_000_000 + " MB)");
            return false;
        }

        // Low-end devices report isLowRamDevice = true even with adequate total RAM
        if (am.isLowRamDevice()) {
            Log.w(TAG, "Device flagged as low-RAM by system — skipping local model");
            return false;
        }

        return true;
    }

    public static boolean isModelPresent(String modelPath) {
        File f = new File(modelPath);
        boolean exists = f.exists() && f.isFile() && f.length() > 0;
        Log.d(TAG, "Model at " + modelPath + ": " + (exists ? "present" : "not found"));
        return exists;
    }
}
