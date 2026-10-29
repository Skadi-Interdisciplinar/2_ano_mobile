package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
    private MaterialButton btnVisualizarAnaliseCompleta;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        initViews();
        setupListeners();
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
        btnVisualizarAnaliseCompleta = findViewById(R.id.btnVisualizarAnaliseCompleta);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
    }

    private void setupListeners() {
        // Notificações e Perfil
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Seleção de câmara
        spnCamara.setOnClickListener(v -> {
            // TODO: Exibir seletor de câmaras
        });

        // Datas
        btnDataInicial.setOnClickListener(v -> {
            // TODO: Exibir DatePickerDialog inicial
        });

        btnDataFinal.setOnClickListener(v -> {
            // TODO: Exibir DatePickerDialog final
        });

        // Ordenar lista
        btnOrdenar.setOnClickListener(v -> {
            Toast.makeText(this, "Ordenando alertas...", Toast.LENGTH_SHORT).show();
        });

        // Visualizar Análise Completa
        btnVisualizarAnaliseCompleta.setOnClickListener(v -> {
            startActivity(new Intent(this, FullAnalysisActivity.class));
        });

        // Bottom Navigation
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
            // Já na tela de relatórios
        });

        navTrimmy.setOnClickListener(v -> {
            startActivity(new Intent(this, TrimmyActivity.class));
        });
    }
}
