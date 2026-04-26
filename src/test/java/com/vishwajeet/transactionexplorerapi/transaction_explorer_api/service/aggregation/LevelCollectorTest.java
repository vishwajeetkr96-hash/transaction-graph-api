package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception.CycleDetectedException;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository.IGraphRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LevelCollectorTest {

    private LevelCollector collector;
    private DummyRepo repo;

    static class DummyRepo implements IGraphRepository {
        private final Map<String, GraphNode> nodes = new HashMap<>();
        private final Map<String, List<GraphNode>> children = new HashMap<>();

        void addNode(GraphNode node) {
            nodes.put(node.getId(), node);
            if (node.getParentId() != null) {
                children.computeIfAbsent(node.getParentId(), k -> new ArrayList<>()).add(node);
            }
        }

        @Override
        public Optional<GraphNode> findById(String id) {
            return Optional.ofNullable(nodes.get(id));
        }

        @Override
        public List<GraphNode> findChildren(String parentId) {
            return children.getOrDefault(parentId, Collections.emptyList());
        }

        @Override
        public boolean exists(String id) {
            return nodes.containsKey(id);
        }

        @Override
        public List<GraphNode> findAll() {
            return new ArrayList<>(nodes.values());
        }
    }

    @BeforeEach
    void setUp() {
        repo = new DummyRepo();
        collector = new LevelCollector(repo);
    }

    @Test
    void collectByLevel_traversesBreadthFirst() {
        GraphNode root = GraphNode.builder()
                .id("R")
                .name("Root")
                .accountNumber("ACC-R")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        GraphNode child1 = GraphNode.builder()
                .id("C1")
                .parentId("R")
                .name("Child 1")
                .accountNumber("ACC-C1")
                .build();

        GraphNode child2 = GraphNode.builder()
                .id("C2")
                .parentId("R")
                .name("Child 2")
                .accountNumber("ACC-C2")
                .build();

        GraphNode grandChild = GraphNode.builder()
                .id("GC1")
                .parentId("C1")
                .name("GrandChild")
                .accountNumber("ACC-GC1")
                .build();

        repo.addNode(root);
        repo.addNode(child1);
        repo.addNode(child2);
        repo.addNode(grandChild);

        Map<Integer, List<GraphNode>> result = collector.collectByLevel(root, 2);

        assertThat(result).containsKeys(0, 1, 2);
        assertThat(result.get(0)).extracting(GraphNode::getId).containsExactly("R");
        assertThat(result.get(1)).extracting(GraphNode::getId).containsExactlyInAnyOrder("C1", "C2");
        assertThat(result.get(2)).extracting(GraphNode::getId).containsExactly("GC1");
    }

    @Test
    void collectByLevel_respectsMaxDepth() {
        GraphNode root = GraphNode.builder().id("R").name("Root").accountNumber("ACC-R").build();
        GraphNode child = GraphNode.builder().id("C1").parentId("R").name("Child").accountNumber("ACC-C1").build();
        repo.addNode(root);
        repo.addNode(child);

        Map<Integer, List<GraphNode>> result = collector.collectByLevel(root, 0);

        // Only root should be collected
        assertThat(result).containsOnlyKeys(0);
        assertThat(result.get(0)).extracting(GraphNode::getId).containsExactly("R");
    }

    @Test
    void collectByLevel_detectsCycle() {
        GraphNode root = GraphNode.builder().id("R").name("Root").accountNumber("ACC-R").build();
        GraphNode child = GraphNode.builder().id("C1").parentId("R").name("Child").accountNumber("ACC-C1").build();

        // Create cycle: child points back to root
        GraphNode cycleNode = GraphNode.builder().id("R").parentId("C1").name("RootAgain").accountNumber("ACC-R").build();

        repo.addNode(root);
        repo.addNode(child);
        repo.addNode(cycleNode);

        assertThrows(CycleDetectedException.class, () -> collector.collectByLevel(root, 2));
    }
}
