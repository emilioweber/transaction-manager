package com.wex.purchasetransaction.transaction.treasury.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TreasuryRate(
    @JsonProperty("record_date") LocalDate recordDate,
    @JsonProperty("country_currency_desc") String countryCurrencyDesc,
    @JsonProperty("exchange_rate") BigDecimal exchangeRate
) {
    public LocalDate getRecordDate() { return recordDate; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
}
