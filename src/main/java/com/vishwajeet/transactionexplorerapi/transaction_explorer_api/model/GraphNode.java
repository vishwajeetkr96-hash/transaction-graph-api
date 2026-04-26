package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model;

import lombok.Builder;
import lombok.Value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Domain model representing a node in the transaction graph.
 *
 * <p>Each node corresponds to a logical entity (e.g., account, activity bucket)
 * and contains transactions. The hierarchy is defined by parentId.</p>
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
public class GraphNode {

    @NotBlank
    String id;                  // Node ID

    String parentId;            // Parent node ID (null for root)

    @NotBlank
    @Size(max = 255)
    String name;                // Human-readable label

    @NotBlank
    @Size(max = 50)
    String accountNumber;       // Logical account/entity identifier

    List<NodeTransaction> transactions; // Transactions belonging to this node

    // Industry-grade additions
    Instant createdAt;                // Node creation timestamp
    Instant updatedAt;                // Node last update timestamp
    Map<String, String> attributes;   // Flexible metadata (tags, labels, etc.)
}
