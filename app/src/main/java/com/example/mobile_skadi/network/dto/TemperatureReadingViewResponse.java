package com.example.mobile_skadi.network.dto;

import java.math.BigDecimal;

/** Contrato de GET current-temperature (TemperatureReadingView no backend). */
public class TemperatureReadingViewResponse {
    private Long readingId;
    private Long thermometerId;
    private Long coldRoomId;
    private Long alertId;
    private BigDecimal temperature;
    private String measuredAt;
    private String eventId;

    public Long getColdRoomId() { return coldRoomId; }
    public BigDecimal getTemperature() { return temperature; }
    public String getMeasuredAt() { return measuredAt; }
}
