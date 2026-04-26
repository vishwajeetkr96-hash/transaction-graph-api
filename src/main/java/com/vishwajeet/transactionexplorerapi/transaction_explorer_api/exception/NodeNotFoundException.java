package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception;

/**
 * Requirement 4.2: Thrown when a requested graph node does not exist.
 *
 * <p>Example: GET /api/graph/nodes/N999 → NodeNotFoundException("Graph node N999 does not exist")</p>
 */
public class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException(String nodeId) {
        super("Graph node " + nodeId + " does not exist");
    }
}
