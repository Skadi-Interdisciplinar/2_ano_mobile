package com.example.mobile_skadi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class TemperatureChartView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    // Dados mockados de temperatura (das 00h às 21h)
    private final float[] temperatures = {0.1f, 0.25f, 0.4f, 0.6f, 0.8f, 1.0f, 1.3f, 1.9f};
    private final String[] xLabels = {"00h", "03h", "06h", "09h", "12h", "15h", "18h", "21h"};
    private final String[] yLabels = {"2 deg", "1 deg", "0 deg"};
    private final float[] yValues = {2.0f, 1.0f, 0.0f};

    public TemperatureChartView(Context context) {
        super(context);
        init();
    }

    public TemperatureChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TemperatureChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(0xFF2E8FEA);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(0xFFEDF2F7);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);

        textPaint.setColor(0xFF2E8FEA);
        textPaint.setTextSize(26f);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = 55f;
        float paddingRight = 30f;
        float paddingTop = 30f;
        float paddingBottom = 45f;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        if (chartWidth <= 0 || chartHeight <= 0) return;

        // Desenha as linhas horizontais da grade e labels do eixo Y
        for (int i = 0; i < yValues.length; i++) {
            float val = yValues[i];
            float normalized = (val - 0.0f) / 2.0f; // min 0, max 2
            float y = paddingTop + chartHeight * (1f - normalized);

            // Linha da grade
            canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, gridPaint);

            // Texto do label Y (substitui 'deg' por '°')
            String label = yLabels[i].replace("deg", "\u00B0");
            canvas.drawText(label, 6f, y + 8f, textPaint);
        }

        // Pontos de coordenadas
        int count = temperatures.length;
        float[] px = new float[count];
        float[] py = new float[count];

        for (int i = 0; i < count; i++) {
            px[i] = paddingLeft + (i / (float)(count - 1)) * chartWidth;
            float norm = (temperatures[i] - 0.0f) / 2.0f;
            norm = Math.max(0f, Math.min(1f, norm));
            py[i] = paddingTop + chartHeight * (1f - norm);
        }

        // Monta o caminho cúbico suavizado (Bezier)
        linePath.reset();
        fillPath.reset();

        linePath.moveTo(px[0], py[0]);
        fillPath.moveTo(px[0], paddingTop + chartHeight);
        fillPath.lineTo(px[0], py[0]);

        for (int i = 1; i < count; i++) {
            float prevX = px[i - 1];
            float prevY = py[i - 1];
            float curX = px[i];
            float curY = py[i];

            float cpx1 = (prevX + curX) / 2f;
            float cpy1 = prevY;
            float cpx2 = (prevX + curX) / 2f;
            float cpy2 = curY;

            linePath.cubicTo(cpx1, cpy1, cpx2, cpy2, curX, curY);
            fillPath.cubicTo(cpx1, cpy1, cpx2, cpy2, curX, curY);
        }

        fillPath.lineTo(px[count - 1], paddingTop + chartHeight);
        fillPath.close();

        // Gradiente de preenchimento suave
        LinearGradient gradient = new LinearGradient(
                0, paddingTop, 0, paddingTop + chartHeight,
                new int[]{0x662E8FEA, 0x052E8FEA},
                null, Shader.TileMode.CLAMP
        );
        fillPaint.setShader(gradient);

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Labels do eixo X
        textPaint.setTextSize(22f);
        textPaint.setColor(0xFF2E8FEA);
        for (int i = 0; i < xLabels.length; i++) {
            float x = paddingLeft + (i / (float)(xLabels.length - 1)) * chartWidth;
            float textWidth = textPaint.measureText(xLabels[i]);
            float textX = Math.max(0, x - textWidth / 2f);
            canvas.drawText(xLabels[i], textX, height - 10f, textPaint);
        }
    }
}