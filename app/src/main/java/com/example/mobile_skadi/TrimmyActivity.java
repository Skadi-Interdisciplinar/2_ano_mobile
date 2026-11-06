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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.example.mobile_skadi.data.ChatMessage;
import com.example.mobile_skadi.data.SkadiDatabaseHelper;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.TrimmyApiClient;
import com.example.mobile_skadi.network.dto.ChatRequest;
import com.example.mobile_skadi.network.dto.ChatResponse;
import com.example.mobile_skadi.network.dto.EncerrarChatResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrimmyActivity extends AppCompatActivity {

    private FrameLayout btnClearChat;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    private LinearLayout layoutTrimmyWelcome;
    private NestedScrollView scrollTrimmyChat;
    private LinearLayout layoutChatMessages;
    private TextView tvInitialUserPrompt;

    private TextView chipSuggest1;

    private EditText etTrimmyMessage;
    private FrameLayout btnSendTrimmy;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private SkadiDatabaseHelper dbHelper;
    private boolean isChatStarted = false;

    @Override
    protected void onResume() {
        super.onResume();
        com.example.mobile_skadi.utils.AvatarManager.applyAvatar(this, btnProfileAvatar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimmy);

        dbHelper = SkadiDatabaseHelper.getInstance(this);

        initViews();
        setupListeners();
        loadChatHistory();
    }

    private void initViews() {
        btnClearChat = findViewById(R.id.btnClearChat);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        layoutTrimmyWelcome = findViewById(R.id.layoutTrimmyWelcome);
        scrollTrimmyChat = findViewById(R.id.scrollTrimmyChat);
        layoutChatMessages = findViewById(R.id.layoutChatMessages);
        tvInitialUserPrompt = findViewById(R.id.tvInitialUserPrompt);

        chipSuggest1 = findViewById(R.id.chipSuggest1);

        etTrimmyMessage = findViewById(R.id.etTrimmyMessage);
        btnSendTrimmy = findViewById(R.id.btnSendTrimmy);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);

        TextView tvTrimmyGreeting = findViewById(R.id.tvTrimmyGreeting);
        if (tvTrimmyGreeting != null) {
            String userFirst = SessionManager.getInstance(this).getFirstName();
            tvTrimmyGreeting.setText("Como podemos te ajudar hoje,\n" + userFirst + "?");
        }

        UserRoleManager.applyBottomNavForRole(this);
    }

    private void loadChatHistory() {
        List<ChatMessage> history = dbHelper.getChatHistory();
        if (history != null && !history.isEmpty()) {
            isChatStarted = true;
            if (layoutTrimmyWelcome != null) layoutTrimmyWelcome.setVisibility(View.GONE);
            if (scrollTrimmyChat != null) scrollTrimmyChat.setVisibility(View.VISIBLE);
            if (btnClearChat != null) btnClearChat.setVisibility(View.VISIBLE);

            layoutChatMessages.removeAllViews();
            for (ChatMessage msg : history) {
                if ("USER".equalsIgnoreCase(msg.getSender())) {
                    renderUserBubble(msg.getMessage());
                } else {
                    renderAiBubble(msg.getMessage(), msg.isError());
                }
            }
            scrollTrimmyChat.post(() -> scrollTrimmyChat.fullScroll(View.FOCUS_DOWN));
        } else {
            isChatStarted = false;
            if (layoutTrimmyWelcome != null) layoutTrimmyWelcome.setVisibility(View.VISIBLE);
            if (scrollTrimmyChat != null) scrollTrimmyChat.setVisibility(View.GONE);
            if (btnClearChat != null) btnClearChat.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        if (btnClearChat != null) {
            btnClearChat.setOnClickListener(v -> confirmClearChat());
        }

        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnProfileAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        // Sugestão rápida na tela inicial do Trimmy
        if (chipSuggest1 != null) {
            chipSuggest1.setOnClickListener(v -> startChatWithPrompt("Quais câmaras estão críticas hoje?"));
        }

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

    private void confirmClearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar histórico")
                .setMessage("Deseja apagar todas as mensagens salvas desta conversa com o Trimmy?")
                .setPositiveButton("Limpar", (dialog, which) -> {
                    dbHelper.clearChatHistory();
                    layoutChatMessages.removeAllViews();
                    isChatStarted = false;
                    if (layoutTrimmyWelcome != null) layoutTrimmyWelcome.setVisibility(View.VISIBLE);
                    if (scrollTrimmyChat != null) scrollTrimmyChat.setVisibility(View.GONE);
                    if (btnClearChat != null) btnClearChat.setVisibility(View.GONE);

                    TrimmyApiClient.getApiService(this).encerrarChat().enqueue(new Callback<EncerrarChatResponse>() {
                        @Override
                        public void onResponse(Call<EncerrarChatResponse> call, Response<EncerrarChatResponse> response) {}
                        @Override
                        public void onFailure(Call<EncerrarChatResponse> call, Throwable t) {}
                    });

                    Toast.makeText(this, "Histórico da conversa apagado.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void startChatWithPrompt(String promptText) {
        if (!isChatStarted) {
            isChatStarted = true;
            layoutTrimmyWelcome.setVisibility(View.GONE);
            scrollTrimmyChat.setVisibility(View.VISIBLE);
            if (btnClearChat != null) btnClearChat.setVisibility(View.VISIBLE);
            layoutChatMessages.removeAllViews();
        }
        sendChatToAi(promptText);
    }

    private void handleSendMessage() {
        String text = etTrimmyMessage.getText() != null ? etTrimmyMessage.getText().toString().trim() : "";
        if (text.isEmpty()) {
            return;
        }
        etTrimmyMessage.setText("");
        startChatWithPrompt(text);
    }

    private void renderUserBubble(String userText) {
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
    }

    private TextView renderAiBubble(String initialText, boolean isError) {
        LinearLayout aiRow = new LinearLayout(this);
        aiRow.setOrientation(LinearLayout.HORIZONTAL);
        aiRow.setGravity(Gravity.START | Gravity.TOP);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dpToPx(16);
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
        tvAi.setText(initialText);
        tvAi.setTextColor(isError ? Color.parseColor("#EF4444") : Color.parseColor("#334155"));
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
        return tvAi;
    }

    private void sendChatToAi(String userText) {
        // 1. Renderiza o balão do usuário e salva no banco de dados local
        renderUserBubble(userText);
        dbHelper.saveChatMessage("USER", userText, false);

        if (btnClearChat != null) {
            btnClearChat.setVisibility(View.VISIBLE);
        }

        // 2. Renderiza o balão temporário de loading da IA
        TextView tvAi = renderAiBubble("Trimmy está analisando os dados...", false);
        tvAi.setTextColor(Color.parseColor("#64748B"));

        scrollTrimmyChat.post(() -> scrollTrimmyChat.fullScroll(View.FOCUS_DOWN));

        // 3. Chamada real ao backend de IA
        ChatRequest request = new ChatRequest(userText);
        TrimmyApiClient.getApiService(this).chat(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResposta() != null) {
                    String resposta = response.body().getResposta();
                    tvAi.setText(resposta);
                    tvAi.setTextColor(Color.parseColor("#334155"));
                    dbHelper.saveChatMessage("AI", resposta, false);
                } else {
                    int code = response.code();
                    String msg;
                    if (code == 401) {
                        msg = "Sua sessão expirou. Faça login novamente no aplicativo.";
                    } else if (code == 403) {
                        msg = "Acesso negado para o seu perfil ou câmara solicitada.";
                    } else if (code == 409) {
                        msg = "A sessão foi alterada durante a consulta. Tente novamente.";
                    } else if (code == 422) {
                        msg = "Parâmetros da requisição inválidos.";
                    } else if (code == 502 || code == 503) {
                        msg = "O serviço de inteligência artificial Trimmy está temporariamente indisponível.";
                    } else {
                        msg = "Não foi possível gerar uma resposta no momento (Erro " + code + ").";
                    }
                    tvAi.setText(msg);
                    tvAi.setTextColor(Color.parseColor("#EF4444"));
                    dbHelper.saveChatMessage("AI", msg, true);
                }
                scrollTrimmyChat.post(() -> scrollTrimmyChat.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                String errorMsg = "Não foi possível conectar ao servidor do Trimmy IA (porta 8000). Verifique se o serviço está ativo.";
                tvAi.setText(errorMsg);
                tvAi.setTextColor(Color.parseColor("#EF4444"));
                dbHelper.saveChatMessage("AI", errorMsg, true);
                scrollTrimmyChat.post(() -> scrollTrimmyChat.fullScroll(View.FOCUS_DOWN));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
