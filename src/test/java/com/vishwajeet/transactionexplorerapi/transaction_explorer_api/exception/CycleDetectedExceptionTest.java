package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CycleDetectedExceptionTest {

    @Test
    void constructor_setsMessageCorrectly() {
        String nodeId = "N42";
        CycleDetectedException ex = new CycleDetectedException(nodeId);

        // Verify type
        assertThat(ex).isInstanceOf(RuntimeException.class);

        // Verify message
        assertThat(ex.getMessage()).isEqualTo("Cycle detected at node N42");
    }

    @Test
    void exception_canBeThrownAndCaught() {
        String nodeId = "N99";
        try {
            throw new CycleDetectedException(nodeId);
        } catch (CycleDetectedException ex) {
            assertThat(ex.getMessage()).contains("N99");
        }
    }
}
