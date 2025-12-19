package com.wex.purchasetransaction.transaction.treasury;

import com.wex.purchasetransaction.config.properties.TreasuryProperties;
import com.wex.purchasetransaction.transaction.client.TreasuryRestClient;
import com.wex.purchasetransaction.transaction.treasury.dto.TreasuryRate;
import com.wex.purchasetransaction.transaction.treasury.dto.TreasuryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Component
public class TreasuryExchangeRateClient implements ExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(TreasuryExchangeRateClient.class);

    private final TreasuryRestClient restClient;
    private final TreasuryProperties properties;

    public TreasuryExchangeRateClient(TreasuryRestClient restClient, TreasuryProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public BigDecimal getExchangeRate(String currency, LocalDate purchaseDate) {
        log.debug("Fetching exchange rate [currency={}, purchaseDate={}]", currency, purchaseDate);

        LocalDate windowStart = purchaseDate.minusMonths(properties.getMaxMonthsLookback());

        TreasuryResponse response = restClient.fetchRates(
                buildFilter(currency, purchaseDate, windowStart)
        );

        validateResponse(currency, purchaseDate, response, windowStart);

        TreasuryRate rate = response.data().getFirst();

        log.debug(
                "Exchange rate resolved [currency={}, recordDate={}, rate={}]",
                currency, rate.getRecordDate(), rate.getExchangeRate()
        );

        return rate.getExchangeRate();
    }

    private String buildFilter(String currency, LocalDate end, LocalDate start) {
        return "currency:eq:%s,record_date:lte:%s,record_date:gte:%s"
                .formatted(currency, end, start);
    }

    private void validateResponse(String currency, LocalDate purchaseDate, TreasuryResponse response, LocalDate windowStart) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            log.warn(
                    "No exchange rate found [currency={}, purchaseDate={}, windowStart={}, windowEnd={}]",
                    currency, purchaseDate, windowStart, purchaseDate
            );

            throw new NoSuchElementException(
                    "No exchange rate available for currency '%s' within the allowed lookup window (%s → %s)"
                            .formatted(currency, windowStart, purchaseDate)
            );
        }
    }
}
