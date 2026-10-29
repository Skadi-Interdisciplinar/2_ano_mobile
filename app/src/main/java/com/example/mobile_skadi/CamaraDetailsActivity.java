package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CamaraDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CAMARA_NAME = "extra_camara_name";
    public static final String EXTRA_CURRENT_TEMP = "extra_current_temp";
    public static final String EXTRA_TEMP_RANGE = "extra_temp_range";
    public static final String EXTRA_IS_CRITICAL = "extra_is_critical";

    private FrameLayout btnBack;
    private TextView tvCamaraTitle;
    private TextView tvCamaraStatusBadge;
    private TextView tvCurrentTemp;
    private TextView tvTempRange;
    private TextView tvStatusBoxCurrent;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

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
        tvCamaraStatusBadge = findViewById(R.id.tvCamaraStatusBadge);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvTempRange = findViewById(R.id.tvTempRange);
        tvStatusBoxCurrent = findViewById(R.id.tvStatusBoxCurrent);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void bindCamaraData() {
        Intent intent = getIntent();
        if (intent == null) return;

        String camaraName = intent.getStringExtra(EXTRA_CAMARA_NAME);
        String currentTemp = intent.getStringExtra(EXTRA_CURRENT_TEMP);
        String tempRange = intent.getStringExtra(EXTRA_TEMP_RANGE);
        boolean isCritical = intent.getBooleanExtra(EXTRA_IS_CRITICAL, false);

        if (camaraName != null && !camaraName.isEmpty()) {
            tvCamaraTitle.setText(camaraName);
        }
        if (currentTemp != null && !currentTemp.isEmpty()) {
            tvCurrentTemp.setText(currentTemp);
            tvStatusBoxCurrent.setText(currentTemp);
        }
        if (tempRange != null && !tempRange.isEmpty()) {
            tvTempRange.setText(tempRange);
        }

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
