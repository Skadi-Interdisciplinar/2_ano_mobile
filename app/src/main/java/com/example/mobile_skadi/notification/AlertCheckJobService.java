package com.example.mobile_skadi.notification;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.example.mobile_skadi.UserRoleManager;
import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.EmployeeDashboardResponse;
import com.example.mobile_skadi.network.dto.ManagerDashboardResponse;
import com.example.mobile_skadi.network.dto.PriorityAlertDto;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class AlertCheckJobService extends JobService {

    private static final String TAG = "AlertCheckJobService";
    public static final int JOB_ID = 9001;
    private static final long PERIODIC_INTERVAL_MS = 15 * 60 * 1000; // 15 minutos

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void schedulePeriodicJob(Context context) {
        if (context == null) return;
        try {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler == null) return;

            JobInfo existingJob = scheduler.getPendingJob(JOB_ID);
            if (existingJob != null) {
                return; // Já está agendado
            }

            ComponentName componentName = new ComponentName(context, AlertCheckJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, componentName)
                    .setPeriodic(PERIODIC_INTERVAL_MS)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();

            int result = scheduler.schedule(jobInfo);
            Log.d(TAG, "Agendamento de alerta background JobService resultado: " + result);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao agendar JobService: " + e.getMessage());
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Executando verificação de alertas em segundo plano com app fechado...");
        executor.execute(() -> {
            boolean needsReschedule = false;
            try {
                Context context = getApplicationContext();
                SessionManager sessionManager = SessionManager.getInstance(context);
                if (sessionManager.hasToken()) {
                    SkadiApiService api = RetrofitClient.getApiService(context);
                    boolean isEmployee = UserRoleManager.isEmployee(context);

                    if (isEmployee) {
                        Response<EmployeeDashboardResponse> resp = api.getEmployeeDashboard().execute();
                        if (resp.isSuccessful() && resp.body() != null) {
                            PriorityAlertDto next = resp.body().getNextAction();
                            if (next != null) {
                                boolean isCritical = next.getSeverityLevel() != null
                                        && next.getSeverityLevel().toUpperCase().contains("CRITIC");
                                boolean isRecognized = "RECONHECIDO".equalsIgnoreCase(next.getStatus());
                                if (isCritical && !isRecognized) {
                                    double curr = next.getCurrentTemperature() != null ? next.getCurrentTemperature().doubleValue() : 8.0;
                                    double max = next.getMaximumTemperature() != null ? next.getMaximumTemperature().doubleValue() : 4.0;
                                    AppNotificationManager.sendCriticalAlertNotification(
                                            context,
                                            next.getColdRoomName(),
                                            curr,
                                            max,
                                            next.getId()
                                    );
                                }
                            }
                        }
                    } else {
                        Response<ManagerDashboardResponse> resp = api.getManagerDashboard().execute();
                        if (resp.isSuccessful() && resp.body() != null && resp.body().getPriorities() != null) {
                            for (PriorityAlertDto p : resp.body().getPriorities()) {
                                boolean isCritical = p.getSeverityLevel() != null
                                        && p.getSeverityLevel().toUpperCase().contains("CRITIC");
                                boolean isRecognized = "RECONHECIDO".equalsIgnoreCase(p.getStatus());
                                if (isCritical && !isRecognized) {
                                    double curr = p.getCurrentTemperature() != null ? p.getCurrentTemperature().doubleValue() : 8.0;
                                    double max = p.getMaximumTemperature() != null ? p.getMaximumTemperature().doubleValue() : 4.0;
                                    AppNotificationManager.sendCriticalAlertNotification(
                                            context,
                                            p.getColdRoomName(),
                                            curr,
                                            max,
                                            p.getId()
                                    );
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro no job de checagem: " + e.getMessage());
                needsReschedule = true;
            } finally {
                jobFinished(params, needsReschedule);
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
