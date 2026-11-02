package com.example.mobile_skadi;

import com.example.mobile_skadi.network.dto.AddressResponse;
import com.example.mobile_skadi.network.dto.AlertResponse;
import com.example.mobile_skadi.network.dto.ColdRoomResponse;
import com.example.mobile_skadi.network.dto.DistributionCenterResponse;
import com.example.mobile_skadi.network.dto.TemperatureReadingResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Cálculos sem dependências Android, também usados pelos testes locais. */
public final class CdMapSummary {
    public enum State { ATTENTION, NORMAL, PARTIAL }

    public final long id;
    public final String name;
    public final AddressResponse address;
    public final Integer roomCount;
    public final int readingCount;
    public final BigDecimal average;
    public final Integer outsideRange;
    public final Integer pendingAlerts;
    public final Instant oldestReading;
    public final boolean complete;

    private CdMapSummary(DistributionCenterResponse cd, AddressResponse address, Integer roomCount,
            int readingCount, BigDecimal average, Integer outsideRange, Integer pendingAlerts,
            Instant oldestReading, boolean complete) {
        id = cd.getId();
        name = cd.getName() == null || cd.getName().trim().isEmpty() ? "CD " + id : cd.getName();
        this.address = address;
        this.roomCount = roomCount;
        this.readingCount = readingCount;
        this.average = average;
        this.outsideRange = outsideRange;
        this.pendingAlerts = pendingAlerts;
        this.oldestReading = oldestReading;
        this.complete = complete;
    }

    public State state() {
        if ((outsideRange != null && outsideRange > 0) || (pendingAlerts != null && pendingAlerts > 0))
            return State.ATTENTION;
        return complete && roomCount != null && roomCount > 0 ? State.NORMAL : State.PARTIAL;
    }

    public static CdMapSummary calculate(DistributionCenterResponse cd, AddressResponse address,
            List<ColdRoomResponse> rooms, Map<Long, TemperatureReadingResponse> readings,
            List<AlertResponse> alerts, boolean roomsAvailable, boolean alertsAvailable) {
        Map<Long, ColdRoomResponse> ownRooms = new LinkedHashMap<>();
        for (ColdRoomResponse room : rooms) {
            if (room.getId() != null && cd.getId().equals(room.getDistributionCenterId()))
                ownRooms.put(room.getId(), room);
        }
        boolean valid = roomsAvailable && alertsAvailable;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0, outside = 0, checkedRanges = 0, pending = 0;
        Instant oldest = null;
        for (ColdRoomResponse room : ownRooms.values()) {
            TemperatureReadingResponse reading = readings.get(room.getId());
            if (reading == null || !room.getId().equals(reading.getColdRoomId()) || reading.getTemperature() == null) {
                valid = false;
                continue;
            }
            BigDecimal temperature = reading.getTemperature();
            sum = sum.add(temperature);
            count++;
            Instant instant = parseMeasuredAt(reading.getMeasuredAt());
            if (instant == null) valid = false;
            else if (oldest == null || instant.isBefore(oldest)) oldest = instant;
            if (room.getMinimumTemperature() == null || room.getMaximumTemperature() == null
                    || room.getMinimumTemperature().compareTo(room.getMaximumTemperature()) >= 0) {
                valid = false;
            } else {
                checkedRanges++;
                if (temperature.compareTo(room.getMinimumTemperature()) < 0
                        || temperature.compareTo(room.getMaximumTemperature()) > 0) outside++;
            }
        }
        Set<Integer> seenAlerts = new HashSet<>();
        for (AlertResponse alert : alerts) {
            if (alert.getColdRoomId() == null || !ownRooms.containsKey(alert.getColdRoomId().longValue())) continue;
            if (alert.getId() != null && !seenAlerts.add(alert.getId())) continue;
            if ("ATIVO".equals(alert.getStatus()) || "RECONHECIDO".equals(alert.getStatus())) pending++;
            else if (!"RESOLVIDO".equals(alert.getStatus())) valid = false;
        }
        return new CdMapSummary(cd, address, roomsAvailable ? ownRooms.size() : null, count,
                count == 0 ? null : sum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP),
                checkedRanges == 0 ? null : outside, roomsAvailable && alertsAvailable ? pending : null,
                oldest, valid && !ownRooms.isEmpty() && count == ownRooms.size());
    }

    public static Instant parseMeasuredAt(String value) {
        if (value == null) return null;
        try { return OffsetDateTime.parse(value).toInstant(); }
        catch (DateTimeParseException ignored) {
            try { return LocalDateTime.parse(value).atZone(ZoneId.of("America/Sao_Paulo")).toInstant(); }
            catch (DateTimeParseException invalid) { return null; }
        }
    }
}
