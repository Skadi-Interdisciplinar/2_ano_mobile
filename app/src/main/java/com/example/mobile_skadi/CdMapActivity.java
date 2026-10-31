package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.DistributionCenterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CdMapActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private LinearLayout pinCdMatriz;
    private LinearLayout pinCdNorte;
    private LinearLayout pinCdSul;

    private LinearLayout layoutEmptySelection;
    private RelativeLayout layoutSelectedCdDetails;
    private TextView tvSelectedCdTitle;
    private TextView tvSelectedCdTemp;
    private TextView tvSelectedCdCamaras;
    private TextView tvSelectedCdAlerts;
    private TextView tvSelectedCdBadge;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cd_map);

        initViews();
        setupListeners();
        loadDistributionCentersFromApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        pinCdMatriz = findViewById(R.id.pinCdMatriz);
        pinCdNorte = findViewById(R.id.pinCdNorte);
        pinCdSul = findViewById(R.id.pinCdSul);

        layoutEmptySelection = findViewById(R.id.layoutEmptySelection);
        layoutSelectedCdDetails = findViewById(R.id.layoutSelectedCdDetails);
        tvSelectedCdTitle = findViewById(R.id.tvSelectedCdTitle);
        tvSelectedCdTemp = findViewById(R.id.tvSelectedCdTemp);
        tvSelectedCdCamaras = findViewById(R.id.tvSelectedCdCamaras);
        tvSelectedCdAlerts = findViewById(R.id.tvSelectedCdAlerts);
        tvSelectedCdBadge = findViewById(R.id.tvSelectedCdBadge);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Marcadores padrão com fallback
        pinCdMatriz.setOnClickListener(v -> showCdDetails(
                "Centro de Distribuição - Matriz",
                "Temperatura média atual: 1,0°C",
                "Total de câmaras: 12",
                "Alertas em aberto no momento: 0",
                false
        ));

        pinCdNorte.setOnClickListener(v -> showCdDetails(
                "Centro de Distribuição - Belterra Norte",
                "Temperatura média atual: 4,2°C",
                "Total de câmaras: 8",
                "Alertas em aberto no momento: 2",
                true
        ));

        pinCdSul.setOnClickListener(v -> showCdDetails(
                "Centro de Distribuição - Unidade Sul",
                "Temperatura média atual: 1,6°C",
                "Total de câmaras: 10",
                "Alertas em aberto no momento: 0",
                false
        ));

        // Bottom Navigation
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

        navRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
            finish();
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
            finish();
        });
    }

    private void loadDistributionCentersFromApi() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        apiService.getDistributionCenters().enqueue(new Callback<List<DistributionCenterResponse>>() {
            @Override
            public void onResponse(Call<List<DistributionCenterResponse>> call, Response<List<DistributionCenterResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bindDistributionCenters(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<DistributionCenterResponse>> call, Throwable t) {
                // Silencioso: fallback local ativo
            }
        });
    }

    private void bindDistributionCenters(List<DistributionCenterResponse> cds) {
        if (cds.size() > 0) {
            DistributionCenterResponse cd0 = cds.get(0);
            pinCdMatriz.setOnClickListener(v -> showCdDetails(
                    cd0.getName() != null ? cd0.getName() : "Centro de Distribuição - Matriz",
                    "Temperatura média atual: 1,0°C",
                    "Total de câmaras: 12",
                    "Alertas em aberto no momento: 0",
                    false
            ));
        }

        if (cds.size() > 1) {
            DistributionCenterResponse cd1 = cds.get(1);
            pinCdNorte.setOnClickListener(v -> showCdDetails(
                    cd1.getName() != null ? cd1.getName() : "Centro de Distribuição - Belterra Norte",
                    "Temperatura média atual: 4,2°C",
                    "Total de câmaras: 8",
                    "Alertas em aberto no momento: 2",
                    true
            ));
        }

        if (cds.size() > 2) {
            DistributionCenterResponse cd2 = cds.get(2);
            pinCdSul.setOnClickListener(v -> showCdDetails(
                    cd2.getName() != null ? cd2.getName() : "Centro de Distribuição - Unidade Sul",
                    "Temperatura média atual: 1,6°C",
                    "Total de câmaras: 10",
                    "Alertas em aberto no momento: 0",
                    false
            ));
        }
    }

    private void showCdDetails(String title, String temp, String camaras, String alerts, boolean isCritical) {
        layoutEmptySelection.setVisibility(View.GONE);
        layoutSelectedCdDetails.setVisibility(View.VISIBLE);

        tvSelectedCdTitle.setText(title);
        tvSelectedCdTemp.setText(temp);
        tvSelectedCdCamaras.setText(camaras);
        tvSelectedCdAlerts.setText(alerts);

        if (isCritical) {
            tvSelectedCdBadge.setText("● Atenção");
            tvSelectedCdBadge.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
            tvSelectedCdBadge.setBackgroundResource(R.drawable.bg_badge_critical);
        } else {
            tvSelectedCdBadge.setText("● Operando Normal");
            tvSelectedCdBadge.setTextColor(ContextCompat.getColor(this, R.color.alert_success));
            tvSelectedCdBadge.setBackgroundResource(R.drawable.bg_badge_stable);
        }
    }
}
