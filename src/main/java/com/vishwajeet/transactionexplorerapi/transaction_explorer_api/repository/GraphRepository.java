package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory repository implementation backed by a static JSON file.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Load graph nodes and transactions from JSON at startup.</li>
 *   <li>Provide fast, thread-safe access to nodes and children.</li>
 * </ul>
 *
 * <p>Industry-Grade Enhancements:</p>
 * <ul>
 *   <li><b>ConcurrentHashMap:</b> Ensures thread safety for concurrent reads/writes.</li>
 *   <li><b>@PostConstruct:</b> Loads data once at application startup.</li>
 *   <li><b>Fail-fast:</b> Throws clear exceptions if JSON is missing or malformed (handled globally).</li>
 * </ul>
 */
@Repository
public class GraphRepository implements IGraphRepository {

    private final Map<String, GraphNode> nodeMap = new ConcurrentHashMap<>();
    private final Map<String, List<GraphNode>> childrenMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/transactions-graph-nodes.json");
        if (is == null) {
            throw new IOException("transactions-graph-nodes.json not found in resources!");
        }

        JsonNode rootNode = mapper.readTree(is);
        List<GraphNode> nodes = mapper.convertValue(rootNode.get("nodes"), new TypeReference<>() {});

        for (GraphNode node : nodes) {
            nodeMap.put(node.getId(), node);
            if (node.getParentId() != null) {
                childrenMap.computeIfAbsent(node.getParentId(),
                        k -> Collections.synchronizedList(new ArrayList<>())).add(node);
            }
        }
    }

    @Override
    public Optional<GraphNode> findById(String id) {
        return Optional.ofNullable(nodeMap.get(id));
    }

    @Override
    public List<GraphNode> findChildren(String parentId) {
        return childrenMap.getOrDefault(parentId, Collections.emptyList());
    }

    @Override
    public boolean exists(String id) {
        return nodeMap.containsKey(id);
    }

    @Override
    public List<GraphNode> findAll() {
        return new ArrayList<>(nodeMap.values());
    }
}
