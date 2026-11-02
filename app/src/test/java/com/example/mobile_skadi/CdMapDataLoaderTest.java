package com.example.mobile_skadi;

import com.example.mobile_skadi.network.SkadiApiService;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class CdMapDataLoaderTest {
    private MockWebServer server;
    private SkadiApiService api;
    @Before public void start() throws Exception {
        server = new MockWebServer(); server.start();
        api = new Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(SkadiApiService.class);
    }
    @After public void stop() throws Exception { server.shutdown(); }
    private MockResponse json(String body) { return new MockResponse().setHeader("Content-Type", "application/json").setBody(body); }
    private MockResponse baseline(String path) {
        if (path.equals("/cd")) return json("[{\"id\":1,\"name\":\"Matriz\"}]");
        if (path.equals("/cold-rooms")) return json("[{\"id\":10,\"distributionCenterId\":1,\"minimumTemperature\":0,\"maximumTemperature\":4},"
                + "{\"id\":99,\"distributionCenterId\":99}]");
        if (path.equals("/alerts")) return json("[]");
        if (path.equals("/distribution-centers/1/addresses")) return json("[{\"id\":8,\"distributionCenterId\":1},"
                + "{\"id\":2,\"distributionCenterId\":1},{\"id\":1,\"distributionCenterId\":99}]");
        return new MockResponse().setResponseCode(404);
    }

    @Test public void onlyAuthorizedCdsAndRoomsAreLoadedAnd404MeansMissingReading() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) { return baseline(request.getPath()); }
        });
        List<CdMapSummary> result = new CdMapDataLoader(api).load();
        assertEquals(1, result.size());
        CdMapSummary s = result.get(0);
        assertEquals(1L, s.id); assertEquals(Integer.valueOf(1), s.roomCount);
        assertEquals(Long.valueOf(2), s.address.getId());
        assertNull(s.average); assertEquals(CdMapSummary.State.PARTIAL, s.state());
        for (int i = 0, n = server.getRequestCount(); i < n; i++) {
            String path = server.takeRequest().getPath();
            assertFalse(path.contains("/99"));
            assertFalse(path.equals("/distribution-centers"));
        }
    }

    @Test public void authenticationAndForbiddenChildCallsAreNotSilentlyIgnored() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                return request.getPath().equals("/cd") ? new MockResponse().setResponseCode(401) : baseline(request.getPath());
            }
        });
        try { new CdMapDataLoader(api).load(); fail("401 must expire session"); }
        catch (CdMapDataLoader.AccessException e) { assertEquals(401, e.status); }
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                return request.getPath().contains("/addresses") ? new MockResponse().setResponseCode(403) : baseline(request.getPath());
            }
        });
        try { new CdMapDataLoader(api).load(); fail("403 must not display cached data"); }
        catch (CdMapDataLoader.AccessException e) { assertEquals(403, e.status); }
    }

    @Test public void parallelAddressAndReadingRequestsNeverExceedFour() throws Exception {
        AtomicInteger active = new AtomicInteger(), peak = new AtomicInteger();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                if (path.equals("/cold-rooms")) {
                    StringBuilder rooms = new StringBuilder("[");
                    for (int i = 1; i <= 12; i++) {
                        if (i > 1) rooms.append(',');
                        rooms.append("{\"id\":").append(i).append(",\"distributionCenterId\":1}");
                    }
                    return json(rooms.append(']').toString());
                }
                if (path.contains("/current-temperature") || path.contains("/addresses")) {
                    int current = active.incrementAndGet(); peak.accumulateAndGet(current, Math::max);
                    try { Thread.sleep(40); return new MockResponse().setResponseCode(404); }
                    finally { active.decrementAndGet(); }
                }
                return baseline(path);
            }
        });
        new CdMapDataLoader(api).load();
        assertTrue(peak.get() > 1); assertTrue(peak.get() <= 4);
    }

    @Test public void cancelInterruptsInFlightNetworkRequest() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                entered.countDown(); return json("[]").setBodyDelay(2, TimeUnit.SECONDS);
            }
        });
        ExecutorService worker = Executors.newSingleThreadExecutor();
        CdMapDataLoader loader = new CdMapDataLoader(api);
        try {
            Future<?> future = worker.submit(() -> { try { loader.load(); fail("Cancelled load must not succeed"); }
                catch (java.io.IOException | InterruptedException | CancellationException expected) { } });
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            loader.cancel(); future.get(4, TimeUnit.SECONDS);
        } finally { worker.shutdownNow(); }
    }
}
