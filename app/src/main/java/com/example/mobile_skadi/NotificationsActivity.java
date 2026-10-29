package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class NotificationsActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private MaterialCardView btnProfileAvatar;

    private LinearLayout notifItem1;
    private LinearLayout notifItem2;
    private View dotUnread1;
    private View dotUnread2;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        notifItem1 = findViewById(R.id.notifItem1);
        notifItem2 = findViewById(R.id.notifItem2);
        dotUnread1 = findViewById(R.id.dotUnread1);
        dotUnread2 = findViewById(R.id.dotUnread2);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        notifItem1.setOnClickListener(v -> {
            markAsRead(notifItem1, dotUnread1);
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_TYPE, "CRITICAL");
            startActivity(intent);
        });

        notifItem2.setOnClickListener(v -> {
            markAsRead(notifItem2, dotUnread2);
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_TYPE, "WARNING");
            startActivity(intent);
        });

        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
        });

        navAlertas.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertsActivity.class));
        });

        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        navRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
        });
    }

    private void markAsRead(LinearLayout item, View dot) {
        item.setBackgroundColor(Color.parseColor("#E2E8F0"));
        dot.setVisibility(View.GONE);
    }
}
