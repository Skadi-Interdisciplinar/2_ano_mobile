package com.example.mobile_skadi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.utils.AvatarManager;
import com.google.android.material.card.MaterialCardView;

import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private FrameLayout btnNotification;
    private MaterialCardView btnProfileAvatar;
    private ImageView ivProfileAvatarMain;
    private MaterialCardView cardProfileAvatarMain;

    private LinearLayout cardPreferences;
    private LinearLayout cardResetPassword;
    private LinearLayout cardContactSupport;
    private LinearLayout cardMyTeam;
    private LinearLayout cardLogout;

    private LinearLayout navCamaras;
    private LinearLayout navAlertas;
    private LinearLayout navInicio;
    private LinearLayout navRelatorios;
    private LinearLayout navTrimmy;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupImagePickers();
        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAvatars();
    }

    private void setupImagePickers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = null;
                        if (extras != null && extras.get("data") instanceof Bitmap) {
                            imageBitmap = (Bitmap) extras.get("data");
                        }
                        if (imageBitmap != null) {
                            AvatarManager.saveAvatar(this, imageBitmap);
                            updateAvatars();
                            Toast.makeText(this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
                            Bitmap selectedBitmap = BitmapFactory.decodeStream(is);
                            if (selectedBitmap != null) {
                                AvatarManager.saveAvatar(this, selectedBitmap);
                                updateAvatars();
                                Toast.makeText(this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Não foi possível carregar a imagem.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Permissão da câmera é necessária para tirar foto.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");
        galleryLauncher.launch(pickIntent);
    }

    private void showPhotoPickerDialog() {
        String[] options;
        if (AvatarManager.hasCustomAvatar(this)) {
            options = new String[]{"Tirar foto (Câmera)", "Escolher da Galeria", "Restaurar foto padrão"};
        } else {
            options = new String[]{"Tirar foto (Câmera)", "Escolher da Galeria"};
        }

        new AlertDialog.Builder(this)
                .setTitle("Foto de Perfil")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else if (which == 1) {
                        openGallery();
                    } else if (which == 2) {
                        AvatarManager.clearAvatar(this);
                        recreate();
                        Toast.makeText(this, "Foto de perfil restaurada para o padrão.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void updateAvatars() {
        if (ivProfileAvatarMain != null) {
            AvatarManager.applyAvatar(this, ivProfileAvatarMain);
        }
        if (btnProfileAvatar != null) {
            AvatarManager.applyAvatar(this, btnProfileAvatar);
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotification = findViewById(R.id.btnNotification);
        btnProfileAvatar = findViewById(R.id.btnProfileAvatar);
        ivProfileAvatarMain = findViewById(R.id.ivProfileAvatarMain);
        cardProfileAvatarMain = findViewById(R.id.cardProfileAvatarMain);

        cardPreferences = findViewById(R.id.cardPreferences);
        cardResetPassword = findViewById(R.id.cardResetPassword);
        cardContactSupport = findViewById(R.id.cardContactSupport);
        cardMyTeam = findViewById(R.id.cardMyTeam);
        cardLogout = findViewById(R.id.cardLogout);

        navCamaras = findViewById(R.id.navCamaras);
        navAlertas = findViewById(R.id.navAlertas);
        navInicio = findViewById(R.id.navInicio);
        navRelatorios = findViewById(R.id.navRelatorios);
        navTrimmy = findViewById(R.id.navTrimmy);

        TextView tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvProfileRole = findViewById(R.id.tvProfileRole);
        TextView tvProfileUsername = findViewById(R.id.tvProfileUsername);

        String sessionName = SessionManager.getInstance(this).getName();
        String sessionUser = SessionManager.getInstance(this).getUsername();
        if (sessionName == null || sessionName.trim().isEmpty()) {
            sessionName = sessionUser;
        }

        if (sessionName != null && !sessionName.trim().isEmpty()) {
            String[] parts = sessionName.replace("_", " ").replace(".", " ").split("\\s+");
            StringBuilder formatted = new StringBuilder();
            for (String p : parts) {
                if (!p.isEmpty()) {
                    formatted.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
                }
            }
            if (tvProfileName != null) tvProfileName.setText(formatted.toString().trim());
        } else {
            if (tvProfileName != null) tvProfileName.setText(UserRoleManager.getFullName());
        }

        if (sessionUser != null && !sessionUser.trim().isEmpty()) {
            if (tvProfileUsername != null) tvProfileUsername.setText("@" + sessionUser);
        } else {
            if (tvProfileUsername != null) tvProfileUsername.setText(UserRoleManager.getUsernameHandle());
        }

        if (tvProfileRole != null) tvProfileRole.setText(UserRoleManager.getRoleLabel());
        if (cardMyTeam != null) {
            cardMyTeam.setVisibility(UserRoleManager.isEmployee() ? View.GONE : View.VISIBLE);
        }
        UserRoleManager.applyBottomNavForRole(this);
        updateAvatars();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        MaterialCardView btnChangeProfilePhoto = findViewById(R.id.btnChangeProfilePhoto);
        if (btnChangeProfilePhoto != null) {
            btnChangeProfilePhoto.setOnClickListener(v -> showPhotoPickerDialog());
        }

        if (cardProfileAvatarMain != null) {
            cardProfileAvatarMain.setOnClickListener(v -> showPhotoPickerDialog());
        }

        // 1. Preferências do sistema
        cardPreferences.setOnClickListener(v -> {
            startActivity(new Intent(this, PreferencesActivity.class));
        });

        // 2. Redefinir senha
        cardResetPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ResetPasswordActivity.class));
        });

        // 3. Fale conosco (Suporte - Opções de chamado ou ligação via Hardware)
        cardContactSupport.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Fale Conosco")
                    .setItems(new String[]{"Abrir chamado online", "Ligar para suporte: (11) 3000-0000"}, (dialog, which) -> {
                        if (which == 0) {
                            OpenTicketBottomSheet.show(this);
                        } else {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL);
                            callIntent.setData(Uri.parse("tel:1130000000"));
                            startActivity(callIntent);
                        }
                    })
                    .show();
        });

        // 4. Minha equipe
        cardMyTeam.setOnClickListener(v -> {
            startActivity(new Intent(this, SquadsActivity.class));
        });

        // 5. Sair da conta (Logout overlay)
        cardLogout.setOnClickListener(v -> {
            LogoutDialog.show(this);
        });

        // 6. Excluir conta
        TextView btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> {
                DeleteAccountDialog.show(this);
            });
        }

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
}
