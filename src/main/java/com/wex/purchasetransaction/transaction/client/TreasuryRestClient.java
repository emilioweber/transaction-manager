package com.wex.purchasetransaction.transaction.client;

import com.wex.purchasetransaction.config.properties.TreasuryProperties;
import com.wex.purchasetransaction.transaction.treasury.dto.TreasuryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class TreasuryRestClient {

    private static final Logger log = LoggerFactory.getLogger(TreasuryRestClient.class);

    private final RestClient restClient;

    public TreasuryRestClient(
            RestClient.Builder builder,
            TreasuryProperties properties) {

        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    HttpServerErrorException.class
            },
            maxAttemptsExpression = "#{@treasuryRetryProperties.maxAttempts}",
            backoff = @Backoff(
                    delayExpression = "#{@treasuryRetryProperties.delayMs}"
            )
    )
    public TreasuryResponse fetchRates(String filter) {
        log.debug("Calling Treasury API with filter={}", filter);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("filter", filter)
                        .queryParam("sort", "-record_date")
                        .queryParam("page[size]", 1)
                        .build()
                )
                .retrieve()
                .body(TreasuryResponse.class);
    }
}

