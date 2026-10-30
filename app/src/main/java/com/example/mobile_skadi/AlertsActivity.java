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
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        // Notificações
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        // Perfil
        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Detalhes dos alertas
        cardAlertCritical.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
            startActivity(intent);
        });

        cardAlertWarning.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
            startActivity(intent);
        });

        cardAlertStable.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, true);
            startActivity(intent);
        });

        // Níveis de alerta
        btnEntendaNiveis.setOnClickListener(v -> {
            AlertLevelsDialog.show(this);
        });

        // Barra inferior de navegação
        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
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
            startActivity(new Intent(this, ReportsActivity.class));
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
        });
    }
}
