package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;

public class TrimmyActivity extends AppCompatActivity {

    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    private LinearLayout layoutTrimmyWelcome;
    private NestedScrollView scrollTrimmyChat;
    private LinearLayout layoutChatMessages;
    private TextView tvInitialUserPrompt;

    private TextView chipSuggest1;
    private TextView chipSuggest2;
    private EditText etTrimmyMessage;
    private FrameLayout btnSendTrimmy;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private boolean isChatStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmy);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        layoutTrimmyWelcome = findViewById(R.id.layoutTrimmyWelcome);
        scrollTrimmyChat = findViewById(R.id.scrollTrimmyChat);
        layoutChatMessages = findViewById(R.id.layoutChatMessages);
        tvInitialUserPrompt = findViewById(R.id.tvInitialUserPrompt);

        chipSuggest1 = findViewById(R.id.chipSuggest1);
        chipSuggest2 = findViewById(R.id.chipSuggest2);
        etTrimmyMessage = findViewById(R.id.etTrimmyMessage);
        btnSendTrimmy = findViewById(R.id.btnSendTrimmy);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);

        TextView tvTrimmyGreeting = findViewById(R.id.tvTrimmyGreeting);
        if (tvTrimmyGreeting != null) {
            tvTrimmyGreeting.setText("Como podemos te ajudar hoje,\n" + UserRoleManager.getFirstNameForGreeting() + "?");
        }
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Sugestões rápidas na tela inicial do Trimmy
        chipSuggest1.setOnClickListener(v -> startChatWithPrompt("Quais câmaras estão críticas hoje?"));
        chipSuggest2.setOnClickListener(v -> startChatWithPrompt("Diagnóstico da Câmara 291"));

        // Envio de mensagem pelo botão ou teclado
        btnSendTrimmy.setOnClickListener(v -> handleSendMessage());

        etTrimmyMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSendMessage();
                return true;
            }
            return false;
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
            // Já estamos na tela do Trimmy
        });
    }

    private void startChatWithPrompt(String promptText) {
        if (!isChatStarted) {
            isChatStarted = true;
            tvInitialUserPrompt.setText(promptText);
            layoutTrimmyWelcome.setVisibility(View.GONE);
            scrollTrimmyChat.setVisibility(View.VISIBLE);
        } else {
            appendUserAndAiMessages(promptText);
        }
    }

    private void handleSendMessage() {
        String text = etTrimmyMessage.getText() != null ? etTrimmyMessage.getText().toString().trim() : "";
        if (text.isEmpty()) {
            return;
        }
        etTrimmyMessage.setText("");
        startChatWithPrompt(text);
    }

    private void appendUserAndAiMessages(String userText) {
        // Balão do usuário
        LinearLayout userRow = new LinearLayout(this);
        userRow.setOrientation(LinearLayout.HORIZONTAL);
        userRow.setGravity(Gravity.END | Gravity.TOP);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dpToPx(16);
        userRow.setLayoutParams(rowParams);

        TextView tvUser = new TextView(this);
        tvUser.setText(userText);
        tvUser.setTextColor(Color.parseColor("#0F2851"));
        tvUser.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvUser.setTypeface(ResourcesCompat.getFont(this, R.font.manrope));
        tvUser.setBackgroundResource(R.drawable.bg_chat_bubble_user);
        int pad = dpToPx(12);
        tvUser.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.setMarginStart(dpToPx(48));
        tvUser.setLayoutParams(bubbleParams);

        userRow.addView(tvUser);
        layoutChatMessages.addView(userRow);

        // Resposta simulada da Trimmy IA
        LinearLayout aiRow = new LinearLayout(this);
        aiRow.setOrientation(LinearLayout.HORIZONTAL);
        aiRow.setGravity(Gravity.START | Gravity.TOP);
        aiRow.setLayoutParams(rowParams);

        FrameLayout aiAvatar = new FrameLayout(this);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30));
        avatarParams.setMarginEnd(dpToPx(10));
        aiAvatar.setLayoutParams(avatarParams);
        aiAvatar.setBackgroundResource(R.drawable.bg_circle_navy_btn);

        ImageView ivLogo = new ImageView(this);
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(dpToPx(18), dpToPx(18), Gravity.CENTER);
        ivLogo.setLayoutParams(logoParams);
        ivLogo.setImageResource(R.drawable.logo_skadi);
        aiAvatar.addView(ivLogo);

        TextView tvAi = new TextView(this);
        tvAi.setText("As câmaras 1 (1,4°C) e 521 (3,7°C) permanecem estáveis e dentro dos parâmetros da RDC nº 216/2004 da ANVISA. Recomendo manter o acompanhamento prioritário na Câmara 291.");
        tvAi.setTextColor(Color.parseColor("#334155"));
        tvAi.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        tvAi.setTypeface(ResourcesCompat.getFont(this, R.font.manrope));
        tvAi.setBackgroundResource(R.drawable.bg_chat_bubble_ai);
        int aiPad = dpToPx(14);
        tvAi.setPadding(aiPad, aiPad, aiPad, aiPad);
        LinearLayout.LayoutParams aiBubbleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        aiBubbleParams.setMarginEnd(dpToPx(28));
        tvAi.setLayoutParams(aiBubbleParams);

        aiRow.addView(aiAvatar);
        aiRow.addView(tvAi);
        layoutChatMessages.addView(aiRow);

        scrollTrimmyChat.post(() -> scrollTrimmyChat.fullScroll(View.FOCUS_DOWN));
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
