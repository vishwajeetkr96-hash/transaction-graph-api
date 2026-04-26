# System Design: Transaction Explorer API

---

## 1. Core Logic & Architecture
The Transaction Explorer API is built to process hierarchical financial data. Because transactions often represent a **directed relationship** (**Parent → Child**), the data is modeled as a **Directed Graph**.

### Node Classification Logic
* **Root Nodes:** Defined as any node where `parentId` is `null`.
* **Leaf Nodes:** Defined as any node that does not appear as a `parentId` for any other record in the dataset.
* **Orphan Nodes:** These are nodes that reference a `parentId` which does not exist in the database. To maintain data integrity, the system treats these as **Level 0 "Pseudo-Roots"**, ensuring they are still discoverable in the exploration logic.

---

## 2. Algorithm: Recursive Discovery
For the `/nodes/{id}/export` endpoint, we implemented a **Recursive Depth-First Search (DFS)**.

### Why DFS?
DFS is ideal for this use case because it allows us to build the full "branch" of a transaction (from parent down to the deepest leaf) before moving to the next sibling. This results in a cleaner, nested JSON structure that is easier for frontend applications to render as a tree.

### Complexity Analysis
* **Time Complexity:** $O(N)$, where $N$ is the number of nodes visited up to the `maxDepth`.
* **Space Complexity:** $O(D)$, where $D$ is the depth of the tree (stack space for recursion).

---

## 3. Cycle Detection & Data Safety
In financial data, cycles (e.g., Node A → Node B → Node A) can cause infinite recursion, leading to a `StackOverflowError` and system crashes.

### The Solution
We implemented a **Visited Set** mechanism within the DFS traversal:
1. As the algorithm visits a node, its ID is added to a `HashSet`.
2. Before moving to a child node, the system checks if the child ID is already in the `HashSet`.
3. **If detected:** The system immediately terminates the recursion and throws a `400 Cycle Detected` error.

---

## 4. Transaction Aggregation
Beyond traversal, the API provides **aggregation per level** to deliver business insights.

### Aggregation Logic
* Implemented via the `LevelCollector` and `UnifiedAggregationService`.
* **LevelCollector:** Performs a breadth-first traversal (BFS) to group nodes by level.
* **UnifiedAggregationService:** Computes aggregates for each level:
    - **Node Count:** Number of nodes at that level.
    - **Transaction Count:** Total transactions across nodes.
    - **Debit Sum:** Sum of all debit transactions.
    - **Credit Sum:** Sum of all credit transactions.
    - **Total Amount:** Debit + Credit.
    - **Net Balance:** Credit − Debit.

### Benefit
This unified aggregation allows analysts to quickly understand the financial impact at each depth of the hierarchy, without manually traversing and summing transactions.

---

## 5. Error Handling Strategy

| Scenario         | HTTP Status       | Business Logic |
|------------------|------------------|----------------|
| **Missing ID**   | `404 Not Found`  | Returned when a requested Node ID does not exist. |
| **Invalid Depth**| `400 Bad Request`| Returned if `maxDepth` is negative or non-numeric. |
| **Circular Ref** | `400 Bad Request`| Returned via the DFS Cycle Detection logic. |
| **Type Mismatch**| `400 Bad Request`| Handled via Global Exception Handler for invalid query params. |

---

## 6. Design Principles
* **Single Responsibility:** Each service (DFS traversal, BFS aggregation, repository access) has a clear, isolated responsibility.
* **Open/Closed Principle:** New repository implementations (JSON, DB, API) can be added without changing service logic.
* **Dependency Inversion:** Services depend on abstractions (`IGraphRepository`, `IGraphService`), not concrete classes.
* **Robust Error Handling:** Ensures predictable responses for invalid inputs and cycles.

---

## 7. Summary
The Transaction Explorer API combines **DFS for hierarchical discovery** with **BFS-based aggregation** to deliver a complete view of transaction graphs.  
This dual approach ensures:
- Accurate classification of nodes (Root, Leaf, Orphan).
- Safe traversal with cycle detection.
- Immediate financial insights through per-level aggregation.

