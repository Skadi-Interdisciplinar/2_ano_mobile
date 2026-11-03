package com.example.mobile_skadi;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.data.SkadiDatabaseHelper;
import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.DistributionCenterResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private TextView tvCentroDistribuicao;
    private LinearLayout spnCamara;
    private TextView tvSelectedCamara;
    private LinearLayout btnDataInicial;
    private TextView tvDataInicial;
    private LinearLayout btnDataFinal;
    private TextView tvDataFinal;
    private TextView btnOrdenar;
    private LinearLayout layoutAlertsHistoryList;
    private TextView tvEmptyAlertsHistory;
    private MaterialButton btnVisualizarAnaliseCompleta;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat alertInputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat itemDateFormat = new SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault());

    private Calendar calDataInicial = Calendar.getInstance();
    private Calendar calDataFinal = Calendar.getInstance();

    private final List<ColdRoomResponse> coldRoomsList = new ArrayList<>();
    private final Map<Long, ColdRoomResponse> coldRoomsMap = new HashMap<>();
    private final Map<Long, TemperatureReadingResponse> readingsMap = new HashMap<>();
    private final List<AlertResponse> allAlertsList = new ArrayList<>();

    private Long selectedColdRoomId = null;
    private boolean isSortDescending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        initViews();
        setupDates();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.example.mobile_skadi.utils.AvatarManager.applyAvatar(this, btnProfileAvatar);
        loadData();
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        tvCentroDistribuicao = findViewById(R.id.tvCentroDistribuicao);
        spnCamara = findViewById(R.id.spnCamara);
        tvSelectedCamara = findViewById(R.id.tvSelectedCamara);
        btnDataInicial = findViewById(R.id.btnDataInicial);
        tvDataInicial = findViewById(R.id.tvDataInicial);
        btnDataFinal = findViewById(R.id.btnDataFinal);
        tvDataFinal = findViewById(R.id.tvDataFinal);
        btnOrdenar = findViewById(R.id.btnOrdenar);
        layoutAlertsHistoryList = findViewById(R.id.layoutAlertsHistoryList);
        tvEmptyAlertsHistory = findViewById(R.id.tvEmptyAlertsHistory);
        btnVisualizarAnaliseCompleta = findViewById(R.id.btnVisualizarAnaliseCompleta);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);

        UserRoleManager.applyBottomNavForRole(this);

        String unit = SessionManager.getInstance(this).getUnit();
        if (unit != null && !unit.isEmpty()) {
            tvCentroDistribuicao.setText(unit);
        }
    }

    private void setupDates() {
        // Data final = Hoje
        calDataFinal = Calendar.getInstance();
        // Data inicial = 7 dias atrás
        calDataInicial = Calendar.getInstance();
        calDataInicial.add(Calendar.DAY_OF_YEAR, -7);

        tvDataInicial.setText(displayDateFormat.format(calDataInicial.getTime()));
        tvDataFinal.setText(displayDateFormat.format(calDataFinal.getTime()));
    }

    private void setupListeners() {
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        btnProfileAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        spnCamara.setOnClickListener(v -> showColdRoomPicker());

        btnDataInicial.setOnClickListener(v -> showDatePicker(true));
        btnDataFinal.setOnClickListener(v -> showDatePicker(false));

        btnOrdenar.setOnClickListener(v -> {
            isSortDescending = !isSortDescending;
            btnOrdenar.setText(isSortDescending ? "Mais recentes" : "Mais antigos");
            renderAlertsList();
        });

        btnVisualizarAnaliseCompleta.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullAnalysisActivity.class);
            if (selectedColdRoomId != null) {
                intent.putExtra(FullAnalysisActivity.EXTRA_COLD_ROOM_ID, selectedColdRoomId);
                intent.putExtra(FullAnalysisActivity.EXTRA_COLD_ROOM_NAME, tvSelectedCamara.getText().toString());
            }
            intent.putExtra(FullAnalysisActivity.EXTRA_CD_NAME, "CD Manaus - Unidade 20");
            intent.putExtra(FullAnalysisActivity.EXTRA_DATE_START, isoDateFormat.format(calDataInicial.getTime()));
            intent.putExtra(FullAnalysisActivity.EXTRA_DATE_END, isoDateFormat.format(calDataFinal.getTime()));
            startActivity(intent);
        });

        // Bottom Navigation
        navCamaras.setOnClickListener(v -> startActivity(new Intent(this, CamarasActivity.class)));
        navAlertas.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        navInicio.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        navRelatorios.setOnClickListener(v -> { /* Já nesta tela */ });
        navTrimmy.setOnClickListener(v -> startActivity(new Intent(this, TrimmyActivity.class)));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = isStartDate ? calDataInicial : calDataFinal;
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        calDataInicial = picked;
                        tvDataInicial.setText(displayDateFormat.format(picked.getTime()));
                    } else {
                        calDataFinal = picked;
                        tvDataFinal.setText(displayDateFormat.format(picked.getTime()));
                    }
                    renderAlertsList();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showColdRoomPicker() {
        if (coldRoomsList.isEmpty()) {
            Toast.makeText(this, "Nenhuma câmara disponível.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> items = new ArrayList<>();
        items.add("Todas as câmaras");
        int selectedIndex = 0;

        for (int i = 0; i < coldRoomsList.size(); i++) {
            ColdRoomResponse cr = coldRoomsList.get(i);
            String title = (cr.getLocation() != null && !cr.getLocation().isEmpty()) ?
                    cr.getLocation() : ("Câmara " + cr.getId());
            items.add(title);
            if (selectedColdRoomId != null && selectedColdRoomId.equals(cr.getId())) {
                selectedIndex = i + 1;
            }
        }

        CharSequence[] charSequenceItems = items.toArray(new CharSequence[0]);

        new AlertDialog.Builder(this)
                .setTitle("Selecione a câmara")
                .setSingleChoiceItems(charSequenceItems, selectedIndex, (dialog, which) -> {
                    if (which == 0) {
                        selectedColdRoomId = null;
                        tvSelectedCamara.setText("Todas");
                    } else {
                        ColdRoomResponse cr = coldRoomsList.get(which - 1);
                        selectedColdRoomId = cr.getId();
                        tvSelectedCamara.setText(charSequenceItems[which]);
                    }
                    dialog.dismiss();
                    renderAlertsList();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadData() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        SkadiDatabaseHelper dbHelper = SkadiDatabaseHelper.getInstance(this);

        // 1. Centro de Distribuição
        apiService.getDistributionCenters().enqueue(new Callback<List<DistributionCenterResponse>>() {
            @Override
            public void onResponse(Call<List<DistributionCenterResponse>> call, Response<List<DistributionCenterResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String cdName = response.body().get(0).getName();
                    if (cdName != null && !cdName.isEmpty()) {
                        tvCentroDistribuicao.setText(cdName);
                        SessionManager.getInstance(ReportsActivity.this).saveSession(
                                SessionManager.getInstance(ReportsActivity.this).getToken(),
                                SessionManager.getInstance(ReportsActivity.this).getUsername(),
                                SessionManager.getInstance(ReportsActivity.this).getRole(),
                                cdName
                        );
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DistributionCenterResponse>> call, Throwable t) {
                // Mantém unidade salva
            }
        });

        // 2. Câmaras Frigoríficas
        apiService.getColdRooms().enqueue(new Callback<List<ColdRoomResponse>>() {
            @Override
            public void onResponse(Call<List<ColdRoomResponse>> call, Response<List<ColdRoomResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    coldRoomsList.clear();
                    coldRoomsMap.clear();
                    coldRoomsList.addAll(response.body());
                    for (ColdRoomResponse cr : response.body()) {
                        coldRoomsMap.put(cr.getId(), cr);
                    }
                    dbHelper.saveColdRooms(response.body());
                } else {
                    loadColdRoomsFromCache(dbHelper);
                }
                loadReadings(apiService, dbHelper);
            }

            @Override
            public void onFailure(Call<List<ColdRoomResponse>> call, Throwable t) {
                loadColdRoomsFromCache(dbHelper);
                loadReadings(apiService, dbHelper);
            }
        });
    }

    private void loadColdRoomsFromCache(SkadiDatabaseHelper dbHelper) {
        List<ColdRoomResponse> cached = dbHelper.getCachedColdRooms();
        if (!cached.isEmpty()) {
            coldRoomsList.clear();
            coldRoomsMap.clear();
            coldRoomsList.addAll(cached);
            for (ColdRoomResponse cr : cached) {
                coldRoomsMap.put(cr.getId(), cr);
            }
        }
    }

    private void loadReadings(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
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
                loadAlerts(apiService, dbHelper);
            }

            @Override
            public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {
                readingsMap.clear();
                readingsMap.putAll(dbHelper.getCachedReadingsMap());
                loadAlerts(apiService, dbHelper);
            }
        });
    }

    private void loadAlerts(SkadiApiService apiService, SkadiDatabaseHelper dbHelper) {
        apiService.getAlerts(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allAlertsList.clear();
                    allAlertsList.addAll(response.body());
                    dbHelper.saveAlerts(response.body());
                } else {
                    allAlertsList.clear();
                    allAlertsList.addAll(dbHelper.getCachedAlerts());
                }
                renderAlertsList();
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                allAlertsList.clear();
                allAlertsList.addAll(dbHelper.getCachedAlerts());
                renderAlertsList();
            }
        });
    }

    private void renderAlertsList() {
        if (layoutAlertsHistoryList == null) return;
        layoutAlertsHistoryList.removeAllViews();

        List<AlertResponse> filtered = new ArrayList<>();

        // Configura início e fim do dia para filtro de datas
        Calendar calStart = (Calendar) calDataInicial.clone();
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = (Calendar) calDataFinal.clone();
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);

        Date startDate = calStart.getTime();
        Date endDate = calEnd.getTime();

        for (AlertResponse alert : allAlertsList) {
            // Filtro por câmara
            if (selectedColdRoomId != null && alert.getColdRoomId() != null) {
                if (!selectedColdRoomId.equals(alert.getColdRoomId().longValue())) {
                    continue;
                }
            }

            // Filtro por data
            Date alertDate = parseAlertDate(alert.getCreatedAt());
            if (alertDate != null) {
                if (alertDate.before(startDate) || alertDate.after(endDate)) {
                    continue;
                }
            }

            filtered.add(alert);
        }

        // Ordenação
        Collections.sort(filtered, (a1, a2) -> {
            Date d1 = parseAlertDate(a1.getCreatedAt());
            Date d2 = parseAlertDate(a2.getCreatedAt());
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;
            return isSortDescending ? d2.compareTo(d1) : d1.compareTo(d2);
        });

        if (filtered.isEmpty()) {
            if (tvEmptyAlertsHistory != null) tvEmptyAlertsHistory.setVisibility(View.VISIBLE);
            return;
        }

        if (tvEmptyAlertsHistory != null) tvEmptyAlertsHistory.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (AlertResponse alert : filtered) {
            View itemView = inflater.inflate(R.layout.item_alert_history, layoutAlertsHistoryList, false);

            TextView tvTitle = itemView.findViewById(R.id.tvAlertHistoryTitle);
            TextView tvDate = itemView.findViewById(R.id.tvAlertHistoryDate);
            TextView tvBadge = itemView.findViewById(R.id.tvAlertHistoryBadge);

            Long roomId = alert.getColdRoomId() != null ? alert.getColdRoomId().longValue() : null;
            ColdRoomResponse cr = roomId != null ? coldRoomsMap.get(roomId) : null;
            TemperatureReadingResponse tr = roomId != null ? readingsMap.get(roomId) : null;

            String roomName = (cr != null && cr.getLocation() != null && !cr.getLocation().isEmpty()) ?
                    cr.getLocation() : ("Câmara " + (roomId != null ? roomId : "?"));

            if (tr != null && tr.getTemperature() != null) {
                tvTitle.setText(String.format(Locale.getDefault(), "%s atingiu %.1f ºC", roomName, tr.getTemperature()));
            } else {
                tvTitle.setText(roomName + " em alerta");
            }

            Date alertDate = parseAlertDate(alert.getCreatedAt());
            if (alertDate != null) {
                tvDate.setText(itemDateFormat.format(alertDate));
            } else if (alert.getCreatedAt() != null) {
                tvDate.setText(alert.getCreatedAt());
            } else {
                tvDate.setText("--/-- · --:--");
            }

            // Badge
            String sev = alert.getSeverityLevel() != null ? alert.getSeverityLevel().toUpperCase() : "";
            if (sev.contains("CRITIC")) {
                tvBadge.setText("● Crítico");
                tvBadge.setTextColor(getResources().getColor(R.color.alert_danger));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_critical);
            } else if (sev.contains("BAIX") || sev.contains("ESTAVEL") || sev.contains("STABLE")) {
                tvBadge.setText("● Estável");
                tvBadge.setTextColor(getResources().getColor(R.color.alert_success));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_stable);
            } else {
                tvBadge.setText("● Atenção");
                tvBadge.setTextColor(getResources().getColor(R.color.alert_orange));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_warning);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                boolean isResolved = "RESOLVIDO".equalsIgnoreCase(alert.getStatus()) ||
                        "RESOLVED".equalsIgnoreCase(alert.getStatus());
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, isResolved);
                startActivity(intent);
            });

            layoutAlertsHistoryList.addView(itemView);
        }
    }

    private Date parseAlertDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            if (raw.length() > 19) {
                raw = raw.substring(0, 19);
            }
            return alertInputFormat.parse(raw);
        } catch (ParseException e) {
            try {
                return isoDateFormat.parse(raw);
            } catch (ParseException ex) {
                return null;
            }
        }
    }
}
