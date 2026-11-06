package com.example.mobile_skadi.network.dto;

public class TokenResponse {
    private String token;
    private Long expirationTime;
    private String validUntil;
    private UserResponse user;

    public TokenResponse() {
    }

    public TokenResponse(String token, Long expirationTime, String validUntil) {
        this.token = token;
        this.expirationTime = expirationTime;
        this.validUntil = validUntil;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
