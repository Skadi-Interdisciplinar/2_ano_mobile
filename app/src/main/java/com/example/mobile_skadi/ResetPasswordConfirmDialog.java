package com.example.mobile_skadi;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ResetPasswordConfirmDialog extends Dialog {

    public interface OnPasswordConfirmedListener {
        void onConfirmed();
    }

    private final OnPasswordConfirmedListener listener;

    public ResetPasswordConfirmDialog(@NonNull Context context, OnPasswordConfirmedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirm_password);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancel = findViewById(R.id.btnCancelResetPassword);
        TextView btnConfirm = findViewById(R.id.btnConfirmResetPassword);

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onConfirmed();
            }
        });
    }

    public static void show(Context context, OnPasswordConfirmedListener listener) {
        ResetPasswordConfirmDialog dialog = new ResetPasswordConfirmDialog(context, listener);
        dialog.show();
    }
}
