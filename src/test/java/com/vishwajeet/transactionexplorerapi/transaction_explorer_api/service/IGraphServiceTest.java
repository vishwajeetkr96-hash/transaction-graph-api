package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IGraphServiceTest {

    /** Dummy implementation of IGraphService for testing the interface contract */
    static class DummyGraphService implements IGraphService {

        @Override
        public NodeResponse exploreNode(String id, int maxDepth) {
            NodeResponse.NodeSummary summary = NodeResponse.NodeSummary.builder()
                    .id(id)
                    .name("Test Node")
                    .accountNumber("ACC-" + id)
                    .build();

            return NodeResponse.builder()
                    .node(summary)
                    .level(0)
                    .isRoot(true)
                    .isLeaf(true)
                    .transactions(List.of(
                            NodeResponse.TransactionSummary.builder()
                                    .txnId("T1")
                                    .direction("CREDIT")
                                    .txnType("TRANSFER")
                                    .amount(100.0)
                                    .currency("INR")
                                    .timestamp(Instant.now().toString())
                                    .description("Dummy transaction")
                                    .build()
                    ))
                    .build();
        }

        @Override
        public List<NodeTransaction> getFilteredChildTransactions(String id, Double min, Double max, String type) {
            NodeTransaction txn = NodeTransaction.builder()
                    .txnId("T1")
                    .direction("DEBIT")
                    .txnType("ATM")
                    .amount(50.0)
                    .currency("INR")
                    .timestamp(Instant.now().toString())
                    .description("Dummy child transaction")
                    .build();

            return List.of(txn);
        }
    }

    @Test
    void exploreNode_returnsValidResponse() {
        IGraphService service = new DummyGraphService();

        NodeResponse response = service.exploreNode("N1", 1);

        assertThat(response.getNode().getId()).isEqualTo("N1");
        assertThat(response.getNode().getAccountNumber()).isEqualTo("ACC-N1");
        assertThat(response.getTransactions()).hasSize(1);
        assertThat(response.isRoot()).isTrue();
        assertThat(response.isLeaf()).isTrue();
    }

    @Test
    void getFilteredChildTransactions_returnsTransactions() {
        IGraphService service = new DummyGraphService();

        List<NodeTransaction> txns = service.getFilteredChildTransactions("N1", null, null, null);

        assertThat(txns).hasSize(1);
        assertThat(txns.get(0).getTxnId()).isEqualTo("T1");
        assertThat(txns.get(0).getTxnType()).isEqualTo("ATM");
    }
}
