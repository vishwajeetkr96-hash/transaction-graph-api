package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NodeResponseTest {

    @Test
    void nodeSummaryBuilder_setsFieldsCorrectly() {
        NodeResponse.NodeSummary summary = NodeResponse.NodeSummary.builder()
                .id("N1")
                .parentId("P1")
                .name("Node 1")
                .accountNumber("ACC1001")
                .build();

        assertThat(summary.getId()).isEqualTo("N1");
        assertThat(summary.getParentId()).isEqualTo("P1");
        assertThat(summary.getName()).isEqualTo("Node 1");
        assertThat(summary.getAccountNumber()).isEqualTo("ACC1001");
    }

    @Test
    void transactionSummaryBuilder_setsFieldsCorrectly() {
        NodeResponse.TransactionSummary txn = NodeResponse.TransactionSummary.builder()
                .txnId("T1")
                .direction("CREDIT")
                .txnType("TRANSFER")
                .amount(1050.0)
                .currency("INR")
                .timestamp("2025-01-01T10:00:00Z")
                .description("TRANSFER transaction T1")
                .build();

        assertThat(txn.getTxnId()).isEqualTo("T1");
        assertThat(txn.getDirection()).isEqualTo("CREDIT");
        assertThat(txn.getTxnType()).isEqualTo("TRANSFER");
        assertThat(txn.getAmount()).isEqualTo(1050.0);
        assertThat(txn.getCurrency()).isEqualTo("INR");
        assertThat(txn.getTimestamp()).isEqualTo("2025-01-01T10:00:00Z");
        assertThat(txn.getDescription()).contains("TRANSFER");
    }

    @Test
    void aggregateInfoBuilder_setsFieldsCorrectly() {
        NodeResponse.AggregateInfo info = NodeResponse.AggregateInfo.builder()
                .level(0)
                .nodeCount(1)
                .transactionCount(5)
                .debitSum(2300.0)
                .creditSum(3450.0)
                .totalAmount(5750.0)
                .netBalance(1150.0)
                .build();

        assertThat(info.getLevel()).isEqualTo(0);
        assertThat(info.getNodeCount()).isEqualTo(1);
        assertThat(info.getTransactionCount()).isEqualTo(5);
        assertThat(info.getDebitSum()).isEqualTo(2300.0);
        assertThat(info.getCreditSum()).isEqualTo(3450.0);
        assertThat(info.getTotalAmount()).isEqualTo(5750.0);
        assertThat(info.getNetBalance()).isEqualTo(1150.0);
    }

    @Test
    void nodeResponseBuilder_andToBuilder_workCorrectly() {
        NodeResponse.NodeSummary summary = NodeResponse.NodeSummary.builder()
                .id("N1")
                .name("Node 1")
                .accountNumber("ACC1001")
                .build();

        NodeResponse response = NodeResponse.builder()
                .node(summary)
                .level(0)
                .parentChain(List.of())
                .children(List.of())
                .isRoot(true)
                .isLeaf(false)
                .transactions(List.of())
                .nextLevelTransactions(List.of())
                .childrenTree(List.of())
                .aggregateInfo(List.of())
                .build();

        assertThat(response.getNode().getId()).isEqualTo("N1");
        assertThat(response.isRoot()).isTrue();
        assertThat(response.isLeaf()).isFalse();

        // Test immutability
        assertThat(response.getChildren()).isEmpty();

        // Test toBuilder
        NodeResponse modified = response.toBuilder().level(1).isLeaf(true).build();
        assertThat(modified.getLevel()).isEqualTo(1);
        assertThat(modified.isLeaf()).isTrue();
        // Original remains unchanged
        assertThat(response.getLevel()).isEqualTo(0);
        assertThat(response.isLeaf()).isFalse();
    }
}
