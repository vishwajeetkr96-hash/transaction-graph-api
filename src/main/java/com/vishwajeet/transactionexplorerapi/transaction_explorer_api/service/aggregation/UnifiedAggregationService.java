package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnifiedAggregationService {

    private final LevelCollector levelCollector;

    public UnifiedAggregationService(LevelCollector levelCollector) {
        this.levelCollector = levelCollector;
    }

    /**
     * Returns a list of AggregateInfo, one entry per level (ordered by level).
     */
    public List<NodeResponse.AggregateInfo> aggregateAll(GraphNode root, int maxDepth) {
        Map<Integer, List<GraphNode>> nodesByLevel = levelCollector.collectByLevel(root, maxDepth);

        return nodesByLevel.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> computeForLevel(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private NodeResponse.AggregateInfo computeForLevel(int level, List<GraphNode> nodes) {
        int nodeCount = nodes.size();

        int txnCount = nodes.stream()
                .filter(n -> n.getTransactions() != null)
                .mapToInt(n -> n.getTransactions().size())
                .sum();

        double debitSum = nodes.stream()
                .filter(n -> n.getTransactions() != null)
                .flatMap(n -> n.getTransactions().stream())
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getDirection()))
                .mapToDouble(NodeTransaction::getAmount)
                .sum();

        double creditSum = nodes.stream()
                .filter(n -> n.getTransactions() != null)
                .flatMap(n -> n.getTransactions().stream())
                .filter(t -> "CREDIT".equalsIgnoreCase(t.getDirection()))
                .mapToDouble(NodeTransaction::getAmount)
                .sum();

        double totalAmount = debitSum + creditSum;
        double netBalance = creditSum - debitSum;

        return NodeResponse.AggregateInfo.builder()
                .level(level)
                .nodeCount(nodeCount)
                .transactionCount(txnCount)
                .debitSum(debitSum)
                .creditSum(creditSum)
                .totalAmount(totalAmount)
                .netBalance(netBalance)
                .build();
    }
}
