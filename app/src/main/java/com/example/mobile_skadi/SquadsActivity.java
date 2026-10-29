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
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnProfileAvatar.setOnClickListener(v -> finish());

        // Ações nos colaboradores -> abre Detalhes do Colaborador
        setupMemberClick(cardMemberDiego, "Diego Valadares", "Operador(a)", "DV", "diego.valadares", "#0F2851", "3", "3");
        setupMemberClick(cardMemberMarise, "Marise Peres", "Gestor(a)", "MP", "marise.peres", "#2E8FEA", "1", "8");
        setupMemberClick(cardMemberRoberta, "Roberta Furtado", "Operador(a)", "RF", "roberta.furtado", "#55D8FF", "2", "5");
        setupMemberClick(cardMemberSergio, "Sérgio Silva", "Operador(a)", "SS", "sergio.silva", "#0F2E4F", "0", "6");
        setupMemberClick(cardMemberCaio, "Caio Pinheiro", "Operador(a)", "CP", "caio.pinheiro", "#718096", "1", "4");
        setupMemberClick(cardMemberJoao, "João Paulo Silveira", "Operador(a)", "JP", "joao.silveira", "#2D3748", "2", "7");

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

    private void setupMemberClick(View card, String name, String role, String initials, String username, String bgColor, String openAlerts, String resolvedAlerts) {
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeamMemberDetailsActivity.class);
            intent.putExtra(TeamMemberDetailsActivity.EXTRA_MEMBER_NAME, name);
            intent.putExtra(TeamMemberDetailsActivity.EXTRA_MEMBER_ROLE, role);
            intent.putExtra(TeamMemberDetailsActivity.EXTRA_MEMBER_INITIALS, initials);
            intent.putExtra(TeamMemberDetailsActivity.EXTRA_MEMBER_USERNAME, username);
            intent.putExtra(TeamMemberDetailsActivity.EXTRA_MEMBER_BG_COLOR, bgColor);
            intent.putExtra(TeamMemberDetailsActivity.EXTRA_OPEN_ALERTS, openAlerts);
            intent.putExtra(TeamMemberDetailsActivity.EXTRA_RESOLVED_ALERTS, resolvedAlerts);
            startActivity(intent);
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
