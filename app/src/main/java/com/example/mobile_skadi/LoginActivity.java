package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.LoginRequest;
import com.example.mobile_skadi.network.dto.TokenResponse;
import com.example.mobile_skadi.network.dto.UserResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsuario, tilSenha;
    private TextInputEditText etUsuario, etSenha;
    private CheckBox cbLembrar;
    private MaterialButton btnEntrar;
    private TextView tvEsqueceuSenha, tvCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = SessionManager.getInstance(this);
        if (sessionManager.isRememberMe() && sessionManager.hasToken()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();

        if (!sessionManager.getUsername().isEmpty() && etUsuario != null) {
            etUsuario.setText(sessionManager.getUsername());
            if (cbLembrar != null) {
                cbLembrar.setChecked(sessionManager.isRememberMe());
            }
        }
    }

    private void initViews() {
        tilUsuario = findViewById(R.id.tilUsuario);
        tilSenha = findViewById(R.id.tilSenha);
        etUsuario = findViewById(R.id.etUsuario);
        etSenha = findViewById(R.id.etSenha);
        cbLembrar = findViewById(R.id.cbLembrar);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvEsqueceuSenha = findViewById(R.id.tvEsqueceuSenha);
        tvCadastrar = findViewById(R.id.tvCadastrar);
    }

    private void setupListeners() {
        btnEntrar.setOnClickListener(v -> onEntrarClicked());

        // Limpa erro ao digitar
        etUsuario.addTextChangedListener(new SimpleTextWatcher(() -> tilUsuario.setError(null)));
        etSenha.addTextChangedListener(new SimpleTextWatcher(() -> tilSenha.setError(null)));

        tvEsqueceuSenha.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        tvCadastrar.setOnClickListener(v -> showRegisterInfoBottomSheet());
    }

    private void showRegisterInfoBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = android.view.LayoutInflater.from(this).inflate(R.layout.bottom_sheet_register_info, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        View btnEntendi = view.findViewById(R.id.btnEntendiCadastro);
        if (btnEntendi != null) {
            btnEntendi.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void onEntrarClicked() {
        String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
        String senha = etSenha.getText() != null ? etSenha.getText().toString().trim() : "";

        if (usuario.isEmpty()) {
            tilUsuario.setError("Informe o usuário");
            etUsuario.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            tilSenha.setError("Informe a senha");
            etSenha.requestFocus();
            return;
        }

        // Feedback de carregamento
        btnEntrar.setEnabled(false);
        btnEntrar.setText("Entrando...");

        SkadiApiService apiService = RetrofitClient.getApiService(this);
        LoginRequest loginRequest = new LoginRequest(usuario, senha);

        apiService.login(loginRequest).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {
                    TokenResponse tokenResponse = response.body();
                    SessionManager sessionManager = SessionManager.getInstance(LoginActivity.this);
                    sessionManager.saveToken(tokenResponse.getToken());
                    sessionManager.saveUsername(usuario);
                    sessionManager.setRememberMe(cbLembrar != null && cbLembrar.isChecked());

                    if (tokenResponse.getUser() != null) {
                        UserResponse u = tokenResponse.getUser();
                        if (u.getNome() != null && !u.getNome().trim().isEmpty()) {
                            sessionManager.saveName(u.getNome());
                        }
                        if (u.getRole() != null) {
                            sessionManager.saveRole(u.getRole());
                        }
                        if (u.getCdGestor() != null && u.getCdGestor().getName() != null) {
                            sessionManager.saveUnit(u.getCdGestor().getName());
                        }
                    }

                    // Buscar perfil do usuário (/me) para sincronizar papel e dados
                    fetchUserProfileAndNavigate(apiService, usuario);
                } else if (response.code() == 401 || response.code() == 403) {
                    btnEntrar.setEnabled(true);
                    btnEntrar.setText("Entrar");
                    tilSenha.setError("Usuário ou senha inválidos");
                    etSenha.requestFocus();
                } else {
                    handleLoginFallback(usuario, "Falha na autenticação (Código " + response.code() + "). Entrando em modo offline.");
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                handleLoginFallback(usuario, "API indisponível. Entrando em modo demonstração.");
            }
        });
    }

    private void fetchUserProfileAndNavigate(SkadiApiService apiService, String fallbackUsername) {
        apiService.me().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    SessionManager sm = SessionManager.getInstance(LoginActivity.this);
                    if (user.getNome() != null && !user.getNome().trim().isEmpty()) {
                        sm.saveName(user.getNome());
                    }
                    if (user.getCdGestor() != null && user.getCdGestor().getName() != null) {
                        sm.saveUnit(user.getCdGestor().getName());
                    }
                    if (user.getRole() != null) {
                        sm.saveRole(user.getRole());
                    }
                    if (user.getRole() != null && (user.getRole().equalsIgnoreCase("ROLE_USER") || user.getRole().equalsIgnoreCase("FUNCIONARIO") || user.getRole().equalsIgnoreCase("OPERADOR"))) {
                        UserRoleManager.setCurrentRole(UserRoleManager.Role.FUNCIONARIO);
                    } else {
                        UserRoleManager.setRoleFromUsername(user.getUsername() != null ? user.getUsername() : fallbackUsername);
                    }
                } else {
                    UserRoleManager.setRoleFromUsername(fallbackUsername);
                }
                navigateToHome();
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                UserRoleManager.setRoleFromUsername(fallbackUsername);
                navigateToHome();
            }
        });
    }

    private void handleLoginFallback(String usuario, String message) {
        btnEntrar.setEnabled(true);
        btnEntrar.setText("Entrar");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        UserRoleManager.setRoleFromUsername(usuario);
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    // Helper para evitar boilerplate de TextWatcher
    static boolean isUsernameValid(String username) {
        return username != null && !username.trim().isEmpty();
    }

    static boolean isPasswordValid(String password) {
        return password != null && !password.trim().isEmpty();
    }

    public static class SimpleTextWatcher implements TextWatcher {
        private final Runnable action;
        public SimpleTextWatcher(Runnable action) { this.action = action; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { action.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}
