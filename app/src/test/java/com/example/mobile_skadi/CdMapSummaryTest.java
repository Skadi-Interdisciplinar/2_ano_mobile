package com.example.mobile_skadi;

import com.example.mobile_skadi.network.dto.*;
import com.google.gson.Gson;
import org.junit.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.junit.Assert.*;

public class CdMapSummaryTest {
    private final Gson gson = new Gson();
    private DistributionCenterResponse cd() { return gson.fromJson("{\"id\":1,\"name\":\"Matriz\"}", DistributionCenterResponse.class); }
    private ColdRoomResponse room(long id, long cd) {
        return gson.fromJson("{\"id\":" + id + ",\"distributionCenterId\":" + cd
                + ",\"minimumTemperature\":-2,\"maximumTemperature\":2}", ColdRoomResponse.class);
    }
    private TemperatureReadingResponse reading(long id, String temperature, String time) {
        return gson.fromJson("{\"coldRoomId\":" + id + ",\"temperature\":" + temperature
                + ",\"measuredAt\":\"" + time + "\"}", TemperatureReadingResponse.class);
    }
    private AlertResponse alert(int id, int room, String status) {
        return gson.fromJson("{\"id\":" + id + ",\"coldRoomId\":" + room + ",\"status\":\"" + status + "\"}", AlertResponse.class);
    }

    @Test public void boundariesAreInclusiveAndDuplicatesAndOtherUnitsDoNotInflateTotals() {
        ColdRoomResponse first = room(1, 1);
        CdMapSummary s = CdMapSummary.calculate(cd(), null,
                Arrays.asList(first, first, room(2, 1), room(99, 2)),
                Map.of(1L, reading(1, "-2", "2026-11-01T10:00:00"),
                        2L, reading(2, "2", "2026-11-01T11:00:00")),
                List.of(alert(99, 99, "ATIVO"), alert(3, 1, "RESOLVIDO")), true, true);
        assertEquals(Integer.valueOf(2), s.roomCount);
        assertEquals(new BigDecimal("0.0"), s.average);
        assertEquals(Integer.valueOf(0), s.outsideRange);
        assertEquals(Integer.valueOf(0), s.pendingAlerts);
        assertEquals(Instant.parse("2026-11-01T13:00:00Z"), s.oldestReading);
        assertEquals(CdMapSummary.State.NORMAL, s.state());
    }

    @Test public void missingReadingDoesNotBecomeZeroAndPendingAlertsAreDeduplicated() {
        CdMapSummary s = CdMapSummary.calculate(cd(), null, List.of(room(1, 1), room(2, 1)),
                Map.of(1L, reading(1, "1.3", "2026-11-01T10:00:00-03:00")),
                List.of(alert(1, 1, "ATIVO"), alert(1, 1, "ATIVO"), alert(2, 2, "RECONHECIDO")), true, true);
        assertEquals(new BigDecimal("1.3"), s.average);
        assertEquals(1, s.readingCount);
        assertFalse(s.complete);
        assertEquals(Integer.valueOf(2), s.pendingAlerts);
        assertEquals(CdMapSummary.State.ATTENTION, s.state());
    }

    @Test public void outsideTemperatureIsAttentionEvenWithoutAlerts() {
        CdMapSummary s = CdMapSummary.calculate(cd(), null, List.of(room(1, 1)),
                Map.of(1L, reading(1, "2.1", "2026-11-01T10:00:00")), List.of(), true, true);
        assertEquals(Integer.valueOf(1), s.outsideRange);
        assertEquals(CdMapSummary.State.ATTENTION, s.state());
    }

    @Test public void unavailableSourcesAndMismatchedReadingsNeverLookNormal() {
        CdMapSummary missing = CdMapSummary.calculate(cd(), null, List.of(), Map.of(), List.of(), false, false);
        assertNull(missing.roomCount); assertNull(missing.pendingAlerts); assertNull(missing.average);
        CdMapSummary wrongRoom = CdMapSummary.calculate(cd(), null, List.of(room(1, 1)),
                Map.of(1L, reading(99, "1", "2026-11-01T10:00:00")), List.of(), true, true);
        assertNull(wrongRoom.average); assertNull(wrongRoom.outsideRange);
        assertEquals(CdMapSummary.State.PARTIAL, wrongRoom.state());
    }

    @Test public void invalidTimestampOrRangeMakesDataPartial() {
        ColdRoomResponse noRange = gson.fromJson("{\"id\":1,\"distributionCenterId\":1}", ColdRoomResponse.class);
        CdMapSummary s = CdMapSummary.calculate(cd(), null, List.of(noRange),
                Map.of(1L, reading(1, "1", "data-invalida")), List.of(), true, true);
        assertFalse(s.complete); assertNull(s.oldestReading); assertNull(s.outsideRange);
        assertEquals(CdMapSummary.State.PARTIAL, s.state());
        assertNull(CdMapSummary.parseMeasuredAt(null));
    }

    @Test public void noRoomsIsDifferentFromZeroTemperatureAndNormalOperation() {
        CdMapSummary s = CdMapSummary.calculate(cd(), null, List.of(), Map.of(), List.of(), true, true);
        assertEquals(Integer.valueOf(0), s.roomCount); assertNull(s.average);
        assertEquals(CdMapSummary.State.PARTIAL, s.state());
    }
}
