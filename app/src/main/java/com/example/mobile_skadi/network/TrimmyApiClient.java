package com.example.mobile_skadi.network;

import android.content.Context;

import com.example.mobile_skadi.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrimmyApiClient {

    private static Retrofit retrofit;

    private TrimmyApiClient() {
    }

    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            String baseUrl = BuildConfig.AI_BASE_URL;
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                baseUrl = "http://127.0.0.1:8000/";
            } else if (baseUrl.contains("localhost")) {
                baseUrl = baseUrl.replace("localhost", "127.0.0.1");
            }
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static TrimmyApiService getApiService(Context context) {
        return getClient(context).create(TrimmyApiService.class);
    }
}
