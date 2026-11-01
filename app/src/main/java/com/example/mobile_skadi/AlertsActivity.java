package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        loadAlertsFromApi();
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

        // Detalhes dos alertas (Fallback padrão)
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
        });

        navRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
        });
    }

    private void loadAlertsFromApi() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        apiService.getAlerts(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bindAlertsFromApi(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                // Silencioso: fallback local ativo
            }
        });
    }

    private void bindAlertsFromApi(List<AlertResponse> alerts) {
        if (alerts.size() > 0) {
            AlertResponse a0 = alerts.get(0);
            cardAlertCritical.setOnClickListener(v -> {
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, a0.getId());
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, "RESOLVED".equalsIgnoreCase(a0.getStatus()) || "RECOGNIZED".equalsIgnoreCase(a0.getStatus()));
                startActivity(intent);
            });
        }

        if (alerts.size() > 1) {
            AlertResponse a1 = alerts.get(1);
            cardAlertWarning.setOnClickListener(v -> {
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, a1.getId());
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, "RESOLVED".equalsIgnoreCase(a1.getStatus()) || "RECOGNIZED".equalsIgnoreCase(a1.getStatus()));
                startActivity(intent);
            });
        }

        if (alerts.size() > 2) {
            AlertResponse a2 = alerts.get(2);
            cardAlertStable.setOnClickListener(v -> {
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, a2.getId());
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, true);
                startActivity(intent);
            });
        }
    }
}
