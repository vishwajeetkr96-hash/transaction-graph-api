package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global interceptor for centralized error handling.
 * Ensures consistent JSON responses for business logic, framework-level errors,
 * and unexpected system failures.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles 404 errors for incorrect URLs/Endpoints.
     * Note: Requires 'spring.mvc.throw-exception-if-no-handler-found=true' in application.properties.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFound(NoHandlerFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "INVALID_URL", "The requested URL does not exist.");
    }

    /**
     * Handles 400 errors when URL parameters fail type conversion (e.g., maxDepth='abc').
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format("Parameter '%s' expects type '%s'.",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return buildResponse(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", detail);
    }

    /**
     * Handles manual validation failures thrown from the Controller or Service.
     * Useful for checking constraints like maxDepth > 5 or minAmount > maxAmount.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage());
    }

    /**
     * Requirement 4.2: Handles missing nodes in the hierarchy.
     */
    @ExceptionHandler(NodeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNodeNotFound(NodeNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "NODE_NOT_FOUND", ex.getMessage());
    }

    /**
     * Requirement 5.3: Prevents infinite recursion in malformed hierarchies.
     */
    @ExceptionHandler(CycleDetectedException.class)
    public ResponseEntity<Map<String, Object>> handleCycleDetected(CycleDetectedException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "CYCLE_DETECTED", ex.getMessage());
    }

    /**
     * Generic fallback for any other unexpected errors (e.g., NullPointerException, DB issues).
     * This prevents the leakage of raw stack traces to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(Exception ex) {
        // In a real app, log the exception here: logger.error(ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected system error occurred.");
    }

    /**
     * Helper to maintain a consistent error structure across the API.
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", error,
                "message", message
        ));
    }
}
