package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CamaraDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CAMARA_ID = "extra_camara_id";
    public static final String EXTRA_CAMARA_NAME = "extra_camara_name";
    public static final String EXTRA_CURRENT_TEMP = "extra_current_temp";
    public static final String EXTRA_TEMP_RANGE = "extra_temp_range";
    public static final String EXTRA_IS_CRITICAL = "extra_is_critical";

    private FrameLayout btnBack;
    private TextView tvCamaraTitle;
    private TextView tvCamaraSubtitle;
    private TextView tvCamaraStatusBadge;
    private TextView tvCurrentTemp;
    private TextView tvTempRange;
    private TextView tvStatusBoxCurrent;

    private TextView tvIdealMin;
    private TextView tvIdealAvg;
    private TextView tvIdealMax;

    private TextView tvGraphMin;
    private TextView tvGraphAvg;
    private TextView tvGraphMax;

    private TextView tvStatusMin;
    private TextView tvStatusAvg;
    private TextView tvStatusMax;

    private TemperatureChartView chartCamaraDetails;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private double minLimit = 0.0;
    private double maxLimit = 4.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara_details);

        initViews();
        bindCamaraData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCamaraTitle = findViewById(R.id.tvCamaraTitle);
        tvCamaraSubtitle = findViewById(R.id.tvCamaraSubtitle);
        if (tvCamaraSubtitle != null) tvCamaraSubtitle.setText(com.example.mobile_skadi.network.SessionManager.getInstance(this).getUnit());
        tvCamaraStatusBadge = findViewById(R.id.tvCamaraStatusBadge);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvTempRange = findViewById(R.id.tvTempRange);
        tvStatusBoxCurrent = findViewById(R.id.tvStatusBoxCurrent);

        tvIdealMin = findViewById(R.id.tvIdealMin);
        tvIdealAvg = findViewById(R.id.tvIdealAvg);
        tvIdealMax = findViewById(R.id.tvIdealMax);

        tvGraphMin = findViewById(R.id.tvGraphMin);
        tvGraphAvg = findViewById(R.id.tvGraphAvg);
        tvGraphMax = findViewById(R.id.tvGraphMax);

        tvStatusMin = findViewById(R.id.tvStatusMin);
        tvStatusAvg = findViewById(R.id.tvStatusAvg);
        tvStatusMax = findViewById(R.id.tvStatusMax);

        chartCamaraDetails = findViewById(R.id.chartCamaraDetails);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void bindCamaraData() {
        Intent intent = getIntent();
        if (intent == null) return;

        long camaraId = intent.getLongExtra(EXTRA_CAMARA_ID, -1L);
        String camaraName = intent.getStringExtra(EXTRA_CAMARA_NAME);
        String currentTemp = intent.getStringExtra(EXTRA_CURRENT_TEMP);
        String tempRange = intent.getStringExtra(EXTRA_TEMP_RANGE);
        boolean isCritical = intent.getBooleanExtra(EXTRA_IS_CRITICAL, false);

        if (camaraName != null && !camaraName.isEmpty()) {
            tvCamaraTitle.setText(camaraName);
        }
        if (currentTemp != null && !currentTemp.isEmpty()) {
            tvCurrentTemp.setText(currentTemp);
            if (tvStatusBoxCurrent != null) tvStatusBoxCurrent.setText(currentTemp);
        }
        if (tempRange != null && !tempRange.isEmpty()) {
            tvTempRange.setText(tempRange);
        }

        updateStatusBadge(isCritical);

        if (camaraId > 0) {
            loadCamaraDetailsFromApi(camaraId);
        }
    }

    private void updateStatusBadge(boolean isCritical) {
        if (isCritical) {
            tvCamaraStatusBadge.setText("● Crítico");
            tvCamaraStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
            tvCamaraStatusBadge.setBackgroundResource(R.drawable.bg_badge_critical);
            tvCurrentTemp.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
        } else {
            tvCamaraStatusBadge.setText("● Estável");
            tvCamaraStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.alert_success));
            tvCamaraStatusBadge.setBackgroundResource(R.drawable.bg_badge_stable);
            tvCurrentTemp.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        }
    }

    private void loadCamaraDetailsFromApi(long id) {
        SkadiApiService apiService = RetrofitClient.getApiService(this);

        // 1. Dados cadastrais da câmara
        apiService.getColdRoomById(id).enqueue(new Callback<ColdRoomResponse>() {
            @Override
            public void onResponse(Call<ColdRoomResponse> call, Response<ColdRoomResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ColdRoomResponse camara = response.body();
                    if (camara.getModel() != null && !camara.getModel().isEmpty()) {
                        tvCamaraTitle.setText(camara.getModel());
                    }
                    if (camara.getDistributionCenterName() != null && !camara.getDistributionCenterName().isEmpty()) {
                        if (tvCamaraSubtitle != null) {
                            tvCamaraSubtitle.setText(camara.getDistributionCenterName());
                        }
                    } else if (camara.getLocation() != null && !camara.getLocation().isEmpty()) {
                        if (tvCamaraSubtitle != null) {
                            tvCamaraSubtitle.setText(camara.getLocation());
                        }
                    }

                    if (camara.getMinimumTemperature() != null) {
                        minLimit = camara.getMinimumTemperature().doubleValue();
                    }
                    if (camara.getMaximumTemperature() != null) {
                        maxLimit = camara.getMaximumTemperature().doubleValue();
                    }

                    double avgLimit = (minLimit + maxLimit) / 2.0;

                    tvTempRange.setText(String.format(Locale.getDefault(), "%.2f°C – %.2f°C", minLimit, maxLimit));

                    if (tvIdealMin != null) tvIdealMin.setText(String.format(Locale.getDefault(), "%.1f°C", minLimit));
                    if (tvIdealAvg != null) tvIdealAvg.setText(String.format(Locale.getDefault(), "%.1f°C", avgLimit));
                    if (tvIdealMax != null) tvIdealMax.setText(String.format(Locale.getDefault(), "%.1f°C", maxLimit));

                    if (tvStatusMin != null) tvStatusMin.setText(String.format(Locale.getDefault(), "%.1f°C", minLimit));
                    if (tvStatusAvg != null) tvStatusAvg.setText(String.format(Locale.getDefault(), "%.1f°C", avgLimit));
                    if (tvStatusMax != null) tvStatusMax.setText(String.format(Locale.getDefault(), "%.1f°C", maxLimit));
                }
            }

            @Override
            public void onFailure(Call<ColdRoomResponse> call, Throwable t) {}
        });

        // 2. Temperatura atual em tempo real
        apiService.getCurrentTemperature(id).enqueue(new Callback<TemperatureReadingResponse>() {
            @Override
            public void onResponse(Call<TemperatureReadingResponse> call, Response<TemperatureReadingResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTemperature() != null) {
                    double curr = response.body().getTemperature().doubleValue();
                    String currStr = String.format(Locale.getDefault(), "%.1f°C", curr);
                    tvCurrentTemp.setText(currStr);
                    if (tvStatusBoxCurrent != null) tvStatusBoxCurrent.setText(currStr);

                    boolean isCritical = curr < minLimit || curr > maxLimit;
                    updateStatusBadge(isCritical);
                }
            }

            @Override
            public void onFailure(Call<TemperatureReadingResponse> call, Throwable t) {}
        });

        // 3. Histórico de leituras das últimas 24 horas
        apiService.getColdRoomReadings(id).enqueue(new Callback<List<TemperatureReadingResponse>>() {
            @Override
            public void onResponse(Call<List<TemperatureReadingResponse>> call, Response<List<TemperatureReadingResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<TemperatureReadingResponse> readings = response.body();
                    double min = Double.MAX_VALUE;
                    double max = -Double.MAX_VALUE;
                    double sum = 0.0;
                    int count = 0;

                    List<Float> points = new ArrayList<>();

                    for (TemperatureReadingResponse r : readings) {
                        if (r.getTemperature() != null) {
                            double val = r.getTemperature().doubleValue();
                            if (val < min) min = val;
                            if (val > max) max = val;
                            sum += val;
                            count++;
                            points.add((float) val);
                        }
                    }

                    if (count > 0) {
                        double avg = sum / count;
                        if (tvGraphMin != null) tvGraphMin.setText(String.format(Locale.getDefault(), "%.1f °C", min));
                        if (tvGraphAvg != null) tvGraphAvg.setText(String.format(Locale.getDefault(), "%.1f °C", avg));
                        if (tvGraphMax != null) tvGraphMax.setText(String.format(Locale.getDefault(), "%.1f °C", max));

                        if (tvStatusMin != null) tvStatusMin.setText(String.format(Locale.getDefault(), "%.1f°C", min));
                        if (tvStatusAvg != null) tvStatusAvg.setText(String.format(Locale.getDefault(), "%.1f°C", avg));
                        if (tvStatusMax != null) tvStatusMax.setText(String.format(Locale.getDefault(), "%.1f°C", max));

                        if (chartCamaraDetails != null && !points.isEmpty()) {
                            chartCamaraDetails.setDataPoints(points);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {}
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        navCamaras.setOnClickListener(v -> finish());

        navAlertas.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertsActivity.class));
        });

        navInicio.setOnClickListener(v -> {
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
}
