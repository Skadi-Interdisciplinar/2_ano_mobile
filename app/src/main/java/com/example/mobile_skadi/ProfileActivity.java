package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    private LinearLayout cardPreferences;
    private LinearLayout cardResetPassword;
    private LinearLayout cardContactSupport;
    private LinearLayout cardMyTeam;
    private LinearLayout cardLogout;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        cardPreferences = findViewById(R.id.cardPreferences);
        cardResetPassword = findViewById(R.id.cardResetPassword);
        cardContactSupport = findViewById(R.id.cardContactSupport);
        cardMyTeam = findViewById(R.id.cardMyTeam);
        cardLogout = findViewById(R.id.cardLogout);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNotification.setOnClickListener(v -> {
            Toast.makeText(this, "Nenhuma nova notificação no momento.", Toast.LENGTH_SHORT).show();
        });

        // 1. Preferências do sistema
        cardPreferences.setOnClickListener(v -> {
            startActivity(new Intent(this, PreferencesActivity.class));
        });

        // 2. Redefinir senha
        cardResetPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ResetPasswordActivity.class));
        });

        // 3. Fale conosco (Abrir chamado)
        cardContactSupport.setOnClickListener(v -> {
            OpenTicketBottomSheet.show(this);
        });

        // 4. Minha equipe
        cardMyTeam.setOnClickListener(v -> {
            startActivity(new Intent(this, SquadsActivity.class));
        });

        // 5. Sair da conta (Logout overlay)
        cardLogout.setOnClickListener(v -> {
            LogoutDialog.show(this);
        });

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
            Toast.makeText(this, "Assistente Trimmy em desenvolvimento.", Toast.LENGTH_SHORT).show();
        });
    }
}
