package com.wex.purchasetransaction.transaction.api;

import com.wex.purchasetransaction.exception.ApiError;
import com.wex.purchasetransaction.transaction.api.dto.ConvertedTransactionResponse;
import com.wex.purchasetransaction.transaction.api.dto.TransactionCreatedResponse;
import com.wex.purchasetransaction.transaction.api.dto.TransactionRequest;
import com.wex.purchasetransaction.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transactions", description = "Purchase transaction operations")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Store a purchase transaction",
            description = "Persists a purchase transaction in USD and returns the stored transaction"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created",
                    content = @Content(schema = @Schema(implementation = TransactionCreatedResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<TransactionCreatedResponse> storeTransaction(@Valid @RequestBody TransactionRequest request) {
        log.debug("Creating transaction");
        TransactionCreatedResponse response = transactionService.storeTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Retrieve a converted transaction",
            description = "Retrieves a stored transaction converted to a target currency using Treasury exchange rates"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Converted transaction returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConvertedTransactionResponse.class))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction or exchange rate not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}/converted")
    public ResponseEntity<ConvertedTransactionResponse> retrieveConvertedTransaction(
        @PathVariable String id,
        @RequestParam @NotBlank String targetCurrency
    ) {
        log.debug("Retrieving converted transaction [id={}, targetCurrency={}]", id, targetCurrency);
        ConvertedTransactionResponse response = transactionService.retrieveConvertedTransaction(id, targetCurrency);
        return ResponseEntity.ok(response);
    }
}
