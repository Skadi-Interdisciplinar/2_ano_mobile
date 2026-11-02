package com.example.mobile_skadi.network.dto;

import java.math.BigDecimal;

public class PriorityAlertDto {
    private Integer id;
    private Long coldRoomId;
    private String coldRoomName;
    private BigDecimal currentTemperature;
    private BigDecimal minimumTemperature;
    private BigDecimal maximumTemperature;
    private String severityLevel;
    private String status;
    private String currentLevel;
    private String createdAt;
    private Long remainingMinutes;
    private Double percentConsumed;
    private String slaStatus;
    private String assigneeName;

    public PriorityAlertDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getColdRoomId() {
        return coldRoomId;
    }

    public void setColdRoomId(Long coldRoomId) {
        this.coldRoomId = coldRoomId;
    }

    public String getColdRoomName() {
        return coldRoomName;
    }

    public void setColdRoomName(String coldRoomName) {
        this.coldRoomName = coldRoomName;
    }

    public BigDecimal getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(BigDecimal currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public BigDecimal getMinimumTemperature() {
        return minimumTemperature;
    }

    public void setMinimumTemperature(BigDecimal minimumTemperature) {
        this.minimumTemperature = minimumTemperature;
    }

    public BigDecimal getMaximumTemperature() {
        return maximumTemperature;
    }

    public void setMaximumTemperature(BigDecimal maximumTemperature) {
        this.maximumTemperature = maximumTemperature;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getRemainingMinutes() {
        return remainingMinutes;
    }

    public void setRemainingMinutes(Long remainingMinutes) {
        this.remainingMinutes = remainingMinutes;
    }

    public Double getPercentConsumed() {
        return percentConsumed;
    }

    public void setPercentConsumed(Double percentConsumed) {
        this.percentConsumed = percentConsumed;
    }

    public String getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(String slaStatus) {
        this.slaStatus = slaStatus;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }
}
