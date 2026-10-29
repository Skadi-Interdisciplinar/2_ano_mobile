package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class TeamMemberDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MEMBER_NAME = "extra_member_name";
    public static final String EXTRA_MEMBER_ROLE = "extra_member_role";
    public static final String EXTRA_MEMBER_INITIALS = "extra_member_initials";
    public static final String EXTRA_MEMBER_USERNAME = "extra_member_username";
    public static final String EXTRA_MEMBER_BG_COLOR = "extra_member_bg_color";
    public static final String EXTRA_OPEN_ALERTS = "extra_open_alerts";
    public static final String EXTRA_RESOLVED_ALERTS = "extra_resolved_alerts";

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    private TextView tvMemberInitials;
    private TextView tvMemberName;
    private TextView tvMemberRole;
    private TextView tvMemberUsername;
    private TextView tvOpenAlertsCount;
    private TextView tvResolvedAlertsCount;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member_details);

        initViews();
        bindMemberData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        tvMemberInitials = findViewById(R.id.tvMemberInitials);
        tvMemberName = findViewById(R.id.tvMemberName);
        tvMemberRole = findViewById(R.id.tvMemberRole);
        tvMemberUsername = findViewById(R.id.tvMemberUsername);
        tvOpenAlertsCount = findViewById(R.id.tvOpenAlertsCount);
        tvResolvedAlertsCount = findViewById(R.id.tvResolvedAlertsCount);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void bindMemberData() {
        Intent intent = getIntent();
        if (intent == null) return;

        String name = intent.getStringExtra(EXTRA_MEMBER_NAME);
        String role = intent.getStringExtra(EXTRA_MEMBER_ROLE);
        String initials = intent.getStringExtra(EXTRA_MEMBER_INITIALS);
        String username = intent.getStringExtra(EXTRA_MEMBER_USERNAME);
        String bgColor = intent.getStringExtra(EXTRA_MEMBER_BG_COLOR);
        String openAlerts = intent.getStringExtra(EXTRA_OPEN_ALERTS);
        String resolvedAlerts = intent.getStringExtra(EXTRA_RESOLVED_ALERTS);

        if (name != null) tvMemberName.setText(name);
        if (role != null) tvMemberRole.setText(role);
        if (initials != null) tvMemberInitials.setText(initials);
        if (username != null) tvMemberUsername.setText(username);
        if (bgColor != null) {
            tvMemberInitials.setBackgroundColor(Color.parseColor(bgColor));
        }
        if (openAlerts != null) tvOpenAlertsCount.setText(openAlerts);
        if (resolvedAlerts != null) tvResolvedAlertsCount.setText(resolvedAlerts);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
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
}
