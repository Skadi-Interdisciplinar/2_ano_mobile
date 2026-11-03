package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.dto.PasswordRecoveryRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";

    private FrameLayout btnBack;
    private TextInputLayout tilNovaSenha;
    private TextInputEditText etNovaSenha;
    private TextInputLayout tilConfirmarSenha;
    private TextInputEditText etConfirmarSenha;
    private MaterialButton btnSalvarNovaSenha;

    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        email = getIntent() != null && getIntent().hasExtra(EXTRA_EMAIL)
                ? getIntent().getStringExtra(EXTRA_EMAIL) : "";

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tilNovaSenha = findViewById(R.id.tilNovaSenha);
        etNovaSenha = findViewById(R.id.etNovaSenha);
        tilConfirmarSenha = findViewById(R.id.tilConfirmarSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        btnSalvarNovaSenha = findViewById(R.id.btnSalvarNovaSenha);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etNovaSenha.addTextChangedListener(new LoginActivity.SimpleTextWatcher(() -> tilNovaSenha.setError(null)));
        etConfirmarSenha.addTextChangedListener(new LoginActivity.SimpleTextWatcher(() -> tilConfirmarSenha.setError(null)));

        btnSalvarNovaSenha.setOnClickListener(v -> onSalvarNovaSenhaClicked());
    }

    private void onSalvarNovaSenhaClicked() {
        String novaSenha = etNovaSenha.getText() != null ? etNovaSenha.getText().toString().trim() : "";
        String confirmacao = etConfirmarSenha.getText() != null ? etConfirmarSenha.getText().toString().trim() : "";

        if (novaSenha.isEmpty()) {
            tilNovaSenha.setError("Informe a nova senha");
            etNovaSenha.requestFocus();
            return;
        }

        if (novaSenha.length() < 6) {
            tilNovaSenha.setError("A senha deve possuir pelo menos 6 caracteres");
            etNovaSenha.requestFocus();
            return;
        }

        if (confirmacao.isEmpty()) {
            tilConfirmarSenha.setError("Confirme a nova senha");
            etConfirmarSenha.requestFocus();
            return;
        }

        if (!novaSenha.equals(confirmacao)) {
            tilConfirmarSenha.setError("As senhas não coincidem");
            etConfirmarSenha.requestFocus();
            return;
        }

        btnSalvarNovaSenha.setEnabled(false);
        btnSalvarNovaSenha.setText("Salvando...");

        PasswordRecoveryRequest request = new PasswordRecoveryRequest(email, novaSenha);
        RetrofitClient.getApiService(this).recoverPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSalvarNovaSenha.setEnabled(true);
                btnSalvarNovaSenha.setText("Salvar nova senha");
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
                    Toast.makeText(NewPasswordActivity.this, "Código de recuperação inválido ou expirado.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSalvarNovaSenha.setEnabled(true);
                btnSalvarNovaSenha.setText("Salvar nova senha");
                Toast.makeText(NewPasswordActivity.this, "Falha de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_password_reset_success, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        MaterialButton btnIrParaLogin = view.findViewById(R.id.btnIrParaLogin);
        btnIrParaLogin.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(NewPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}
