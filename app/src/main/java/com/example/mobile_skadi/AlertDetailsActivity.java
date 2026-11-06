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
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

    private TextView tvAlertDetailTemp;
    private TextView tvAlertDetailLimit;
    private TextView tvAlertDetailCamaraName;
    private TextView tvAlertDetailUnitName;
    private TextView tvAlertHeaderUnit;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private boolean isResolved = false;
    private Integer alertId = null;
    private Long currentRoomId = null;

    @Override
    protected void onResume() {
        super.onResume();
        com.example.mobile_skadi.utils.AvatarManager.applyAvatar(this, btnProfileAvatar);
    }

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

        isResolved = getIntent().getBooleanExtra(EXTRA_IS_RESOLVED, false);
        setAlertResolved(isResolved);

        loadAlertDetails();
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

        tvAlertDetailTemp = findViewById(R.id.tvAlertDetailTemp);
        tvAlertDetailLimit = findViewById(R.id.tvAlertDetailLimit);
        tvAlertDetailCamaraName = findViewById(R.id.tvAlertDetailCamaraName);
        tvAlertDetailUnitName = findViewById(R.id.tvAlertDetailUnitName);
        tvAlertHeaderUnit = findViewById(R.id.tvAlertHeaderUnit);
        if (tvAlertHeaderUnit != null) tvAlertHeaderUnit.setText(com.example.mobile_skadi.network.SessionManager.getInstance(this).getUnit());

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);

        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnJustificar.setOnClickListener(v -> {
            if (isResolved) {
                Toast.makeText(this, "Este alerta já foi resolvido e seu atendimento está concluído.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Alerta pendente -> abre o diálogo de justificativa
            AlertJustificationDialog.show(this, (reason, description) -> {
                if (alertId != null && alertId > 0) {
                    RetrofitClient.getApiService(this).recognizeAlert(alertId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            handleJustificationSuccess();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            handleJustificationSuccess();
                        }
                    });
                } else {
                    handleJustificationSuccess();
                }
            });
        });

        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
            finish();
        });

        navAlertas.setOnClickListener(v -> finish());

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

    private void handleJustificationSuccess() {
        setAlertResolved(true);
        if (tvEndTime != null) {
            tvEndTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        if (btnJustificar != null) {
            btnJustificar.setText("Atendimento finalizado");
            btnJustificar.setEnabled(false);
            btnJustificar.setAlpha(0.6f);
        }
        Toast.makeText(AlertDetailsActivity.this, "Alerta justificado e atendimento registrado!", Toast.LENGTH_SHORT).show();

        // Atualiza a temperatura atual imediatamente
        if (currentRoomId != null) {
            RetrofitClient.getApiService(this).getCurrentTemperature(currentRoomId).enqueue(new Callback<TemperatureReadingResponse>() {
                @Override
                public void onResponse(Call<TemperatureReadingResponse> call, Response<TemperatureReadingResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getTemperature() != null) {
                        if (tvAlertDetailTemp != null) {
                            tvAlertDetailTemp.setText(String.format(Locale.getDefault(), "%.1f°C", response.body().getTemperature()));
                        }
                    }
                }

                @Override
                public void onFailure(Call<TemperatureReadingResponse> call, Throwable t) {}
            });
        }
    }

    private void loadAlertDetails() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);

        // Limpa campos para não exibir dados mockados do layout
        if (tvStartTime != null) tvStartTime.setText("--:--");
        if (tvEndTime != null) tvEndTime.setText("--:--");

        // 1. Busca primeiro em alertas ativos
        apiService.getAlerts(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                AlertResponse current = null;
                if (response.isSuccessful() && response.body() != null) {
                    for (AlertResponse a : response.body()) {
                        if (alertId != null && alertId.equals(a.getId())) {
                            current = a;
                            break;
                        }
                    }
                }

                if (current != null) {
                    bindAlert(current, apiService);
                } else {
                    // 2. Não achou nos ativos: busca no histórico (resolvidos)
                    loadFromHistory(apiService);
                }
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                loadFromHistory(apiService);
            }
        });
    }

    private void loadFromHistory(SkadiApiService apiService) {
        apiService.getAlertsHistory(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> callH, Response<List<AlertResponse>> responseH) {
                if (responseH.isSuccessful() && responseH.body() != null) {
                    AlertResponse current = null;
                    for (AlertResponse a : responseH.body()) {
                        if (alertId != null && alertId.equals(a.getId())) {
                            current = a;
                            break;
                        }
                    }
                    if (current == null && !responseH.body().isEmpty()) {
                        current = responseH.body().get(0);
                    }
                    if (current != null) {
                        bindAlert(current, apiService);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> callH, Throwable t) {}
        });
    }

    private void bindAlert(AlertResponse alert, SkadiApiService apiService) {
        if (alert.getCreatedAt() != null && tvStartTime != null) {
            tvStartTime.setText(formatTime(alert.getCreatedAt()));
        }

        boolean resolved = "RESOLVIDO".equalsIgnoreCase(alert.getStatus()) ||
                "RESOLVED".equalsIgnoreCase(alert.getStatus()) ||
                "RECOGNIZED".equalsIgnoreCase(alert.getStatus());
        setAlertResolved(resolved);

        if (resolved) {
            if (tvEndTime != null) {
                tvEndTime.setText(formatTime(alert.getCreatedAt()));
            }
            if (btnJustificar != null) {
                btnJustificar.setText("Atendimento finalizado");
                btnJustificar.setEnabled(false);
                btnJustificar.setAlpha(0.6f);
            }
        } else {
            if (btnJustificar != null) {
                btnJustificar.setText("Assumir / Justificar");
                btnJustificar.setEnabled(true);
                btnJustificar.setAlpha(1.0f);
            }
        }

        if (alert.getColdRoomId() != null) {
            currentRoomId = alert.getColdRoomId().longValue();
            apiService.getColdRoomById(currentRoomId).enqueue(new Callback<ColdRoomResponse>() {
                @Override
                public void onResponse(Call<ColdRoomResponse> call, Response<ColdRoomResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ColdRoomResponse cr = response.body();
                        if (tvAlertDetailCamaraName != null) {
                            String name = (cr.getLocation() != null && !cr.getLocation().isEmpty())
                                    ? cr.getLocation() : (cr.getModel() != null ? cr.getModel() : "Câmara " + cr.getId());
                            tvAlertDetailCamaraName.setText(name);
                        }
                        if (tvAlertDetailLimit != null && cr.getMaximumTemperature() != null) {
                            tvAlertDetailLimit.setText(String.format(Locale.getDefault(), "Faixa: %.1f°C a %.1f°C",
                                    cr.getMinimumTemperature() != null ? cr.getMinimumTemperature() : -2.0,
                                    cr.getMaximumTemperature()));
                        }
                        if (tvAlertDetailUnitName != null && cr.getDistributionCenterName() != null) {
                            if (tvAlertHeaderUnit != null) tvAlertHeaderUnit.setText(cr.getDistributionCenterName());
                            tvAlertDetailUnitName.setText(cr.getDistributionCenterName());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ColdRoomResponse> call, Throwable t) {}
            });

            apiService.getCurrentTemperature(currentRoomId).enqueue(new Callback<TemperatureReadingResponse>() {
                @Override
                public void onResponse(Call<TemperatureReadingResponse> call, Response<TemperatureReadingResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getTemperature() != null) {
                        if (tvAlertDetailTemp != null) {
                            tvAlertDetailTemp.setText(String.format(Locale.getDefault(), "%.1f°C", response.body().getTemperature()));
                        }
                    }
                }

                @Override
                public void onFailure(Call<TemperatureReadingResponse> call, Throwable t) {}
            });
        }
    }

    private String formatTime(String rawDate) {
        try {
            if (rawDate == null || rawDate.isEmpty()) return "";
            if (rawDate.contains("T")) {
                LocalDateTime ldt = LocalDateTime.parse(rawDate);
                return ldt.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            return rawDate;
        } catch (Exception e) {
            if (rawDate.length() >= 16 && rawDate.contains("T")) {
                return rawDate.substring(11, 16);
            }
            return rawDate;
        }
    }

    private void setAlertResolved(boolean resolved) {
        this.isResolved = resolved;

        if (resolved) {
            colEndTime.setVisibility(View.VISIBLE);
            tvStartTime.setTextColor(Color.parseColor("#94A3B8"));
            tvEndTime.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            tvAlertStatus.setText("Resolvido");
            tvAlertStatus.setTextColor(ContextCompat.getColor(this, R.color.alert_success));
            layoutActionButtons.setVisibility(View.VISIBLE);
        } else {
            colEndTime.setVisibility(View.GONE);
            tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            tvAlertStatus.setText("Em andamento");
            tvAlertStatus.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
            layoutActionButtons.setVisibility(View.VISIBLE);
        }
    }
}
