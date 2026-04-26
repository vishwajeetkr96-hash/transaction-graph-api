package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NodeNotFoundExceptionTest {

    @Test
    void constructor_setsMessageCorrectly() {
        String nodeId = "N999";
        NodeNotFoundException ex = new NodeNotFoundException(nodeId);

        // Verify type
        assertThat(ex).isInstanceOf(RuntimeException.class);

        // Verify message
        assertThat(ex.getMessage()).isEqualTo("Graph node N999 does not exist");
    }

    @Test
    void exception_canBeThrownAndCaught() {
        String nodeId = "N123";
        try {
            throw new NodeNotFoundException(nodeId);
        } catch (NodeNotFoundException ex) {
            assertThat(ex.getMessage()).contains("Graph node N123 does not exist");
        }
    }
}
