package com.wex.purchasetransaction.transaction.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConvertedTransactionResponse(
    String id,
    String description,
    LocalDate transactionDate,
    BigDecimal purchaseAmountUsd,
    BigDecimal exchangeRate,
    BigDecimal convertedAmount,
    String targetCurrency
) {
    public ConvertedTransactionResponse(String id, String description, LocalDate transactionDate, BigDecimal purchaseAmountUsd) {
        this(
            id,
            description,
            transactionDate,
            purchaseAmountUsd,
            null,
            null,
            null
        );
    }
}


