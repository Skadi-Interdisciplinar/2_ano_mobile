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



import com.example.mobile_skadi.network.RetrofitClient;

import com.example.mobile_skadi.network.SessionManager;

import com.example.mobile_skadi.network.SkadiApiService;

import com.example.mobile_skadi.network.dto.EmployeeDashboardResponse;

import com.example.mobile_skadi.network.dto.ManagerDashboardResponse;

import com.example.mobile_skadi.network.dto.PriorityAlertDto;

import com.example.mobile_skadi.notification.AppNotificationManager;

import com.example.mobile_skadi.utils.AvatarManager;

import com.google.android.material.button.MaterialButton;

import com.google.android.material.card.MaterialCardView;



import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.List;

import java.util.Locale;



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



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);



        notifPermissionLauncher = registerForActivityResult(

                new ActivityResultContracts.RequestPermission(),

                isGranted -> {

                    if (isGranted) {

                        AppNotificationManager.initChannels(this);
        com.example.mobile_skadi.notification.AlertCheckJobService.schedulePeriodicJob(this);

                    }

                }

        );



        AppNotificationManager.initChannels(this);
        com.example.mobile_skadi.notification.AlertCheckJobService.schedulePeriodicJob(this);

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

            String firstName = SessionManager.getInstance(this).getName();

            if (firstName == null || firstName.trim().isEmpty()) {

                firstName = SessionManager.getInstance(this).getUsername();

            }

            if (firstName != null && firstName.contains(" ")) {

                firstName = firstName.split(" ")[0];

            }

            if (tvManagerGreeting != null) {

                tvManagerGreeting.setText("Olá, " + (firstName != null && !firstName.isEmpty() ? firstName : "Gestor") + "!");

            }

            if (tvManagerSubtitle != null) {

                tvManagerSubtitle.setText("Visão do CD • " + unit);

            }

        }

    }



    private void setupListeners() {

        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));

        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));



        // Manager

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



        // Employee

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

        startActivity(new Intent(this, AlertsActivity.class));

    }



    private void loadHomeData() {

        SkadiApiService apiService = RetrofitClient.getApiService(this);

        boolean isEmployee = UserRoleManager.isEmployee(this);



        String currentTimeStr = "Atualizado às " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        if (tvManagerLastUpdated != null) tvManagerLastUpdated.setText(currentTimeStr);

        if (tvEmployeeLastUpdated != null) tvEmployeeLastUpdated.setText(currentTimeStr);



        if (isEmployee) {

            // Chamada direta e única ao endpoint de funcionário

            apiService.getEmployeeDashboard().enqueue(new Callback<EmployeeDashboardResponse>() {

                @Override

                public void onResponse(Call<EmployeeDashboardResponse> call, Response<EmployeeDashboardResponse> response) {

                    if (response.isSuccessful() && response.body() != null) {

                        bindEmployeeData(response.body());

                    }

                }



                @Override

                public void onFailure(Call<EmployeeDashboardResponse> call, Throwable t) {

                    android.util.Log.e("HomeActivity", "Falha ao carregar dashboard do funcionário: " + t.getMessage());

                }

            });

        } else {

            // Chamada direta e única ao endpoint de gestor

            apiService.getManagerDashboard().enqueue(new Callback<ManagerDashboardResponse>() {

                @Override

                public void onResponse(Call<ManagerDashboardResponse> call, Response<ManagerDashboardResponse> response) {

                    if (response.isSuccessful() && response.body() != null) {

                        bindManagerData(response.body());

                    }

                }



                @Override

                public void onFailure(Call<ManagerDashboardResponse> call, Throwable t) {

                    android.util.Log.e("HomeActivity", "Falha ao carregar dashboard do gestor: " + t.getMessage());

                }

            });

        }

    }



    private void bindManagerData(ManagerDashboardResponse data) {

        if (data.getCdName() != null && !data.getCdName().isEmpty() && tvManagerSubtitle != null) {

            tvManagerSubtitle.setText("Visão do CD • " + data.getCdName());

        }



        if (tvManagerCountCriticos != null) tvManagerCountCriticos.setText(String.valueOf(data.getCriticosCount()));

        if (tvManagerCountSlaVencido != null) tvManagerCountSlaVencido.setText(String.valueOf(data.getSlaVencidoCount()));

        if (tvManagerCountForaFaixa != null) tvManagerCountForaFaixa.setText(String.valueOf(data.getForaFaixaCount()));

        if (tvManagerCountSemResponsavel != null) tvManagerCountSemResponsavel.setText(String.valueOf(data.getSemResponsavelCount()));



        if (tvManagerCamarasStateLine1 != null) {

            tvManagerCamarasStateLine1.setText(String.format(Locale.getDefault(),

                    "Total: %d  •  Na faixa: %d  •  Fora da faixa: %d",

                    data.getTotalCamaras(), data.getNaFaixaCamaras(), data.getForaFaixaCamaras()));

        }

        if (tvManagerCamarasStateLine2 != null) {

            tvManagerCamarasStateLine2.setText(String.format(Locale.getDefault(),

                    "Offline: %d  •  Sem dados: %d",

                    data.getOfflineCamaras(), data.getSemDadosCamaras()));

        }



        // Prioridades Agora

        if (containerManagerPriorities != null) {

            containerManagerPriorities.removeAllViews();

            List<PriorityAlertDto> priorities = data.getPriorities();

            if (priorities == null || priorities.isEmpty()) {

                if (tvManagerNoPriorities != null) tvManagerNoPriorities.setVisibility(View.VISIBLE);

            } else {

                if (tvManagerNoPriorities != null) tvManagerNoPriorities.setVisibility(View.GONE);

                LayoutInflater inflater = LayoutInflater.from(this);

                for (PriorityAlertDto p : priorities) {

                    View itemView = createPriorityAlertItem(inflater, containerManagerPriorities, p);

                    containerManagerPriorities.addView(itemView);



                    // Dispara notificação local se houver alerta crítico pendente

                    if (p.getSeverityLevel() != null && p.getSeverityLevel().contains("CRITIC")

                            && !"RECONHECIDO".equalsIgnoreCase(p.getStatus())) {

                        double curr = p.getCurrentTemperature() != null ? p.getCurrentTemperature().doubleValue() : 8.0;

                        double max = p.getMaximumTemperature() != null ? p.getMaximumTemperature().doubleValue() : 4.0;

                        AppNotificationManager.sendCriticalAlertNotification(this, p.getColdRoomName(), curr, max, p.getId());

                    }

                }

            }

        }



        // Tendência de Temperatura

        if (temperatureChart != null && data.getTrendReadings() != null && !data.getTrendReadings().isEmpty()) {

            temperatureChart.setDataPoints(data.getTrendReadings());

        }

    }



    private void bindEmployeeData(EmployeeDashboardResponse data) {

        if (data.getCdName() != null && !data.getCdName().isEmpty() && tvEmployeeSubtitle != null) {

            tvEmployeeSubtitle.setText("Minha fila · " + data.getCdName());

        }



        if (tvEmployeeCountAtribuidos != null) tvEmployeeCountAtribuidos.setText(String.valueOf(data.getAtribuidosCount()));

        if (tvEmployeeCountCriticos != null) tvEmployeeCountCriticos.setText(String.valueOf(data.getCriticosCount()));

        if (tvEmployeeCountEmAtendimento != null) tvEmployeeCountEmAtendimento.setText(String.valueOf(data.getEmAtendimentoCount()));

        if (tvEmployeeCountSlaVencido != null) tvEmployeeCountSlaVencido.setText(String.valueOf(data.getSlaVencidoCount()));



        // Faixa Âmbar de Próximos do Vencimento

        if (layoutEmployeeSlaAlert != null) {

            if (data.getProximosVencimentoCount() > 0) {

                layoutEmployeeSlaAlert.setVisibility(View.VISIBLE);

                if (tvEmployeeCountProximosVencimento != null) {

                    tvEmployeeCountProximosVencimento.setText(data.getProximosVencimentoCount() + " alertas próximos do vencimento (70% do prazo consumido)");

                }

            } else {

                layoutEmployeeSlaAlert.setVisibility(View.GONE);

            }

        }



        // PRÓXIMA AÇÃO (Card #1)

        PriorityAlertDto next = data.getNextAction();

        if (cardEmployeeNextAction != null) {

            if (next == null) {

                cardEmployeeNextAction.setVisibility(View.GONE);

            } else {

                cardEmployeeNextAction.setVisibility(View.VISIBLE);

                boolean isCritical = next.getSeverityLevel() != null && next.getSeverityLevel().contains("CRITIC");

                boolean isRecognized = next.getStatus() != null && "RECONHECIDO".equalsIgnoreCase(next.getStatus());



                if (tvEmployeeNextBadge != null) {

                    tvEmployeeNextBadge.setText(isCritical ? "Crítico" : "Atenção");

                    tvEmployeeNextBadge.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));

                    tvEmployeeNextBadge.setBackgroundResource(isCritical ? R.drawable.bg_badge_critical : R.drawable.bg_icon_square_yellow);

                }



                if (tvEmployeeNextStatusBadge != null) {

                    tvEmployeeNextStatusBadge.setText(isRecognized ? "Em atendimento" : "Pendente");

                    tvEmployeeNextStatusBadge.setTextColor(Color.parseColor(isRecognized ? "#0284C7" : "#D97706"));

                }



                if (tvEmployeeNextTimeElapsed != null) {

                    String timeElapsed = formatTimeElapsed(next.getCreatedAt());

                    tvEmployeeNextTimeElapsed.setText(timeElapsed);

                    tvEmployeeNextTimeElapsed.setVisibility(timeElapsed.isEmpty() ? View.GONE : View.VISIBLE);

                }



                if (tvEmployeeNextCamaraTitle != null) {

                    tvEmployeeNextCamaraTitle.setText(next.getColdRoomName() != null ? next.getColdRoomName() : "Câmara " + next.getColdRoomId());

                }



                if (tvEmployeeNextTemp != null) {

                    if (next.getCurrentTemperature() != null) {

                        tvEmployeeNextTemp.setText(String.format(Locale.getDefault(), "%.1f°C", next.getCurrentTemperature()));

                        tvEmployeeNextTemp.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));

                    } else {

                        tvEmployeeNextTemp.setText("Sem leitura");

                        tvEmployeeNextTemp.setTextColor(Color.parseColor("#64748B"));

                    }

                }



                if (tvEmployeeNextTempRange != null) {

                    if (next.getMinimumTemperature() != null && next.getMaximumTemperature() != null) {

                        tvEmployeeNextTempRange.setText(String.format(Locale.getDefault(),

                                "Faixa recomendada: %.1f°C a %.1f°C", next.getMinimumTemperature(), next.getMaximumTemperature()));

                    } else {

                        tvEmployeeNextTempRange.setText("Faixa recomendada: -2,0°C a 4,0°C");

                    }

                }



                if (tvEmployeeNextSlaInfo != null) {

                    String slaText = formatSlaText(next);

                    tvEmployeeNextSlaInfo.setText("SLA: " + slaText + " • " + (next.getCurrentLevel() != null ? "Nível " + next.getCurrentLevel() : "Operador"));

                }



                if (btnEmployeeNextAction != null) {

                    btnEmployeeNextAction.setText(isRecognized ? "Ver Detalhes do Alerta" : "Iniciar Atendimento");

                    btnEmployeeNextAction.setOnClickListener(v -> {

                        if (!isRecognized && next.getId() != null) {

                            RetrofitClient.getApiService(this).recognizeAlert(next.getId()).enqueue(new Callback<Void>() {

                                @Override

                                public void onResponse(Call<Void> call, Response<Void> response) {}

                                @Override

                                public void onFailure(Call<Void> call, Throwable t) {}

                            });

                        }

                        Intent intent = new Intent(this, AlertDetailsActivity.class);

                        intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, next.getId());

                        intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);

                        startActivity(intent);

                    });

                }



                // Notificação de alerta crítico operacional

                if (isCritical && !isRecognized) {

                    double curr = next.getCurrentTemperature() != null ? next.getCurrentTemperature().doubleValue() : 8.0;

                    double max = next.getMaximumTemperature() != null ? next.getMaximumTemperature().doubleValue() : 4.0;

                    AppNotificationManager.sendCriticalAlertNotification(this, next.getColdRoomName(), curr, max, next.getId());

                }

            }

        }



        // MINHA FILA

        if (containerEmployeeQueue != null) {

            containerEmployeeQueue.removeAllViews();

            List<PriorityAlertDto> queue = data.getQueue();

            if (queue == null || queue.isEmpty()) {

                if (tvEmployeeEmptyQueue != null) tvEmployeeEmptyQueue.setVisibility(View.VISIBLE);

                if (tvEmployeeQueueCount != null) tvEmployeeQueueCount.setText("0 alertas");

            } else {

                if (tvEmployeeEmptyQueue != null) tvEmployeeEmptyQueue.setVisibility(View.GONE);

                if (tvEmployeeQueueCount != null) tvEmployeeQueueCount.setText(queue.size() + " alertas");

                LayoutInflater inflater = LayoutInflater.from(this);

                for (PriorityAlertDto p : queue) {

                    View itemView = createPriorityAlertItem(inflater, containerEmployeeQueue, p);

                    containerEmployeeQueue.addView(itemView);

                }

            }

        }

    }



    private View createPriorityAlertItem(LayoutInflater inflater, LinearLayout parent, PriorityAlertDto p) {

        View view = inflater.inflate(R.layout.item_home_priority_alert, parent, false);



        TextView tvBadge = view.findViewById(R.id.tvPriorityBadge);

        TextView tvStatusBadge = view.findViewById(R.id.tvPriorityStatusBadge);

        TextView tvTimeElapsed = view.findViewById(R.id.tvPriorityTimeElapsed);

        TextView tvCamaraTitle = view.findViewById(R.id.tvPriorityCamaraTitle);

        TextView tvTemp = view.findViewById(R.id.tvPriorityTemp);

        TextView tvRangeAndSla = view.findViewById(R.id.tvPriorityRangeAndSla);

        TextView tvAssignee = view.findViewById(R.id.tvPriorityAssignee);



        boolean isCritical = p.getSeverityLevel() != null && p.getSeverityLevel().contains("CRITIC");

        boolean isRecognized = p.getStatus() != null && "RECONHECIDO".equalsIgnoreCase(p.getStatus());



        if (tvBadge != null) {

            tvBadge.setText(isCritical ? "Crítico" : "Atenção");

            tvBadge.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));

            tvBadge.setBackgroundResource(isCritical ? R.drawable.bg_badge_critical : R.drawable.bg_icon_square_yellow);

        }



        if (tvStatusBadge != null) {

            tvStatusBadge.setText(isRecognized ? "Em atendimento" : "Pendente");

            tvStatusBadge.setTextColor(Color.parseColor(isRecognized ? "#0284C7" : "#D97706"));

        }



        if (tvTimeElapsed != null) {

            String timeElapsed = formatTimeElapsed(p.getCreatedAt());

            tvTimeElapsed.setText(timeElapsed);

            tvTimeElapsed.setVisibility(timeElapsed.isEmpty() ? View.GONE : View.VISIBLE);

        }



        if (tvCamaraTitle != null) {

            tvCamaraTitle.setText(p.getColdRoomName() != null ? p.getColdRoomName() : "Câmara " + p.getColdRoomId());

        }



        if (tvTemp != null) {

            if (p.getCurrentTemperature() != null) {

                tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", p.getCurrentTemperature()));

                tvTemp.setTextColor(ContextCompat.getColor(this, isCritical ? R.color.alert_danger : R.color.alert_orange));

            } else {

                tvTemp.setText("Sem leitura");

                tvTemp.setTextColor(Color.parseColor("#64748B"));

            }

        }



        if (tvRangeAndSla != null) {

            String rangeStr = (p.getMinimumTemperature() != null && p.getMaximumTemperature() != null)

                    ? String.format(Locale.getDefault(), "Faixa: %.1f°C a %.1f°C", p.getMinimumTemperature(), p.getMaximumTemperature())

                    : "Faixa padrão: -2°C a 4°C";

            tvRangeAndSla.setText(rangeStr + " • SLA: " + formatSlaText(p));

        }



        if (tvAssignee != null) {

            tvAssignee.setText("Atendimento: " + (p.getAssigneeName() != null ? p.getAssigneeName() : "Operador"));

        }



        view.setOnClickListener(v -> {

            Intent intent = new Intent(this, AlertDetailsActivity.class);

            if (p.getId() != null) {

                intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, p.getId());

            }

            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);

            startActivity(intent);

        });



        return view;

    }



    private String formatSlaText(PriorityAlertDto p) {

        if ("SLA_VENCIDO".equals(p.getSlaStatus())) {

            return "Vencido (" + (p.getRemainingMinutes() != null ? Math.abs(p.getRemainingMinutes()) : 0) + " min acima)";

        } else if ("PROXIMO_VENCIMENTO".equals(p.getSlaStatus())) {

            return (p.getRemainingMinutes() != null ? p.getRemainingMinutes() : 0) + " min restantes (70% consumido)";

        } else if ("SEM_REFERENCIA".equals(p.getSlaStatus())) {

            return "Sem prazo de referência";

        }

        return (p.getRemainingMinutes() != null ? p.getRemainingMinutes() : 0) + " min restantes (No prazo)";

    }



    private String formatTimeElapsed(String isoDate) {

        if (isoDate == null || isoDate.isEmpty()) return "";

        try {

            LocalDateTime ldt = LocalDateTime.parse(isoDate.length() > 19 ? isoDate.substring(0, 19) : isoDate);

            long minutes = java.time.Duration.between(ldt, LocalDateTime.now()).toMinutes();

            if (minutes < 1) return "Agora";

            if (minutes < 60) return "Há " + minutes + " min";

            long hours = minutes / 60;

            if (hours < 24) return "Há " + hours + " h";

            return "Há " + (hours / 24) + " d";

        } catch (Exception e) {

            return "";

        }

    }

}

