package com.wex.purchasetransaction.transaction.api;

import com.wex.purchasetransaction.config.web.RateLimitFilter;
import com.wex.purchasetransaction.config.web.TokenAuthenticationFilter;
import com.wex.purchasetransaction.transaction.api.dto.TransactionRequest;
import com.wex.purchasetransaction.transaction.api.dto.ConvertedTransactionResponse;
import com.wex.purchasetransaction.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TransactionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {RateLimitFilter.class, TokenAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(roles = "USER")
    void shouldStoreTransaction() throws Exception {
        when(transactionService.storeTransaction(any(TransactionRequest.class)))
                .thenAnswer(invocation -> {
                    TransactionRequest req = invocation.getArgument(0);
                    return new ConvertedTransactionResponse(
                            "tx-123",
                            req.description(),
                            req.transactionDate(),
                            req.purchaseAmountUsd()
                    );
                });

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "description": "Office Supplies",
                  "transactionDate": "2024-01-10",
                  "purchaseAmountUsd": 10.13
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("tx-123"))
                .andExpect(jsonPath("$.description").value("Office Supplies"))
                .andExpect(jsonPath("$.purchaseAmountUsd").value(10.13));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRetrieveConvertedTransaction() throws Exception {
        when(transactionService.retrieveConvertedTransaction("tx-123", "Euro"))
                .thenReturn(new ConvertedTransactionResponse(
                        "tx-123",
                        "Laptop",
                        LocalDate.of(2024, 1, 1),
                        new BigDecimal("100.00"),
                        new BigDecimal("0.9"),
                        new BigDecimal("90.00"),
                        "Euro"
                ));

        mockMvc.perform(get("/api/v1/transactions/tx-123/converted")
                        .param("targetCurrency", "Euro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchangeRate").value(0.9))
                .andExpect(jsonPath("$.convertedAmount").value(90.00))
                .andExpect(jsonPath("$.targetCurrency").value("Euro"));
    }
}

