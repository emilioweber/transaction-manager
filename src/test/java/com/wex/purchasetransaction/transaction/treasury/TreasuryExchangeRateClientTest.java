package com.wex.purchasetransaction.transaction.treasury;

import com.wex.purchasetransaction.config.properties.TreasuryProperties;
import com.wex.purchasetransaction.transaction.client.TreasuryRestClient;
import com.wex.purchasetransaction.transaction.treasury.dto.TreasuryRate;
import com.wex.purchasetransaction.transaction.treasury.dto.TreasuryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class TreasuryExchangeRateClientTest {

    @Mock
    private TreasuryRestClient restClient;

    private TreasuryExchangeRateClient exchangeRateClient;

    @BeforeEach
    void setUp() {
        TreasuryProperties properties = new TreasuryProperties();
        properties.setMaxMonthsLookback(6);

        exchangeRateClient = new TreasuryExchangeRateClient(restClient, properties);
    }

    @Test
    void shouldReturnExchangeRateWhenResponseIsValid() {
        LocalDate purchaseDate = LocalDate.of(2024, 1, 15);

        TreasuryRate rate = new TreasuryRate(
                LocalDate.of(2024, 1, 10),
                "Euro Zone-Euro",
                new BigDecimal("0.90")
        );

        TreasuryResponse response = new TreasuryResponse(List.of(rate));

        when(restClient.fetchRates(anyString())).thenReturn(response);

        BigDecimal result = exchangeRateClient.getExchangeRate("Euro", purchaseDate);

        assertEquals(0, result.compareTo(new BigDecimal("0.90"))
        );

        verify(restClient).fetchRates(
                eq("currency:eq:Euro,record_date:lte:2024-01-15,record_date:gte:2023-07-15")
        );
    }

    @Test
    void shouldThrowWhenResponseIsNull() {
        when(restClient.fetchRates(anyString())).thenReturn(null);

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> exchangeRateClient.getExchangeRate(
                        "Euro",
                        LocalDate.of(2024, 1, 15)
                )
        );

        assertTrue(ex.getMessage().contains("No exchange rate available"));
    }

    @Test
    void shouldThrowWhenResponseIsEmpty() {
        when(restClient.fetchRates(anyString())).thenReturn(new TreasuryResponse(List.of()));

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> exchangeRateClient.getExchangeRate(
                        "Euro",
                        LocalDate.of(2024, 1, 15)
                )
        );

        assertTrue(ex.getMessage().contains("No exchange rate available"));
    }
}
