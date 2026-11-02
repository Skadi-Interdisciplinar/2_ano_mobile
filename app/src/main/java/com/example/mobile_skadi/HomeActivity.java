package com.example.mobile_skadi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mobile_skadi.data.SkadiDatabaseHelper;
import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.example.mobile_skadi.notification.AppNotificationManager;
import com.example.mobile_skadi.utils.AvatarManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L; // 5 minutos

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadHomeData();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    // Shared Header
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    // Manager Views
    private LinearLayout layoutHomeManager;
    private TextView tvManagerGreeting;
    private TextView tvManagerSubtitle;
    private TextView tvManagerLastUpdated;

    private MaterialCardView cardManagerCriticos;
    private TextView tvManagerCountCriticos;
    private MaterialCardView cardManagerSlaVencido;
    private TextView tvManagerCountSlaVencido;
    private MaterialCardView cardManagerForaFaixa;
    private TextView tvManagerCountForaFaixa;
    private MaterialCardView cardManagerSemResponsavel;
    private TextView tvManagerCountSemResponsavel;

    private TextView tvManagerCamarasStateLine1;
    private TextView tvManagerCamarasStateLine2;
    private TextView btnManagerVerTodasPrioridades;
    private LinearLayout containerManagerPriorities;
    private TextView tvManagerNoPriorities;

    private TemperatureChartView temperatureChart;
    private TextView tvManagerChartTitle;
    private TextView tvManagerChartPeriod;
    private MaterialCardView btnMapaUnidades;
    private MaterialCardView btnNiveisAlerta;

    // Employee Views
    private LinearLayout layoutHomeEmployee;
    private TextView tvEmployeeGreeting;
    private TextView tvEmployeeSubtitle;
    private TextView tvEmployeeLastUpdated;

    private MaterialCardView cardEmployeeAtribuidos;
    private TextView tvEmployeeCountAtribuidos;
    private MaterialCardView cardEmployeeCriticos;
    private TextView tvEmployeeCountCriticos;
    private MaterialCardView cardEmployeeEmAtendimento;
    private TextView tvEmployeeCountEmAtendimento;
    private MaterialCardView cardEmployeeSlaVencido;
    private TextView tvEmployeeCountSlaVencido;

    private MaterialCardView layoutEmployeeSlaAlert;
    private TextView tvEmployeeCountProximosVencimento;

    private MaterialCardView cardEmployeeNextAction;
    private TextView tvEmployeeNextBadge;
    private TextView tvEmployeeNextStatusBadge;
    private TextView tvEmployeeNextTimeElapsed;
    private TextView tvEmployeeNextCamaraTitle;
    private TextView tvEmployeeNextTemp;
    private TextView tvEmployeeNextTempRange;
    private TextView tvEmployeeNextSlaInfo;
    private MaterialButton btnEmployeeNextAction;

    private TextView tvEmployeeQueueCount;
    private LinearLayout containerEmployeeQueue;
    private TextView tvEmployeeEmptyQueue;
    private MaterialCardView btnEmployeeNiveisAlerta;

    // Shared Bottom Navigation
    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private ActivityResultLauncher<String> notifPermissionLauncher;

    private final Map<Long, ColdRoomResponse> coldRoomsMap = new HashMap<>();
    private final Map<Long, TemperatureReadingResponse> latestReadingsMap = new HashMap<>();
    private final List<TemperatureReadingResponse> allReadingsHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        notifPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        AppNotificationManager.initChannels(this);
                    }
                }
        );

        AppNotificationManager.initChannels(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AvatarManager.applyAvatar(this, btnProfileAvatar);
        applyRoleLayout();
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
        // Header
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        // Manager
        layoutHomeManager = findViewById(R.id.layoutHomeManager);
        tvManagerGreeting = findViewById(R.id.tvManagerGreeting);
        tvManagerSubtitle = findViewById(R.id.tvManagerSubtitle);
        tvManagerLastUpdated = findViewById(R.id.tvManagerLastUpdated);

        cardManagerCriticos = findViewById(R.id.cardManagerCriticos);
        tvManagerCountCriticos = findViewById(R.id.tvManagerCountCriticos);
        cardManagerSlaVencido = findViewById(R.id.cardManagerSlaVencido);
        tvManagerCountSlaVencido = findViewById(R.id.tvManagerCountSlaVencido);
        cardManagerForaFaixa = findViewById(R.id.cardManagerForaFaixa);
        tvManagerCountForaFaixa = findViewById(R.id.tvManagerCountForaFaixa);
        cardManagerSemResponsavel = findViewById(R.id.cardManagerSemResponsavel);
        tvManagerCountSemResponsavel = findViewById(R.id.tvManagerCountSemResponsavel);

        tvManagerCamarasStateLine1 = findViewById(R.id.tvManagerCamarasStateLine1);
        tvManagerCamarasStateLine2 = findViewById(R.id.tvManagerCamarasStateLine2);
        btnManagerVerTodasPrioridades = findViewById(R.id.btnManagerVerTodasPrioridades);
        containerManagerPriorities = findViewById(R.id.containerManagerPriorities);
        tvManagerNoPriorities = findViewById(R.id.tvManagerNoPriorities);

        temperatureChart = findViewById(R.id.temperatureChart);
        tvManagerChartTitle = findViewById(R.id.tvManagerChartTitle);
        tvManagerChartPeriod = findViewById(R.id.tvManagerChartPeriod);
        btnMapaUnidades = findViewById(R.id.btnMapaUnidades);
        btnNiveisAlerta = findViewById(R.id.btnNiveisAlerta);

        // Employee
        layoutHomeEmployee = findViewById(R.id.layoutHomeEmployee);
        tvEmployeeGreeting = findViewById(R.id.tvEmployeeGreeting);
        tvEmployeeSubtitle = findViewById(R.id.tvEmployeeSubtitle);
        tvEmployeeLastUpdated = findViewById(R.id.tvEmployeeLastUpdated);

        cardEmployeeAtribuidos = findViewById(R.id.cardEmployeeAtribuidos);
        tvEmployeeCountAtribuidos = findViewById(R.id.tvEmployeeCountAtribuidos);
        cardEmployeeCriticos = findViewById(R.id.cardEmployeeCriticos);
        tvEmployeeCountCriticos = findViewById(R.id.tvEmployeeCountCriticos);
        cardEmployeeEmAtendimento = findViewById(R.id.cardEmployeeEmAtendimento);
        tvEmployeeCountEmAtendimento = findViewById(R.id.tvEmployeeCountEmAtendimento);
        cardEmployeeSlaVencido = findViewById(R.id.cardEmployeeSlaVencido);
        tvEmployeeCountSlaVencido = findViewById(R.id.tvEmployeeCountSlaVencido);

        layoutEmployeeSlaAlert = findViewById(R.id.layoutEmployeeSlaAlert);
        tvEmployeeCountProximosVencimento = findViewById(R.id.tvEmployeeCountProximosVencimento);

        cardEmployeeNextAction = findViewById(R.id.cardEmployeeNextAction);
        tvEmployeeNextBadge = findViewById(R.id.tvEmployeeNextBadge);
        tvEmployeeNextStatusBadge = findViewById(R.id.tvEmployeeNextStatusBadge);
        tvEmployeeNextTimeElapsed = findViewById(R.id.tvEmployeeNextTimeElapsed);
        tvEmployeeNextCamaraTitle = findViewById(R.id.tvEmployeeNextCamaraTitle);
        tvEmployeeNextTemp = findViewById(R.id.tvEmployeeNextTemp);
        tvEmployeeNextTempRange = findViewById(R.id.tvEmployeeNextTempRange);
        tvEmployeeNextSlaInfo = findViewById(R.id.tvEmployeeNextSlaInfo);
        btnEmployeeNextAction = findViewById(R.id.btnEmployeeNextAction);

        tvEmployeeQueueCount = findViewById(R.id.tvEmployeeQueueCount);
        containerEmployeeQueue = findViewById(R.id.containerEmployeeQueue);
        tvEmployeeEmptyQueue = findViewById(R.id.tvEmployeeEmptyQueue);
        btnEmployeeNiveisAlerta = findViewById(R.id.btnEmployeeNiveisAlerta);

        // Navigation
        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);

        applyRoleLayout();
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void applyRoleLayout() {
        boolean isEmployee = UserRoleManager.isEmployee(this);
        if (layoutHomeManager != null) {
            layoutHomeManager.setVisibility(isEmployee ? View.GONE : View.VISIBLE);
        }
        if (layoutHomeEmployee != null) {
            layoutHomeEmployee.setVisibility(isEmployee ? View.VISIBLE : View.GONE);
        }

        String unit = SessionManager.getInstance(this).getUnit();
        if (unit == null || unit.trim().isEmpty()) {
            unit = "CD Manaus - Unidade 20";
        }

        if (isEmployee) {
            String firstName = UserRoleManager.getFirstNameForGreeting(this);
            if (tvEmployeeGreeting != null) {
                tvEmployeeGreeting.setText("Olá, " + firstName + "!");
            }
            if (tvEmployeeSubtitle != null) {
                tvEmployeeSubtitle.setText("Minha fila · " + unit);
            }
        } else {
            if (tvManagerGreeting != null) {
                tvManagerGreeting.setText("Visão do CD");
            }
            if (tvManagerSubtitle != null) {
                tvManagerSubtitle.setText(unit);
            }
        }
    }

    private void setupListeners() {
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Manager listeners
        if (btnManagerVerTodasPrioridades != null) {
            btnManagerVerTodasPrioridades.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        }
        if (cardManagerCriticos != null) {
            cardManagerCriticos.setOnClickListener(v -> openFilteredAlerts());
        }
        if (cardManagerSlaVencido != null) {
            cardManagerSlaVencido.setOnClickListener(v -> openFilteredAlerts());
        }
        if (cardManagerForaFaixa != null) {
            cardManagerForaFaixa.setOnClickListener(v -> startActivity(new Intent(this, CamarasActivity.class)));
        }
        if (cardManagerSemResponsavel != null) {
            cardManagerSemResponsavel.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        }
        if (btnMapaUnidades != null) {
            btnMapaUnidades.setOnClickListener(v -> startActivity(new Intent(this, CdMapActivity.class)));
        }
        if (btnNiveisAlerta != null) {
            btnNiveisAlerta.setOnClickListener(v -> AlertLevelsDialog.show(this));
        }

        // Employee listeners
        if (cardEmployeeAtribuidos != null) {
            cardEmployeeAtribuidos.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        }
        if (cardEmployeeCriticos != null) {
            cardEmployeeCriticos.setOnClickListener(v -> openFilteredAlerts());
        }
        if (cardEmployeeEmAtendimento != null) {
            cardEmployeeEmAtendimento.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        }
        if (cardEmployeeSlaVencido != null) {
            cardEmployeeSlaVencido.setOnClickListener(v -> openFilteredAlerts());
        }
        if (btnEmployeeNiveisAlerta != null) {
            btnEmployeeNiveisAlerta.setOnClickListener(v -> AlertLevelsDialog.show(this));
        }

        // Bottom Navigation
        navCamaras.setOnClickListener(v -> startActivity(new Intent(this, CamarasActivity.class)));
        navAlertas.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        navInicio.setOnClickListener(v -> { /* Já na Home */ });
        navRelatorios.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        navTrimmy.setOnClickListener(v -> startActivity(new Intent(this, TrimmyActivity.class)));
    }

    private void openFilteredAlerts() {
        Intent intent = new Intent(this, AlertsActivity.class);
        startActivity(intent);
    }

    private void loadHomeData() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        SkadiDatabaseHelper dbHelper = SkadiDatabaseHelper.getInstance(this);

        String currentTimeStr = "Atualizado às " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        if (tvManagerLastUpdated != null) tvManagerLastUpdated.setText(currentTimeStr);
        if (tvEmployeeLastUpdated != null) tvEmployeeLastUpdated.setText(currentTimeStr);

        // 1. Carrega Câmaras
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
        // 2. Carrega Leituras
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
        latestReadingsMap.clear();
        allReadingsHistory.clear();
        if (readings != null) {
            allReadingsHistory.addAll(readings);
            for (TemperatureReadingResponse tr : readings) {
                if (tr.getColdRoomId() != null) {
                    // Mantém a leitura mais recente de cada câmara
                    TemperatureReadingResponse current = latestReadingsMap.get(tr.getColdRoomId());
                    if (current == null) {
                        latestReadingsMap.put(tr.getColdRoomId(), tr);
                    } else if (tr.getMeasuredAt() != null && current.getMeasuredAt() != null) {
                        try {
                            LocalDateTime tNew = LocalDateTime.parse(tr.getMeasuredAt());
                            LocalDateTime tOld = LocalDateTime.parse(current.getMeasuredAt());
                            if (tNew.isAfter(tOld)) {
                                latestReadingsMap.put(tr.getColdRoomId(), tr);
                            }
                        } catch (Exception ignored) {
                            latestReadingsMap.put(tr.getColdRoomId(), tr);
                        }
                    }
                }
            }
        }
    }

    private void loadAlerts(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
        // 3. Carrega Alertas
        apiService.getAlerts(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.saveAlerts(response.body());
                    bindDashboard(response.body());
                } else {
                    List<AlertResponse> cached = dbHelper.getCachedAlerts();
                    bindDashboard(cached);
                }
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                List<AlertResponse> cached = dbHelper.getCachedAlerts();
                bindDashboard(cached);
            }
        });
    }

    private void bindDashboard(List<AlertResponse> alerts) {
        boolean isEmployee = UserRoleManager.isEmployee(this);

        // 1. Processar métricas de câmaras
        int totalCamaras = coldRoomsMap.size();
        int naFaixaCount = 0;
        int foraFaixaCount = 0;
        int offlineCount = 0;
        int semDadosCount = 0;

        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<Long, ColdRoomResponse> entry : coldRoomsMap.entrySet()) {
            Long roomId = entry.getKey();
            ColdRoomResponse cr = entry.getValue();
            TemperatureReadingResponse tr = latestReadingsMap.get(roomId);

            if (tr == null || tr.getTemperature() == null) {
                semDadosCount++;
            } else {
                boolean isOffline = false;
                if (tr.getMeasuredAt() != null) {
                    try {
                        LocalDateTime readTime = LocalDateTime.parse(tr.getMeasuredAt());
                        long minutesDiff = Math.abs(Duration.between(readTime, now).toMinutes());
                        if (minutesDiff > 10) {
                            isOffline = true;
                        }
                    } catch (Exception ignored) {}
                }

                if (isOffline) {
                    offlineCount++;
                } else {
                    double temp = tr.getTemperature().doubleValue();
                    double min = cr.getMinimumTemperature() != null ? cr.getMinimumTemperature().doubleValue() : -2.0;
                    double max = cr.getMaximumTemperature() != null ? cr.getMaximumTemperature().doubleValue() : 4.0;

                    if (temp >= min && temp <= max) {
                        naFaixaCount++;
                    } else {
                        foraFaixaCount++;
                    }
                }
            }
        }

        // 2. Processar métricas de alertas abertos
        List<AlertResponse> openAlerts = new ArrayList<>();
        int criticosCount = 0;
        int slaVencidoCount = 0;
        int proximosVencimentoCount = 0;
        int semResponsavelCount = 0;

        if (alerts != null) {
            for (AlertResponse alert : alerts) {
                boolean isResolved = alert.getStatus() != null && "RESOLVIDO".equalsIgnoreCase(alert.getStatus());
                if (!isResolved) {
                    openAlerts.add(alert);

                    boolean isCritical = alert.getSeverityLevel() != null &&
                            ("CRITICO".equalsIgnoreCase(alert.getSeverityLevel()) || "CRITICA".equalsIgnoreCase(alert.getSeverityLevel()));
                    if (isCritical) {
                        criticosCount++;
                    }

                    // SLA calculation
                    SlaStatus sla = calculateSla(alert, now);
                    if (sla.isVencido) {
                        slaVencidoCount++;
                    } else if (sla.isProximo) {
                        proximosVencimentoCount++;
                    }

                    // Responsável
                    if (alert.getCurrentLevel() == null || "OPERADOR".equalsIgnoreCase(alert.getCurrentLevel())) {
                        semResponsavelCount++;
                    }
                }
            }
        }

        // Ordenar alertas por prioridade: Críticos -> SLA vencido -> SLA próximo -> mais antigos
        Collections.sort(openAlerts, new Comparator<AlertResponse>() {
            @Override
            public int compare(AlertResponse a1, AlertResponse a2) {
                boolean c1 = isAlertCritical(a1);
                boolean c2 = isAlertCritical(a2);
                if (c1 != c2) return c1 ? -1 : 1;

                SlaStatus s1 = calculateSla(a1, now);
                SlaStatus s2 = calculateSla(a2, now);
                if (s1.isVencido != s2.isVencido) return s1.isVencido ? -1 : 1;
                if (s1.isProximo != s2.isProximo) return s1.isProximo ? -1 : 1;

                return 0;
            }
        });

        if (isEmployee) {
            bindEmployeeDashboard(openAlerts, criticosCount, slaVencidoCount, proximosVencimentoCount, now);
        } else {
            bindManagerDashboard(totalCamaras, naFaixaCount, foraFaixaCount, offlineCount, semDadosCount,
                    criticosCount, slaVencidoCount, semResponsavelCount, openAlerts, now);
        }
    }

    private void bindManagerDashboard(int totalCamaras, int naFaixa, int foraFaixa, int offline, int semDados,
                                      int criticos, int slaVencido, int semResponsavel,
                                      List<AlertResponse> openAlerts, LocalDateTime now) {
        // KPIs de intervenção do Gestor
        if (tvManagerCountCriticos != null) tvManagerCountCriticos.setText(String.valueOf(criticos));
        if (tvManagerCountSlaVencido != null) tvManagerCountSlaVencido.setText(String.valueOf(slaVencido));
        if (tvManagerCountForaFaixa != null) tvManagerCountForaFaixa.setText(String.valueOf(foraFaixa));
        if (tvManagerCountSemResponsavel != null) tvManagerCountSemResponsavel.setText(String.valueOf(semResponsavel));

        // Resumo de câmaras
        if (tvManagerCamarasStateLine1 != null) {
            tvManagerCamarasStateLine1.setText(String.format(Locale.getDefault(),
                    "Total: %d  •  Na faixa: %d  •  Fora da faixa: %d", totalCamaras, naFaixa, foraFaixa));
        }
        if (tvManagerCamarasStateLine2 != null) {
            tvManagerCamarasStateLine2.setText(String.format(Locale.getDefault(),
                    "Offline: %d  •  Sem dados: %d", offline, semDados));
        }

        // Prioridades Agora (Top 3)
        if (containerManagerPriorities != null) {
            containerManagerPriorities.removeAllViews();
            int topCount = Math.min(openAlerts.size(), 3);
            if (topCount == 0) {
                if (tvManagerNoPriorities != null) tvManagerNoPriorities.setVisibility(View.VISIBLE);
            } else {
                if (tvManagerNoPriorities != null) tvManagerNoPriorities.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(this);
                for (int i = 0; i < topCount; i++) {
                    AlertResponse alert = openAlerts.get(i);
                    View itemView = createPriorityAlertItem(inflater, containerManagerPriorities, alert, now);
                    containerManagerPriorities.addView(itemView);
                }
            }
        }

        // Gráfico de Tendência da câmara prioritária ou câmara #1
        if (temperatureChart != null) {
            List<Float> chartPoints = new ArrayList<>();
            Long targetRoomId = !openAlerts.isEmpty() && openAlerts.get(0).getColdRoomId() != null
                    ? openAlerts.get(0).getColdRoomId().longValue() : 1L;

            ColdRoomResponse cr = coldRoomsMap.get(targetRoomId);
            if (cr != null && cr.getLocation() != null && tvManagerChartTitle != null) {
                tvManagerChartTitle.setText("Tendência · " + cr.getLocation());
            }

            for (TemperatureReadingResponse tr : allReadingsHistory) {
                if (tr.getColdRoomId() != null && tr.getColdRoomId().equals(targetRoomId) && tr.getTemperature() != null) {
                    chartPoints.add(tr.getTemperature().floatValue());
                }
            }
            if (chartPoints.isEmpty()) {
                TemperatureReadingResponse latest = latestReadingsMap.get(targetRoomId);
                if (latest != null && latest.getTemperature() != null) {
                    chartPoints.add(latest.getTemperature().floatValue());
                }
            }
            if (!chartPoints.isEmpty()) {
                temperatureChart.setDataPoints(chartPoints);
            }
        }
    }

    private void bindEmployeeDashboard(List<AlertResponse> openAlerts, int criticos, int slaVencido, int proximosVencimento, LocalDateTime now) {
        int totalAtribuidos = openAlerts.size();
        int emAtendimentoCount = 0;
        for (AlertResponse a : openAlerts) {
            if (a.getStatus() != null && "RECONHECIDO".equalsIgnoreCase(a.getStatus())) {
                emAtendimentoCount++;
            }
        }

        // KPIs da fila do funcionário
        if (tvEmployeeCountAtribuidos != null) tvEmployeeCountAtribuidos.setText(String.valueOf(totalAtribuidos));
        if (tvEmployeeCountCriticos != null) tvEmployeeCountCriticos.setText(String.valueOf(criticos));
        if (tvEmployeeCountEmAtendimento != null) tvEmployeeCountEmAtendimento.setText(String.valueOf(emAtendimentoCount));
        if (tvEmployeeCountSlaVencido != null) tvEmployeeCountSlaVencido.setText(String.valueOf(slaVencido));

        // Faixa Âmbar de Próximos do Vencimento
        if (layoutEmployeeSlaAlert != null) {
            if (proximosVencimento > 0) {
                layoutEmployeeSlaAlert.setVisibility(View.VISIBLE);
                if (tvEmployeeCountProximosVencimento != null) {
                    tvEmployeeCountProximosVencimento.setText(proximosVencimento + " alertas próximos do vencimento (70% do prazo consumido)");
                }
            } else {
                layoutEmployeeSlaAlert.setVisibility(View.GONE);
            }
        }

        // PRÓXIMA AÇÃO (Card de Destaque #1)
        if (cardEmployeeNextAction != null) {
            if (openAlerts.isEmpty()) {
                cardEmployeeNextAction.setVisibility(View.GONE);
            } else {
                cardEmployeeNextAction.setVisibility(View.VISIBLE);
                AlertResponse nextAlert = openAlerts.get(0);
                Long roomId = nextAlert.getColdRoomId() != null ? nextAlert.getColdRoomId().longValue() : 1L;
                ColdRoomResponse cr = coldRoomsMap.get(roomId);
                TemperatureReadingResponse reading = latestReadingsMap.get(roomId);

                boolean isCritical = isAlertCritical(nextAlert);
                boolean isRecognized = nextAlert.getStatus() != null && "RECONHECIDO".equalsIgnoreCase(nextAlert.getStatus());

                if (tvEmployeeNextBadge != null) {
                    tvEmployeeNextBadge.setText(isCritical ? "Crítico" : "Atenção");
                    tvEmployeeNextBadge.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));
                    tvEmployeeNextBadge.setBackgroundResource(isCritical ? R.drawable.bg_badge_critical : R.drawable.bg_icon_square_yellow);
                }

                if (tvEmployeeNextStatusBadge != null) {
                    tvEmployeeNextStatusBadge.setText(isRecognized ? "Em atendimento" : "Pendente");
                    tvEmployeeNextStatusBadge.setTextColor(Color.parseColor(isRecognized ? "#0284C7" : "#D97706"));
                }

                if (tvEmployeeNextTimeElapsed != null && nextAlert.getCreatedAt() != null) {
                    tvEmployeeNextTimeElapsed.setText(formatElapsedTime(nextAlert.getCreatedAt(), now));
                }

                if (tvEmployeeNextCamaraTitle != null) {
                    tvEmployeeNextCamaraTitle.setText(cr != null && cr.getLocation() != null ? cr.getLocation() : "Câmara " + roomId);
                }

                if (tvEmployeeNextTemp != null) {
                    if (reading != null && reading.getTemperature() != null) {
                        tvEmployeeNextTemp.setText(String.format(Locale.getDefault(), "%.1f°C", reading.getTemperature()));
                        tvEmployeeNextTemp.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));
                    } else {
                        tvEmployeeNextTemp.setText("Sem leitura");
                        tvEmployeeNextTemp.setTextColor(Color.parseColor("#64748B"));
                    }
                }

                if (tvEmployeeNextTempRange != null) {
                    if (cr != null && cr.getMinimumTemperature() != null && cr.getMaximumTemperature() != null) {
                        tvEmployeeNextTempRange.setText(String.format(Locale.getDefault(),
                                "Faixa recomendada: %.1f°C a %.1f°C", cr.getMinimumTemperature(), cr.getMaximumTemperature()));
                    } else {
                        tvEmployeeNextTempRange.setText("Faixa recomendada: -2,0°C a 4,0°C");
                    }
                }

                SlaStatus sla = calculateSla(nextAlert, now);
                if (tvEmployeeNextSlaInfo != null) {
                    tvEmployeeNextSlaInfo.setText("SLA: " + sla.description + " • " + (nextAlert.getCurrentLevel() != null ? "Nível " + nextAlert.getCurrentLevel() : "Etapa Operador"));
                }

                if (btnEmployeeNextAction != null) {
                    btnEmployeeNextAction.setText(isRecognized ? "Ver Detalhes do Alerta" : "Iniciar Atendimento");
                    btnEmployeeNextAction.setOnClickListener(v -> {
                        if (!isRecognized && nextAlert.getId() != null) {
                            RetrofitClient.getApiService(this).recognizeAlert(nextAlert.getId()).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {}
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {}
                            });
                        }
                        Intent intent = new Intent(this, AlertDetailsActivity.class);
                        intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, nextAlert.getId());
                        intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
                        startActivity(intent);
                    });
                }
            }
        }

        // MINHA FILA (Itens seguintes da fila)
        if (containerEmployeeQueue != null) {
            containerEmployeeQueue.removeAllViews();
            if (openAlerts.isEmpty()) {
                if (tvEmployeeEmptyQueue != null) tvEmployeeEmptyQueue.setVisibility(View.VISIBLE);
                if (tvEmployeeQueueCount != null) tvEmployeeQueueCount.setText("0 alertas");
            } else {
                if (tvEmployeeEmptyQueue != null) tvEmployeeEmptyQueue.setVisibility(View.GONE);
                if (tvEmployeeQueueCount != null) tvEmployeeQueueCount.setText(openAlerts.size() + " alertas");
                LayoutInflater inflater = LayoutInflater.from(this);
                // Exibe os alertas a partir do segundo se o primeiro estiver no destaque, ou todos
                for (int i = 0; i < openAlerts.size(); i++) {
                    AlertResponse alert = openAlerts.get(i);
                    View itemView = createPriorityAlertItem(inflater, containerEmployeeQueue, alert, now);
                    containerEmployeeQueue.addView(itemView);
                }
            }
        }
    }

    private View createPriorityAlertItem(LayoutInflater inflater, LinearLayout parent, AlertResponse alert, LocalDateTime now) {
        View view = inflater.inflate(R.layout.item_home_priority_alert, parent, false);

        TextView tvBadge = view.findViewById(R.id.tvPriorityBadge);
        TextView tvStatusBadge = view.findViewById(R.id.tvPriorityStatusBadge);
        TextView tvTimeElapsed = view.findViewById(R.id.tvPriorityTimeElapsed);
        TextView tvCamaraTitle = view.findViewById(R.id.tvPriorityCamaraTitle);
        TextView tvTemp = view.findViewById(R.id.tvPriorityTemp);
        TextView tvRangeAndSla = view.findViewById(R.id.tvPriorityRangeAndSla);
        TextView tvAssignee = view.findViewById(R.id.tvPriorityAssignee);

        boolean isCritical = isAlertCritical(alert);
        boolean isRecognized = alert.getStatus() != null && "RECONHECIDO".equalsIgnoreCase(alert.getStatus());
        Long roomId = alert.getColdRoomId() != null ? alert.getColdRoomId().longValue() : 1L;
        ColdRoomResponse cr = coldRoomsMap.get(roomId);
        TemperatureReadingResponse reading = latestReadingsMap.get(roomId);

        if (tvBadge != null) {
            tvBadge.setText(isCritical ? "Crítico" : "Atenção");
            tvBadge.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));
            tvBadge.setBackgroundResource(isCritical ? R.drawable.bg_badge_critical : R.drawable.bg_icon_square_yellow);
        }

        if (tvStatusBadge != null) {
            tvStatusBadge.setText(isRecognized ? "Em atendimento" : "Pendente");
            tvStatusBadge.setTextColor(Color.parseColor(isRecognized ? "#0284C7" : "#D97706"));
        }

        if (tvTimeElapsed != null && alert.getCreatedAt() != null) {
            tvTimeElapsed.setText(formatElapsedTime(alert.getCreatedAt(), now));
        }

        if (tvCamaraTitle != null) {
            tvCamaraTitle.setText(cr != null && cr.getLocation() != null ? cr.getLocation() : "Câmara " + roomId);
        }

        if (tvTemp != null) {
            if (reading != null && reading.getTemperature() != null) {
                tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", reading.getTemperature()));
                tvTemp.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));
            } else {
                tvTemp.setText("Sem leitura");
                tvTemp.setTextColor(Color.parseColor("#64748B"));
            }
        }

        SlaStatus sla = calculateSla(alert, now);
        if (tvRangeAndSla != null) {
            String rangeStr = (cr != null && cr.getMinimumTemperature() != null && cr.getMaximumTemperature() != null)
                    ? String.format(Locale.getDefault(), "Faixa: %.1f°C a %.1f°C", cr.getMinimumTemperature(), cr.getMaximumTemperature())
                    : "Faixa padrão: -2°C a 4°C";
            tvRangeAndSla.setText(rangeStr + " • SLA: " + sla.description);
        }

        if (tvAssignee != null) {
            String roleStr = alert.getCurrentLevel() != null ? "Nível " + alert.getCurrentLevel() : "Operador";
            tvAssignee.setText("Atendimento: " + roleStr);
        }

        view.setOnClickListener(v -> {
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            if (alert.getId() != null) {
                intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, alert.getId());
            }
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
            startActivity(intent);
        });

        return view;
    }

    private boolean isAlertCritical(AlertResponse alert) {
        if (alert == null || alert.getSeverityLevel() == null) return false;
        String s = alert.getSeverityLevel().toUpperCase();
        return s.contains("CRITIC") || s.contains("URGENT");
    }

    private SlaStatus calculateSla(AlertResponse alert, LocalDateTime now) {
        SlaStatus status = new SlaStatus();
        if (alert == null || alert.getCreatedAt() == null || alert.getReferenceShelfLifeHours() == null) {
            status.description = "Sem prazo de referência";
            return status;
        }

        try {
            LocalDateTime created = LocalDateTime.parse(alert.getCreatedAt());
            long elapsedMinutes = Math.abs(Duration.between(created, now).toMinutes());
            long totalAllowedMinutes = (long) (alert.getReferenceShelfLifeHours().doubleValue() * 60);

            if (totalAllowedMinutes <= 0) {
                status.description = "Sem referência";
                return status;
            }

            long remainingMinutes = totalAllowedMinutes - elapsedMinutes;
            double percentConsumed = (elapsedMinutes * 100.0) / totalAllowedMinutes;

            if (percentConsumed >= 100) {
                status.isVencido = true;
                status.description = "SLA Vencido (" + Math.abs(remainingMinutes) + " min acima)";
            } else if (percentConsumed >= 70) {
                status.isProximo = true;
                status.description = remainingMinutes + " min restantes (70% consumido)";
            } else {
                status.description = remainingMinutes + " min restantes (No prazo)";
            }
        } catch (Exception e) {
            status.description = "No prazo";
        }

        return status;
    }

    private String formatElapsedTime(String rawDate, LocalDateTime now) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(rawDate);
            long minutes = Math.abs(Duration.between(ldt, now).toMinutes());
            if (minutes < 1) return "Agora";
            if (minutes < 60) return "Há " + minutes + " min";
            long hours = minutes / 60;
            return "Há " + hours + " h";
        } catch (Exception e) {
            return "Recente";
        }
    }

    private static class SlaStatus {
        boolean isVencido = false;
        boolean isProximo = false;
        String description = "No prazo";
    }
}
