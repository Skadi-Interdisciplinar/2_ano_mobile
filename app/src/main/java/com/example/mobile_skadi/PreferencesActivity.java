package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.card.MaterialCardView;

public class PreferencesActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    private SwitchCompat switchDarkMode;
    private LinearLayout cardLanguage;
    private TextView tvSelectedLanguage;
    private SwitchCompat switchNotifications;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private final String[] languages = {"Português - BR", "English - US", "Español - ES"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        switchDarkMode = findViewById(R.id.switchDarkMode);
        cardLanguage = findViewById(R.id.cardLanguage);
        tvSelectedLanguage = findViewById(R.id.tvSelectedLanguage);
        switchNotifications = findViewById(R.id.switchNotifications);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Nenhuma nova notificação no momento.", Toast.LENGTH_SHORT).show();
        });

        btnProfileAvatar.setOnClickListener(v -> finish());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Modo escuro ativado" : "Modo escuro desativado", Toast.LENGTH_SHORT).show();
        });

        cardLanguage.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Selecione o idioma")
                    .setItems(languages, (dialog, which) -> {
                        tvSelectedLanguage.setText(languages[which]);
                        Toast.makeText(this, "Idioma alterado para " + languages[which], Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Notificações ativadas" : "Notificações silenciadas", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
            finish();
        });

        navAlertas.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertsActivity.class));
            finish();
        });

        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
            finish();
        });

        navTrimmy.setOnClickListener(v -> {
            Toast.makeText(this, "Assistente Trimmy em desenvolvimento.", Toast.LENGTH_SHORT).show();
        });
    }
}
