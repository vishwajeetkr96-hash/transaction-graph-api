package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model;

import lombok.Builder;
import lombok.Value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

/**
 * Domain model representing a transaction within a graph node.
 *
 * <p>Each transaction has direction, type, amount, currency, timestamp, and description.</p>
 *
 * <p>Industry-Grade Enhancements:</p>
 * <ul>
 *   <li><b>Immutable:</b> @Value ensures thread safety and immutability.</li>
 *   <li><b>Builder:</b> @Builder simplifies construction in services/tests.</li>
 *   <li><b>Auditing:</b> createdAt/updatedAt fields for lifecycle tracking.</li>
 *   <li><b>Extensibility:</b> attributes map for custom metadata.</li>
 *   <li><b>Validation:</b> javax.validation annotations for input safety.</li>
 * </ul>
 */
@Value
@Builder
@Jacksonized
public class NodeTransaction {

    @NotBlank
    String txnId;       // Transaction ID

    @NotBlank
    String direction;   // DEBIT or CREDIT

    @NotBlank
    String txnType;     // SALARY, TRANSFER, POS, ATM, etc.

    @NotNull
    Double amount;      // Transaction amount

    @NotBlank
    String currency;    // Currency code (e.g., INR, USD)

    @NotNull
    String timestamp;  // ISO-8601 timestamp

    String description; // Transaction description

    // Industry-grade additions
    Instant createdAt;              // Transaction creation timestamp
    Instant updatedAt;              // Transaction last update timestamp
    Map<String, String> attributes; // Flexible metadata (tags, labels, etc.)
}
