package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.controller;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.IGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Transaction Graph Node Explorer.
 *
 * Responsibilities:
 * - Expose HTTP endpoints for graph exploration and transaction filtering.
 * - Delegate business logic to {@link IGraphService}.
 * - Keep controller thin and aligned with REST best practices.
 */
@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final IGraphService graphService;

    public GraphController(IGraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * Explore node details including hierarchy, parent chain, children, transactions,
     * depth-limited children tree, and aggregate info.
     *
     * @param id Node ID
     * @param maxDepth Depth limit (default=1, max=5)
     * @return NodeResponse DTO
     */
    @GetMapping("/nodes/{id}")
    public ResponseEntity<NodeResponse> getNodeDetails(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int maxDepth) {

        if (maxDepth < 0 || maxDepth > 5) {
            return ResponseEntity.badRequest().build();
        }

        NodeResponse response = graphService.exploreNode(id, maxDepth);
        return ResponseEntity.ok(response);
    }

    /**
     * Filter child-node transactions by amount and type.
     *
     * @param id Node ID
     * @param minAmount Minimum amount
     * @param maxAmount Maximum amount
     * @param txnType Transaction type filter
     * @return List of filtered transactions
     */
    @GetMapping("/nodes/{id}/children-transactions")
    public ResponseEntity<List<NodeTransaction>> getFilteredTransactions(
            @PathVariable String id,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String txnType) {

        if (minAmount != null && maxAmount != null && minAmount > maxAmount) {
            return ResponseEntity.badRequest().build();
        }

        List<NodeTransaction> filteredResults =
                graphService.getFilteredChildTransactions(id, minAmount, maxAmount, txnType);

        return ResponseEntity.ok(filteredResults);
    }
}
