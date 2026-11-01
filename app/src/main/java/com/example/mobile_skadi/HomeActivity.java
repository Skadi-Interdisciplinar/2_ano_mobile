package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.data.SkadiDatabaseHelper;
import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.google.android.material.card.MaterialCardView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L; // 5 minutos (sincronizado com o Arduino)

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadHomeData();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private TextView btnVerTodosAlertas;
    private MaterialCardView cardAlertaCritico;
    private MaterialCardView btnMapaUnidades;
    private MaterialCardView btnNiveisAlerta;

    private TextView tvGreeting;
    private TextView tvHomeCountAlertasDia;
    private TextView tvHomeCountCamarasEstaveis;
    private TextView tvHomeCountAlertasCriticos;
    private TextView tvHomeTempMedia;

    private TextView tvHomeAlertBadge;
    private TextView tvHomeAlertTitle;
    private TextView tvHomeAlertRange;
    private TextView tvHomeAlertTimeElapsed;
    private TextView tvHomeAlertTemp;
    private TextView tvHomeAlertTime;

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
        setContentView(R.layout.activity_home);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreeting();
        loadHomeData();
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
        btnVerTodosAlertas = findViewById(R.id.btnVerTodosAlertas);
        cardAlertaCritico = findViewById(R.id.cardAlertaCritico);
        btnMapaUnidades = findViewById(R.id.btnMapaUnidades);
        btnNiveisAlerta = findViewById(R.id.btnNiveisAlerta);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvHomeCountAlertasDia = findViewById(R.id.tvHomeCountAlertasDia);
        tvHomeCountCamarasEstaveis = findViewById(R.id.tvHomeCountCamarasEstaveis);
        tvHomeCountAlertasCriticos = findViewById(R.id.tvHomeCountAlertasCriticos);
        tvHomeTempMedia = findViewById(R.id.tvHomeTempMedia);

        tvHomeAlertBadge = findViewById(R.id.tvHomeAlertBadge);
        tvHomeAlertTitle = findViewById(R.id.tvHomeAlertTitle);
        tvHomeAlertRange = findViewById(R.id.tvHomeAlertRange);
        tvHomeAlertTimeElapsed = findViewById(R.id.tvHomeAlertTimeElapsed);
        tvHomeAlertTemp = findViewById(R.id.tvHomeAlertTemp);
        tvHomeAlertTime = findViewById(R.id.tvHomeAlertTime);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);

        updateGreeting();

        View layoutManagerExtraSection = findViewById(R.id.layoutManagerExtraSection);
        if (layoutManagerExtraSection != null) {
            layoutManagerExtraSection.setVisibility(UserRoleManager.isEmployee() ? View.GONE : View.VISIBLE);
        }
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void updateGreeting() {
        if (tvGreeting != null) {
            String name = SessionManager.getInstance(this).getName();
            if (name == null || name.trim().isEmpty()) {
                name = SessionManager.getInstance(this).getUsername();
            }
            if (name != null && !name.trim().isEmpty()) {
                String firstName = name.trim().split(" ")[0];
                tvGreeting.setText("Olá, " + firstName + "!");
            } else {
                tvGreeting.setText("Olá, " + UserRoleManager.getFirstNameForGreeting() + "!");
            }
        }
    }

    private void setupListeners() {
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnVerTodosAlertas.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));

        cardAlertaCritico.setOnClickListener(v -> startActivity(new Intent(this, AlertDetailsActivity.class)));
        btnMapaUnidades.setOnClickListener(v -> startActivity(new Intent(this, CdMapActivity.class)));
        btnNiveisAlerta.setOnClickListener(v -> AlertLevelsDialog.show(this));

        navCamaras.setOnClickListener(v -> startActivity(new Intent(this, CamarasActivity.class)));
        navAlertas.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        navInicio.setOnClickListener(v -> { /* Já na Home */ });
        navRelatorios.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        navTrimmy.setOnClickListener(v -> startActivity(new Intent(this, TrimmyActivity.class)));
    }

    private void loadHomeData() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        SkadiDatabaseHelper dbHelper = SkadiDatabaseHelper.getInstance(this);

        // 1. Carrega câmaras
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
                loadReadings(apiService, dbHelper);
            }

            @Override
            public void onFailure(Call<List<ColdRoomResponse>> call, Throwable t) {
                coldRoomsMap.clear();
                coldRoomsMap.putAll(dbHelper.getCachedColdRoomsMap());
                loadReadings(apiService, dbHelper);
            }
        });
    }

    private void loadReadings(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
        // 2. Carrega leituras de temperatura
        apiService.getAllReadings().enqueue(new Callback<List<TemperatureReadingResponse>>() {
            @Override
            public void onResponse(Call<List<TemperatureReadingResponse>> call, Response<List<TemperatureReadingResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.saveReadings(response.body());
                    processReadings(response.body());
                } else {
                    processReadings(dbHelper.getCachedReadings());
                }
                loadAlerts(apiService, dbHelper);
            }

            @Override
            public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {
                processReadings(dbHelper.getCachedReadings());
                loadAlerts(apiService, dbHelper);
            }
        });
    }

    private void processReadings(List<TemperatureReadingResponse> readings) {
        readingsMap.clear();
        BigDecimal totalTemp = BigDecimal.ZERO;
        int count = 0;
        for (TemperatureReadingResponse tr : readings) {
            if (tr.getColdRoomId() != null) {
                readingsMap.put(tr.getColdRoomId(), tr);
            }
            if (tr.getTemperature() != null) {
                totalTemp = totalTemp.add(tr.getTemperature());
                count++;
            }
        }
        if (count > 0 && tvHomeTempMedia != null) {
            BigDecimal avg = totalTemp.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
            tvHomeTempMedia.setText(String.format(Locale.getDefault(), "%.1f °C", avg));
        }
    }

    private void loadAlerts(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
        // 3. Carrega alertas
        apiService.getAlerts(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.saveAlerts(response.body());
                    bindAlerts(response.body());
                } else {
                    List<AlertResponse> cached = dbHelper.getCachedAlerts();
                    if (!cached.isEmpty()) bindAlerts(cached);
                }
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                List<AlertResponse> cached = dbHelper.getCachedAlerts();
                if (!cached.isEmpty()) bindAlerts(cached);
            }
        });
    }

    private void bindAlerts(List<AlertResponse> alerts) {
        int totalAtivos = 0;
        int totalCriticos = 0;
        AlertResponse topAlert = null;

        for (AlertResponse alert : alerts) {
            boolean isResolved = "RESOLVIDO".equalsIgnoreCase(alert.getStatus()) ||
                    "RESOLVED".equalsIgnoreCase(alert.getStatus()) ||
                    "RECOGNIZED".equalsIgnoreCase(alert.getStatus());
            if (!isResolved) {
                totalAtivos++;
                String sev = alert.getSeverityLevel() != null ? alert.getSeverityLevel().toUpperCase() : "";
                if (sev.contains("CRITIC")) {
                    totalCriticos++;
                    if (topAlert == null) topAlert = alert;
                } else if (topAlert == null) {
                    topAlert = alert;
                }
            }
        }

        if (tvHomeCountAlertasDia != null) tvHomeCountAlertasDia.setText(String.valueOf(totalAtivos));
        if (tvHomeCountAlertasCriticos != null) tvHomeCountAlertasCriticos.setText(String.valueOf(totalCriticos));

        int totalCamaras = coldRoomsMap.size();
        int estaveis = Math.max(0, totalCamaras - totalAtivos);
        if (tvHomeCountCamarasEstaveis != null) {
            tvHomeCountCamarasEstaveis.setText(String.valueOf(estaveis));
        }

        if (topAlert != null) {
            Long roomId = topAlert.getColdRoomId() != null ? topAlert.getColdRoomId().longValue() : null;
            ColdRoomResponse cr = roomId != null ? coldRoomsMap.get(roomId) : null;
            TemperatureReadingResponse reading = roomId != null ? readingsMap.get(roomId) : null;

            if (tvHomeAlertTitle != null) {
                if (cr != null && cr.getLocation() != null && !cr.getLocation().isEmpty()) {
                    tvHomeAlertTitle.setText(cr.getLocation());
                } else if (roomId != null) {
                    tvHomeAlertTitle.setText("Câmara " + roomId);
                }
            }

            if (tvHomeAlertRange != null && cr != null && cr.getMinimumTemperature() != null && cr.getMaximumTemperature() != null) {
                tvHomeAlertRange.setText("Mín. " + cr.getMinimumTemperature() + "°C - Máx. " + cr.getMaximumTemperature() + "°C");
            }

            if (tvHomeAlertTemp != null && reading != null && reading.getTemperature() != null) {
                tvHomeAlertTemp.setText(String.format(Locale.getDefault(), "%.1f°", reading.getTemperature()));
            }

            if (tvHomeAlertTime != null && topAlert.getCreatedAt() != null) {
                tvHomeAlertTime.setText(formatTime(topAlert.getCreatedAt()));
            }

            final AlertResponse chosenAlert = topAlert;
            cardAlertaCritico.setOnClickListener(v -> {
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, chosenAlert.getId());
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
                startActivity(intent);
            });
            cardAlertaCritico.setVisibility(View.VISIBLE);
        } else {
            // Se não há alertas ativos
            if (cardAlertaCritico != null) cardAlertaCritico.setVisibility(View.GONE);
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
}
