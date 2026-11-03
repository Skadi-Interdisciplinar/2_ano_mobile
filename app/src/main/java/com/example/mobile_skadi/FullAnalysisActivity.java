package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.example.mobile_skadi.utils.AvatarManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FullAnalysisActivity extends AppCompatActivity {

    public static final String EXTRA_COLD_ROOM_ID = "EXTRA_COLD_ROOM_ID";
    public static final String EXTRA_COLD_ROOM_NAME = "EXTRA_COLD_ROOM_NAME";
    public static final String EXTRA_CD_NAME = "EXTRA_CD_NAME";
    public static final String EXTRA_DATE_START = "EXTRA_DATE_START";
    public static final String EXTRA_DATE_END = "EXTRA_DATE_END";

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private MaterialButton btnBaixarPdf;

    private TextView tvAnalysisHeaderSubtitle;
    private TextView tvFilterCd;
    private TextView tvFilterCamara;
    private TextView tvFilterDataInicial;
    private TextView tvFilterDataFinal;

    private TextView tvAnalysisCdTitle;
    private TextView tvAnalysisSubtitle;
    private TextView tvMetricMin;
    private TextView tvMetricAvg;
    private TextView tvMetricMax;

    private LinearLayout containerOccurrences;
    private TextView tvNoOccurrences;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private Long coldRoomId;
    private String coldRoomName;
    private String cdName;
    private String dateStart;
    private String dateEnd;

    @Override
    protected void onResume() {
        super.onResume();
        AvatarManager.applyAvatar(this, btnProfileAvatar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_analysis);

        initViews();
        parseIntentExtras();
        setupListeners();
        loadAnalysisData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        btnBaixarPdf = findViewById(R.id.btnBaixarPdf);

        tvAnalysisHeaderSubtitle = findViewById(R.id.tvAnalysisHeaderSubtitle);
        tvFilterCd = findViewById(R.id.tvFilterCd);
        tvFilterCamara = findViewById(R.id.tvFilterCamara);
        tvFilterDataInicial = findViewById(R.id.tvFilterDataInicial);
        tvFilterDataFinal = findViewById(R.id.tvFilterDataFinal);

        tvAnalysisCdTitle = findViewById(R.id.tvAnalysisCdTitle);
        tvAnalysisSubtitle = findViewById(R.id.tvAnalysisSubtitle);
        tvMetricMin = findViewById(R.id.tvMetricMin);
        tvMetricAvg = findViewById(R.id.tvMetricAvg);
        tvMetricMax = findViewById(R.id.tvMetricMax);

        containerOccurrences = findViewById(R.id.containerOccurrences);
        tvNoOccurrences = findViewById(R.id.tvNoOccurrences);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void parseIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_COLD_ROOM_ID)) {
                long rId = intent.getLongExtra(EXTRA_COLD_ROOM_ID, 0L);
                if (rId > 0) coldRoomId = rId;
            }
            coldRoomName = intent.getStringExtra(EXTRA_COLD_ROOM_NAME);
            cdName = intent.getStringExtra(EXTRA_CD_NAME);
            dateStart = intent.getStringExtra(EXTRA_DATE_START);
            dateEnd = intent.getStringExtra(EXTRA_DATE_END);
        }

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat br = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (dateStart == null || dateStart.isEmpty()) {
            dateStart = iso.format(new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000));
        }
        if (dateEnd == null || dateEnd.isEmpty()) {
            dateEnd = iso.format(new Date());
        }

        if (cdName == null || cdName.isEmpty()) {
            cdName = "CD Manaus - Unidade 20";
        }

        // Formatação amigável das datas
        try {
            Date dStart = iso.parse(dateStart);
            Date dEnd = iso.parse(dateEnd);
            if (dStart != null && tvFilterDataInicial != null) tvFilterDataInicial.setText(br.format(dStart));
            if (dEnd != null && tvFilterDataFinal != null) tvFilterDataFinal.setText(br.format(dEnd));
        } catch (Exception e) {
            if (tvFilterDataInicial != null) tvFilterDataInicial.setText(dateStart);
            if (tvFilterDataFinal != null) tvFilterDataFinal.setText(dateEnd);
        }

        if (tvAnalysisHeaderSubtitle != null) tvAnalysisHeaderSubtitle.setText(cdName);
        if (tvFilterCd != null) tvFilterCd.setText(cdName);
        if (tvAnalysisCdTitle != null) tvAnalysisCdTitle.setText("Centro de Distribuição " + cdName);

        String camaraLabel = (coldRoomName != null && !coldRoomName.isEmpty()) ? coldRoomName : (coldRoomId != null ? "Câmara " + coldRoomId : "Todas as câmaras");
        if (tvFilterCamara != null) tvFilterCamara.setText(camaraLabel);
        if (tvAnalysisSubtitle != null) tvAnalysisSubtitle.setText("Relatório de temperatura – " + camaraLabel);
    }

    private void loadAnalysisData() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);

        // 1. Carrega métricas reais de temperatura
        if (coldRoomId != null && coldRoomId > 0) {
            apiService.getColdRoomReadings(coldRoomId).enqueue(new Callback<List<TemperatureReadingResponse>>() {
                @Override
                public void onResponse(Call<List<TemperatureReadingResponse>> call, Response<List<TemperatureReadingResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        calculateAndBindMetrics(response.body());
                    } else {
                        fallbackColdRoomLimits(apiService, coldRoomId);
                    }
                }

                @Override
                public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {
                    fallbackColdRoomLimits(apiService, coldRoomId);
                }
            });
        } else {
            apiService.getAllReadings().enqueue(new Callback<List<TemperatureReadingResponse>>() {
                @Override
                public void onResponse(Call<List<TemperatureReadingResponse>> call, Response<List<TemperatureReadingResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        calculateAndBindMetrics(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {}
            });
        }

        // 2. Carrega ocorrências reais (alertas ativos + resolvidos)
        Integer filterId = coldRoomId != null ? coldRoomId.intValue() : null;
        apiService.getAlerts(filterId).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                List<AlertResponse> active = (response.isSuccessful() && response.body() != null)
                        ? response.body() : new ArrayList<>();

                apiService.getAlertsHistory(filterId).enqueue(new Callback<List<AlertResponse>>() {
                    @Override
                    public void onResponse(Call<List<AlertResponse>> callH, Response<List<AlertResponse>> responseH) {
                        List<AlertResponse> history = (responseH.isSuccessful() && responseH.body() != null)
                                ? responseH.body() : new ArrayList<>();

                        List<AlertResponse> combined = new ArrayList<>();
                        combined.addAll(active);
                        combined.addAll(history);

                        bindOccurrences(combined);
                    }

                    @Override
                    public void onFailure(Call<List<AlertResponse>> callH, Throwable t) {
                        bindOccurrences(active);
                    }
                });
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                bindOccurrences(new ArrayList<>());
            }
        });
    }

    private void fallbackColdRoomLimits(SkadiApiService apiService, Long cId) {
        apiService.getColdRoomById(cId).enqueue(new Callback<ColdRoomResponse>() {
            @Override
            public void onResponse(Call<ColdRoomResponse> call, Response<ColdRoomResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ColdRoomResponse cr = response.body();
                    double min = cr.getMinimumTemperature() != null ? cr.getMinimumTemperature().doubleValue() : 0.0;
                    double max = cr.getMaximumTemperature() != null ? cr.getMaximumTemperature().doubleValue() : 4.0;
                    double avg = (min + max) / 2.0;
                    if (tvMetricMin != null) tvMetricMin.setText(String.format(Locale.getDefault(), "%.1f°C", min));
                    if (tvMetricAvg != null) tvMetricAvg.setText(String.format(Locale.getDefault(), "%.1f°C", avg));
                    if (tvMetricMax != null) tvMetricMax.setText(String.format(Locale.getDefault(), "%.1f°C", max));
                }
            }

            @Override
            public void onFailure(Call<ColdRoomResponse> call, Throwable t) {}
        });
    }

    private void calculateAndBindMetrics(List<TemperatureReadingResponse> readings) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double sum = 0.0;
        int count = 0;

        for (TemperatureReadingResponse r : readings) {
            if (r.getTemperature() != null) {
                double val = r.getTemperature().doubleValue();
                if (val < min) min = val;
                if (val > max) max = val;
                sum += val;
                count++;
            }
        }

        if (count > 0) {
            double avg = sum / count;
            if (tvMetricMin != null) tvMetricMin.setText(String.format(Locale.getDefault(), "%.1f°C", min));
            if (tvMetricAvg != null) tvMetricAvg.setText(String.format(Locale.getDefault(), "%.1f°C", avg));
            if (tvMetricMax != null) tvMetricMax.setText(String.format(Locale.getDefault(), "%.1f°C", max));
        }
    }

    private void bindOccurrences(List<AlertResponse> alerts) {
        if (containerOccurrences == null) return;
        containerOccurrences.removeAllViews();

        if (alerts == null || alerts.isEmpty()) {
            if (tvNoOccurrences != null) tvNoOccurrences.setVisibility(View.VISIBLE);
            return;
        }

        if (tvNoOccurrences != null) tvNoOccurrences.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (AlertResponse alert : alerts) {
            View row = inflater.inflate(R.layout.item_occurrence_row, containerOccurrences, false);

            TextView tvTitle = row.findViewById(R.id.tvOccurTitle);
            TextView tvTime = row.findViewById(R.id.tvOccurTime);
            TextView tvBadge = row.findViewById(R.id.tvOccurBadge);

            int camId = alert.getColdRoomId() != null ? alert.getColdRoomId() : 1;
            boolean isResolved = "RESOLVIDO".equalsIgnoreCase(alert.getStatus());
            boolean isCritical = "CRITICO".equalsIgnoreCase(alert.getSeverityLevel())
                    || "CRITICA".equalsIgnoreCase(alert.getSeverityLevel())
                    || "CRITICO".equalsIgnoreCase(alert.getCurrentLevel());

            if (isResolved) {
                tvTitle.setText("Câmara " + camId + " normalizada");
                tvBadge.setText("● Resolvido");
                tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.alert_success));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_stable);
            } else if (isCritical) {
                tvTitle.setText("Câmara " + camId + " em nível crítico");
                tvBadge.setText("● Crítico");
                tvBadge.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.alert_danger));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_critical);
            } else {
                tvTitle.setText("Câmara " + camId + " oscilação preventiva");
                tvBadge.setText("● Atenção");
                tvBadge.setTextColor(android.graphics.Color.parseColor("#D97706"));
                tvBadge.setBackgroundResource(R.drawable.bg_chip_filter_unselected);
            }

            if (alert.getCreatedAt() != null && !alert.getCreatedAt().isEmpty()) {
                tvTime.setText(alert.getCreatedAt().replace("T", " "));
            } else {
                tvTime.setText("Recente");
            }

            row.setOnClickListener(v -> {
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                if (alert.getId() != null) {
                    intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, alert.getId());
                }
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, isResolved);
                startActivity(intent);
            });

            containerOccurrences.addView(row);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));

        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnBaixarPdf.setOnClickListener(v -> {
            Intent intent = new Intent(this, GeneratedReportActivity.class);
            if (coldRoomId != null) {
                intent.putExtra(GeneratedReportActivity.EXTRA_COLD_ROOM_ID, coldRoomId);
            }
            intent.putExtra(GeneratedReportActivity.EXTRA_COLD_ROOM_NAME, coldRoomName);
            intent.putExtra(GeneratedReportActivity.EXTRA_CD_NAME, cdName);
            intent.putExtra(GeneratedReportActivity.EXTRA_DATE_START, dateStart);
            intent.putExtra(GeneratedReportActivity.EXTRA_DATE_END, dateEnd);
            startActivity(intent);
        });

        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
            finish();
        });

        navAlertas.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertsActivity.class));
            finish();
        });

        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navRelatorios.setOnClickListener(v -> finish());

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
            finish();
        });
    }
}
