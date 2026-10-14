package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private TextView btnVerTodosAlertas;
    private MaterialCardView cardAlertaCritico;
    private MaterialCardView btnMapaUnidades;
    private MaterialCardView btnNiveisAlerta;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        btnVerTodosAlertas = findViewById(R.id.btnVerTodosAlertas);
        cardAlertaCritico = findViewById(R.id.cardAlertaCritico);
        btnMapaUnidades = findViewById(R.id.btnMapaUnidades);
        btnNiveisAlerta = findViewById(R.id.btnNiveisAlerta);

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
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Ver todos os alertas
        btnVerTodosAlertas.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertsActivity.class));
        });

        // Detalhes do alerta crítico
        cardAlertaCritico.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertDetailsActivity.class));
        });

        // Mapa das unidades
        btnMapaUnidades.setOnClickListener(v -> {
            // TODO: Abrir BottomSheet / Tela do Mapa
        });

        // Níveis de alerta
        btnNiveisAlerta.setOnClickListener(v -> {
            AlertLevelsDialog.show(this);
        });

        // Bottom Navigation
        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
        });

        navAlertas.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertsActivity.class));
        });

        navInicio.setOnClickListener(v -> {
            // Tela atual (Início)
        });

        navRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
        });

        navTrimmy.setOnClickListener(v -> {
            // TODO: Navegar para tela do Trimmy (Chat de IA no Gestor Empresarial)
        });
    }
}