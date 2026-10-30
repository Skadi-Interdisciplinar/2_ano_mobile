package com.example.mobile_skadi;

import com.example.mobile_skadi.network.dto.LoginRequest;
import com.example.mobile_skadi.network.dto.TokenResponse;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NetworkDtoUnitTest {

    @Test
    public void loginRequest_instantiatesAndGetsFieldsCorrectly() {
        LoginRequest request = new LoginRequest("maria.rosa", "senha123");
        assertEquals("maria.rosa", request.getUsername());
        assertEquals("senha123", request.getSenha());
    }

    @Test
    public void tokenResponse_instantiatesAndGetsFieldsCorrectly() {
        TokenResponse response = new TokenResponse("jwt-token-xyz", 3600L, "2026-10-30T12:00:00");
        assertEquals("jwt-token-xyz", response.getToken());
        assertEquals(Long.valueOf(3600L), response.getExpirationTime());
        assertNotNull(response.getValidUntil());
    }
}
