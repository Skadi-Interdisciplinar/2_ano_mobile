package com.example.mobile_skadi;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GeneratedReportActivity extends AppCompatActivity {

    private FrameLayout btnBackReport;
    private ImageView btnShareReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_report);

        btnBackReport = findViewById(R.id.btnBackReport);
        btnShareReport = findViewById(R.id.btnShareReport);

        btnBackReport.setOnClickListener(v -> finish());

        btnShareReport.setOnClickListener(v -> {
            Toast.makeText(this, "Baixando PDF do Relatório Skadi...", Toast.LENGTH_SHORT).show();
            // TODO: Exportar para PDF / Compartilhar documento
        });
    }
}
