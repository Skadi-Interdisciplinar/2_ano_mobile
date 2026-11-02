package com.example.mobile_skadi.network.dto;

import java.util.ArrayList;
import java.util.List;

public class AddressResponse {
    private Long id;
    private String zipCode;
    private String street;
    private Integer number;
    private String city;
    private String neighborhood;
    private String complement;
    private String stateAbbreviation;
    private Long distributionCenterId;

    public Long getId() { return id; }
    public Long getDistributionCenterId() { return distributionCenterId; }

    public String displayAddress() {
        List<String> parts = new ArrayList<>();
        add(parts, street);
        if (number != null) add(parts, number.toString());
        add(parts, neighborhood);
        add(parts, city);
        add(parts, stateAbbreviation);
        add(parts, zipCode);
        return String.join(", ", parts);
    }

    public String searchAddress() {
        // Não localizar só a cidade/país como se fosse a posição da unidade.
        if (street == null || street.trim().isEmpty() || city == null || city.trim().isEmpty()) return "";
        return displayAddress() + ", Brasil";
    }

    private static void add(List<String> parts, String value) {
        if (value != null && !value.trim().isEmpty()) parts.add(value.trim());
    }
}
