package com.wex.purchasetransaction.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "treasury.exchange-rate")
public class TreasuryProperties {

    private String baseUrl;
    private int maxMonthsLookback;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getMaxMonthsLookback() {
        return maxMonthsLookback;
    }

    public void setMaxMonthsLookback(int maxMonthsLookback) {
        this.maxMonthsLookback = maxMonthsLookback;
    }
}