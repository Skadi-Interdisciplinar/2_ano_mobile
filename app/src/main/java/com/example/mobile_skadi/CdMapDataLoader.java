package com.example.mobile_skadi;

import com.example.mobile_skadi.network.SkadiApiService;
import com.example.mobile_skadi.network.dto.AddressResponse;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.DistributionCenterResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingViewResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Response;

/** Uma execução cancelável. Apenas /cd decide quais unidades podem entrar no resultado. */
public final class CdMapDataLoader {
    public static final class AccessException extends IOException {
        public final int status;
        AccessException(int status) { super("HTTP " + status); this.status = status; }
    }

    public static final class HttpStatusException extends IOException {
        public final int status;
        HttpStatusException(int status) { super("HTTP " + status); this.status = status; }
    }

    private final SkadiApiService api;
    private final Set<Call<?>> calls = ConcurrentHashMap.newKeySet();
    private final ExecutorService workers = Executors.newFixedThreadPool(4);
    private volatile boolean cancelled;

    public CdMapDataLoader(SkadiApiService api) { this.api = api; }

    public void cancel() {
        cancelled = true;
        for (Call<?> call : calls) call.cancel();
        for (Runnable queued : workers.shutdownNow()) {
            if (queued instanceof Future<?>) ((Future<?>) queued).cancel(true);
        }
    }

    private <T> T request(Call<T> call) throws IOException {
        if (cancelled || Thread.currentThread().isInterrupted()) throw new CancellationException();
        calls.add(call);
        try {
            if (cancelled) { call.cancel(); throw new CancellationException(); }
            Response<T> response = call.execute();
            if (response.code() == 401 || response.code() == 403) throw new AccessException(response.code());
            if (!response.isSuccessful()) throw new HttpStatusException(response.code());
            return response.body();
        } finally { calls.remove(call); }
    }

    private <T> T optional(Call<T> call) throws IOException {
        try { return request(call); }
        catch (AccessException denied) { throw denied; }
        catch (HttpStatusException unavailable) {
            if (unavailable.status == 404) return null;
            throw unavailable;
        }
        catch (IOException unavailable) { return null; }
    }

    public List<CdMapSummary> load() throws IOException, InterruptedException {
        try {
            List<DistributionCenterResponse> source = request(api.getDistributionCenters());
            if (source == null) throw new IOException("Unidades indisponíveis");
            Map<Long, DistributionCenterResponse> cds = new LinkedHashMap<>();
            for (DistributionCenterResponse cd : source) if (cd.getId() != null) cds.put(cd.getId(), cd);
            if (cds.isEmpty()) return Collections.emptyList();

            List<ColdRoomResponse> roomSource = optional(api.getColdRooms());
            List<AlertResponse> alertSource = optional(api.getAlerts(null));
            List<ColdRoomResponse> rooms = new ArrayList<>();
            if (roomSource != null) for (ColdRoomResponse room : roomSource)
                if (room.getId() != null && cds.containsKey(room.getDistributionCenterId())) rooms.add(room);

            Map<Long, AddressResponse> addresses = new ConcurrentHashMap<>();
            Map<Long, TemperatureReadingViewResponse> readings = new ConcurrentHashMap<>();
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Long cdId : cds.keySet()) tasks.add(() -> {
                List<AddressResponse> items = optional(api.getDistributionCenterAddresses(cdId));
                if (items != null) items.stream()
                        .filter(a -> a.getId() != null && cdId.equals(a.getDistributionCenterId()))
                        .min(Comparator.comparing(AddressResponse::getId))
                        .ifPresent(a -> addresses.put(cdId, a));
                return null;
            });
            Set<Long> scheduledRooms = ConcurrentHashMap.newKeySet();
            for (ColdRoomResponse room : rooms) if (scheduledRooms.add(room.getId())) tasks.add(() -> {
                TemperatureReadingViewResponse reading = optional(api.getCurrentTemperature(room.getId()));
                if (reading != null) readings.put(room.getId(), reading);
                return null;
            });
            List<Future<Void>> futures = workers.invokeAll(tasks);
            for (Future<Void> future : futures) {
                try { future.get(); }
                catch (ExecutionException failure) {
                    if (failure.getCause() instanceof AccessException) throw (AccessException) failure.getCause();
                    if (failure.getCause() instanceof HttpStatusException) throw (HttpStatusException) failure.getCause();
                    throw new IOException("Falha ao carregar dados do mapa", failure.getCause());
                }
            }
            if (cancelled) throw new CancellationException();
            List<CdMapSummary> result = new ArrayList<>();
            for (DistributionCenterResponse cd : cds.values()) result.add(CdMapSummary.calculate(cd,
                    addresses.get(cd.getId()), rooms, readings,
                    alertSource == null ? Collections.emptyList() : alertSource,
                    roomSource != null, alertSource != null));
            return result;
        } finally {
            for (Call<?> call : calls) call.cancel();
            workers.shutdownNow();
        }
    }
}
