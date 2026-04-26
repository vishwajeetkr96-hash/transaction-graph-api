package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.controller;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.dto.NodeResponse;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.NodeTransaction;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.IGraphService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = GraphController.class)
class GraphControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IGraphService graphService;

    @Test
    void getNodeDetails_returnsOkAndJson() throws Exception {
        NodeResponse.NodeSummary summary = NodeResponse.NodeSummary.builder()
                .id("N1").name("Node 1").accountNumber("ACC1001").build();

        NodeResponse dto = NodeResponse.builder()
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

        Mockito.when(graphService.exploreNode(eq("N1"), eq(1))).thenReturn(dto);

        mockMvc.perform(get("/api/graph/nodes/N1")
                        .param("maxDepth", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.node.id", is("N1")))
                .andExpect(jsonPath("$.level", is(0)))
                .andExpect(jsonPath("$.root", is(true)));
    }

    @Test
    void getNodeDetails_invalidMaxDepth_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N1")
                        .param("maxDepth", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFilteredTransactions_returnsOkAndList() throws Exception {
        NodeTransaction t = NodeTransaction.builder()
                .txnId("T1")
                .direction("CREDIT")
                .txnType("TRANSFER")
                .amount(1000.0)
                .currency("INR")
                .timestamp(Instant.parse("2025-01-01T10:00:00Z").toString()) // String timestamp
                .description("desc")
                .build();

        Mockito.when(graphService.getFilteredChildTransactions(eq("N1"), eq(500.0), eq(2000.0), eq("TRANSFER")))
                .thenReturn(List.of(t));

        mockMvc.perform(get("/api/graph/nodes/N1/children-transactions")
                        .param("minAmount", "500")
                        .param("maxAmount", "2000")
                        .param("txnType", "TRANSFER")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].txnId", is("T1")));
    }

    @Test
    void getFilteredTransactions_minGreaterThanMax_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/graph/nodes/N1/children-transactions")
                        .param("minAmount", "2000")
                        .param("maxAmount", "1000"))
                .andExpect(status().isBadRequest());
    }
}
