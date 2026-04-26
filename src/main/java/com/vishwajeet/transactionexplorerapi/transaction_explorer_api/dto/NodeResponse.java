package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * DTO for returning enriched GraphNode details to API clients.
 *
 * <p>Encapsulates hierarchy metadata, parent/child relationships,
 * transactions, and optional aggregate information.</p>
 *
 * <p>Design Principles:</p>
 * <ul>
 *   <li><b>Immutable:</b> @Value ensures thread safety and immutability.</li>
 *   <li><b>Builder:</b> @Builder provides flexible construction in services/tests.</li>
 *   <li><b>Composition:</b> Uses nested DTOs (NodeSummary, TransactionSummary, AggregateInfo).</li>
 *   <li><b>Extensible:</b> Can add fields (e.g., childrenTree, aggregates) without breaking clients.</li>
 * </ul>
 */
@Value
@Builder(toBuilder = true)
public class NodeResponse {
    NodeSummary node;                       // Core node details
    int level;                              // Hierarchy level
    List<NodeSummary> parentChain;          // Ordered root → parent chain
    List<NodeSummary> children;             // Direct children
    boolean isRoot;                         // Root flag
    boolean isLeaf;                         // Leaf flag
    List<TransactionSummary> transactions;  // Transactions of this node
    List<TransactionSummary> nextLevelTransactions; // Transactions of direct children
    List<NodeResponse> childrenTree;        // Depth-limited nested children (Bonus 5.1)
    List<AggregateInfo> aggregateInfo;      // Aggregate info per level (Bonus 5.2)

    /**
     * Lightweight summary of a node for API responses.
     */
    @Value
    @Builder
    public static class NodeSummary {
        String id;
        String parentId;
        String name;
        String accountNumber;
    }

    /**
     * Lightweight summary of a transaction for API responses.
     */
    @Value
    @Builder
    public static class TransactionSummary {
        String txnId;
        String direction;   // DEBIT or CREDIT
        String txnType;     // SALARY, TRANSFER, POS, ATM, etc.
        double amount;
        String currency;
        String timestamp;   // ISO-8601 string
        String description;
    }

    /**
     * Aggregate information per level (Bonus 5.2).
     * Document in README whether totalAmount is debit-only, credit-only, or absolute sum.
     */
    @Value
    @Builder
    public static class AggregateInfo {
        int level;
        int nodeCount;
        int transactionCount;
        double debitSum;
        double creditSum;
        double totalAmount; // debit + credit
        double netBalance;  // credit - debit
    }
}
