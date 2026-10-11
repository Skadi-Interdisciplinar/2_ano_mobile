package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class AlertsActivity extends AppCompatActivity {

    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private MaterialCardView cardAlertCritical;
    private MaterialCardView cardAlertWarning;
    private MaterialCardView cardAlertStable;
    private TextView btnEntendaNiveis;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        cardAlertCritical = findViewById(R.id.cardAlertCritical);
        cardAlertWarning = findViewById(R.id.cardAlertWarning);
        cardAlertStable = findViewById(R.id.cardAlertStable);
        btnEntendaNiveis = findViewById(R.id.btnEntendaNiveis);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        // Notificações
        btnNotification.setOnClickListener(v -> {
            // TODO: Abrir tela/modal de Notificações
        });

        // Perfil
        btnProfileAvatar.setOnClickListener(v -> {
            // TODO: Navegar para tela de Perfil do Gestor
        });

        // Detalhes dos alertas
        cardAlertCritical.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertDetailsActivity.class));
        });

        cardAlertWarning.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertDetailsActivity.class));
        });

        cardAlertStable.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertDetailsActivity.class));
        });

        // Níveis de alerta
        btnEntendaNiveis.setOnClickListener(v -> {
            AlertLevelsDialog.show(this);
        });

        // Barra inferior de navegação
        navCamaras.setOnClickListener(v -> {
            // TODO: Navegar para tela de Câmaras
        });

        navAlertas.setOnClickListener(v -> {
            // Já estamos na tela de Alertas
        });

        navInicio.setOnClickListener(v -> {
            // Volta para a tela inicial (Home)
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navRelatorios.setOnClickListener(v -> {
            // TODO: Navegar para tela de Relatórios
        });

        navTrimmy.setOnClickListener(v -> {
            // TODO: Navegar para tela do Trimmy
        });
    }
}
