package com.wex.purchasetransaction.transaction.client;

import com.wex.purchasetransaction.config.properties.TreasuryProperties;
import com.wex.purchasetransaction.transaction.treasury.dto.TreasuryRate;
import com.wex.purchasetransaction.transaction.treasury.dto.TreasuryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class TreasuryRestClientTest {

    private MockRestServiceServer mockServer;
    private TreasuryRestClient treasuryRestClient;

    @BeforeEach
    void setUp() {
        TreasuryProperties properties = new TreasuryProperties();
        properties.setBaseUrl("http://localhost");

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        treasuryRestClient = new TreasuryRestClient(builder, properties);
    }

    @Test
    void shouldFetchExchangeRateSuccessfully() {
        String jsonResponse = """
            {
              "data": [
                {
                  "record_date": "2024-01-01",
                  "country_currency_desc": "Euro Zone-Euro",
                  "exchange_rate": 1.1
                }
              ]
            }
            """;

        mockServer.expect(request -> {
                    URI uri = request.getURI();
                    assertEquals("http", uri.getScheme());
                    assertEquals("localhost", uri.getHost());
                    assertTrue(uri.getQuery().contains("filter=currency:eq:Euro"));
                    assertTrue(uri.getQuery().contains("sort=-record_date"));
                    assertTrue(uri.getQuery().contains("page[size]=1"));
                })
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        TreasuryResponse response = treasuryRestClient.fetchRates("currency:eq:Euro");

        assertNotNull(response);
        assertEquals(1, response.data().size());

        TreasuryRate rate = response.data().getFirst();

        assertEquals(LocalDate.of(2024, 1, 1), rate.getRecordDate());
        assertEquals(
                0,
                rate.getExchangeRate().compareTo(new BigDecimal("1.1"))
        );

        mockServer.verify();
    }

}
