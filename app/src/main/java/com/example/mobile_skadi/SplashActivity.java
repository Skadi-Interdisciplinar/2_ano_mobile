package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View layoutCenterBrand = findViewById(R.id.layoutCenterBrand);
        if (layoutCenterBrand != null) {
            layoutCenterBrand.setAlpha(0f);
            layoutCenterBrand.setScaleX(0.88f);
            layoutCenterBrand.setScaleY(0.88f);
            layoutCenterBrand.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(850)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DURATION_MS);
    }

    private void navigateToNextScreen() {
        if (isFinishing() || isDestroyed()) return;

        Intent targetIntent;
        if (SessionManager.getInstance(this).hasToken()) {
            targetIntent = new Intent(this, HomeActivity.class);
        } else {
            targetIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(targetIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
