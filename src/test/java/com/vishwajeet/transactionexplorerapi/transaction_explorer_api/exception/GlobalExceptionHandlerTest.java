package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNoHandlerFound_returnsNotFoundResponse() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/bad-url", null);
        ResponseEntity<Map<String, Object>> response = handler.handleNoHandlerFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("error", "INVALID_URL");
        assertThat(response.getBody()).containsEntry("message", "The requested URL does not exist.");
    }

    @Test
    void handleTypeMismatch_returnsBadRequestResponse() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException("abc", Integer.class, "maxDepth", null, new IllegalArgumentException());

        ResponseEntity<Map<String, Object>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "TYPE_MISMATCH");
        assertThat(response.getBody().get("message").toString()).contains("Parameter 'maxDepth'");
    }

    @Test
    void handleIllegalArgument_returnsBadRequestResponse() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid depth");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "INVALID_ARGUMENT");
        assertThat(response.getBody()).containsEntry("message", "Invalid depth");
    }

    @Test
    void handleNodeNotFound_returnsNotFoundResponse() {
        NodeNotFoundException ex = new NodeNotFoundException("N1");
        ResponseEntity<Map<String, Object>> response = handler.handleNodeNotFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("error", "NODE_NOT_FOUND");
        assertThat(response.getBody().get("message").toString()).contains("N1");
    }

    @Test
    void handleCycleDetected_returnsBadRequestResponse() {
        CycleDetectedException ex = new CycleDetectedException("N2");
        ResponseEntity<Map<String, Object>> response = handler.handleCycleDetected(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "CYCLE_DETECTED");
        assertThat(response.getBody().get("message").toString()).contains("N2");
    }

    @Test
    void handleGeneralError_returnsInternalServerErrorResponse() {
        Exception ex = new Exception("Unexpected");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneralError(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("error", "INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected system error occurred.");
    }
}
