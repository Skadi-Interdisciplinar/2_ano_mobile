package com.example.mobile_skadi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.example.mobile_skadi.R;
import com.example.mobile_skadi.network.SessionManager;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class AvatarManager {

    private static final String AVATAR_FILE_NAME = "custom_profile_avatar.png";

    public static boolean saveAvatar(Context context, Bitmap bitmap) {
        if (context == null || bitmap == null) return false;
        try {
            Bitmap circular = getCircularBitmap(bitmap);
            File file = new File(context.getFilesDir(), AVATAR_FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                circular.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Bitmap loadAvatar(Context context) {
        if (context == null) return null;
        File file = new File(context.getFilesDir(), AVATAR_FILE_NAME);
        if (!file.exists()) return null;
        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasCustomAvatar(Context context) {
        if (context == null) return false;
        File file = new File(context.getFilesDir(), AVATAR_FILE_NAME);
        return file.exists();
    }

    public static boolean clearAvatar(Context context) {
        if (context == null) return false;
        File file = new File(context.getFilesDir(), AVATAR_FILE_NAME);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        int x = (width - size) / 2;
        int y = (height - size) / 2;
        Bitmap squared = Bitmap.createBitmap(bitmap, x, y, size, size);

        int targetSize = Math.min(size, 512);
        Bitmap scaled = Bitmap.createScaledBitmap(squared, targetSize, targetSize, true);
        if (squared != bitmap && squared != scaled) {
            squared.recycle();
        }

        Bitmap output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setShader(new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float radius = targetSize / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        return output;
    }

    public static String getUserInitial(Context context) {
        if (context == null) return "U";
        String name = SessionManager.getInstance(context).getName();
        if (name == null || name.trim().isEmpty()) {
            name = SessionManager.getInstance(context).getUsername();
        }
        if (name != null && !name.trim().isEmpty()) {
            String trimmed = name.trim();
            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    return String.valueOf(c).toUpperCase(Locale.getDefault());
                }
            }
        }
        return "U";
    }

    public static Bitmap createInitialAvatar(Context context, String initial, int size) {
        if (size <= 0) size = 256;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Fundo circular elegante (azul refinado com excelente contraste)
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#2563EB"));
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, bgPaint);

        // Letra inicial em branco
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        try {
            Typeface typeface = ResourcesCompat.getFont(context, R.font.manrope);
            if (typeface != null) {
                textPaint.setTypeface(Typeface.create(typeface, Typeface.BOLD));
            } else {
                textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            }
        } catch (Exception e) {
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float baseline = radius - (fontMetrics.ascent + fontMetrics.descent) / 2f;
        canvas.drawText(initial, radius, baseline, textPaint);

        return bitmap;
    }

    public static void applyAvatar(Context context, View view) {
        if (context == null || view == null) return;
        ImageView targetIv = null;
        if (view instanceof ImageView) {
            targetIv = (ImageView) view;
        } else if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            if (card.getChildCount() > 0 && card.getChildAt(0) instanceof ImageView) {
                targetIv = (ImageView) card.getChildAt(0);
            }
        }
        if (targetIv == null) return;

        Bitmap avatar = loadAvatar(context);
        if (avatar != null) {
            targetIv.setImageBitmap(avatar);
            targetIv.setBackground(null);
            targetIv.setPadding(0, 0, 0, 0);
            targetIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // Enquanto o usuário não colocou foto personalizada, exibe a inicial do nome/usuário
            String initial = getUserInitial(context);
            int size = targetIv.getWidth() > 0 ? targetIv.getWidth() : 256;
            Bitmap initialBmp = createInitialAvatar(context, initial, size);
            targetIv.setImageBitmap(initialBmp);
            targetIv.setBackground(null);
            targetIv.setPadding(0, 0, 0, 0);
            targetIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
