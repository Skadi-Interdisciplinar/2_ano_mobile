package com.example.mobile_skadi;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsuario, tilSenha;
    private TextInputEditText etUsuario, etSenha;
    private CheckBox cbLembrar;
    private MaterialButton btnEntrar;
    private TextView tvEsqueceuSenha, tvCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
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
            // TODO: implementar fluxo de recuperacao de senha
        });

        tvCadastrar.setOnClickListener(v -> {
            // TODO: navegar para tela de cadastro
        });
    }

    private void onEntrarClicked() {
        String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
        String senha = etSenha.getText() != null ? etSenha.getText().toString().trim() : "";

        if (usuario.isEmpty()) {
            tilUsuario.setError("Informe o usuario");
            etUsuario.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            tilSenha.setError("Informe a senha");
            etSenha.requestFocus();
            return;
        }

        // TODO: chamar API de autenticacao
        // Ex: authViewModel.login(usuario, senha);
    }

    // Helper para evitar boilerplate de TextWatcher
    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable action;
        SimpleTextWatcher(Runnable action) { this.action = action; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { action.run(); }
        @Override public void afterTextChanged(Editable s) {}
    }
}

