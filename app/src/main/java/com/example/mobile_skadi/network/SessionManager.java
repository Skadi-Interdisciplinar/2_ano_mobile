package com.example.mobile_skadi.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "skadi_session_pref";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_UNIT = "unit";

    private static SessionManager instance;
    private final SharedPreferences preferences;

    private SessionManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveSession(String token, String username, String role, String unit) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .putString(KEY_UNIT, unit)
                .apply();
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public void saveUsername(String username) {
        preferences.edit().putString(KEY_USERNAME, username).apply();
    }

    public void saveRole(String role) {
        preferences.edit().putString(KEY_ROLE, role).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "");
    }

    public String getUnit() {
        return preferences.getString(KEY_UNIT, "Belterra");
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
