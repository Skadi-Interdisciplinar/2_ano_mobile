package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.data.SkadiDatabaseHelper;
import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CamarasActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L; // 5 minutos (sincronizado com o Arduino)

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadCamarasFromApi();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

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

    private TextView tvNameCamara291, tvTempCamara291, tvRangeCamara291;
    private TextView tvNameCamara61, tvTempCamara61, tvRangeCamara61;
    private TextView tvNameCamara1, tvTempCamara1, tvRangeCamara1;
    private TextView tvNameCamara521, tvTempCamara521, tvRangeCamara521;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private final Map<Long, TemperatureReadingResponse> readingsMap = new HashMap<>();

    enum FilterType {
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

    @Override
    protected void onResume() {
        super.onResume();
        com.example.mobile_skadi.utils.AvatarManager.applyAvatar(this, btnProfileAvatar);
        loadCamarasFromApi();
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
        etSearchCamaras = findViewById(R.id.etSearchCamaras);

        chipTodas = findViewById(R.id.chipTodas);
        chipDentroFaixa = findViewById(R.id.chipDentroFaixa);
        chipAcimaMax = findViewById(R.id.chipAcimaMax);
        chipAbaixoMin = findViewById(R.id.chipAbaixoMin);

        cardCamara291 = findViewById(R.id.cardCamara291);
        cardCamara61 = findViewById(R.id.cardCamara61);
        cardCamara1 = findViewById(R.id.cardCamara1);
        cardCamara521 = findViewById(R.id.cardCamara521);

        tvNameCamara291 = findViewById(R.id.tvNameCamara291);
        tvTempCamara291 = findViewById(R.id.tvTempCamara291);
        tvRangeCamara291 = findViewById(R.id.tvRangeCamara291);

        tvNameCamara61 = findViewById(R.id.tvNameCamara61);
        tvTempCamara61 = findViewById(R.id.tvTempCamara61);
        tvRangeCamara61 = findViewById(R.id.tvRangeCamara61);

        tvNameCamara1 = findViewById(R.id.tvNameCamara1);
        tvTempCamara1 = findViewById(R.id.tvTempCamara1);
        tvRangeCamara1 = findViewById(R.id.tvRangeCamara1);

        tvNameCamara521 = findViewById(R.id.tvNameCamara521);
        tvTempCamara521 = findViewById(R.id.tvTempCamara521);
        tvRangeCamara521 = findViewById(R.id.tvRangeCamara521);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        // Notificações e Perfil
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Detalhes das câmaras (padrão com fallback)
        cardCamara291.setOnClickListener(v -> openCamaraDetails(null, "Câmara 291", "8,4°C", "0°C — 4°C", true));
        cardCamara61.setOnClickListener(v -> openCamaraDetails(null, "Câmara 61", "2,3°C", "0°C — 2°C", true));
        cardCamara1.setOnClickListener(v -> openCamaraDetails(null, "Câmara 1", "1,4°C", "0°C — 4°C", false));
        cardCamara521.setOnClickListener(v -> openCamaraDetails(null, "Câmara 521", "3,7°C", "0°C — 4°C", false));

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
        navCamaras.setOnClickListener(v -> { /* Já na tela de câmaras */ });
        navAlertas.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        navRelatorios.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        navTrimmy.setOnClickListener(v -> startActivity(new Intent(this, TrimmyActivity.class)));
    }

    private void loadCamarasFromApi() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        SkadiDatabaseHelper dbHelper = SkadiDatabaseHelper.getInstance(this);

        // Carrega as leituras de temperatura primeiro ou em paralelo
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
                fetchColdRooms(apiService, dbHelper);
            }

            @Override
            public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {
                readingsMap.clear();
                readingsMap.putAll(dbHelper.getCachedReadingsMap());
                fetchColdRooms(apiService, dbHelper);
            }
        });
    }

    private void fetchColdRooms(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
        apiService.getColdRooms().enqueue(new Callback<List<ColdRoomResponse>>() {
            @Override
            public void onResponse(Call<List<ColdRoomResponse>> call, Response<List<ColdRoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    dbHelper.saveColdRooms(response.body());
                    bindColdRoomsFromApi(response.body());
                } else {
                    List<ColdRoomResponse> cached = dbHelper.getCachedColdRooms();
                    if (!cached.isEmpty()) {
                        bindColdRoomsFromApi(cached);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ColdRoomResponse>> call, Throwable t) {
                List<ColdRoomResponse> cached = dbHelper.getCachedColdRooms();
                if (!cached.isEmpty()) {
                    bindColdRoomsFromApi(cached);
                }
            }
        });
    }

    private void bindColdRoomsFromApi(List<ColdRoomResponse> coldRooms) {
        if (!coldRooms.isEmpty()) {
            ColdRoomResponse cr0 = coldRooms.get(0);
            bindRoomCard(cardCamara291, tvNameCamara291, tvTempCamara291, tvRangeCamara291, cr0, "--°C");
            cardCamara291.setVisibility(View.VISIBLE);
        } else {
            if (cardCamara291 != null) cardCamara291.setVisibility(View.GONE);
        }

        if (coldRooms.size() > 1) {
            ColdRoomResponse cr1 = coldRooms.get(1);
            bindRoomCard(cardCamara61, tvNameCamara61, tvTempCamara61, tvRangeCamara61, cr1, "--°C");
            cardCamara61.setVisibility(View.VISIBLE);
        } else {
            if (cardCamara61 != null) cardCamara61.setVisibility(View.GONE);
        }

        if (coldRooms.size() > 2) {
            ColdRoomResponse cr2 = coldRooms.get(2);
            bindRoomCard(cardCamara1, tvNameCamara1, tvTempCamara1, tvRangeCamara1, cr2, "--°C");
            cardCamara1.setVisibility(View.VISIBLE);
        } else {
            if (cardCamara1 != null) cardCamara1.setVisibility(View.GONE);
        }

        if (coldRooms.size() > 3) {
            ColdRoomResponse cr3 = coldRooms.get(3);
            bindRoomCard(cardCamara521, tvNameCamara521, tvTempCamara521, tvRangeCamara521, cr3, "--°C");
            cardCamara521.setVisibility(View.VISIBLE);
        } else {
            if (cardCamara521 != null) cardCamara521.setVisibility(View.GONE);
        }
    }

    private void bindRoomCard(MaterialCardView card, TextView tvName, TextView tvTemp, TextView tvRange,
                              ColdRoomResponse cr, String defaultTemp) {
        if (card == null) return;

        String name = cr.getLocation() != null && !cr.getLocation().isEmpty() ? cr.getLocation() :
                (cr.getModel() != null ? cr.getModel() : "Câmara " + cr.getId());
        if (tvName != null) tvName.setText(name);

        String rangeStr = "";
        if (cr.getMinimumTemperature() != null && cr.getMaximumTemperature() != null) {
            rangeStr = "Mín. " + cr.getMinimumTemperature() + "°C | Máx. " + cr.getMaximumTemperature() + "°C";
            if (tvRange != null) tvRange.setText(rangeStr);
        }

        TemperatureReadingResponse reading = cr.getId() != null ? readingsMap.get(cr.getId()) : null;
        String tempDisplay = defaultTemp;
        boolean isCritical = true;

        if (reading != null && reading.getTemperature() != null) {
            tempDisplay = String.format(Locale.getDefault(), "%.1f°C", reading.getTemperature());
            if (tvTemp != null) tvTemp.setText(tempDisplay);

            if (cr.getMaximumTemperature() != null && reading.getTemperature().compareTo(cr.getMaximumTemperature()) > 0) {
                isCritical = true;
            } else if (cr.getMinimumTemperature() != null && reading.getTemperature().compareTo(cr.getMinimumTemperature()) < 0) {
                isCritical = true;
            } else {
                isCritical = false;
            }
        }

        final String finalTemp = tempDisplay;
        final String finalRange = rangeStr;
        final boolean finalCritical = isCritical;
        card.setOnClickListener(v -> openCamaraDetails(cr.getId(), name, finalTemp, finalRange, finalCritical));
    }

    private void openCamaraDetails(Long id, String name, String currentTemp, String tempRange, boolean isCritical) {
        Intent intent = new Intent(this, CamaraDetailsActivity.class);
        if (id != null) {
            intent.putExtra(CamaraDetailsActivity.EXTRA_CAMARA_ID, id);
        }
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
        filterCard(cardCamara291, tvNameCamara291 != null ? tvNameCamara291.getText().toString() : "câmara 291", FilterType.ACIMA_DA_MAX);
        filterCard(cardCamara61, tvNameCamara61 != null ? tvNameCamara61.getText().toString() : "câmara 61", FilterType.ACIMA_DA_MAX);
        filterCard(cardCamara1, tvNameCamara1 != null ? tvNameCamara1.getText().toString() : "câmara 1", FilterType.DENTRO_DA_FAIXA);
        filterCard(cardCamara521, tvNameCamara521 != null ? tvNameCamara521.getText().toString() : "câmara 521", FilterType.DENTRO_DA_FAIXA);
    }

    private void filterCard(View card, String camaraName, FilterType cardCategory) {
        if (card == null) return;
        boolean visible = shouldShowCamara(camaraName, cardCategory, currentFilter, currentSearchQuery);
        card.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    static boolean shouldShowCamara(String camaraName, FilterType cardCategory, FilterType activeFilter, String searchQuery) {
        boolean matchesFilter = (activeFilter == FilterType.TODAS) || (activeFilter == cardCategory);
        String normalizedQuery = searchQuery != null ? searchQuery.trim().toLowerCase() : "";
        boolean matchesSearch = normalizedQuery.isEmpty() || (camaraName != null && camaraName.toLowerCase().contains(normalizedQuery));
        return matchesFilter && matchesSearch;
    }
}
