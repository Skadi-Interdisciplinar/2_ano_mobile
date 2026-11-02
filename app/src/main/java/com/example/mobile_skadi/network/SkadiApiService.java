package com.example.mobile_skadi.network;

import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.DistributionCenterResponse;
import com.example.mobile_skadi.network.dto.LoginRequest;
import com.example.mobile_skadi.network.dto.NotificationResponse;
import com.example.mobile_skadi.network.dto.PasswordCodeVerificationRequest;
import com.example.mobile_skadi.network.dto.PasswordRecoveryRequest;
import com.example.mobile_skadi.network.dto.SupportRequestRequest;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;
import com.example.mobile_skadi.network.dto.TokenResponse;
import com.example.mobile_skadi.network.dto.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SkadiApiService {

    @POST("auth/login")
    Call<TokenResponse> login(@Body LoginRequest request);

    @POST("auth/recover-password")
    Call<Void> recoverPassword(@Body PasswordRecoveryRequest request);

    @POST("auth/verify-recovery-code")
    Call<Void> verifyRecoveryCode(@Body PasswordCodeVerificationRequest request);

    @GET("me")
    Call<UserResponse> me();

    @GET("cold-rooms")
    Call<List<ColdRoomResponse>> getColdRooms();

    @GET("cold-rooms/{id}")
    Call<ColdRoomResponse> getColdRoomById(@Path("id") Long id);

    @GET("alerts")
    Call<List<AlertResponse>> getAlerts(@Query("coldRoomId") Integer coldRoomId);

    @GET("alerts/history")
    Call<List<AlertResponse>> getAlertsHistory(@Query("coldRoomId") Integer coldRoomId);

    @GET("readings/cold-rooms/{coldRoomId}/current-temperature")
    Call<TemperatureReadingResponse> getCurrentTemperature(@Path("coldRoomId") Long coldRoomId);

    @GET("readings")
    Call<List<TemperatureReadingResponse>> getAllReadings();

    @PATCH("alerts/{id}/recognize")
    Call<Void> recognizeAlert(@Path("id") Integer id);

    @GET("notifications")
    Call<List<NotificationResponse>> getNotifications();

    @PATCH("notifications/{id}/read")
    Call<NotificationResponse> markNotificationAsRead(@Path("id") Integer id);

    @GET("cd")
    Call<List<DistributionCenterResponse>> getDistributionCenters();

    @POST("support")
    Call<Void> createSupportRequest(@Body SupportRequestRequest request);
}
