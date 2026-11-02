package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.data.SkadiDatabaseHelper;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.google.android.material.card.MaterialCardView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertsActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L; // 5 minutos (sincronizado com leituras de sensores/Arduino)

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadAlertsFromApi();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private MaterialCardView cardAlertCritical;
    private MaterialCardView cardAlertWarning;
    private MaterialCardView cardAlertStable;
    private TextView btnEntendaNiveis;
    private TextView tvEmptyAlerts;

    // Resumo de contadores
    private TextView tvCountAtivos;
    private TextView tvCountCriticos;
    private TextView tvCountResolvidos;

    // Card Crítico
    private TextView tvAlertBadgeCritical;
    private TextView tvAlertTimeCritical;
    private TextView tvAlertTempCritical;
    private TextView tvAlertTitleCritical;
    private TextView tvAlertRangeCritical;

    // Card Atenção
    private TextView tvAlertBadgeWarning;
    private TextView tvAlertTimeWarning;
    private TextView tvAlertTempWarning;
    private TextView tvAlertTitleWarning;
    private TextView tvAlertRangeWarning;

    // Card Estável
    private TextView tvAlertBadgeStable;
    private TextView tvAlertTimeStable;
    private TextView tvAlertTempStable;
    private TextView tvAlertTitleStable;
    private TextView tvAlertRangeStable;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private final Map<Long, ColdRoomResponse> coldRoomsMap = new HashMap<>();
    private final Map<Long, TemperatureReadingResponse> readingsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.mobile_skadi.utils.AvatarManager.applyAvatar(this, btnProfileAvatar);
        loadAlertsFromApi();
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        cardAlertCritical = findViewById(R.id.cardAlertCritical);
        cardAlertWarning = findViewById(R.id.cardAlertWarning);
        cardAlertStable = findViewById(R.id.cardAlertStable);
        btnEntendaNiveis = findViewById(R.id.btnEntendaNiveis);
        tvEmptyAlerts = findViewById(R.id.tvEmptyAlerts);

        tvCountAtivos = findViewById(R.id.tvCountAtivos);
        tvCountCriticos = findViewById(R.id.tvCountCriticos);
        tvCountResolvidos = findViewById(R.id.tvCountResolvidos);

        tvAlertBadgeCritical = findViewById(R.id.tvAlertBadgeCritical);
        tvAlertTimeCritical = findViewById(R.id.tvAlertTimeCritical);
        tvAlertTempCritical = findViewById(R.id.tvAlertTempCritical);
        tvAlertTitleCritical = findViewById(R.id.tvAlertTitleCritical);
        tvAlertRangeCritical = findViewById(R.id.tvAlertRangeCritical);

        tvAlertBadgeWarning = findViewById(R.id.tvAlertBadgeWarning);
        tvAlertTimeWarning = findViewById(R.id.tvAlertTimeWarning);
        tvAlertTempWarning = findViewById(R.id.tvAlertTempWarning);
        tvAlertTitleWarning = findViewById(R.id.tvAlertTitleWarning);
        tvAlertRangeWarning = findViewById(R.id.tvAlertRangeWarning);

        tvAlertBadgeStable = findViewById(R.id.tvAlertBadgeStable);
        tvAlertTimeStable = findViewById(R.id.tvAlertTimeStable);
        tvAlertTempStable = findViewById(R.id.tvAlertTempStable);
        tvAlertTitleStable = findViewById(R.id.tvAlertTitleStable);
        tvAlertRangeStable = findViewById(R.id.tvAlertRangeStable);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

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

        btnEntendaNiveis.setOnClickListener(v -> AlertLevelsDialog.show(this));

        navCamaras.setOnClickListener(v -> startActivity(new Intent(this, CamarasActivity.class)));
        navAlertas.setOnClickListener(v -> { /* Já em Alertas */ });
        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        navRelatorios.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        navTrimmy.setOnClickListener(v -> startActivity(new Intent(this, TrimmyActivity.class)));
    }

    private void loadAlertsFromApi() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        SkadiDatabaseHelper dbHelper = SkadiDatabaseHelper.getInstance(this);

        // 1. Carrega as câmaras para cruzar nome e faixa de temperatura
        apiService.getColdRooms().enqueue(new Callback<List<ColdRoomResponse>>() {
            @Override
            public void onResponse(Call<List<ColdRoomResponse>> call, Response<List<ColdRoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    coldRoomsMap.clear();
                    for (ColdRoomResponse cr : response.body()) {
                        coldRoomsMap.put(cr.getId(), cr);
                    }
                    dbHelper.saveColdRooms(response.body());
                } else {
                    coldRoomsMap.clear();
                    coldRoomsMap.putAll(dbHelper.getCachedColdRoomsMap());
                }
                loadReadingsAndAlerts(apiService, dbHelper);
            }

            @Override
            public void onFailure(Call<List<ColdRoomResponse>> call, Throwable t) {
                coldRoomsMap.clear();
                coldRoomsMap.putAll(dbHelper.getCachedColdRoomsMap());
                loadReadingsAndAlerts(apiService, dbHelper);
            }
        });
    }

    private void loadReadingsAndAlerts(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
        // 2. Carrega as leituras de temperatura atuais
        apiService.getAllReadings().enqueue(new Callback<List<TemperatureReadingResponse>>() {
            @Override
            public void onResponse(Call<List<TemperatureReadingResponse>> call, Response<List<TemperatureReadingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    readingsMap.clear();
                    for (TemperatureReadingResponse tr : response.body()) {
                        if (tr.getColdRoomId() != null) {
                            readingsMap.put(tr.getColdRoomId(), tr);
                        }
                    }
                    dbHelper.saveReadings(response.body());
                } else {
                    readingsMap.clear();
                    readingsMap.putAll(dbHelper.getCachedReadingsMap());
                }
                fetchAlerts(apiService, dbHelper);
            }

            @Override
            public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {
                readingsMap.clear();
                readingsMap.putAll(dbHelper.getCachedReadingsMap());
                fetchAlerts(apiService, dbHelper);
            }
        });
    }

    private void fetchAlerts(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
        apiService.getAlerts(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.saveAlerts(response.body());
                    bindAlertsFromApi(response.body());
                } else {
                    loadAlertsFromCache(dbHelper);
                }
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                loadAlertsFromCache(dbHelper);
            }
        });
    }

    private void loadAlertsFromCache(SkadiDatabaseHelper dbHelper) {
        List<AlertResponse> cached = dbHelper.getCachedAlerts();
        if (!cached.isEmpty()) {
            bindAlertsFromApi(cached);
            Toast.makeText(this, "Modo offline: exibindo alertas do SQLite.", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindAlertsFromApi(List<AlertResponse> alerts) {
        int totalAtivos = 0;
        int totalCriticos = 0;
        int totalResolvidos = 0;

        for (AlertResponse alert : alerts) {
            boolean isResolved = "RESOLVIDO".equalsIgnoreCase(alert.getStatus()) ||
                    "RESOLVED".equalsIgnoreCase(alert.getStatus()) ||
                    "RECOGNIZED".equalsIgnoreCase(alert.getStatus());
            if (isResolved) {
                totalResolvidos++;
            } else {
                totalAtivos++;
                String sev = alert.getSeverityLevel() != null ? alert.getSeverityLevel().toUpperCase() : "";
                if (sev.contains("CRITIC") || sev.contains("URGENT")) {
                    totalCriticos++;
                }
            }
        }

        if (tvCountAtivos != null) tvCountAtivos.setText(String.valueOf(totalAtivos));
        if (tvCountCriticos != null) tvCountCriticos.setText(String.valueOf(totalCriticos));
        if (tvCountResolvidos != null) tvCountResolvidos.setText(String.valueOf(totalResolvidos));

        if (tvEmptyAlerts != null) {
            tvEmptyAlerts.setVisibility(alerts.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (!alerts.isEmpty()) {
            AlertResponse a0 = alerts.get(0);
            bindAlertCard(cardAlertCritical, tvAlertBadgeCritical, tvAlertTimeCritical,
                    tvAlertTempCritical, tvAlertTitleCritical, tvAlertRangeCritical, a0);
            cardAlertCritical.setVisibility(View.VISIBLE);
        } else {
            if (cardAlertCritical != null) cardAlertCritical.setVisibility(View.GONE);
        }

        if (alerts.size() > 1) {
            AlertResponse a1 = alerts.get(1);
            bindAlertCard(cardAlertWarning, tvAlertBadgeWarning, tvAlertTimeWarning,
                    tvAlertTempWarning, tvAlertTitleWarning, tvAlertRangeWarning, a1);
            cardAlertWarning.setVisibility(View.VISIBLE);
        } else {
            if (cardAlertWarning != null) cardAlertWarning.setVisibility(View.GONE);
        }

        if (alerts.size() > 2) {
            AlertResponse a2 = alerts.get(2);
            bindAlertCard(cardAlertStable, tvAlertBadgeStable, tvAlertTimeStable,
                    tvAlertTempStable, tvAlertTitleStable, tvAlertRangeStable, a2);
            cardAlertStable.setVisibility(View.VISIBLE);
        } else {
            if (cardAlertStable != null) cardAlertStable.setVisibility(View.GONE);
        }
    }

    private void bindAlertCard(MaterialCardView card, TextView tvBadge, TextView tvTime,
                               TextView tvTemp, TextView tvTitle, TextView tvRange,
                               AlertResponse alert) {
        if (card == null) return;

        Long roomId = alert.getColdRoomId() != null ? alert.getColdRoomId().longValue() : null;
        ColdRoomResponse cr = roomId != null ? coldRoomsMap.get(roomId) : null;
        TemperatureReadingResponse reading = roomId != null ? readingsMap.get(roomId) : null;

        // Título da câmara
        if (tvTitle != null) {
            if (cr != null && cr.getLocation() != null && !cr.getLocation().isEmpty()) {
                tvTitle.setText(cr.getLocation());
            } else if (roomId != null) {
                tvTitle.setText("Câmara " + roomId);
            }
        }

        // Faixa de temperatura
        if (tvRange != null) {
            if (cr != null && cr.getMinimumTemperature() != null && cr.getMaximumTemperature() != null) {
                tvRange.setText("Mín. " + cr.getMinimumTemperature() + "°C | Máx. " + cr.getMaximumTemperature() + "°C");
            }
        }

        // Temperatura medida
        if (tvTemp != null) {
            if (reading != null && reading.getTemperature() != null) {
                tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", reading.getTemperature()));
            }
        }

        // Horário do alerta
        if (tvTime != null && alert.getCreatedAt() != null) {
            tvTime.setText(formatAlertTime(alert.getCreatedAt()));
        }

        // Badge
        if (tvBadge != null) {
            String sev = alert.getSeverityLevel() != null ? alert.getSeverityLevel().toUpperCase() : "";
            if (sev.contains("CRITIC") || sev.contains("URGENT")) {
                tvBadge.setText("● Crítico");
            } else if (sev.contains("BAIX") || sev.contains("ATEN")) {
                tvBadge.setText("● Atenção");
            } else {
                tvBadge.setText("● " + alert.getStatus());
            }
        }

        // Clique para detalhes
        boolean isResolved = "RESOLVIDO".equalsIgnoreCase(alert.getStatus()) ||
                "RESOLVED".equalsIgnoreCase(alert.getStatus()) ||
                "RECOGNIZED".equalsIgnoreCase(alert.getStatus());
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, alert.getId());
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, isResolved);
            startActivity(intent);
        });
    }

    private String formatAlertTime(String rawDate) {
        try {
            if (rawDate == null || rawDate.isEmpty()) return "";
            if (rawDate.contains("T")) {
                LocalDateTime ldt = LocalDateTime.parse(rawDate);
                return ldt.format(DateTimeFormatter.ofPattern("HH'h'mm"));
            }
            return rawDate;
        } catch (Exception e) {
            if (rawDate.length() >= 16 && rawDate.contains("T")) {
                return rawDate.substring(11, 16).replace(":", "h");
            }
            return rawDate;
        }
    }
}
