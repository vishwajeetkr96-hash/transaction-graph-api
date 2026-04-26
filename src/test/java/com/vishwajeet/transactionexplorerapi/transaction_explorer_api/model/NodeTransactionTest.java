package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeTransactionTest {

    @Test
    void builder_setsFieldsCorrectly() {
        Instant now = Instant.now();

        NodeTransaction txn = NodeTransaction.builder()
                .txnId("T1")
                .direction("CREDIT")
                .txnType("TRANSFER")
                .amount(1000.0)
                .currency("INR")
                .timestamp(now.toString())
                .description("Salary transfer")
                .createdAt(now)
                .updatedAt(now)
                .attributes(Map.of("category", "salary", "region", "east"))
                .build();

        assertThat(txn.getTxnId()).isEqualTo("T1");
        assertThat(txn.getDirection()).isEqualTo("CREDIT");
        assertThat(txn.getTxnType()).isEqualTo("TRANSFER");
        assertThat(txn.getAmount()).isEqualTo(1000.0);
        assertThat(txn.getCurrency()).isEqualTo("INR");
        assertThat(txn.getTimestamp()).isEqualTo(now.toString());
        assertThat(txn.getDescription()).isEqualTo("Salary transfer");
        assertThat(txn.getCreatedAt()).isEqualTo(now);
        assertThat(txn.getUpdatedAt()).isEqualTo(now);
        assertThat(txn.getAttributes()).containsEntry("category", "salary");
    }

    @Test
    void attributesMap_isImmutable() {
        NodeTransaction txn = NodeTransaction.builder()
                .txnId("T2")
                .direction("DEBIT")
                .txnType("ATM")
                .amount(200.0)
                .currency("USD")
                .timestamp(Instant.now().toString())
                .attributes(Map.of("channel", "ATM"))
                .build();

        Map<String, String> attrs = txn.getAttributes();
        assertThat(attrs).containsEntry("channel", "ATM");

        // Verify immutability by expecting UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> attrs.put("newKey", "newValue"));
    }

    @Test
    void builder_allowsNullOptionalFields() {
        NodeTransaction txn = NodeTransaction.builder()
                .txnId("T3")
                .direction("DEBIT")
                .txnType("POS")
                .amount(50.0)
                .currency("EUR")
                .timestamp(Instant.now().toString())
                .build();

        assertThat(txn.getDescription()).isNull();
        assertThat(txn.getCreatedAt()).isNull();
        assertThat(txn.getUpdatedAt()).isNull();
        assertThat(txn.getAttributes()).isNull();
    }
}
