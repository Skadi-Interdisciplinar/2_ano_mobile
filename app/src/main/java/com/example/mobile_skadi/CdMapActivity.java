package com.example.mobile_skadi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.mobile_skadi.network.RetrofitClient;
import com.example.mobile_skadi.network.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CdMapActivity extends AppCompatActivity {
    private static final long REFRESH_INTERVAL = 300000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService background = Executors.newSingleThreadExecutor();
    private final Map<Long, CdMapSummary> summaries = new LinkedHashMap<>();
    private final Map<Long, LatLng> positions = new LinkedHashMap<>();
    private final Map<Long, Marker> markers = new LinkedHashMap<>();
    private GoogleMap map;
    private CdMapDataLoader loader;
    private CdMapGeocoder geocoder;
    private Future<?> loadTask;
    private CancellationTokenSource locationTask;
    private ActivityResultLauncher<String> locationPermission;
    private Location userLocation;
    private CameraPosition savedCamera;
    private Long selectedId, nearestId;
    private boolean started, cameraPositioned, hybrid;
    private int geocodingPending;
    private TextView status;
    private final Runnable periodicRefresh = new Runnable() {
        @Override public void run() {
            if (started) { refresh(); handler.postDelayed(this, REFRESH_INTERVAL); }
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_cd_map);
        status = findViewById(R.id.tvMapStatus);
        if (state != null) {
            if (state.containsKey("selectedCd")) selectedId = state.getLong("selectedCd");
            savedCamera = state.getParcelable("mapCamera");
            userLocation = state.getParcelable("mapLocation");
            hybrid = state.getBoolean("hybrid");
        }
        locationPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted && started) requestPosition(); else toast(R.string.cd_map_location_denied);
        });
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefreshMap).setOnClickListener(v -> refresh());
        findViewById(R.id.btnMapUnits).setOnClickListener(v -> showUnitList());
        findViewById(R.id.btnMyLocation).setOnClickListener(v -> locateUser());
        findViewById(R.id.btnMapType).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.cd_map_type)
                .setSingleChoiceItems(new String[]{getString(R.string.cd_map_normal), getString(R.string.cd_map_hybrid)},
                        hybrid ? 1 : 0, (dialog, choice) -> {
                            hybrid = choice == 1;
                            if (map != null) map.setMapType(hybrid ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
                            dialog.dismiss();
                        }).show());
        setupNavigation();
        setupMap();
    }

    private void setupNavigation() {
        findViewById(R.id.navCamaras).setOnClickListener(v -> navigate(CamarasActivity.class));
        findViewById(R.id.navAlertas).setOnClickListener(v -> navigate(AlertsActivity.class));
        findViewById(R.id.navRelatorios).setOnClickListener(v -> navigate(ReportsActivity.class));
        findViewById(R.id.navTrimmy).setOnClickListener(v -> navigate(TrimmyActivity.class));
        findViewById(R.id.navInicio).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });
        UserRoleManager.applyBottomNavForRole(this);
    }

    private void navigate(Class<?> target) { startActivity(new Intent(this, target)); finish(); }

    private void setupMap() {
        if (!BuildConfig.MAPS_CONFIGURED) { mapUnavailable(R.string.cd_map_missing_key); return; }
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            mapUnavailable(R.string.cd_map_play_services); return;
        }
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragmentContainer);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.mapFragmentContainer, fragment).commitNow();
        }
        fragment.getMapAsync(googleMap -> {
            map = googleMap;
            map.setMapType(hybrid ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            updateMapPadding();
            map.setOnCameraMoveStartedListener(reason -> {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) cameraPositioned = true;
            });
            map.setOnMarkerClickListener(marker -> {
                if (marker.getTag() instanceof Long) {
                    selectedId = (Long) marker.getTag(); showSelected(); marker.showInfoWindow();
                }
                return true;
            });
            if (savedCamera != null) {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(savedCamera)); cameraPositioned = true;
            } else map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-14.235, -51.925), 3));
            enableLocationLayer();
            for (Map.Entry<Long, LatLng> entry : positions.entrySet()) addMarker(entry.getKey(), entry.getValue());
            updateDistances();
            if (geocodingPending == 0) fitCamera(false);
        });
    }

    private void mapUnavailable(int text) {
        TextView message = findViewById(R.id.tvMapUnavailable);
        message.setText(text); message.setVisibility(View.VISIBLE);
        findViewById(R.id.btnMapType).setEnabled(false);
    }

    @Override protected void onStart() {
        super.onStart(); started = true;
        if (!SessionManager.getInstance(this).hasToken()) { requireLogin(); return; }
        refresh();
        handler.postDelayed(periodicRefresh, REFRESH_INTERVAL);
        if (hasLocationPermission() && BuildConfig.MAPS_CONFIGURED) requestPosition();
    }

    @Override protected void onStop() {
        started = false; handler.removeCallbacks(periodicRefresh); cancelLoading();
        if (locationTask != null) locationTask.cancel();
        super.onStop();
    }

    @Override protected void onDestroy() { background.shutdownNow(); super.onDestroy(); }

    @Override protected void onSaveInstanceState(Bundle state) {
        if (selectedId != null) state.putLong("selectedCd", selectedId);
        if (map != null) state.putParcelable("mapCamera", map.getCameraPosition());
        else if (savedCamera != null) state.putParcelable("mapCamera", savedCamera);
        if (userLocation != null) state.putParcelable("mapLocation", userLocation);
        state.putBoolean("hybrid", hybrid);
        super.onSaveInstanceState(state);
    }

    private void cancelLoading() {
        if (loader != null) loader.cancel();
        if (loadTask != null) loadTask.cancel(true);
        if (geocoder != null) geocoder.cancel();
    }

    private void refresh() {
        if (!started) return;
        if (!SessionManager.getInstance(this).hasToken()) { requireLogin(); return; }
        cancelLoading(); status.setText(R.string.cd_map_loading);
        if (summaries.isEmpty()) text(R.id.tvMapEmptySelection, getString(R.string.cd_map_loading));
        findViewById(R.id.mapLoading).setVisibility(View.VISIBLE);
        CdMapDataLoader current = new CdMapDataLoader(RetrofitClient.getApiService(this));
        loader = current;
        loadTask = background.submit(() -> {
            try {
                List<CdMapSummary> data = current.load();
                runOnUiThread(() -> {
                    if (started && loader == current) {
                        findViewById(R.id.mapLoading).setVisibility(View.GONE); render(data);
                    }
                });
            } catch (CdMapDataLoader.AccessException access) {
                runOnUiThread(() -> {
                    if (!started || loader != current) return;
                    clearData(); findViewById(R.id.mapLoading).setVisibility(View.GONE);
                    if (access.status == 401) requireLogin();
                    else {
                        status.setText(R.string.cd_map_forbidden);
                        text(R.id.tvMapEmptySelection, getString(R.string.cd_map_forbidden));
                    }
                });
            } catch (CdMapDataLoader.HttpStatusException apiError) {
                runOnUiThread(() -> {
                    if (!started || loader != current) return;
                    findViewById(R.id.mapLoading).setVisibility(View.GONE);
                    String readable = getString(R.string.cd_map_api_error, apiError.status);
                    status.setText(readable);
                    if (summaries.isEmpty()) text(R.id.tvMapEmptySelection, readable);
                });
            } catch (InterruptedException | CancellationException cancelled) {
                Thread.currentThread().interrupt();
            } catch (IOException | RuntimeException unavailable) {
                runOnUiThread(() -> {
                    if (started && loader == current) {
                        findViewById(R.id.mapLoading).setVisibility(View.GONE);
                        int message = summaries.isEmpty() ? R.string.cd_map_error : R.string.cd_map_cached_error;
                        status.setText(message);
                        if (summaries.isEmpty()) text(R.id.tvMapEmptySelection, getString(message));
                    }
                });
            }
        });
    }

    private void requireLogin() {
        SessionManager.getInstance(this).clear(); clearData(); toast(R.string.cd_map_session_expired);
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void clearData() {
        summaries.clear(); positions.clear(); markers.clear(); nearestId = null;
        selectedId = null;
        if (map != null) map.clear();
        findViewById(R.id.layoutEmptySelection).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutSelectedCdDetails).setVisibility(View.GONE);
        findViewById(R.id.btnMapUnits).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.tvMapNearest)).setText(R.string.cd_map_distance_missing);
    }

    private void render(List<CdMapSummary> data) {
        summaries.clear(); positions.clear(); markers.clear(); nearestId = null;
        if (map != null) map.clear();
        for (CdMapSummary summary : data) summaries.put(summary.id, summary);
        findViewById(R.id.btnMapUnits).setVisibility(summaries.size() > 1 ? View.VISIBLE : View.GONE);
        if (selectedId == null || !summaries.containsKey(selectedId))
            selectedId = summaries.isEmpty() ? null : summaries.keySet().iterator().next();
        showSelected();
        if (data.isEmpty()) {
            status.setText(R.string.cd_map_empty);
            text(R.id.tvMapEmptySelection, getString(R.string.cd_map_empty));
            updateDistances(); return;
        }
        geocoder = new CdMapGeocoder(this);
        CdMapGeocoder current = geocoder;
        geocodingPending = data.size();
        status.setText(getResources().getQuantityString(R.plurals.cd_map_locating, data.size(), data.size()));
        for (CdMapSummary summary : data) {
            String address = summary.address == null ? "" : summary.address.searchAddress();
            current.locate(address, point -> {
                if (!started || geocoder != current) return;
                if (point != null) { positions.put(summary.id, point); addMarker(summary.id, point); }
                geocodingPending--; updateDistances();
                if (geocodingPending == 0) {
                    status.setText(getResources().getQuantityString(R.plurals.cd_map_loaded, summaries.size(),
                            summaries.size(), summaries.size() - positions.size()));
                    fitCamera(false);
                }
            });
        }
    }

    private void addMarker(long id, LatLng point) {
        if (map == null) return;
        CdMapSummary summary = summaries.get(id);
        if (summary == null) return;
        float hue = summary.state() == CdMapSummary.State.ATTENTION ? BitmapDescriptorFactory.HUE_RED
                : summary.state() == CdMapSummary.State.NORMAL ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_VIOLET;
        Marker marker = map.addMarker(new MarkerOptions().position(point).title(summary.name)
                .snippet(distanceLabel(id)).icon(BitmapDescriptorFactory.defaultMarker(hue)));
        if (marker != null) { marker.setTag(id); markers.put(id, marker); }
    }

    private Double distance(long id) {
        LatLng position = positions.get(id);
        return userLocation == null || position == null ? null : CdMapGeometry.distanceMeters(
                userLocation.getLatitude(), userLocation.getLongitude(), position.latitude, position.longitude);
    }

    private String distanceLabel(long id) {
        Double meters = distance(id);
        if (meters == null) return getString(positions.containsKey(id) ? R.string.cd_map_distance_missing : R.string.cd_map_position_missing);
        return meters < 1000 ? String.format(new Locale("pt", "BR"), "%.0f m", meters)
                : String.format(new Locale("pt", "BR"), "%.1f km", meters / 1000);
    }

    private void updateDistances() {
        nearestId = null;
        if (userLocation != null) for (Long id : positions.keySet())
            if (nearestId == null || distance(id) < distance(nearestId)) nearestId = id;
        TextView nearest = findViewById(R.id.tvMapNearest);
        nearest.setVisibility(nearestId == null ? View.GONE : View.VISIBLE);
        if (nearestId != null) {
            nearest.setText(getString(R.string.cd_map_nearest,
                    summaries.get(nearestId).name, distanceLabel(nearestId)));
        }
        for (Map.Entry<Long, Marker> entry : markers.entrySet()) {
            CdMapSummary summary = summaries.get(entry.getKey());
            entry.getValue().setTitle(entry.getKey().equals(nearestId)
                    ? getString(R.string.cd_map_nearest_marker, summary.name) : summary.name);
            entry.getValue().setSnippet(distanceLabel(entry.getKey()));
        }
        showSelected();
    }

    private void fitCamera(boolean force) {
        if (map == null || positions.isEmpty() || (!force && cameraPositioned)) return;
        if (userLocation != null && nearestId != null) {
            moveBounds(new LatLngBounds.Builder().include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()))
                    .include(positions.get(nearestId)).build());
        } else if (positions.size() == 1) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(positions.values().iterator().next(), 13));
        } else {
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            for (LatLng point : positions.values()) bounds.include(point);
            moveBounds(bounds.build());
        }
        cameraPositioned = true;
    }

    private void moveBounds(LatLngBounds bounds) {
        View container = findViewById(R.id.mapFragmentContainer);
        container.post(() -> {
            if (!started || map == null) return;
            if (bounds.northeast.equals(bounds.southwest)) map.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.northeast, 14));
            else if (container.getWidth() > 0 && container.getHeight() > 0)
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, container.getWidth(), container.getHeight(), dp(32)));
        });
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private void updateMapPadding() {
        if (map == null) return;
        View card = findViewById(R.id.cardCdInfo);
        View nav = findViewById(R.id.bottomNavContainer);
        View mapView = findViewById(R.id.mapFragmentContainer);
        mapView.post(() -> {
            if (map == null || card.getVisibility() != View.VISIBLE) return;
            int cardHeight = card.getHeight() > 0 ? card.getHeight() : dp(84);
            int navHeight = nav.getHeight() > 0 ? nav.getHeight() : dp(76);
            int bottom = cardHeight + navHeight + dp(44);
            map.setPadding(0, dp(8), dp(64), bottom);
        });
    }

    private void showUnitList() {
        if (summaries.isEmpty()) { toast(R.string.cd_map_empty); return; }
        List<CdMapSummary> ordered = new ArrayList<>(summaries.values());
        ordered.sort(Comparator.comparingDouble((CdMapSummary s) -> distance(s.id) == null ? Double.MAX_VALUE : distance(s.id))
                .thenComparing(s -> s.name));
        String[] labels = new String[ordered.size()];
        for (int i = 0; i < ordered.size(); i++) labels[i] = ordered.get(i).name + "\n" + distanceLabel(ordered.get(i).id);
        new AlertDialog.Builder(this).setTitle(R.string.cd_map_units).setItems(labels, (dialog, which) -> {
            selectedId = ordered.get(which).id; showSelected();
            if (map != null && positions.containsKey(selectedId)) {
                map.animateCamera(CameraUpdateFactory.newLatLng(positions.get(selectedId))); cameraPositioned = true;
            }
        }).show();
    }

    private void showSelected() {
        CdMapSummary s = selectedId == null ? null : summaries.get(selectedId);
        findViewById(R.id.layoutEmptySelection).setVisibility(s == null ? View.VISIBLE : View.GONE);
        findViewById(R.id.layoutSelectedCdDetails).setVisibility(s == null ? View.GONE : View.VISIBLE);
        if (s == null) {
            if (!summaries.isEmpty()) text(R.id.tvMapEmptySelection, getString(R.string.cd_map_select));
            updateMapPadding(); return;
        }
        text(R.id.tvSelectedCdTitle, s.name);
        text(R.id.tvSelectedCdAddress, s.address == null || s.address.displayAddress().isEmpty()
                ? getString(R.string.cd_map_address_missing) : s.address.displayAddress());
        text(R.id.tvSelectedCdDistance, distance(s.id) == null ? distanceLabel(s.id)
                : getString(R.string.cd_map_distance, distanceLabel(s.id)));
        text(R.id.tvSelectedCdTemp, getString(R.string.cd_map_temperature, s.average == null
                ? getString(R.string.cd_map_unavailable) : String.format(new Locale("pt", "BR"), "%.1f °C", s.average)));
        text(R.id.tvSelectedCdCamaras, getString(R.string.cd_map_camera_count, number(s.roomCount)));
        text(R.id.tvSelectedCdAlerts, getString(R.string.cd_map_alert_count, number(s.pendingAlerts)));
        text(R.id.tvSelectedCdOutside, getString(R.string.cd_map_outside_count, number(s.outsideRange)));
        text(R.id.tvSelectedCdUpdated, updatedLabel(s.oldestReading));
        text(R.id.tvSelectedCdCoverage, s.roomCount == null ? getString(R.string.cd_map_rooms_unavailable)
                : s.roomCount == 0 ? getString(R.string.cd_map_no_rooms)
                : s.complete ? getString(R.string.cd_map_complete)
                : getString(R.string.cd_map_partial, s.readingCount, s.roomCount));
        TextView badge = findViewById(R.id.tvSelectedCdBadge);
        if (s.state() == CdMapSummary.State.ATTENTION) {
            badge.setText(R.string.cd_map_attention);
            badge.setTextColor(ContextCompat.getColor(this, R.color.alert_danger));
            badge.setBackgroundResource(R.drawable.bg_badge_critical);
        } else if (s.state() == CdMapSummary.State.NORMAL) {
            badge.setText(R.string.cd_map_stable);
            badge.setTextColor(ContextCompat.getColor(this, R.color.alert_success));
            badge.setBackgroundResource(R.drawable.bg_badge_stable);
        } else {
            badge.setText(R.string.cd_map_incomplete); badge.setTextColor(0xff7047a3);
            badge.setBackgroundResource(R.drawable.bg_badge_map_partial);
        }
        updateMapPadding();
    }

    private String number(Integer value) { return value == null ? getString(R.string.cd_map_unavailable) : value.toString(); }
    private void text(int view, String value) { ((TextView) findViewById(view)).setText(value); }

    private String updatedLabel(Instant oldest) {
        if (oldest == null) return getString(R.string.cd_map_update_unknown);
        long minutes = Math.max(0, Duration.between(oldest, Instant.now()).toMinutes());
        if (minutes == 0) return getString(R.string.cd_map_updated_now);
        if (minutes < 60) return getString(R.string.cd_map_updated_minutes, minutes);
        if (minutes < 1440) return getString(R.string.cd_map_updated_hours, minutes / 60);
        return getString(R.string.cd_map_updated_days, minutes / 1440);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void locateUser() {
        if (hasLocationPermission()) requestPosition(); else locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    @SuppressLint("MissingPermission") private void enableLocationLayer() {
        if (map != null && hasLocationPermission()) map.setMyLocationEnabled(true);
    }

    @SuppressLint("MissingPermission") private void requestPosition() {
        if (!hasLocationPermission() || !started) return;
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            toast(R.string.cd_map_play_services); return;
        }
        if (locationTask != null) locationTask.cancel();
        CancellationTokenSource current = new CancellationTokenSource(); locationTask = current;
        toast(R.string.cd_map_location_loading);
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).setMaxUpdateAgeMillis(60000)
                .setDurationMillis(15000).build();
        LocationServices.getFusedLocationProviderClient(this).getCurrentLocation(request, current.getToken())
                .addOnSuccessListener(location -> {
                    if (!started || locationTask != current) return;
                    if (location == null) { toast(R.string.cd_map_location_failed); return; }
                    userLocation = location; enableLocationLayer(); updateDistances(); fitCamera(true);
                }).addOnFailureListener(error -> { if (started && locationTask == current) toast(R.string.cd_map_location_failed); });
    }
    private void toast(int message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }
}
