package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;

import java.util.List;

/**
 * Abstraction for graph exploration services.
 *
 * <p>Allows multiple implementations (JSON, DB, cached) without changing controllers.</p>
 */
public interface IGraphService {
    NodeResponse exploreNode(String id, int maxDepth);

    List<NodeTransaction> getFilteredChildTransactions(String id, Double min, Double max, String type);
}
