package com.example.mobile_skadi.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.mobile_skadi.AlertDetailsActivity;
import com.example.mobile_skadi.AlertsActivity;
import com.example.mobile_skadi.R;

import java.util.Locale;

public class AppNotificationManager {

    public static final String CHANNEL_ID_CRITICAL = "skadi_channel_critical";
    public static final String CHANNEL_NAME_CRITICAL = "Alertas Críticos Skadi";
    public static final String CHANNEL_DESC_CRITICAL = "Notificações urgentes de temperatura fora dos padrões ANVISA";

    private static final int NOTIFICATION_CRITICAL_BASE_ID = 2001;
    private static final int NOTIFICATION_TEST_ID = 1001;

    public static void initChannels(Context context) {
        if (context == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID_CRITICAL,
                        CHANNEL_NAME_CRITICAL,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESC_CRITICAL);
                channel.enableLights(true);
                channel.setLightColor(0xFFDC2626);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 200, 500});

                manager.createNotificationChannel(channel);
            }
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void sendCriticalAlertNotification(Context context, String camaraName, double currentTemp, double maxTemp, Integer alertId) {
        if (context == null) return;
        if (!hasNotificationPermission(context)) return;

        initChannels(context);

        Intent intent;
        if (alertId != null && alertId > 0) {
            intent = new Intent(context, AlertDetailsActivity.class);
            intent.putExtra(AlertDetailsActivity.EXTRA_ALERT_ID, alertId);
            intent.putExtra(AlertDetailsActivity.EXTRA_IS_RESOLVED, false);
        } else {
            intent = new Intent(context, AlertsActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = alertId != null ? alertId : NOTIFICATION_CRITICAL_BASE_ID;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String title = "🚨 Desvio Crítico: " + (camaraName != null ? camaraName : "Câmara Frigorífica");
        String body = String.format(Locale.getDefault(),
                "Temperatura atual de %.1f°C excedeu o limite máximo seguro de %.1f°C. Risco iminente à carga!",
                currentTemp, maxTemp);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_CRITICAL)
                .setSmallIcon(R.drawable.ic_bell_rounded)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setColor(0xFFDC2626)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setContentIntent(pendingIntent);

        int notifId = alertId != null ? alertId : NOTIFICATION_CRITICAL_BASE_ID;
        NotificationManagerCompat.from(context).notify(notifId, builder.build());
    }

    public static void sendTestNotification(Context context) {
        if (context == null) return;
        if (!hasNotificationPermission(context)) return;

        initChannels(context);

        Intent intent = new Intent(context, AlertsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_TEST_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_CRITICAL)
                .setSmallIcon(R.drawable.ic_bell_rounded)
                .setContentTitle("🔔 Notificações Skadi Ativas")
                .setContentText("Você receberá alertas do sistema instantaneamente sempre que houver desvios térmicos.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(0xFF0F2851)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_TEST_ID, builder.build());
    }
}
