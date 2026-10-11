package com.example.mobile_skadi;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public class AlertJustificationDialog extends BottomSheetDialog {

    public interface OnJustificationRegisteredListener {
        void onJustificationRegistered(String reason, String description);
    }

    private final OnJustificationRegisteredListener listener;

    public AlertJustificationDialog(@NonNull Context context, OnJustificationRegisteredListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_alert_justification, null);
        setContentView(view);

        // Torna o fundo do bottom sheet transparente para manter as bordas arredondadas do card
        if (getWindow() != null) {
            View bottomSheet = getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        FrameLayout btnBack = view.findViewById(R.id.btnBackJustification);
        EditText etReason = view.findViewById(R.id.etAlertReason);
        EditText etDescription = view.findViewById(R.id.etAlertDescription);
        MaterialButton btnSubmit = view.findViewById(R.id.btnRegistrarJustificativa);

        btnBack.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> {
            String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";
            String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            if (listener != null) {
                listener.onJustificationRegistered(reason, desc);
            }
            dismiss();
        });
    }

    public static void show(Context context, OnJustificationRegisteredListener listener) {
        AlertJustificationDialog dialog = new AlertJustificationDialog(context, listener);
        dialog.show();
    }
}
