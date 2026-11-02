package com.example.mobile_skadi;

import com.example.mobile_skadi.network.dto.AddressResponse;
import com.google.gson.Gson;
import org.junit.Test;
import static org.junit.Assert.*;

public class CdMapGeometryTest {
    @Test public void straightLineDistanceHandlesSamePointAndDateLine() {
        assertEquals(0, CdMapGeometry.distanceMeters(-23.5, -46.6, -23.5, -46.6), 0.01);
        assertEquals(111195, CdMapGeometry.distanceMeters(0, 0, 1, 0), 5);
        assertEquals(222390, CdMapGeometry.distanceMeters(0, 179, 0, -179), 10);
    }

    @Test public void addressChangesInvalidateCacheWithoutCaseOrWhitespaceNoise() {
        assertEquals(CdMapGeometry.addressKey(" Rua A,  10 "), CdMapGeometry.addressKey("rua a, 10"));
        assertNotEquals(CdMapGeometry.addressKey("Rua A, 10"), CdMapGeometry.addressKey("Rua A, 11"));
    }

    @Test public void addressUsesRealApiNamesAndDoesNotLocateCountryAlone() {
        Gson gson = new Gson();
        AddressResponse full = gson.fromJson("{\"id\":3,\"distributionCenterId\":1,\"street\":\"Rua A\","
                + "\"number\":10,\"city\":\"Sao Paulo\",\"stateAbbreviation\":\"SP\",\"zipCode\":\"01001000\"}", AddressResponse.class);
        assertTrue(full.searchAddress().contains("01001000"));
        assertTrue(full.searchAddress().endsWith("Brasil"));
        assertEquals("", gson.fromJson("{}", AddressResponse.class).searchAddress());
    }
}
