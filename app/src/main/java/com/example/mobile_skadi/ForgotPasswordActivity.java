package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.dto.PasswordRecoveryRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "EXTRA_EMAIL";

    private FrameLayout btnBack;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnEnviarCodigo;
    private TextView tvVoltarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        tvVoltarLogin = findViewById(R.id.tvVoltarLogin);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvVoltarLogin.setOnClickListener(v -> finish());

        etEmail.addTextChangedListener(new LoginActivity.SimpleTextWatcher(() -> tilEmail.setError(null)));

        btnEnviarCodigo.setOnClickListener(v -> onEnviarCodigoClicked());
    }

    private void onEnviarCodigoClicked() {
        String rawEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        final String email = rawEmail.toLowerCase();

        if (email.isEmpty()) {
            tilEmail.setError("Informe seu e-mail cadastrado");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Informe um formato de e-mail válido");
            etEmail.requestFocus();
            return;
        }

        btnEnviarCodigo.setEnabled(false);
        btnEnviarCodigo.setText("Enviando...");

        PasswordRecoveryRequest request = new PasswordRecoveryRequest(email);
        RetrofitClient.getApiService(this).recoverPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnEnviarCodigo.setEnabled(true);
                btnEnviarCodigo.setText("Enviar código");

                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Código enviado para " + email, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                    intent.putExtra(EXTRA_EMAIL, email);
                    startActivity(intent);
                } else if (response.code() == 404) {
                    tilEmail.setError("E-mail não encontrado no sistema");
                    etEmail.requestFocus();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Erro ao enviar código (código " + response.code() + ").", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnEnviarCodigo.setEnabled(true);
                btnEnviarCodigo.setText("Enviar código");
                Toast.makeText(ForgotPasswordActivity.this, "Falha de rede ao conectar à API: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
