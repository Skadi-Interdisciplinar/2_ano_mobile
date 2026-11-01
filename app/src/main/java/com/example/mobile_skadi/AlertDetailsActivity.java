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

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ALERT_ID = "EXTRA_ALERT_ID";
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
    private Integer alertId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);

        initViews();
        setupListeners();

        if (getIntent().hasExtra(EXTRA_ALERT_ID)) {
            int id = getIntent().getIntExtra(EXTRA_ALERT_ID, -1);
            if (id > 0) {
                alertId = id;
            }
        }

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
        if (btnJustificar != null) {
            btnJustificar.setVisibility(UserRoleManager.isEmployee() ? View.GONE : View.VISIBLE);
        }
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        // Voltar para a tela anterior
        btnBack.setOnClickListener(v -> finish());

        // Notificações e Perfil
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Ação 1: Justificar alerta (Abre modal de Justificativa - Frame 204)
        btnJustificar.setOnClickListener(v -> {
            AlertJustificationDialog.show(this, (reason, description) -> {
                if (alertId != null && alertId > 0) {
                    RetrofitClient.getApiService(this).recognizeAlert(alertId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            setAlertResolved(true);
                            Toast.makeText(AlertDetailsActivity.this, "Justificativa registrada com sucesso!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            setAlertResolved(true);
                            Toast.makeText(AlertDetailsActivity.this, "Justificativa registrada (modo offline)!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    setAlertResolved(true);
                    Toast.makeText(this, "Justificativa registrada com sucesso!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Toolbar inferior
        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
            finish();
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
            startActivity(new Intent(this, ReportsActivity.class));
            finish();
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
            finish();
        });
    }

    private void setAlertResolved(boolean resolved) {
        this.isResolved = resolved;

        if (resolved) {
            // Estado Resolvido
            colEndTime.setVisibility(View.VISIBLE);
            tvStartTime.setTextColor(Color.parseColor("#94A3B8")); // Hora inicial mais clara
            tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.primary_dark)); // Fim em destaque escuro
            tvAlertStatus.setText("Resolvido");
            tvAlertStatus.setTextColor(ContextCompat.getColor(this, R.color.alert_success));
            layoutActionButtons.setVisibility(View.GONE); // Botões ocultos
        } else {
            // Estado Em Andamento
            colEndTime.setVisibility(View.GONE);
            tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            tvAlertStatus.setText("Em andamento");
            tvAlertStatus.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
            layoutActionButtons.setVisibility(View.VISIBLE); // Botões visíveis
        }
    }
}
