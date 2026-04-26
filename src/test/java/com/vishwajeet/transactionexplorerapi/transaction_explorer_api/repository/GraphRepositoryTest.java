package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class GraphRepositoryTest {

    private GraphRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        repository = new GraphRepository();

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

        // Access private maps via reflection
        Field nodeMapField = GraphRepository.class.getDeclaredField("nodeMap");
        nodeMapField.setAccessible(true);
        Map<String, GraphNode> nodeMap = (Map<String, GraphNode>) nodeMapField.get(repository);

        Field childrenMapField = GraphRepository.class.getDeclaredField("childrenMap");
        childrenMapField.setAccessible(true);
        Map<String, List<GraphNode>> childrenMap = (Map<String, List<GraphNode>>) childrenMapField.get(repository);

        // Populate maps
        nodeMap.put(parent.getId(), parent);
        nodeMap.put(child.getId(), child);
        childrenMap.put(parent.getId(), List.of(child));
    }

    @Test
    void findById_returnsNodeWhenExists() {
        assertThat(repository.findById("P1")).isPresent();
        assertThat(repository.findById("P1").get().getName()).isEqualTo("Parent Node");
    }

    @Test
    void findById_returnsEmptyWhenNotExists() {
        assertThat(repository.findById("Unknown")).isEmpty();
    }

    @Test
    void findChildren_returnsChildrenList() {
        List<GraphNode> children = repository.findChildren("P1");
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getId()).isEqualTo("C1");
    }

    @Test
    void exists_returnsTrueWhenNodePresent() {
        assertThat(repository.exists("C1")).isTrue();
    }

    @Test
    void exists_returnsFalseWhenNodeAbsent() {
        assertThat(repository.exists("Missing")).isFalse();
    }

    @Test
    void findAll_returnsAllNodes() {
        List<GraphNode> allNodes = repository.findAll();
        assertThat(allNodes).extracting(GraphNode::getId).contains("P1", "C1");
    }
}
