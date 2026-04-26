package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.controller;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.IGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GraphControllerTest {

    private final IGraphService graphService = mock(IGraphService.class);
    private GraphController controller;

    @BeforeEach
    void setUp() {
        controller = new GraphController(graphService);
    }

    @Test
    void getNodeDetails_validRequest_callsServiceAndReturnsResponse() {
        // Arrange
        String nodeId = "N1";
        NodeResponse.NodeSummary summary = NodeResponse.NodeSummary.builder()
                .id("N1").name("Node 1").accountNumber("ACC1001").build();

        NodeResponse responseDto = NodeResponse.builder()
                .node(summary)
                .level(0)
                .parentChain(List.of())
                .children(List.of())
                .transactions(List.of())
                .nextLevelTransactions(List.of())
                .childrenTree(List.of())
                .aggregateInfo(List.of())
                .isRoot(true)
                .isLeaf(false)
                .build();

        when(graphService.exploreNode(nodeId, 1)).thenReturn(responseDto);

        // Act
        ResponseEntity<NodeResponse> resp = controller.getNodeDetails(nodeId, 1);

        // Assert
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getNode().getId()).isEqualTo("N1");
        verify(graphService, times(1)).exploreNode(nodeId, 1);
    }

    @Test
    void getFilteredTransactions_validParams_callsServiceAndReturnsList() {
        // Arrange
        String nodeId = "N1";
        NodeTransaction t = NodeTransaction.builder()
                .txnId("T1")
                .direction("CREDIT")
                .txnType("TRANSFER")
                .amount(1000.0)
                .currency("INR")
                .timestamp(Instant.parse("2025-01-01T10:00:00Z").toString()) // String timestamp
                .description("desc")
                .build();

        when(graphService.getFilteredChildTransactions(nodeId, 500.0, 2000.0, "TRANSFER"))
                .thenReturn(List.of(t));

        // Act
        ResponseEntity<List<NodeTransaction>> resp = controller.getFilteredTransactions(nodeId, 500.0, 2000.0, "TRANSFER");

        // Assert
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).hasSize(1);
        assertThat(resp.getBody().get(0).getTxnId()).isEqualTo("T1");
        verify(graphService, times(1)).getFilteredChildTransactions(nodeId, 500.0, 2000.0, "TRANSFER");
    }

    @Test
    void getNodeDetails_invalidMaxDepth_returnsBadRequest() {
        ResponseEntity<NodeResponse> resp = controller.getNodeDetails("N1", 6);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(graphService);
    }

    @Test
    void getFilteredTransactions_minGreaterThanMax_returnsBadRequest() {
        ResponseEntity<List<NodeTransaction>> resp = controller.getFilteredTransactions("N1", 2000.0, 1000.0, null);
        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(graphService);
    }
}
