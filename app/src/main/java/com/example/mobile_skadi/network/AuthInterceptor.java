package com.example.mobile_skadi.network;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = SessionManager.getInstance(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        String path = original.url().encodedPath();
        boolean isAuthEndpoint = path.contains("auth/login")
                || path.contains("auth/recover-password")
                || path.contains("auth/verify-recovery-code")
                || path.contains("auth/reset-password");

        if (!isAuthEndpoint) {
            String token = sessionManager.getToken();
            if (token != null && !token.trim().isEmpty()) {
                builder.header("Authorization", "Bearer " + token.trim());
            }
        }

        return chain.proceed(builder.build());
    }
}