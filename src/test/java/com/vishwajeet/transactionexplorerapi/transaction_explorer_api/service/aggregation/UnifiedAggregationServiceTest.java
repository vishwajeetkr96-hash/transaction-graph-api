package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnifiedAggregationServiceTest {

    private UnifiedAggregationService service;

    /**
     * Dummy LevelCollector that just returns a fixed map of nodes by level.
     */
    static class DummyCollector extends LevelCollector {
        private final Map<Integer, List<GraphNode>> nodesByLevel;

        DummyCollector(Map<Integer, List<GraphNode>> nodesByLevel) {
            super(null); // we don’t use the repository in this dummy
            this.nodesByLevel = nodesByLevel;
        }

        @Override
        public Map<Integer, List<GraphNode>> collectByLevel(GraphNode root, int maxDepth) {
            return nodesByLevel;
        }
    }

    @BeforeEach
    void setUp() {
        // Prepare sample transactions
        NodeTransaction debitTxn = NodeTransaction.builder()
                .txnId("D1").direction("DEBIT").txnType("ATM").amount(100.0)
                .currency("INR").timestamp(Instant.now().toString()).build();

        NodeTransaction creditTxn = NodeTransaction.builder()
                .txnId("C1").direction("CREDIT").txnType("POS").amount(200.0)
                .currency("INR").timestamp(Instant.now().toString()).build();

        GraphNode nodeLevel0 = GraphNode.builder()
                .id("N0").name("Root").accountNumber("ACC0")
                .transactions(List.of(debitTxn, creditTxn)).build();

        GraphNode nodeLevel1 = GraphNode.builder()
                .id("N1").parentId("N0").name("Child").accountNumber("ACC1")
                .transactions(List.of(creditTxn)).build();

        Map<Integer, List<GraphNode>> nodesByLevel = Map.of(
                0, List.of(nodeLevel0),
                1, List.of(nodeLevel1)
        );

        // Pass DummyCollector into constructor
        service = new UnifiedAggregationService(new DummyCollector(nodesByLevel));
    }

    @Test
    void aggregateAll_computesCorrectAggregatesPerLevel() {
        GraphNode root = GraphNode.builder().id("N0").name("Root").accountNumber("ACC0").build();

        List<NodeResponse.AggregateInfo> aggregates = service.aggregateAll(root, 2);

        assertThat(aggregates).hasSize(2);

        NodeResponse.AggregateInfo level0 = aggregates.get(0);
        assertThat(level0.getLevel()).isEqualTo(0);
        assertThat(level0.getNodeCount()).isEqualTo(1);
        assertThat(level0.getTransactionCount()).isEqualTo(2);
        assertThat(level0.getDebitSum()).isEqualTo(100.0);
        assertThat(level0.getCreditSum()).isEqualTo(200.0);
        assertThat(level0.getTotalAmount()).isEqualTo(300.0);
        assertThat(level0.getNetBalance()).isEqualTo(100.0);

        NodeResponse.AggregateInfo level1 = aggregates.get(1);
        assertThat(level1.getLevel()).isEqualTo(1);
        assertThat(level1.getNodeCount()).isEqualTo(1);
        assertThat(level1.getTransactionCount()).isEqualTo(1);
        assertThat(level1.getDebitSum()).isEqualTo(0.0);
        assertThat(level1.getCreditSum()).isEqualTo(200.0);
        assertThat(level1.getTotalAmount()).isEqualTo(200.0);
        assertThat(level1.getNetBalance()).isEqualTo(200.0);
    }
}
