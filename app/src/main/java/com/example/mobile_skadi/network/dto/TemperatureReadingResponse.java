package com.example.mobile_skadi.network.dto;

import java.math.BigDecimal;

public class TemperatureReadingResponse {
    private Long readingId;
    private Long thermometerId;
    private Long coldRoomId;
    private Long alertId;
    private BigDecimal temperature;
    private String measuredAt;
    private String eventId;

    public TemperatureReadingResponse() {
    }

    public Long getReadingId() {
        return readingId;
    }

    public void setReadingId(Long readingId) {
        this.readingId = readingId;
    }

    public Long getThermometerId() {
        return thermometerId;
    }

    public void setThermometerId(Long thermometerId) {
        this.thermometerId = thermometerId;
    }

    public Long getColdRoomId() {
        return coldRoomId;
    }

    public void setColdRoomId(Long coldRoomId) {
        this.coldRoomId = coldRoomId;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public String getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(String measuredAt) {
        this.measuredAt = measuredAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
