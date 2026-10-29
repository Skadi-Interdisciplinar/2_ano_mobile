package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class SquadsActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    private EditText etSearchTeam;

    private LinearLayout cardMemberDiego;
    private LinearLayout cardMemberMarise;
    private LinearLayout cardMemberRoberta;
    private LinearLayout cardMemberSergio;
    private LinearLayout cardMemberCaio;
    private LinearLayout cardMemberJoao;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squads);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        etSearchTeam = findViewById(R.id.etSearchTeam);

        cardMemberDiego = findViewById(R.id.cardMemberDiego);
        cardMemberMarise = findViewById(R.id.cardMemberMarise);
        cardMemberRoberta = findViewById(R.id.cardMemberRoberta);
        cardMemberSergio = findViewById(R.id.cardMemberSergio);
        cardMemberCaio = findViewById(R.id.cardMemberCaio);
        cardMemberJoao = findViewById(R.id.cardMemberJoao);

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

        btnProfileAvatar.setOnClickListener(v -> finish());

        // Ações nos colaboradores
        setupMemberClick(cardMemberDiego, "Diego Valadares - Operador(a)");
        setupMemberClick(cardMemberMarise, "Marise Peres - Gestor(a)");
        setupMemberClick(cardMemberRoberta, "Roberta Furtado - Operador(a)");
        setupMemberClick(cardMemberSergio, "Sérgio Silva - Operador(a)");
        setupMemberClick(cardMemberCaio, "Caio Pinheiro - Operador(a)");
        setupMemberClick(cardMemberJoao, "João Paulo Silveira - Operador(a)");

        // Busca em tempo real
        etSearchTeam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTeamMembers(s != null ? s.toString().trim().toLowerCase() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
            startActivity(new Intent(this, TrimmyActivity.class));
            finish();
        });
    }

    private void setupMemberClick(View card, String details) {
        card.setOnClickListener(v -> {
            Toast.makeText(this, details, Toast.LENGTH_SHORT).show();
        });
    }

    private void filterTeamMembers(String query) {
        filterCard(cardMemberDiego, "diego valadares operador", query);
        filterCard(cardMemberMarise, "marise peres gestora gestor", query);
        filterCard(cardMemberRoberta, "roberta furtado operador", query);
        filterCard(cardMemberSergio, "sérgio silva sergio silva operador", query);
        filterCard(cardMemberCaio, "caio pinheiro operador", query);
        filterCard(cardMemberJoao, "joão paulo silveira joao paulo silveira operador", query);
    }

    private void filterCard(View card, String text, String query) {
        if (query.isEmpty() || text.contains(query)) {
            card.setVisibility(View.VISIBLE);
        } else {
            card.setVisibility(View.GONE);
        }
    }
}
