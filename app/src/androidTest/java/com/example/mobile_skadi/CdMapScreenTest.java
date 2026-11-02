package com.example.mobile_skadi;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

/** Fixtures apenas no servidor de teste do emulador; nunca conecta ao backend real. */
@RunWith(AndroidJUnit4.class)
public class CdMapScreenTest {
    private Context context;
    private MockWebServer server;
    private Field retrofitField;
    private Object previousRetrofit;
    private ActivityScenario<CdMapActivity> scenario;

    @Before public void setup() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        server = new MockWebServer(); server.start();
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) {
                String path = request.getPath();
                if ("/cd".equals(path)) return json("[{\"id\":1,\"name\":\"Unidade de teste\"}]");
                if ("/cold-rooms".equals(path)) return json("[{\"id\":1,\"distributionCenterId\":1,\"minimumTemperature\":0,\"maximumTemperature\":4}]");
                if ("/alerts".equals(path)) return json("[]");
                if (path.endsWith("/addresses")) return json("[]");
                if (path.endsWith("/current-temperature")) return json("{\"coldRoomId\":1,\"temperature\":2.4,\"measuredAt\":\"2026-11-01T10:00:00\"}");
                return new MockResponse().setResponseCode(404);
            }
        });
        Retrofit fixture = new Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build();
        // Troca restrita ao processo instrumentado, sem adicionar hooks no produto.
        retrofitField = RetrofitClient.class.getDeclaredField("retrofit");
        retrofitField.setAccessible(true);
        previousRetrofit = retrofitField.get(null);
        retrofitField.set(null, fixture);
        SessionManager.getInstance(context).clear();
        SessionManager.getInstance(context).saveToken("token-sintetico-sem-validade-externa");
        UserRoleManager.setCurrentRole(UserRoleManager.Role.GESTOR);
    }

    @After public void cleanup() throws Exception {
        if (scenario != null) scenario.close();
        SessionManager.getInstance(context).clear();
        if (retrofitField != null) retrofitField.set(null, previousRetrofit);
        UserRoleManager.setCurrentRole(UserRoleManager.Role.GESTOR);
        if (server != null) server.shutdown();
    }

    private MockResponse json(String body) { return new MockResponse().setHeader("Content-Type", "application/json").setBody(body); }

    private void awaitLoaded() throws Exception {
        long limit = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < limit) {
            final boolean[] ready = {false};
            scenario.onActivity(a -> {
                String text = ((android.widget.TextView) a.findViewById(R.id.tvMapStatus)).getText().toString();
                ready[0] = text.startsWith("1 unidade");
            });
            if (ready[0]) return;
            Thread.sleep(100);
        }
        fail("Fixture do mapa nao terminou de carregar");
    }

    private void screenshot(String name) throws Exception {
        Bitmap bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull(bitmap);
        File output = new File(context.getExternalFilesDir(null), name + ".png");
        try (FileOutputStream file = new FileOutputStream(output)) { bitmap.compress(Bitmap.CompressFormat.PNG, 100, file); }
        bitmap.recycle();
    }

    @Test public void basicManagerCanSelectRealFixtureAndKeepsSelectionAfterRotation() throws Exception {
        UserRoleManager.setCurrentPlan(UserRoleManager.Plan.BASICO);
        scenario = ActivityScenario.launch(CdMapActivity.class); awaitLoaded();
        onView(withId(R.id.btnMapUnits)).perform(click());
        onView(withText(org.hamcrest.Matchers.startsWith("Unidade de teste"))).perform(click());
        onView(withId(R.id.tvSelectedCdTitle)).check(matches(withText("Unidade de teste")));
        onView(withId(R.id.tvSelectedCdTemp)).check(matches(withText("Temperatura média: 2,4 °C")));
        screenshot("mapa-gestor-basico");
        scenario.recreate(); awaitLoaded();
        onView(withId(R.id.tvSelectedCdTitle)).check(matches(withText("Unidade de teste")));
    }

    @Test public void enterpriseManagerSeesEmptySelectionAndMapConfigurationState() throws Exception {
        UserRoleManager.setCurrentPlan(UserRoleManager.Plan.EMPRESARIAL);
        scenario = ActivityScenario.launch(CdMapActivity.class); awaitLoaded();
        onView(withId(R.id.layoutEmptySelection)).check(matches(isDisplayed()));
        if (!BuildConfig.MAPS_CONFIGURED) onView(withId(R.id.tvMapUnavailable)).check(matches(withText(R.string.cd_map_missing_key)));
        screenshot("mapa-gestor-empresarial");
    }

    @Test public void operatorDoesNotReceiveReportsNavigation() throws Exception {
        UserRoleManager.setCurrentRole(UserRoleManager.Role.FUNCIONARIO);
        scenario = ActivityScenario.launch(CdMapActivity.class); awaitLoaded();
        onView(withId(R.id.navRelatorios)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        screenshot("mapa-operador");
    }

    @Test public void forbiddenResponseClearsOldDetailsAndShowsAccessState() throws Exception {
        server.setDispatcher(new Dispatcher() {
            @Override public MockResponse dispatch(RecordedRequest request) { return new MockResponse().setResponseCode(403); }
        });
        scenario = ActivityScenario.launch(CdMapActivity.class);
        long limit = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        boolean[] forbidden = {false};
        while (!forbidden[0] && System.nanoTime() < limit) {
            scenario.onActivity(a -> forbidden[0] = ((android.widget.TextView) a.findViewById(R.id.tvMapStatus))
                    .getText().toString().equals(context.getString(R.string.cd_map_forbidden)));
            Thread.sleep(100);
        }
        assertTrue(forbidden[0]);
        onView(withId(R.id.layoutSelectedCdDetails)).check(matches(withEffectiveVisibility(Visibility.GONE)));
    }
}
