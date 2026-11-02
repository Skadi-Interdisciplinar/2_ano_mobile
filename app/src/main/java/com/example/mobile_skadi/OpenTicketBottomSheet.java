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
            "Dúvida operacional",
            "Sugestão de melhoria",
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
        View btnCallSupport = view.findViewById(R.id.btnCallSupport);

        if (btnCallSupport != null) {
            btnCallSupport.setOnClickListener(v -> {
                android.content.Intent callIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                callIntent.setData(android.net.Uri.parse("tel:1130000000"));
                getContext().startActivity(callIntent);
            });
        }

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
            btnSubmit.setText("Enviando...");

            String tipoEscolhido = tvTicketTypeSelected.getText() != null ? tvTicketTypeSelected.getText().toString() : "Outro assunto";
            String categoria = mapToSupportCategory(tipoEscolhido);
            String fullDescricao = "[" + tipoEscolhido + "] " + desc;

            SupportRequestRequest request = new SupportRequestRequest(categoria, fullDescricao);

            RetrofitClient.getApiService(getContext()).createSupportRequest(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Enviar chamado");

                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Chamado enviado com sucesso!", Toast.LENGTH_LONG).show();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Não foi possível abrir o chamado (código: " + response.code() + ").", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Enviar chamado");
                    Toast.makeText(getContext(), "Falha de rede ao conectar com suporte: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String mapToSupportCategory(String tipo) {
        if (tipo == null) return "OUTRO";
        if (tipo.contains("Dúvida")) return "DUVIDA";
        if (tipo.contains("Sugestão")) return "SUGESTAO";
        if (tipo.contains("Falha") || tipo.contains("Problema") || tipo.contains("Erro") || tipo.contains("Reclamação")) {
            return "RECLAMACAO";
        }
        return "OUTRO";
    }

    public static void show(Context context) {
        OpenTicketBottomSheet dialog = new OpenTicketBottomSheet(context);
        dialog.show();
    }
}
