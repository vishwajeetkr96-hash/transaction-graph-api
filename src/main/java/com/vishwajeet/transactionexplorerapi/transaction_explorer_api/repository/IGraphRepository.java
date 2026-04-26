package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for accessing GraphNode data.
 *
 * <p>Design Principles:</p>
 * <ul>
 *   <li><b>Dependency Inversion:</b> Services depend on this interface, not concrete implementations.</li>
 *   <li><b>Single Responsibility:</b> Defines only graph node retrieval operations.</li>
 *   <li><b>Open/Closed:</b> New implementations (JSON, DB, API) can be added without changing service logic.</li>
 * </ul>
 */
public interface IGraphRepository {
    Optional<GraphNode> findById(String id);

    List<GraphNode> findChildren(String parentId);

    boolean exists(String id);

    List<GraphNode> findAll();
}
