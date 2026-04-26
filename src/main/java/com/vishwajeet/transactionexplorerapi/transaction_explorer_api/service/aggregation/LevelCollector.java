package com.vishwajeet.transactionexplorerapi.transaction_explorer_api.service.aggregation;

import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.exception.CycleDetectedException;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.model.GraphNode;
import com.vishwajeet.transactionexplorerapi.transaction_explorer_api.repository.IGraphRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LevelCollector {

    private final IGraphRepository repository;

    public LevelCollector(IGraphRepository repository) {
        this.repository = repository;
    }

    /**
     * Collects nodes by BFS level starting from root up to maxDepth (inclusive).
     * Returns an ordered map (level -> list of nodes).
     */
    public Map<Integer, List<GraphNode>> collectByLevel(GraphNode root, int maxDepth) {
        Map<Integer, List<GraphNode>> nodesByLevel = new LinkedHashMap<>();
        Queue<Map.Entry<GraphNode, Integer>> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        queue.add(Map.entry(root, 0));
        visited.add(root.getId());

        while (!queue.isEmpty()) {
            Map.Entry<GraphNode, Integer> entry = queue.poll();
            GraphNode current = entry.getKey();
            int level = entry.getValue();

            nodesByLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(current);

            if (level < maxDepth) {
                for (GraphNode child : repository.findChildren(current.getId())) {
                    if (!visited.add(child.getId())) {
                        throw new CycleDetectedException(child.getId());
                    }
                    queue.add(Map.entry(child, level + 1));
                }
            }
        }

        return nodesByLevel;
    }
}
