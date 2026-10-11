package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AlertDetailsActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private MaterialButton btnJustificar;
    private MaterialButton btnReconhecerAlerta;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        btnJustificar = findViewById(R.id.btnJustificar);
        btnReconhecerAlerta = findViewById(R.id.btnReconhecerAlerta);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        // Voltar para a tela anterior
        btnBack.setOnClickListener(v -> finish());

        // Notificações e Perfil
        btnNotification.setOnClickListener(v -> {
            // TODO: Abrir tela/modal de Notificações
        });

        btnProfileAvatar.setOnClickListener(v -> {
            // TODO: Abrir perfil
        });

        // Ações do alerta
        btnJustificar.setOnClickListener(v -> {
            // TODO: Abrir modal/fluxo de Justificar alerta
        });

        btnReconhecerAlerta.setOnClickListener(v -> {
            // TODO: Chamar endpoint para reconhecer alerta
        });

        // Toolbar inferior
        navCamaras.setOnClickListener(v -> {
            // TODO: Navegar para Câmaras
        });

        navAlertas.setOnClickListener(v -> {
            // Volta para a lista de alertas
            finish();
        });

        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navRelatorios.setOnClickListener(v -> {
            // TODO: Navegar para Relatórios
        });

        navTrimmy.setOnClickListener(v -> {
            // TODO: Navegar para Trimmy
        });
    }
}
