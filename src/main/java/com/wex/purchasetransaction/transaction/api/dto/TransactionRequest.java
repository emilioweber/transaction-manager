package com.wex.purchasetransaction.transaction.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    String description,

    @NotNull(message = "Transaction date is required")
    LocalDate transactionDate,

    @NotNull(message = "Purchase amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    BigDecimal purchaseAmountUsd
) {}
