package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GraphNodeTest {

    @Test
    void builder_setsFieldsCorrectly() {
        Instant now = Instant.now();

        NodeTransaction txn = NodeTransaction.builder()
                .txnId("T1")
                .direction("CREDIT")
                .txnType("TRANSFER")
                .amount(500.0)
                .currency("INR")
                .timestamp(now.toString())
                .description("Test transaction")
                .build();

        GraphNode node = GraphNode.builder()
                .id("N1")
                .parentId("P1")
                .name("Savings Account")
                .accountNumber("ACC1001")
                .transactions(List.of(txn))
                .createdAt(now)
                .updatedAt(now)
                .attributes(Map.of("region", "East", "status", "active"))
                .build();

        assertThat(node.getId()).isEqualTo("N1");
        assertThat(node.getParentId()).isEqualTo("P1");
        assertThat(node.getName()).isEqualTo("Savings Account");
        assertThat(node.getAccountNumber()).isEqualTo("ACC1001");
        assertThat(node.getTransactions()).hasSize(1);
        assertThat(node.getTransactions().get(0).getTxnId()).isEqualTo("T1");
        assertThat(node.getCreatedAt()).isEqualTo(now);
        assertThat(node.getUpdatedAt()).isEqualTo(now);
        assertThat(node.getAttributes()).containsEntry("region", "East");
        assertThat(node.getAttributes()).containsEntry("status", "active");
    }

    @Test
    void graphNode_attributesMap_isImmutable() {
        GraphNode node = GraphNode.builder()
                .id("N2")
                .name("Checking Account")
                .accountNumber("ACC2002")
                .attributes(Map.of("type", "checking"))
                .build();

        Map<String, String> attrs = node.getAttributes();

        // Verify existing entry
        assertThat(attrs).containsEntry("type", "checking");

        // Verify immutability by expecting UnsupportedOperationException
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            attrs.put("newKey", "newValue");
        });
    }

    @Test
    void builder_allowsNullOptionalFields() {
        GraphNode node = GraphNode.builder()
                .id("N3")
                .name("Loan Account")
                .accountNumber("ACC3003")
                .build();

        assertThat(node.getParentId()).isNull();
        assertThat(node.getTransactions()).isNull();
        assertThat(node.getCreatedAt()).isNull();
        assertThat(node.getUpdatedAt()).isNull();
        assertThat(node.getAttributes()).isNull();
    }
}
