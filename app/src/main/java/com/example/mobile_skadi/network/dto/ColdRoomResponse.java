package com.example.mobile_skadi.network.dto;

import java.math.BigDecimal;

public class ColdRoomResponse {
    private Long id;
    private String model;
    private String location;
    private BigDecimal minimumTemperature;
    private BigDecimal maximumTemperature;
    private Long distributionCenterId;
    private String distributionCenterName;
    private Long thermometerId;
    private String thermometerModel;

    public ColdRoomResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public Long getDistributionCenterId() {
        return distributionCenterId;
    }

    public void setDistributionCenterId(Long distributionCenterId) {
        this.distributionCenterId = distributionCenterId;
    }

    public String getDistributionCenterName() {
        return distributionCenterName;
    }

    public void setDistributionCenterName(String distributionCenterName) {
        this.distributionCenterName = distributionCenterName;
    }

    public Long getThermometerId() {
        return thermometerId;
    }

    public void setThermometerId(Long thermometerId) {
        this.thermometerId = thermometerId;
    }

    public String getThermometerModel() {
        return thermometerModel;
    }

    public void setThermometerModel(String thermometerModel) {
        this.thermometerModel = thermometerModel;
    }
}
