package com.example.mobile_skadi;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Set;

/** Geocodificação nativa em até quatro workers. Nenhuma operação roda na UI. */
public final class CdMapGeocoder {
    public interface Callback { void accept(LatLng point); }
    private final Context context;
    private final SharedPreferences cache;
    // Limite compartilhado: uma atualização não cria outras quatro chamadas
    // enquanto uma chamada nativa da execução anterior termina de cancelar.
    private static final ThreadPoolExecutor WORKERS = new ThreadPoolExecutor(
            4, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    static { WORKERS.allowCoreThreadTimeOut(true); }
    private final Set<Future<?>> tasks = ConcurrentHashMap.newKeySet();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean cancelled;

    public CdMapGeocoder(Context context) {
        this.context = context.getApplicationContext();
        cache = this.context.getSharedPreferences("skadi_cd_coordinates", Context.MODE_PRIVATE);
    }

    public void cancel() {
        cancelled = true;
        for (Future<?> task : tasks) task.cancel(true);
        WORKERS.purge();
    }

    public void locate(String address, Callback callback) {
        tasks.add(WORKERS.submit(() -> {
            if (cancelled) return;
            LatLng result = null;
            String key = CdMapGeometry.addressKey(address);
            if (!address.isEmpty()) {
                if (cache.contains(key + "_lat") && cache.contains(key + "_lng")) {
                    double lat = Double.longBitsToDouble(cache.getLong(key + "_lat", 0));
                    double lng = Double.longBitsToDouble(cache.getLong(key + "_lng", 0));
                    if (Double.isFinite(lat) && Double.isFinite(lng) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180)
                        result = new LatLng(lat, lng);
                }
                if (result == null && !cancelled) {
                    try { if (Geocoder.isPresent()) result = search(address); }
                    catch (RuntimeException unavailable) { /* Serviço nativo indisponível. */ }
                }
                if (result != null && !cancelled) cache.edit()
                        .putLong(key + "_lat", Double.doubleToLongBits(result.latitude))
                        .putLong(key + "_lng", Double.doubleToLongBits(result.longitude)).apply();
            }
            LatLng point = result;
            main.post(() -> { if (!cancelled) callback.accept(point); });
        }));
    }

    @SuppressWarnings("deprecation") // API bloqueante mantida em worker para suportar Android 10+.
    private LatLng search(String address) {
        try {
            List<Address> found = new Geocoder(context, new Locale("pt", "BR")).getFromLocationName(address, 1);
            if (found != null && !found.isEmpty() && found.get(0).hasLatitude() && found.get(0).hasLongitude()) {
                double lat = found.get(0).getLatitude(), lng = found.get(0).getLongitude();
                if (Double.isFinite(lat) && Double.isFinite(lng) && Math.abs(lat) <= 90 && Math.abs(lng) <= 180)
                    return new LatLng(lat, lng);
            }
        } catch (IOException | RuntimeException unavailable) { /* Exibir unidade sem localização. */ }
        return null;
    }
}
