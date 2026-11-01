package com.example.mobile_skadi.network;

import com.example.mobile_skadi.network.dto.ChatRequest;
import com.example.mobile_skadi.network.dto.ChatResponse;
import com.example.mobile_skadi.network.dto.RelatorioPdfRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TrimmyApiService {

    @POST("chat")
    Call<ChatResponse> chat(@Body ChatRequest request);

    @POST("relatorios/pdf")
    Call<ResponseBody> downloadPdfReport(@Body RelatorioPdfRequest request);
}
