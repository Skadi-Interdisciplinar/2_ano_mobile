package com.example.mobile_skadi;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ErrorStateActivity extends AppCompatActivity {

    public static final String EXTRA_ERROR_TYPE = "extra_error_type";
    public static final String TYPE_NO_INTERNET = "NO_INTERNET";
    public static final String TYPE_SOMETHING_WRONG = "SOMETHING_WRONG";
    public static final String TYPE_NOT_FOUND = "NOT_FOUND";

    private LinearLayout btnBackTop;
    private ImageView ivErrorIcon;
    private TextView tvErrorTitle;
    private TextView tvErrorDescription;
    private MaterialButton btnRetryError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_state);

        btnBackTop = findViewById(R.id.btnBackTop);
        ivErrorIcon = findViewById(R.id.ivErrorIcon);
        tvErrorTitle = findViewById(R.id.tvErrorTitle);
        tvErrorDescription = findViewById(R.id.tvErrorDescription);
        btnRetryError = findViewById(R.id.btnRetryError);

        bindErrorState();

        btnBackTop.setOnClickListener(v -> finish());
        btnRetryError.setOnClickListener(v -> finish());
    }

    private void bindErrorState() {
        String type = getIntent() != null ? getIntent().getStringExtra(EXTRA_ERROR_TYPE) : TYPE_NO_INTERNET;
        if (type == null) type = TYPE_NO_INTERNET;

        switch (type) {
            case TYPE_SOMETHING_WRONG:
                btnBackTop.setVisibility(View.VISIBLE);
                ivErrorIcon.setImageResource(R.drawable.ic_error_circle_x);
                tvErrorTitle.setText(getErrorTitleForType(type));
                tvErrorDescription.setText("Não foi possível carregar as informações no momento. Tente novamente em instantes.");
                btnRetryError.setVisibility(View.VISIBLE);
                break;

            case TYPE_NOT_FOUND:
                btnBackTop.setVisibility(View.VISIBLE);
                ivErrorIcon.setImageResource(R.drawable.ic_error_not_found);
                tvErrorTitle.setText(getErrorTitleForType(type));
                tvErrorDescription.setText("A página que você está procurando não existe ou foi movida.");
                btnRetryError.setVisibility(View.GONE);
                break;

            case TYPE_NO_INTERNET:
            default:
                btnBackTop.setVisibility(View.GONE);
                ivErrorIcon.setImageResource(R.drawable.ic_error_wifi_off);
                tvErrorTitle.setText(getErrorTitleForType(type));
                tvErrorDescription.setText("Você está offline. Verifique sua conexão e tente novamente.");
                btnRetryError.setVisibility(View.VISIBLE);
                break;
        }
    }

    static String getErrorTitleForType(String type) {
        if (TYPE_SOMETHING_WRONG.equals(type)) {
            return "Algo deu errado";
        } else if (TYPE_NOT_FOUND.equals(type)) {
            return "Página não encontrada";
        }
        return "Conecte-se à internet";
    }
}
