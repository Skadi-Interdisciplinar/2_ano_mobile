package com.example.mobile_skadi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.mobile_skadi.network.TrimmyApiClient;
import com.example.mobile_skadi.network.dto.RelatorioPdfRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeneratedReportActivity extends AppCompatActivity {

    private FrameLayout btnBackReport;
    private ImageView btnShareReport;

    private Long coldRoomId;
    private String startDate;
    private String endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_report);

        btnBackReport = findViewById(R.id.btnBackReport);
        btnShareReport = findViewById(R.id.btnShareReport);

        if (getIntent().hasExtra("EXTRA_COLD_ROOM_ID")) {
            long rId = getIntent().getLongExtra("EXTRA_COLD_ROOM_ID", 0L);
            if (rId > 0) coldRoomId = rId;
        }
        startDate = getIntent().getStringExtra("EXTRA_DATE_START");
        endDate = getIntent().getStringExtra("EXTRA_DATE_END");

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (startDate == null || startDate.isEmpty()) {
            startDate = iso.format(new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000));
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = iso.format(new Date());
        }

        btnBackReport.setOnClickListener(v -> finish());
        btnShareReport.setOnClickListener(v -> downloadAndOpenPdf());
    }

    private void downloadAndOpenPdf() {
        Toast.makeText(this, "Baixando PDF do relatório...", Toast.LENGTH_SHORT).show();

        RelatorioPdfRequest request = new RelatorioPdfRequest(null, coldRoomId, startDate, endDate);
        TrimmyApiClient.getApiService(this).downloadPdfReport(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAndOpenPdfFile(response.body());
                } else {
                    int code = response.code();
                    String msg = "Erro ao gerar PDF (" + code + ").";
                    if (code == 401) msg = "Sessão expirada. Faça login novamente.";
                    else if (code == 403) msg = "Acesso não permitido para este relatório.";
                    else if (code == 422) msg = "Filtros de data ou câmara inválidos.";
                    else if (code == 502 || code == 503) msg = "Serviço de geração de relatório indisponível.";
                    Toast.makeText(GeneratedReportActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(GeneratedReportActivity.this,
                        "Não foi possível conectar ao servidor de relatórios (porta 8000).",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveAndOpenPdfFile(ResponseBody body) {
        try {
            File pdfDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (pdfDir == null) pdfDir = getCacheDir();
            File pdfFile = new File(pdfDir, "skadi-relatorio-" + startDate + "-" + endDate + ".pdf");

            InputStream inputStream = body.byteStream();
            OutputStream outputStream = new FileOutputStream(pdfFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Toast.makeText(this, "PDF salvo: " + pdfFile.getName(), Toast.LENGTH_SHORT).show();

            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile
            );

            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(fileUri, "application/pdf");
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(openIntent, "Abrir Relatório PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "PDF baixado em " + getPackageName() + ", erro ao abrir leitor externo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
