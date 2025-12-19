package com.wex.purchasetransaction.transaction.treasury.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TreasuryResponse(
    @JsonProperty("data") List<TreasuryRate> data
) {}

