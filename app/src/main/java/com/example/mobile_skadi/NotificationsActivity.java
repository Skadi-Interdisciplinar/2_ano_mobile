package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.NotificationResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private MaterialCardView btnProfileAvatar;

    private LinearLayout notifItem1;
    private LinearLayout notifItem2;
    private View dotUnread1;
    private View dotUnread2;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupListeners();
        loadNotificationsFromApi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        notifItem1 = findViewById(R.id.notifItem1);
        notifItem2 = findViewById(R.id.notifItem2);
        dotUnread1 = findViewById(R.id.dotUnread1);
        dotUnread2 = findViewById(R.id.dotUnread2);

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

        // Fallback inicial
        notifItem1.setOnClickListener(v -> {
            markAsRead(notifItem1, dotUnread1);
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
            startActivity(intent);
        });

        notifItem2.setOnClickListener(v -> {
            markAsRead(notifItem2, dotUnread2);
            Intent intent = new Intent(this, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
            startActivity(intent);
        });

        navCamaras.setOnClickListener(v -> {
            startActivity(new Intent(this, CamarasActivity.class));
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

    private void loadNotificationsFromApi() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);
        apiService.getNotifications().enqueue(new Callback<List<NotificationResponse>>() {
            @Override
            public void onResponse(Call<List<NotificationResponse>> call, Response<List<NotificationResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bindNotificationsFromApi(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                // Silencioso: fallback local ativo
            }
        });
    }

    private void bindNotificationsFromApi(List<NotificationResponse> notifications) {
        if (notifications.size() > 0) {
            NotificationResponse n0 = notifications.get(0);
            if (n0.getReadAt() != null && !n0.getReadAt().isEmpty()) {
                dotUnread1.setVisibility(View.GONE);
                notifItem1.setBackgroundColor(Color.parseColor("#E2E8F0"));
            }
            notifItem1.setOnClickListener(v -> {
                markAsRead(notifItem1, dotUnread1, n0.getId());
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                if (n0.getAlertId() != null) {
                    intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, n0.getAlertId());
                }
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
                startActivity(intent);
            });
        }

        if (notifications.size() > 1) {
            NotificationResponse n1 = notifications.get(1);
            if (n1.getReadAt() != null && !n1.getReadAt().isEmpty()) {
                dotUnread2.setVisibility(View.GONE);
                notifItem2.setBackgroundColor(Color.parseColor("#E2E8F0"));
            }
            notifItem2.setOnClickListener(v -> {
                markAsRead(notifItem2, dotUnread2, n1.getId());
                Intent intent = new Intent(this, AlertDetailsActivity.class);
                if (n1.getAlertId() != null) {
                    intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, n1.getAlertId());
                }
                intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
                startActivity(intent);
            });
        }
    }

    private void markAsRead(LinearLayout item, View dot, Integer notifId) {
        markAsRead(item, dot);
        if (notifId != null && notifId > 0) {
            RetrofitClient.getApiService(this).markNotificationAsRead(notifId).enqueue(new Callback<NotificationResponse>() {
                @Override
                public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {}
                @Override
                public void onFailure(Call<NotificationResponse> call, Throwable t) {}
            });
        }
    }

    private void markAsRead(LinearLayout item, View dot) {
        item.setBackgroundColor(Color.parseColor("#E2E8F0"));
        dot.setVisibility(View.GONE);
    }
}
