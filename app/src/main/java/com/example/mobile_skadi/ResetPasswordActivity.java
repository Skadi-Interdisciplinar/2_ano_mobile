package com.example.mobile_skadi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.dto.ChangePasswordRequest;
import com.example.mobile_skadi.utils.AvatarManager;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private AppCompatButton btnSubmitNewPassword;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    @Override
    protected void onResume() {
        super.onResume();
        AvatarManager.applyAvatar(this, btnProfileAvatar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnSubmitNewPassword = findViewById(R.id.btnSubmitNewPassword);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnProfileAvatar.setOnClickListener(v -> finish());

        btnSubmitNewPassword.setOnClickListener(v -> {
            String current = etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";
            String newPass = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
            String confirmPass = etConfirmNewPassword.getText() != null ? etConfirmNewPassword.getText().toString().trim() : "";

            if (!arePasswordFieldsFilled(current, newPass, confirmPass)) {
                Toast.makeText(this, "Preencha todos os campos para continuar.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "A nova senha deve possuir pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!doPasswordsMatch(newPass, confirmPass)) {
                Toast.makeText(this, "A nova senha e a confirmação não coincidem.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSubmitNewPassword.setEnabled(false);
            btnSubmitNewPassword.setText("Redefinindo...");

            ChangePasswordRequest request = new ChangePasswordRequest(current, newPass, confirmPass);
            RetrofitClient.getApiService(this).changePassword(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnSubmitNewPassword.setEnabled(true);
                    btnSubmitNewPassword.setText("Confirmar nova senha");

                    if (response.isSuccessful()) {
                        ResetPasswordConfirmDialog.show(ResetPasswordActivity.this, () -> {
                            Toast.makeText(ResetPasswordActivity.this, "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else if (response.code() == 400 || response.code() == 401) {
                        Toast.makeText(ResetPasswordActivity.this, "Senha atual incorreta ou inválida.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Erro ao alterar senha (código " + response.code() + ").", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnSubmitNewPassword.setEnabled(true);
                    btnSubmitNewPassword.setText("Confirmar nova senha");
                    Toast.makeText(ResetPasswordActivity.this, "Erro de rede ao conectar com a API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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

    static boolean arePasswordFieldsFilled(String current, String newPass, String confirmPass) {
        return current != null && !current.trim().isEmpty()
                && newPass != null && !newPass.trim().isEmpty()
                && confirmPass != null && !confirmPass.trim().isEmpty();
    }

    static boolean doPasswordsMatch(String newPass, String confirmPass) {
        return newPass != null && newPass.trim().equals(confirmPass != null ? confirmPass.trim() : null);
    }
}
