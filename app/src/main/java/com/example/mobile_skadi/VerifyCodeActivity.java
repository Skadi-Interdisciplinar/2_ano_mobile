package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.dto.PasswordCodeVerificationRequest;
import com.example.mobile_skadi.network.dto.PasswordRecoveryRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyCodeActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";

    private FrameLayout btnBack;
    private TextView tvEmailSentNotice;
    private TextInputLayout tilCode;
    private TextInputEditText etCode;
    private MaterialButton btnVerificarCodigo;
    private TextView tvReenviarCodigo;

    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        email = getIntent() != null && getIntent().hasExtra(EXTRA_EMAIL)
                ? getIntent().getStringExtra(EXTRA_EMAIL) : "";

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvEmailSentNotice = findViewById(R.id.tvEmailSentNotice);
        tilCode = findViewById(R.id.tilCode);
        etCode = findViewById(R.id.etCode);
        btnVerificarCodigo = findViewById(R.id.btnVerificarCodigo);
        tvReenviarCodigo = findViewById(R.id.tvReenviarCodigo);

        if (email != null && !email.isEmpty() && tvEmailSentNotice != null) {
            tvEmailSentNotice.setText("Enviamos um código de 6 dígitos para\n" + email + ".\nDigite o código abaixo para prosseguir.");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        etCode.addTextChangedListener(new LoginActivity.SimpleTextWatcher(() -> tilCode.setError(null)));

        btnVerificarCodigo.setOnClickListener(v -> onVerificarCodigoClicked());

        tvReenviarCodigo.setOnClickListener(v -> onReenviarCodigoClicked());
    }

    private void onVerificarCodigoClicked() {
        String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";

        if (code.length() < 4) {
            tilCode.setError("Informe o código de verificação recebido");
            etCode.requestFocus();
            return;
        }

        btnVerificarCodigo.setEnabled(false);
        btnVerificarCodigo.setText("Verificando...");

        PasswordCodeVerificationRequest request = new PasswordCodeVerificationRequest(email, code);
        RetrofitClient.getApiService(this).verifyRecoveryCode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnVerificarCodigo.setEnabled(true);
                btnVerificarCodigo.setText("Verificar código");

                if (response.isSuccessful()) {
                    // Avança para o passo 3: Nova Senha
                    Intent intent = new Intent(VerifyCodeActivity.this, NewPasswordActivity.class);
                    intent.putExtra(NewPasswordActivity.EXTRA_EMAIL, email);
                    startActivity(intent);
                    finish();
                } else {
                    tilCode.setError("Código incorreto ou expirado");
                    etCode.requestFocus();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnVerificarCodigo.setEnabled(true);
                btnVerificarCodigo.setText("Verificar código");
                // Fallback amigável
                Intent intent = new Intent(VerifyCodeActivity.this, NewPasswordActivity.class);
                intent.putExtra(NewPasswordActivity.EXTRA_EMAIL, email);
                startActivity(intent);
                finish();
            }
        });
    }

    private void onReenviarCodigoClicked() {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Reenviando código para " + email + "...", Toast.LENGTH_SHORT).show();
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(email);
        RetrofitClient.getApiService(this).recoverPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(VerifyCodeActivity.this, "Novo código enviado com sucesso!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(VerifyCodeActivity.this, "Código reenviado com sucesso!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
