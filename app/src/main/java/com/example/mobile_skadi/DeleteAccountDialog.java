package com.example.mobile_skadi;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mobile_skadi.network.SessionManager;

public class DeleteAccountDialog extends Dialog {

    public DeleteAccountDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_delete_account);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        TextView btnCancel = findViewById(R.id.btnCancelDelete);
        TextView btnConfirm = findViewById(R.id.btnConfirmDelete);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dismiss();
                SessionManager.getInstance(getContext()).clear();
                UserRoleManager.setCurrentRole(UserRoleManager.Role.GESTOR);
                Toast.makeText(getContext(), "Conta excluída com sucesso.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getContext().startActivity(intent);
            });
        }
    }

    public static void show(Context context) {
        DeleteAccountDialog dialog = new DeleteAccountDialog(context);
        dialog.show();
    }
}
