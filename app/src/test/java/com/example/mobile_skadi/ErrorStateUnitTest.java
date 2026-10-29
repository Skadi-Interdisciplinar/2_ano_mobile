package com.example.mobile_skadi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorStateUnitTest {

    @Test
    public void getErrorTitleForType_noInternetOrDefault_returnsOfflineTitle() {
        assertEquals(
                "Conecte-se à internet",
                ErrorStateActivity.getErrorTitleForType(ErrorStateActivity.TYPE_NO_INTERNET)
        );
        assertEquals(
                "Conecte-se à internet",
                ErrorStateActivity.getErrorTitleForType(null)
        );
    }

    @Test
    public void getErrorTitleForType_somethingWrong_returnsGenericErrorTitle() {
        assertEquals(
                "Algo deu errado",
                ErrorStateActivity.getErrorTitleForType(ErrorStateActivity.TYPE_SOMETHING_WRONG)
        );
    }

    @Test
    public void getErrorTitleForType_notFound_returns404Title() {
        assertEquals(
                "Página não encontrada",
                ErrorStateActivity.getErrorTitleForType(ErrorStateActivity.TYPE_NOT_FOUND)
        );
    }
}
