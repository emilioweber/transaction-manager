package com.wex.purchasetransaction.transaction.service;

import com.wex.purchasetransaction.auth.repository.UserRepository;
import com.wex.purchasetransaction.auth.service.AuthenticatedUserProvider;
import com.wex.purchasetransaction.config.dto.AuthenticatedUser;
import com.wex.purchasetransaction.transaction.api.dto.TransactionCreatedResponse;
import com.wex.purchasetransaction.transaction.api.dto.TransactionRequest;
import com.wex.purchasetransaction.transaction.api.dto.ConvertedTransactionResponse;
import com.wex.purchasetransaction.transaction.repository.Transaction;
import com.wex.purchasetransaction.transaction.repository.TransactionRepository;
import com.wex.purchasetransaction.transaction.treasury.ExchangeRateClient;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, ExchangeRateClient exchangeRateClient, AuthenticatedUserProvider authenticatedUserProvider, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.exchangeRateClient = exchangeRateClient;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionCreatedResponse storeTransaction(TransactionRequest request) {

        AuthenticatedUser user = authenticatedUserProvider.get();

        Transaction transaction = new Transaction(
                request.description(),
                request.transactionDate(),
                request.purchaseAmountUsd().setScale(2, RoundingMode.HALF_EVEN),
                userRepository.getReferenceById(user.id())
        );

        Transaction saved = transactionRepository.save(transaction);

        return new TransactionCreatedResponse(
                saved.getId(),
                saved.getDescription(),
                saved.getTransactionDate(),
                saved.getAmountUsd()
        );
    }

    public ConvertedTransactionResponse retrieveConvertedTransaction(String id, String targetCurrency) {
        AuthenticatedUser user = authenticatedUserProvider.get();

        Transaction transaction =
                transactionRepository.findByIdAndUserId(id, user.id())
                        .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        BigDecimal exchangeRate =
                exchangeRateClient.getExchangeRate(targetCurrency, transaction.getTransactionDate());

        BigDecimal convertedAmount =
                transaction.getAmountUsd()
                        .multiply(exchangeRate)
                        .setScale(2, RoundingMode.HALF_EVEN);

        return new ConvertedTransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getAmountUsd(),
                exchangeRate,
                convertedAmount,
                targetCurrency
        );
    }

}
