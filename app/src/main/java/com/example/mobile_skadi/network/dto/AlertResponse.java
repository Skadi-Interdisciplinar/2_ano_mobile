package com.example.mobile_skadi.network.dto;

import java.math.BigDecimal;

public class AlertResponse {
    private Integer id;
    private Integer coldRoomId;
    private BigDecimal referenceShelfLifeHours;
    private String currentLevel;
    private String createdAt;
    private String type;
    private String severityLevel;
    private String status;

    public AlertResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getColdRoomId() {
        return coldRoomId;
    }

    public void setColdRoomId(Integer coldRoomId) {
        this.coldRoomId = coldRoomId;
    }

    public BigDecimal getReferenceShelfLifeHours() {
        return referenceShelfLifeHours;
    }

    public void setReferenceShelfLifeHours(BigDecimal referenceShelfLifeHours) {
        this.referenceShelfLifeHours = referenceShelfLifeHours;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}
