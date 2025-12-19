package com.wex.purchasetransaction.transaction.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreatedResponse(
    String id,
    String description,
    LocalDate transactionDate,
    BigDecimal purchaseAmountUsd
) {}
