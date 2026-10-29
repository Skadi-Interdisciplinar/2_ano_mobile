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

        // Marcadores do mapa de CDs
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

        navInicio.setOnClickListener(v -> finish());

        navRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
            finish();
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
            finish();
        });
    }

    private void showCdDetails(String title, String temp, String camaras, String alerts, boolean isCritical) {
        layoutEmptySelection.setVisibility(View.GONE);
        layoutSelectedCdDetails.setVisibility(View.VISIBLE);

        tvSelectedCdTitle.setText(title);
        tvSelectedCdTemp.setText(temp);
        tvSelectedCdCamaras.setText(camaras);
        tvSelectedCdAlerts.setText(alerts);

        if (isCritical) {
            tvSelectedCdBadge.setText("● Crítico");
            tvSelectedCdBadge.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
            tvSelectedCdBadge.setBackgroundResource(R.drawable.bg_badge_critical);
        } else {
            tvSelectedCdBadge.setText("● Estável");
            tvSelectedCdBadge.setTextColor(ContextCompat.getColor(this, R.color.alert_success));
            tvSelectedCdBadge.setBackgroundResource(R.drawable.bg_badge_stable);
        }
    }
}
