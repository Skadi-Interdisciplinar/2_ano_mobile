package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.NotificationResponse;
import com.example.mobile_skadi.utils.AvatarManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L;
    private final android.os.Handler refreshHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadAlertsAndNotifications();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private FrameLayout btnBack;
    private MaterialCardView btnProfileAvatar;

    private ProgressBar progressLoadingNotifs;
    private LinearLayout layoutEmptyNotifications;
    private LinearLayout containerNotifications;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onResume() {
        super.onResume();
        AvatarManager.applyAvatar(this, btnProfileAvatar);
        loadAlertsAndNotifications();
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        progressLoadingNotifs = findViewById(R.id.progressLoadingNotifs);
        layoutEmptyNotifications = findViewById(R.id.layoutEmptyNotifications);
        containerNotifications = findViewById(R.id.containerNotifications);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
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

        navRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
            finish();
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
            finish();
        });
    }

    private void loadAlertsAndNotifications() {
        if (progressLoadingNotifs != null) {
            progressLoadingNotifs.setVisibility(View.VISIBLE);
        }
        if (layoutEmptyNotifications != null) {
            layoutEmptyNotifications.setVisibility(View.GONE);
        }

        SkadiApiService apiService = RetrofitClient.getApiService(this);

        // Busca alertas ativos (gerados a partir das oscilações de temperatura)
        apiService.getAlerts(null).enqueue(new Callback<List<AlertResponse>>() {
            @Override
            public void onResponse(Call<List<AlertResponse>> call, Response<List<AlertResponse>> response) {
                List<AlertResponse> activeAlerts = (response.isSuccessful() && response.body() != null)
                        ? response.body() : new ArrayList<>();

                // Busca histórico de alertas (resolvidos)
                apiService.getAlertsHistory(null).enqueue(new Callback<List<AlertResponse>>() {
                    @Override
                    public void onResponse(Call<List<AlertResponse>> callH, Response<List<AlertResponse>> responseH) {
                        List<AlertResponse> historyAlerts = (responseH.isSuccessful() && responseH.body() != null)
                                ? responseH.body() : new ArrayList<>();

                        List<AlertResponse> allAlerts = new ArrayList<>();
                        allAlerts.addAll(activeAlerts);
                        allAlerts.addAll(historyAlerts);

                        if (allAlerts.isEmpty()) {
                            // Tenta carregar notificações de usuário caso não haja alertas diretos
                            loadUserNotificationsFallback(apiService);
                        } else {
                            displayAlerts(allAlerts);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AlertResponse>> callH, Throwable t) {
                        if (!activeAlerts.isEmpty()) {
                            displayAlerts(activeAlerts);
                        } else {
                            loadUserNotificationsFallback(apiService);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<List<AlertResponse>> call, Throwable t) {
                loadUserNotificationsFallback(apiService);
            }
        });
    }

    private void loadUserNotificationsFallback(SkadiApiService apiService) {
        apiService.getNotifications().enqueue(new Callback<List<NotificationResponse>>() {
            @Override
            public void onResponse(Call<List<NotificationResponse>> call, Response<List<NotificationResponse>> response) {
                if (progressLoadingNotifs != null) {
                    progressLoadingNotifs.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    displayUserNotifications(response.body());
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                if (progressLoadingNotifs != null) {
                    progressLoadingNotifs.setVisibility(View.GONE);
                }
                showEmptyState();
            }
        });
    }

    private void displayAlerts(List<AlertResponse> alerts) {
        if (progressLoadingNotifs != null) {
            progressLoadingNotifs.setVisibility(View.GONE);
        }
        if (containerNotifications == null) return;

        containerNotifications.removeAllViews();

        if (alerts.isEmpty()) {
            showEmptyState();
            return;
        }

        if (layoutEmptyNotifications != null) {
            layoutEmptyNotifications.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (AlertResponse alert : alerts) {
            View itemView = inflater.inflate(R.layout.item_notification_row, containerNotifications, false);

            FrameLayout frameIcon = itemView.findViewById(R.id.frameNotifIcon);
            ImageView ivIcon = itemView.findViewById(R.id.ivNotifIcon);
            TextView tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            TextView tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            View dotUnread = itemView.findViewById(R.id.dotUnread);
            View rootItem = itemView.findViewById(R.id.rootNotifItem);
            View clickableRow = itemView.findViewById(R.id.clickableRow);

            boolean isResolved = alert.getStatus() != null && "RESOLVIDO".equalsIgnoreCase(alert.getStatus());
            boolean isRecognized = alert.getStatus() != null && "RECONHECIDO".equalsIgnoreCase(alert.getStatus());
            boolean isCritical = alert.getSeverityLevel() != null && "CRITICO".equalsIgnoreCase(alert.getSeverityLevel())
                    || (alert.getCurrentLevel() != null && "CRITICO".equalsIgnoreCase(alert.getCurrentLevel()));

            int coldRoomId = alert.getColdRoomId() != null ? alert.getColdRoomId() : 1;

            if (isResolved) {
                frameIcon.setBackgroundResource(R.drawable.bg_notif_circle_green);
                ivIcon.setImageResource(R.drawable.ic_check_circle_green);
                tvTitle.setText("Resolvido");
                tvTitle.setTextColor(Color.parseColor("#10B981"));
                tvMessage.setText("A temperatura da Câmara " + coldRoomId + " estabilizou dentro da faixa recomendada.");
                dotUnread.setVisibility(View.GONE);
                if (clickableRow != null) clickableRow.setBackgroundColor(Color.parseColor("#E2E8F0"));
            } else if (isCritical) {
                frameIcon.setBackgroundResource(R.drawable.bg_notif_circle_red);
                ivIcon.setImageResource(R.drawable.ic_alert_triangle_red);
                tvTitle.setText("Oscilação detectada");
                tvTitle.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
                tvMessage.setText("A temperatura da Câmara " + coldRoomId + " subiu além do limite máximo permitido.");
                dotUnread.setVisibility(isRecognized ? View.GONE : View.VISIBLE);
                if (clickableRow != null) {
                    clickableRow.setBackgroundColor(isRecognized ? Color.parseColor("#E2E8F0") : Color.WHITE);
                }
            } else {
                frameIcon.setBackgroundResource(R.drawable.bg_notif_circle_yellow);
                ivIcon.setImageResource(R.drawable.ic_alert_triangle_orange);
                tvTitle.setText("Oscilação detectada");
                tvTitle.setTextColor(Color.parseColor("#D97706"));
                tvMessage.setText("A temperatura da Câmara " + coldRoomId + " requer verificação preventiva.");
                dotUnread.setVisibility(isRecognized ? View.GONE : View.VISIBLE);
                if (clickableRow != null) {
                    clickableRow.setBackgroundColor(isRecognized ? Color.parseColor("#E2E8F0") : Color.WHITE);
                }
            }

            if (clickableRow != null) {
                clickableRow.setOnClickListener(v -> {
                    if (!isResolved && !isRecognized) {
                        dotUnread.setVisibility(View.GONE);
                        clickableRow.setBackgroundColor(Color.parseColor("#E2E8F0"));
                        if (alert.getId() != null) {
                            RetrofitClient.getApiService(this).recognizeAlert(alert.getId()).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {}
                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {}
                            });
                        }
                    }

                    Intent intent = new Intent(NotificationsActivity.this, AlertDetailsActivity.class);
                    if (alert.getId() != null) {
                        intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, alert.getId());
                    }
                    intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, isResolved);
                    startActivity(intent);
                });
            }

            containerNotifications.addView(itemView);
        }
    }

    private void displayUserNotifications(List<NotificationResponse> notifications) {
        if (containerNotifications == null) return;
        containerNotifications.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (NotificationResponse notif : notifications) {
            View itemView = inflater.inflate(R.layout.item_notification_row, containerNotifications, false);

            FrameLayout frameIcon = itemView.findViewById(R.id.frameNotifIcon);
            ImageView ivIcon = itemView.findViewById(R.id.ivNotifIcon);
            TextView tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            TextView tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            View dotUnread = itemView.findViewById(R.id.dotUnread);
            View clickableRow = itemView.findViewById(R.id.clickableRow);

            boolean isRead = notif.getReadAt() != null && !notif.getReadAt().isEmpty();

            frameIcon.setBackgroundResource(R.drawable.bg_notif_circle_yellow);
            ivIcon.setImageResource(R.drawable.ic_alert_triangle_orange);
            tvTitle.setText("Alerta do sistema");
            tvTitle.setTextColor(Color.parseColor("#D97706"));
            tvMessage.setText("Alerta registrado #" + (notif.getAlertId() != null ? notif.getAlertId() : notif.getId()));

            dotUnread.setVisibility(isRead ? View.GONE : View.VISIBLE);
            if (clickableRow != null) {
                clickableRow.setBackgroundColor(isRead ? Color.parseColor("#E2E8F0") : Color.WHITE);
                clickableRow.setOnClickListener(v -> {
                    dotUnread.setVisibility(View.GONE);
                    clickableRow.setBackgroundColor(Color.parseColor("#E2E8F0"));
                    if (notif.getId() != null) {
                        RetrofitClient.getApiService(this).markNotificationAsRead(notif.getId()).enqueue(new Callback<NotificationResponse>() {
                            @Override
                            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {}
                            @Override
                            public void onFailure(Call<NotificationResponse> call, Throwable t) {}
                        });
                    }

                    Intent intent = new Intent(NotificationsActivity.this, AlertDetailsActivity.class);
                    if (notif.getAlertId() != null) {
                        intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, notif.getAlertId());
                    }
                    intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
                    startActivity(intent);
                });
            }

            containerNotifications.addView(itemView);
        }
    }

    private void showEmptyState() {
        if (containerNotifications != null) {
            containerNotifications.removeAllViews();
        }
        if (layoutEmptyNotifications != null) {
            layoutEmptyNotifications.setVisibility(View.VISIBLE);
        }
    }
}
