package com.example.mobile_skadi;

import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AddressResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.DistributionCenterResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeneratedReportActivity extends AppCompatActivity {

    public static final String EXTRA_COLD_ROOM_ID = "EXTRA_COLD_ROOM_ID";
    public static final String EXTRA_COLD_ROOM_NAME = "EXTRA_COLD_ROOM_NAME";
    public static final String EXTRA_CD_NAME = "EXTRA_CD_NAME";
    public static final String EXTRA_DATE_START = "EXTRA_DATE_START";
    public static final String EXTRA_DATE_END = "EXTRA_DATE_END";

    private FrameLayout btnBackReport;
    private ImageView btnShareReport;
    private View documentContent;

    private TextView tvDocCdNome;
    private TextView tvDocCdEndereco;
    private TextView tvDocCdCnpj;
    private TextView tvDocResponsavelTecnico;

    private TextView tvDocCamara;
    private TextView tvDocPeriodo;
    private TextView tvDocFaixaTemperatura;
    private TextView tvDocTotalRegistros;
    private TextView tvDocRegistrosConformes;
    private TextView tvDocRegistrosAlerta;
    private TextView tvDocTempMedia;
    private TextView tvDocTempMax;
    private TextView tvDocTempMin;

    private Long coldRoomId;
    private String coldRoomName;
    private String cdName;
    private String startDate;
    private String endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_report);

        initViews();
        parseIntentExtras();
        setupListeners();
        loadDocumentData();
    }

    private void initViews() {
        btnBackReport = findViewById(R.id.btnBackReport);
        btnShareReport = findViewById(R.id.btnShareReport);
        documentContent = findViewById(R.id.documentContent);

        tvDocCdNome = findViewById(R.id.tvDocCdNome);
        tvDocCdEndereco = findViewById(R.id.tvDocCdEndereco);
        tvDocCdCnpj = findViewById(R.id.tvDocCdCnpj);
        tvDocResponsavelTecnico = findViewById(R.id.tvDocResponsavelTecnico);

        tvDocCamara = findViewById(R.id.tvDocCamara);
        tvDocPeriodo = findViewById(R.id.tvDocPeriodo);
        tvDocFaixaTemperatura = findViewById(R.id.tvDocFaixaTemperatura);
        tvDocTotalRegistros = findViewById(R.id.tvDocTotalRegistros);
        tvDocRegistrosConformes = findViewById(R.id.tvDocRegistrosConformes);
        tvDocRegistrosAlerta = findViewById(R.id.tvDocRegistrosAlerta);
        tvDocTempMedia = findViewById(R.id.tvDocTempMedia);
        tvDocTempMax = findViewById(R.id.tvDocTempMax);
        tvDocTempMin = findViewById(R.id.tvDocTempMin);
    }

    private void parseIntentExtras() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_COLD_ROOM_ID)) {
                long rId = intent.getLongExtra(EXTRA_COLD_ROOM_ID, 0L);
                if (rId > 0) coldRoomId = rId;
            }
            coldRoomName = intent.getStringExtra(EXTRA_COLD_ROOM_NAME);
            cdName = intent.getStringExtra(EXTRA_CD_NAME);
            startDate = intent.getStringExtra(EXTRA_DATE_START);
            endDate = intent.getStringExtra(EXTRA_DATE_END);
        }

        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat br = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (startDate == null || startDate.isEmpty()) {
            startDate = iso.format(new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000));
        }
        if (endDate == null || endDate.isEmpty()) {
            endDate = iso.format(new Date());
        }

        if (cdName == null || cdName.isEmpty()) {
            cdName = "CD Manaus - Unidade 20";
        }

        // Responsável Técnico atual logado
        String userName = SessionManager.getInstance(this).getName();
        if (userName != null && !userName.isEmpty()) {
            if (tvDocResponsavelTecnico != null) tvDocResponsavelTecnico.setText(userName);
        }

        // Centro de Distribuição
        if (tvDocCdNome != null) tvDocCdNome.setText(cdName);

        // Câmara selecionada
        String cLabel = (coldRoomName != null && !coldRoomName.isEmpty()) ? coldRoomName : "Todas as câmaras";
        if (tvDocCamara != null) tvDocCamara.setText(cLabel);

        // Período formatado
        try {
            Date dStart = iso.parse(startDate);
            Date dEnd = iso.parse(endDate);
            if (dStart != null && dEnd != null && tvDocPeriodo != null) {
                tvDocPeriodo.setText(br.format(dStart) + " - " + br.format(dEnd));
            }
        } catch (Exception ignored) {
            if (tvDocPeriodo != null) tvDocPeriodo.setText(startDate + " - " + endDate);
        }
    }

    private void loadDocumentData() {
        SkadiApiService apiService = RetrofitClient.getApiService(this);

        // 1. Carrega dados do CD e Endereço Real
        apiService.getDistributionCenters().enqueue(new Callback<List<DistributionCenterResponse>>() {
            @Override
            public void onResponse(Call<List<DistributionCenterResponse>> call, Response<List<DistributionCenterResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    DistributionCenterResponse matchedCd = null;
                    for (DistributionCenterResponse cd : response.body()) {
                        if (cd.getName() != null && cd.getName().equalsIgnoreCase(cdName)) {
                            matchedCd = cd;
                            break;
                        }
                    }
                    if (matchedCd == null) {
                        matchedCd = response.body().get(0);
                    }

                    if (matchedCd != null) {
                        if (tvDocCdCnpj != null && matchedCd.getCnpj() != null) {
                            tvDocCdCnpj.setText(matchedCd.getCnpj());
                        }

                        if (matchedCd.getId() != null) {
                            apiService.getDistributionCenterAddresses(matchedCd.getId()).enqueue(new Callback<List<AddressResponse>>() {
                                @Override
                                public void onResponse(Call<List<AddressResponse>> call, Response<List<AddressResponse>> addrResp) {
                                    if (addrResp.isSuccessful() && addrResp.body() != null && !addrResp.body().isEmpty()) {
                                        AddressResponse addr = addrResp.body().get(0);
                                        if (tvDocCdEndereco != null) {
                                            tvDocCdEndereco.setText(addr.displayAddress());
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<AddressResponse>> call, Throwable t) {}
                            });
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DistributionCenterResponse>> call, Throwable t) {}
        });

        // 2. Carrega faixa de temperatura da câmara (se selecionada)
        if (coldRoomId != null && coldRoomId > 0) {
            apiService.getColdRoomById(coldRoomId).enqueue(new Callback<ColdRoomResponse>() {
                @Override
                public void onResponse(Call<ColdRoomResponse> call, Response<ColdRoomResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ColdRoomResponse cr = response.body();
                        if (cr.getMinimumTemperature() != null && cr.getMaximumTemperature() != null && tvDocFaixaTemperatura != null) {
                            tvDocFaixaTemperatura.setText(String.format(Locale.getDefault(), "%.1f°C a %.1f°C",
                                    cr.getMinimumTemperature().doubleValue(), cr.getMaximumTemperature().doubleValue()));
                        }
                    }
                }

                @Override
                public void onFailure(Call<ColdRoomResponse> call, Throwable t) {}
            });
        }

        // 3. Carrega estatísticas de leituras reais
        Call<List<TemperatureReadingResponse>> readingsCall = (coldRoomId != null && coldRoomId > 0)
                ? apiService.getColdRoomReadings(coldRoomId)
                : apiService.getAllReadings();

        readingsCall.enqueue(new Callback<List<TemperatureReadingResponse>>() {
            @Override
            public void onResponse(Call<List<TemperatureReadingResponse>> call, Response<List<TemperatureReadingResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<TemperatureReadingResponse> list = response.body();
                    int total = list.size();
                    int compliant = 0;
                    double sumTemp = 0.0;
                    double minT = Double.MAX_VALUE;
                    double maxT = -Double.MAX_VALUE;
                    int validReadings = 0;

                    for (TemperatureReadingResponse r : list) {
                        if (r.getTemperature() != null) {
                            double val = r.getTemperature().doubleValue();
                            sumTemp += val;
                            if (val < minT) minT = val;
                            if (val > maxT) maxT = val;
                            validReadings++;

                            if (val >= 0.0 && val <= 4.0) {
                                compliant++;
                            }
                        }
                    }

                    int compliantPct = total > 0 ? (int) Math.round((compliant * 100.0) / total) : 0;
                    int alertCount = total - compliant;
                    int alertPct = total > 0 ? (int) Math.round((alertCount * 100.0) / total) : 0;

                    if (tvDocTotalRegistros != null) tvDocTotalRegistros.setText(String.valueOf(total));
                    if (tvDocRegistrosConformes != null) tvDocRegistrosConformes.setText(compliant + " (" + compliantPct + "%)");
                    if (tvDocRegistrosAlerta != null) tvDocRegistrosAlerta.setText(alertCount + " (" + alertPct + "%)");

                    if (validReadings > 0) {
                        double avg = sumTemp / validReadings;
                        if (tvDocTempMedia != null) tvDocTempMedia.setText(String.format(Locale.getDefault(), "%.1f°C", avg));
                        if (tvDocTempMax != null) tvDocTempMax.setText(String.format(Locale.getDefault(), "%.1f°C", maxT));
                        if (tvDocTempMin != null) tvDocTempMin.setText(String.format(Locale.getDefault(), "%.1f°C", minT));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TemperatureReadingResponse>> call, Throwable t) {}
        });
    }

    private void setupListeners() {
        btnBackReport.setOnClickListener(v -> finish());
        btnShareReport.setOnClickListener(v -> saveAndOpenPdfFile());
    }

    /**
     * Salva o documento visual completo como arquivo PDF na memória do celular
     * e o abre com o visualizador/compartilhador do sistema.
     */
    private void saveAndOpenPdfFile() {
        Toast.makeText(this, "Gerando PDF do relatório...", Toast.LENGTH_SHORT).show();

        try {
            View docView = documentContent != null ? documentContent : findViewById(android.R.id.content);
            if (docView == null) return;

            int width = docView.getWidth() > 0 ? docView.getWidth() : 1080;
            int height = docView.getHeight() > 0 ? docView.getHeight() : 1920;

            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);

            docView.draw(page.getCanvas());
            document.finishPage(page);

            // Salva na pasta de Downloads pública do celular
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            String fileName = "skadi-relatorio-" + System.currentTimeMillis() + ".pdf";
            File pdfFile = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.flush();
            fos.close();

            Toast.makeText(this, "PDF salvo em Downloads: " + fileName, Toast.LENGTH_LONG).show();

            // Abre o PDF com o aplicativo padrão do usuário
            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile
            );

            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(fileUri, "application/pdf");
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent chooser = Intent.createChooser(openIntent, "Abrir Relatório PDF");
            startActivity(chooser);

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao exportar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
