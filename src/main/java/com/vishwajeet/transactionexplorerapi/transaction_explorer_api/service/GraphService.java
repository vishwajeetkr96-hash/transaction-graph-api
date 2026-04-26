package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception.CycleDetectedException;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception.NodeNotFoundException;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository.IGraphRepository;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation.LevelCollector;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation.UnifiedAggregationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer implementing graph exploration logic.
 *
 * Core changes:
 * - Replaced per-strategy map with LevelCollector + UnifiedAggregationService.
 * - Single traversal via LevelCollector; unified aggregates via UnifiedAggregationService.
 */
@Service
public class GraphService implements IGraphService {

    private final IGraphRepository repository;
    private final LevelCollector levelCollector;
    private final UnifiedAggregationService unifiedAggregationService;

    public GraphService(IGraphRepository repository,
                        LevelCollector levelCollector,
                        UnifiedAggregationService unifiedAggregationService) {
        this.repository = repository;
        this.levelCollector = levelCollector;
        this.unifiedAggregationService = unifiedAggregationService;
    }

    /**
     * Explore a node and its descendants up to a given depth.
     *
     * @param id Node ID
     * @param maxDepth Depth limit (default=1, max=5)
     * @return Enriched NodeResponse DTO
     */
    @Override
    public NodeResponse exploreNode(String id, int maxDepth) {
        GraphNode node = repository.findById(id)
                .orElseThrow(() -> new NodeNotFoundException(id));

        NodeResponse.NodeSummary summary = mapNodeSummary(node);

        NodeResponse response = NodeResponse.builder()
                .node(summary)
                .transactions(node.getTransactions() != null
                        ? node.getTransactions().stream().map(this::mapTransaction).toList()
                        : new ArrayList<>())
                .build();

        // Level calculation
        int level = calculateLevel(id, new HashSet<>());
        response = response.toBuilder().level(level).build();

        // Parent chain
        List<NodeResponse.NodeSummary> parentChain = buildParentChain(id).stream()
                .map(this::mapNodeSummary)
                .toList();
        response = response.toBuilder().parentChain(parentChain).build();

        // Direct children
        List<NodeResponse.NodeSummary> children = repository.findChildren(id).stream()
                .map(this::mapNodeSummary)
                .toList();
        response = response.toBuilder().children(children).build();

        // Flags
        response = response.toBuilder()
                .isRoot(level == 0)
                .isLeaf(children.isEmpty())
                .build();

        // Next-level transactions
        List<NodeResponse.TransactionSummary> nextLevelTxns = repository.findChildren(id).stream()
                .filter(child -> child.getTransactions() != null)
                .flatMap(child -> child.getTransactions().stream())
                .map(this::mapTransaction)
                .toList();
        response = response.toBuilder().nextLevelTransactions(nextLevelTxns).build();

        // Depth-limited children tree
        Set<String> visited = new HashSet<>();
        visited.add(id);
        List<NodeResponse> childrenTree = maxDepth > 0
                ? buildChildHierarchy(id, maxDepth - 1, level, visited)
                : new ArrayList<>();
        response = response.toBuilder().childrenTree(childrenTree).build();

        // Collect nodes by level once (LevelCollector) and compute unified aggregates
        // UnifiedAggregationService.aggregateAll returns consolidated AggregateInfo per level
        List<NodeResponse.AggregateInfo> aggregates = unifiedAggregationService.aggregateAll(node, maxDepth);
        response = response.toBuilder()
                .aggregateInfo(aggregates)
                .build();

        return response;
    }

    /**
     * Recursive helper to build the children hierarchy.
     */
    private List<NodeResponse> buildChildHierarchy(String parentId, int depth, int level, Set<String> visited) {
        if (depth < 0) return new ArrayList<>();

        List<GraphNode> children = repository.findChildren(parentId);
        List<NodeResponse> childResponses = new ArrayList<>();

        for (GraphNode child : children) {
            if (visited.contains(child.getId())) {
                throw new CycleDetectedException(child.getId());
            }

            Set<String> branchVisited = new HashSet<>(visited);
            branchVisited.add(child.getId());

            NodeResponse.NodeSummary summary = mapNodeSummary(child);

            NodeResponse childRes = NodeResponse.builder()
                    .node(summary)
                    .transactions(child.getTransactions() != null
                            ? child.getTransactions().stream().map(this::mapTransaction).toList()
                            : new ArrayList<>())
                    .level(level + 1)
                    .isRoot(false)
                    .isLeaf(repository.findChildren(child.getId()).isEmpty())
                    .build();

            childRes = childRes.toBuilder()
                    .childrenTree(depth > 0
                            ? buildChildHierarchy(child.getId(), depth - 1, level + 1, branchVisited)
                            : new ArrayList<>())
                    .build();

            childResponses.add(childRes);
        }
        return childResponses;
    }

    /**
     * Calculate the level of a node in the hierarchy.
     */
    private int calculateLevel(String id, Set<String> visited) {
        if (visited.contains(id)) throw new CycleDetectedException(id);
        visited.add(id);

        GraphNode node = repository.findById(id).orElse(null);
        if (node == null || node.getParentId() == null || !repository.exists(node.getParentId())) {
            return 0;
        }
        return 1 + calculateLevel(node.getParentId(), visited);
    }

    /**
     * Build the parent chain from root to direct parent.
     */
    private List<GraphNode> buildParentChain(String id) {
        Deque<GraphNode> chain = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        GraphNode current = repository.findById(id).orElse(null);

        while (current != null && current.getParentId() != null) {
            if (visited.contains(current.getId())) throw new CycleDetectedException(id);
            visited.add(current.getId());

            Optional<GraphNode> parent = repository.findById(current.getParentId());
            if (parent.isPresent()) {
                chain.addFirst(parent.get());
                current = parent.get();
            } else {
                break;
            }
        }
        return new ArrayList<>(chain);
    }

    /**
     * Filter transactions belonging to direct child nodes.
     */
    @Override
    public List<NodeTransaction> getFilteredChildTransactions(String id, Double min, Double max, String type) {
        return repository.findChildren(id).stream()
                .filter(Objects::nonNull)
                .flatMap(child -> Optional.ofNullable(child.getTransactions()).orElse(Collections.emptyList()).stream())
                .filter(t -> (min == null || t.getAmount() >= min))
                .filter(t -> (max == null || t.getAmount() <= max))
                .filter(t -> (type == null || t.getTxnType().equalsIgnoreCase(type)))
                .collect(Collectors.toList());
    }

    /**
     * Map domain GraphNode to DTO NodeSummary.
     */
    private NodeResponse.NodeSummary mapNodeSummary(GraphNode node) {
        return NodeResponse.NodeSummary.builder()
                .id(node.getId())
                .parentId(node.getParentId())
                .name(node.getName())
                .accountNumber(node.getAccountNumber())
                .build();
    }

    /**
     * Map domain NodeTransaction to DTO TransactionSummary.
     */
    private NodeResponse.TransactionSummary mapTransaction(NodeTransaction txn) {
        return NodeResponse.TransactionSummary.builder()
                .txnId(txn.getTxnId())
                .direction(txn.getDirection())
                .txnType(txn.getTxnType())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .timestamp(txn.getTimestamp())
                .description(txn.getDescription())
                .build();
    }
}
