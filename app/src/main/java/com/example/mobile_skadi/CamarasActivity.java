package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CamarasActivity extends AppCompatActivity {

    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private EditText etSearchCamaras;

    private TextView chipTodas;
    private TextView chipDentroFaixa;
    private TextView chipAcimaMax;
    private TextView chipAbaixoMin;

    private MaterialCardView cardCamara291;
    private MaterialCardView cardCamara61;
    private MaterialCardView cardCamara1;
    private MaterialCardView cardCamara521;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private enum FilterType {
        TODAS, DENTRO_DA_FAIXA, ACIMA_DA_MAX, ABAIXO_DA_MIN
    }

    private FilterType currentFilter = FilterType.TODAS;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camaras);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        etSearchCamaras = findViewById(R.id.etSearchCamaras);

        chipTodas = findViewById(R.id.chipTodas);
        chipDentroFaixa = findViewById(R.id.chipDentroFaixa);
        chipAcimaMax = findViewById(R.id.chipAcimaMax);
        chipAbaixoMin = findViewById(R.id.chipAbaixoMin);

        cardCamara291 = findViewById(R.id.cardCamara291);
        cardCamara61 = findViewById(R.id.cardCamara61);
        cardCamara1 = findViewById(R.id.cardCamara1);
        cardCamara521 = findViewById(R.id.cardCamara521);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        // Notificações e Perfil
        btnNotification.setOnClickListener(v -> {
            // TODO: Abrir tela/modal de Notificações
        });

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Detalhes das câmaras
        cardCamara291.setOnClickListener(v -> {
            openCamaraDetails("Câmara 291", "8,4°C", "0°C — 4°C", true);
        });

        cardCamara61.setOnClickListener(v -> {
            openCamaraDetails("Câmara 61", "2,3°C", "0°C — 2°C", true);
        });

        cardCamara1.setOnClickListener(v -> {
            openCamaraDetails("Câmara 1", "1,4°C", "0°C — 4°C", false);
        });

        cardCamara521.setOnClickListener(v -> {
            openCamaraDetails("Câmara 521", "3,7°C", "0°C — 4°C", false);
        });

        // Filtros (Chips)
        chipTodas.setOnClickListener(v -> selectFilter(FilterType.TODAS));
        chipDentroFaixa.setOnClickListener(v -> selectFilter(FilterType.DENTRO_DA_FAIXA));
        chipAcimaMax.setOnClickListener(v -> selectFilter(FilterType.ACIMA_DA_MAX));
        chipAbaixoMin.setOnClickListener(v -> selectFilter(FilterType.ABAIXO_DA_MIN));

        // Busca em tempo real
        etSearchCamaras.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bottom Navigation
        navCamaras.setOnClickListener(v -> {
            // Já na tela de câmaras
        });

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

    private void openCamaraDetails(String name, String currentTemp, String tempRange, boolean isCritical) {
        Intent intent = new Intent(this, CamaraDetailsActivity.class);
        intent.putExtra(CamaraDetailsActivity.EXTRA_CAMARA_NAME, name);
        intent.putExtra(CamaraDetailsActivity.EXTRA_CURRENT_TEMP, currentTemp);
        intent.putExtra(CamaraDetailsActivity.EXTRA_TEMP_RANGE, tempRange);
        intent.putExtra(CamaraDetailsActivity.EXTRA_IS_CRITICAL, isCritical);
        startActivity(intent);
    }

    private void selectFilter(FilterType filter) {
        this.currentFilter = filter;
        updateChipStyles();
        applyFilters();
    }

    private void updateChipStyles() {
        resetChip(chipTodas);
        resetChip(chipDentroFaixa);
        resetChip(chipAcimaMax);
        resetChip(chipAbaixoMin);

        switch (currentFilter) {
            case TODAS:
                setChipSelected(chipTodas);
                break;
            case DENTRO_DA_FAIXA:
                setChipSelected(chipDentroFaixa);
                break;
            case ACIMA_DA_MAX:
                setChipSelected(chipAcimaMax);
                break;
            case ABAIXO_DA_MIN:
                setChipSelected(chipAbaixoMin);
                break;
        }
    }

    private void resetChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_filter_unselected);
        chip.setTextColor(Color.parseColor("#64748B"));
    }

    private void setChipSelected(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_filter_selected);
        chip.setTextColor(Color.WHITE);
    }

    private void applyFilters() {
        // Câmaras e suas classificações:
        // 291: Acima da máx (8,4°C)
        // 61:  Acima da máx (2,3°C)
        // 1:   Dentro da faixa (1,4°C)
        // 521: Dentro da faixa (3,7°C)

        filterCard(cardCamara291, "câmara 291", FilterType.ACIMA_DA_MAX);
        filterCard(cardCamara61, "câmara 61", FilterType.ACIMA_DA_MAX);
        filterCard(cardCamara1, "câmara 1", FilterType.DENTRO_DA_FAIXA);
        filterCard(cardCamara521, "câmara 521", FilterType.DENTRO_DA_FAIXA);
    }

    private void filterCard(View card, String camaraName, FilterType cardCategory) {
        boolean matchesFilter = (currentFilter == FilterType.TODAS) || (currentFilter == cardCategory);
        boolean matchesSearch = currentSearchQuery.isEmpty() || camaraName.contains(currentSearchQuery);

        card.setVisibility((matchesFilter && matchesSearch) ? View.VISIBLE : View.GONE);
    }
}
