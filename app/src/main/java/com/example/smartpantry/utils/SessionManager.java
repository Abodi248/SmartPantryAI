package com.example.smartpantry.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "smartpantry_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final long NOT_LOGGED_IN = -1L;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void login(long userId, String displayName) {
        prefs.edit()
             .putLong(KEY_USER_ID, userId)
             .putString(KEY_DISPLAY_NAME, displayName)
             .apply();
    }

    public void logout() {
        prefs.edit()
             .putLong(KEY_USER_ID, NOT_LOGGED_IN)
             .remove(KEY_DISPLAY_NAME)
             .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getLong(KEY_USER_ID, NOT_LOGGED_IN) != NOT_LOGGED_IN;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, NOT_LOGGED_IN);
    }

    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "");
    }

    public void updateDisplayName(String displayName) {
        prefs.edit().putString(KEY_DISPLAY_NAME, displayName).apply();
    }
}
