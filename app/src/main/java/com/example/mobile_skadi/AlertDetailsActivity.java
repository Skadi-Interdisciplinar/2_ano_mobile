package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AlertDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_IS_RESOLVED = "EXTRA_IS_RESOLVED";

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private MaterialButton btnJustificar;

    private TextView tvStartTime;
    private TextView tvStartTimeLabel;
    private LinearLayout colEndTime;
    private TextView tvEndTime;
    private TextView tvEndTimeLabel;
    private TextView tvAlertStatus;
    private LinearLayout layoutActionButtons;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private boolean isResolved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        initViews();
        setupListeners();

        // Verifica estado inicial via intent (padrão: em andamento)
        isResolved = getIntent().getBooleanExtra(EXTRA_IS_RESOLVED, false);
        setAlertResolved(isResolved);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        btnJustificar = findViewById(R.id.btnJustificar);

        tvStartTime = findViewById(R.id.tvStartTime);
        tvStartTimeLabel = findViewById(R.id.tvStartTimeLabel);
        colEndTime = findViewById(R.id.colEndTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvEndTimeLabel = findViewById(R.id.tvEndTimeLabel);
        tvAlertStatus = findViewById(R.id.tvAlertStatus);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);

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

        // Ação 1: Justificar alerta (Abre modal de Justificativa - Frame 204)
        btnJustificar.setOnClickListener(v -> {
            AlertJustificationDialog.show(this, (reason, description) -> {
                setAlertResolved(true);
                Toast.makeText(this, "Justificativa registrada com sucesso!", Toast.LENGTH_SHORT).show();
                // TODO: Enviar justificativa para a API
            });
        });

        // Toolbar inferior
        navCamaras.setOnClickListener(v -> {
            // TODO: Navegar para Câmaras
        });

        navAlertas.setOnClickListener(v -> {
            // Já está no fluxo de alertas
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

    private void setAlertResolved(boolean resolved) {
        this.isResolved = resolved;

        if (resolved) {
            // Estado Resolvido (Foto 2)
            colEndTime.setVisibility(View.VISIBLE);
            tvStartTime.setTextColor(Color.parseColor("#94A3B8")); // Hora inicial mais clara
            tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.primary_dark)); // Fim em destaque escuro
            tvAlertStatus.setText("Resolvido");
            tvAlertStatus.setTextColor(ContextCompat.getColor(this, R.color.alert_success));
            layoutActionButtons.setVisibility(View.GONE); // Botões ocultos
        } else {
            // Estado Em Andamento (Foto anterior)
            colEndTime.setVisibility(View.GONE);
            tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            tvAlertStatus.setText("Em andamento");
            tvAlertStatus.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
            layoutActionButtons.setVisibility(View.VISIBLE); // Botões visíveis
        }
    }
}
