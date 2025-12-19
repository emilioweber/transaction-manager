package com.wex.purchasetransaction.transaction.service;

import com.wex.purchasetransaction.auth.repository.UserRepository;
import com.wex.purchasetransaction.auth.repository.entity.User;
import com.wex.purchasetransaction.auth.service.AuthenticatedUserProvider;
import com.wex.purchasetransaction.config.dto.AuthenticatedUser;
import com.wex.purchasetransaction.transaction.api.dto.TransactionCreatedResponse;
import com.wex.purchasetransaction.transaction.api.dto.TransactionRequest;
import com.wex.purchasetransaction.transaction.api.dto.ConvertedTransactionResponse;
import com.wex.purchasetransaction.transaction.repository.Transaction;
import com.wex.purchasetransaction.transaction.repository.TransactionRepository;
import com.wex.purchasetransaction.transaction.treasury.ExchangeRateClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class TransactionServiceTest {

    static final int USER_ID = 1;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    ExchangeRateClient exchangeRateClient;

    @Mock
    UserRepository userRepository;

    @Mock
    AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    TransactionService transactionService;

    AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setup() {
        authenticatedUser = new AuthenticatedUser(USER_ID, "admin", "USER");
        when(authenticatedUserProvider.get()).thenReturn(authenticatedUser);
    }

    @Test
    void shouldStoreTransactionWithAuthenticatedUser() {
        TransactionRequest request = new TransactionRequest(
                "Office Supplies",
                LocalDate.of(2024, 1, 10),
                new BigDecimal("10.129")
        );

        User userRef = new User();
        userRef.setId(USER_ID);

        when(userRepository.getReferenceById(USER_ID)).thenReturn(userRef);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction t = invocation.getArgument(0);
                    t.setId("tx-123");
                    return t;
                });

        TransactionCreatedResponse response = transactionService.storeTransaction(request);

        assertEquals("tx-123", response.id());
        assertEquals("Office Supplies", response.description());
        assertEquals(
                0,
                response.purchaseAmountUsd().compareTo(new BigDecimal("10.13"))
        );

        verify(transactionRepository).save(any(Transaction.class));
        verify(userRepository).getReferenceById(USER_ID);
    }

    @Test
    void shouldRetrieveConvertedTransactionForAuthenticatedUser() {
        Transaction transaction = new Transaction(
                "Laptop",
                LocalDate.of(2024, 1, 1),
                new BigDecimal("100.00"),
                new User()
        );
        transaction.setId("tx-123");

        when(transactionRepository.findByIdAndUserId("tx-123", USER_ID))
                .thenReturn(Optional.of(transaction));

        when(exchangeRateClient.getExchangeRate("Euro", transaction.getTransactionDate()))
                .thenReturn(new BigDecimal("0.9"));

        ConvertedTransactionResponse response =
                transactionService.retrieveConvertedTransaction("tx-123", "Euro");

        assertEquals("tx-123", response.id());
        assertEquals(0, response.exchangeRate().compareTo(new BigDecimal("0.9")));
        assertEquals(0, response.convertedAmount().compareTo(new BigDecimal("90.00")));
        assertEquals("Euro", response.targetCurrency());

        verify(transactionRepository).findByIdAndUserId("tx-123", USER_ID);
    }

    @Test
    void shouldThrowWhenTransactionDoesNotBelongToUser() {
        when(transactionRepository.findByIdAndUserId("tx-999", USER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> transactionService.retrieveConvertedTransaction("tx-999", "Euro")
        );

        assertEquals("Transaction not found", ex.getMessage());
    }

}
