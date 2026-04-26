package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception;

/**
 * Requirement 5.3: Thrown when a cycle is detected in the graph hierarchy.
 *
 * <p>Example: A node whose parentId points to one of its descendants.</p>
 */
public class CycleDetectedException extends RuntimeException {

    public CycleDetectedException(String nodeId) {
        super("Cycle detected at node " + nodeId);
    }
}
