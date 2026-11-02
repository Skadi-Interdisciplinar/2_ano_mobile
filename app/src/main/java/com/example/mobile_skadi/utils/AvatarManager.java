package com.example.mobile_skadi.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.FileOutputStream;

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
            targetIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            targetIv.setPadding(0, 0, 0, 0);
        }
    }
}
