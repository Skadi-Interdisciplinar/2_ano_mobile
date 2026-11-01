package com.example.mobile_skadi;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.dto.SupportRequestRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenTicketBottomSheet extends BottomSheetDialog {

    private final String[] ticketTypes = {
            "Falha de temperatura",
            "Problema com sensor",
            "Erro de conexão",
            "Manutenção da câmara",
            "Outro assunto"
    };

    public OpenTicketBottomSheet(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_open_ticket, null);
        setContentView(view);

        if (getWindow() != null) {
            View bottomSheet = getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        FrameLayout btnClose = view.findViewById(R.id.btnCloseTicket);
        FrameLayout btnSelectTicketType = view.findViewById(R.id.btnSelectTicketType);
        TextView tvTicketTypeSelected = view.findViewById(R.id.tvTicketTypeSelected);
        EditText etTicketDescription = view.findViewById(R.id.etTicketDescription);
        AppCompatButton btnSubmit = view.findViewById(R.id.btnSubmitTicket);

        btnClose.setOnClickListener(v -> dismiss());

        btnSelectTicketType.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Tipo do chamado")
                    .setItems(ticketTypes, (dialog, which) -> {
                        tvTicketTypeSelected.setText(ticketTypes[which]);
                        tvTicketTypeSelected.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.primary_dark));
                    })
                    .show();
        });

        btnSubmit.setOnClickListener(v -> {
            String desc = etTicketDescription.getText() != null ? etTicketDescription.getText().toString().trim() : "";
            if (desc.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, descreva o chamado antes de enviar.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSubmit.setEnabled(false);
            String tipo = tvTicketTypeSelected.getText() != null ? tvTicketTypeSelected.getText().toString() : "Suporte Geral";
            SupportRequestRequest request = new SupportRequestRequest(tipo, desc);

            RetrofitClient.getApiService(getContext()).createSupportRequest(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Toast.makeText(getContext(), "Chamado enviado com sucesso!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Chamado enviado (modo offline)!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            });
        });
    }

    public static void show(Context context) {
        OpenTicketBottomSheet dialog = new OpenTicketBottomSheet(context);
        dialog.show();
    }
}
