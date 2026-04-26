package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class IGraphRepositoryTest {

    // Simple dummy implementation for testing the interface contract
    static class DummyGraphRepository implements IGraphRepository {
        private final Map<String, GraphNode> nodeMap = new HashMap<>();
        private final Map<String, List<GraphNode>> childrenMap = new HashMap<>();

        void addNode(GraphNode node) {
            nodeMap.put(node.getId(), node);
            if (node.getParentId() != null) {
                childrenMap.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node);
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

    @Test
    void repository_contract_isRespected() {
        DummyGraphRepository repo = new DummyGraphRepository();

        GraphNode parent = GraphNode.builder()
                .id("P1")
                .name("Parent Node")
                .accountNumber("ACC-P1")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        GraphNode child = GraphNode.builder()
                .id("C1")
                .parentId("P1")
                .name("Child Node")
                .accountNumber("ACC-C1")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        repo.addNode(parent);
        repo.addNode(child);

        // findById
        assertThat(repo.findById("P1")).isPresent();
        assertThat(repo.findById("Unknown")).isEmpty();

        // findChildren
        assertThat(repo.findChildren("P1")).hasSize(1).extracting(GraphNode::getId).contains("C1");

        // exists
        assertThat(repo.exists("C1")).isTrue();
        assertThat(repo.exists("Missing")).isFalse();

        // findAll
        assertThat(repo.findAll()).hasSize(2).extracting(GraphNode::getId).contains("P1", "C1");
    }
}
