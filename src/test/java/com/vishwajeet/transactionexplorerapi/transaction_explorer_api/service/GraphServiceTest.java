package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception.CycleDetectedException;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception.NodeNotFoundException;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository.IGraphRepository;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation.LevelCollector;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation.UnifiedAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphServiceTest {

    private GraphService service;
    private DummyRepo repo;

    static class DummyRepo implements IGraphRepository {
        private final Map<String, GraphNode> nodes = new HashMap<>();
        private final Map<String, List<GraphNode>> children = new HashMap<>();

        void addNode(GraphNode node) {
            nodes.put(node.getId(), node);
            if (node.getParentId() != null) {
                children.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }

        @Override
        public Optional<GraphNode> findById(String id) {
            return Optional.ofNullable(nodes.get(id));
        }

        @Override
        public List<GraphNode> findChildren(String parentId) {
            return children.getOrDefault(parentId, Collections.emptyList());
        }

        @Override
        public boolean exists(String id) {
            return nodes.containsKey(id);
        }

        @Override
        public List<GraphNode> findAll() {
            return new ArrayList<>(nodes.values());
        }
    }

    /**
     * Dummy aggregation service that bypasses LevelCollector logic.
     */
    static class DummyAggregationService extends UnifiedAggregationService {
        public DummyAggregationService(LevelCollector collector) {
            super(collector);
        }

        @Override
        public List<NodeResponse.AggregateInfo> aggregateAll(GraphNode root, int maxDepth) {
            return List.of(NodeResponse.AggregateInfo.builder()
                    .level(0)
                    .nodeCount(1)
                    .transactionCount(root.getTransactions() == null ? 0 : root.getTransactions().size())
                    .debitSum(0)
                    .creditSum(0)
                    .totalAmount(0)
                    .netBalance(0)
                    .build());
        }
    }

    @BeforeEach
    void setUp() {
        repo = new DummyRepo();
        LevelCollector collector = new LevelCollector(repo);
        UnifiedAggregationService aggregationService = new DummyAggregationService(collector);
        service = new GraphService(repo, collector, aggregationService);
    }

    @Test
    void exploreNode_returnsResponseWithChildrenAndTransactions() {
        NodeTransaction txn = NodeTransaction.builder()
                .txnId("T1")
                .direction("CREDIT")
                .txnType("TRANSFER")
                .amount(100.0)
                .currency("INR")
                .timestamp(Instant.now().toString())
                .description("Test txn")
                .build();

        GraphNode root = GraphNode.builder()
                .id("R")
                .name("Root")
                .accountNumber("ACC-R")
                .transactions(List.of(txn))
                .build();

        GraphNode child = GraphNode.builder()
                .id("C1")
                .parentId("R")
                .name("Child")
                .accountNumber("ACC-C1")
                .build();

        repo.addNode(root);
        repo.addNode(child);

        NodeResponse response = service.exploreNode("R", 1);

        assertThat(response.getNode().getId()).isEqualTo("R");
        assertThat(response.getTransactions()).hasSize(1);
        assertThat(response.getChildren()).extracting(NodeResponse.NodeSummary::getId).contains("C1");
        assertThat(response.isRoot()).isTrue();
        assertThat(response.isLeaf()).isFalse();
        assertThat(response.getAggregateInfo()).isNotEmpty();
    }

    @Test
    void exploreNode_throwsNodeNotFoundException() {
        assertThrows(NodeNotFoundException.class, () -> service.exploreNode("Missing", 1));
    }

    @Test
    void exploreNode_detectsCycleInHierarchy() {
        GraphNode root = GraphNode.builder().id("R").name("Root").accountNumber("ACC-R").build();
        GraphNode child = GraphNode.builder().id("C1").parentId("R").name("Child").accountNumber("ACC-C1").build();
        // Cycle: root also has parentId = C1
        GraphNode cycleNode = GraphNode.builder().id("R").parentId("C1").name("RootAgain").accountNumber("ACC-R").build();

        repo.addNode(root);
        repo.addNode(child);
        repo.addNode(cycleNode);

        assertThrows(CycleDetectedException.class, () -> service.exploreNode("R", 2));
    }

    @Test
    void getFilteredChildTransactions_appliesFiltersCorrectly() {
        NodeTransaction txn1 = NodeTransaction.builder()
                .txnId("T1").direction("DEBIT").txnType("ATM").amount(50.0)
                .currency("INR").timestamp(Instant.now().toString()).build();

        NodeTransaction txn2 = NodeTransaction.builder()
                .txnId("T2").direction("CREDIT").txnType("POS").amount(200.0)
                .currency("INR").timestamp(Instant.now().toString()).build();

        GraphNode root = GraphNode.builder().id("R").name("Root").accountNumber("ACC-R").build();
        GraphNode child = GraphNode.builder().id("C1").parentId("R").name("Child").accountNumber("ACC-C1")
                .transactions(List.of(txn1, txn2)).build();

        repo.addNode(root);
        repo.addNode(child);

        List<NodeTransaction> filtered = service.getFilteredChildTransactions("R", 100.0, null, "POS");
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).getTxnId()).isEqualTo("T2");
    }
}
