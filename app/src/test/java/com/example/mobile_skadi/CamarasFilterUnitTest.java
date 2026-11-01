package com.example.mobile_skadi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CamarasFilterUnitTest {

    @Test
    public void shouldShowCamara_filterTodas_showsAllCategories() {
        assertTrue(CamarasActivity.shouldShowCamara(
                "câmara 291",
                CamarasActivity.FilterType.ACIMA_DA_MAX,
                CamarasActivity.FilterType.TODAS,
                ""
        ));
        assertTrue(CamarasActivity.shouldShowCamara(
                "câmara 1",
                CamarasActivity.FilterType.DENTRO_DA_FAIXA,
                CamarasActivity.FilterType.TODAS,
                ""
        ));
    }

    @Test
    public void shouldShowCamara_filterBySpecificCategory_filtersCorrectly() {
        assertTrue(CamarasActivity.shouldShowCamara(
                "câmara 291",
                CamarasActivity.FilterType.ACIMA_DA_MAX,
                CamarasActivity.FilterType.ACIMA_DA_MAX,
                ""
        ));
        assertFalse(CamarasActivity.shouldShowCamara(
                "câmara 1",
                CamarasActivity.FilterType.DENTRO_DA_FAIXA,
                CamarasActivity.FilterType.ACIMA_DA_MAX,
                ""
        ));
        assertFalse(CamarasActivity.shouldShowCamara(
                "câmara 521",
                CamarasActivity.FilterType.DENTRO_DA_FAIXA,
                CamarasActivity.FilterType.ABAIXO_DA_MIN,
                ""
        ));
    }

    @Test
    public void shouldShowCamara_withSearchQuery_matchesCaseInsensitive() {
        assertTrue(CamarasActivity.shouldShowCamara(
                "câmara 291",
                CamarasActivity.FilterType.ACIMA_DA_MAX,
                CamarasActivity.FilterType.TODAS,
                "291"
        ));
        assertTrue(CamarasActivity.shouldShowCamara(
                "câmara 521",
                CamarasActivity.FilterType.DENTRO_DA_FAIXA,
                CamarasActivity.FilterType.DENTRO_DA_FAIXA,
                "  CÂMARA 521 "
        ));
        assertFalse(CamarasActivity.shouldShowCamara(
                "câmara 61",
                CamarasActivity.FilterType.ACIMA_DA_MAX,
                CamarasActivity.FilterType.TODAS,
                "291"
        ));
    }

    @Test
    public void camaraDetailsExtras_constantsAreDefinedCorrectly() {
        assertEquals("extra_camara_id", CamaraDetailsActivity.EXTRA_CAMARA_ID);
        assertEquals("extra_camara_name", CamaraDetailsActivity.EXTRA_CAMARA_NAME);
        assertEquals("extra_current_temp", CamaraDetailsActivity.EXTRA_CURRENT_TEMP);
        assertEquals("extra_temp_range", CamaraDetailsActivity.EXTRA_TEMP_RANGE);
        assertEquals("extra_is_critical", CamaraDetailsActivity.EXTRA_IS_CRITICAL);
    }
}
