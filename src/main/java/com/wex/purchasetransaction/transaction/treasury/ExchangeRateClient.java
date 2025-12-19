package com.wex.purchasetransaction.transaction.treasury;

import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateClient {
    @Cacheable(
            cacheNames = "treasuryRates",
            key = "#currency + ':' + #purchaseDate",
            sync = true
    )
    BigDecimal getExchangeRate(String currency, LocalDate transactionDate);
}
