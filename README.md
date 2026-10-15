# Graph Algorithms Library

A Java library implementing fundamental graph algorithms, data structures, and network flow models.

## Architectural Overview

The library employs an object-oriented design parameterized by vertex type `V` (`Graph<V>`).

- **`Graph<V>`**: Abstract base class defining adjacency representations, vertex and edge collections, and common traversal procedures.
- **`UndirectedGraph<V>`**: Extends `Graph<V>` for symmetric relations. Implements connectivity, biconnectivity, Eulerian cycles, and bipartite matching.
- **`Digraph<V>`**: Extends `Graph<V>` for directed relations. Implements topological sorting, strong connectivity, and directed Eulerian cycles.
- **`WeightedUndirectedGraph<V>`**: Extends `UndirectedGraph<V>`. Supports scalar edge weights (`double`), single-source and all-pairs shortest paths, and minimum spanning trees.
- **`WeightedDigraph<V>`**: Extends `Digraph<V>`. Supports directed scalar weights, shortest paths, and maximum network flow models.
- **`DisjointSetUnion<T>`**: Disjoint-set data structure with union-by-rank and path compression heuristics.

## Directory Structure

```
graph-algorithms/
├── pom.xml
├── src/
│   ├── main/java/
│   │   ├── graphs/          # Graph abstractions and algorithm implementations
│   │   └── utils/           # Builders, generators, and auxiliary data structures
│   └── test/java/
│       └── tests/           # Regression and stress test suites
├── inputs/
│   ├── test/                # Input graphs for automated regression verification
│   └── user/                # Directory for user-defined input instances
└── scripts/                 # Standalone execution scripts
```

## Compilation and Execution

### Requirements
- Java Development Kit (JDK) 17 or higher.
- Apache Maven (optional; standalone scripts are provided).

### Building with Maven
```bash
mvn clean compile
mvn test
```

### Building without Maven (Fallback Scripts)
```bash
./scripts/run_tests.sh
./scripts/run_stress_tests.sh
```

## Input Format

Graphs are parsed from edge-list representations:

**Unweighted Edges** (`inputs/test/u_*.txt`, `d_*.txt`):
```text
u v
v w
```

**Weighted Edges** (`inputs/test/w_*.txt`):
```text
u v weight
v w weight
```

## Implemented Algorithms and Complexities

All algorithms are deterministic and operate within polynomial time bounds relative to the number of vertices ($V$) and edges ($E$).

### Unweighted Graphs (`Graph<V>`, `UndirectedGraph<V>`, `Digraph<V>`)

| Category | Algorithm | Time Complexity | Space Complexity |
|---|---|---|---|
| Traversal | Breadth-First Search (BFS) | $\Theta(V + E)$ | $O(V)$ |
| Connectivity | Connected Components (DFS) | $\Theta(V + E)$ | $O(V)$ |
| Connectivity | Bridges and Articulation Points (Tarjan) | $\Theta(V + E)$ | $O(V)$ |
| Connectivity | Biconnected Components (Hopcroft-Tarjan) | $\Theta(V + E)$ | $O(V + E)$ |
| Cycles | Undirected Cycle Detection (DFS) | $O(V + E)$ | $O(V)$ |
| Cycles | Directed Cycle Detection (DFS 3-color) | $O(V + E)$ | $O(V)$ |
| Topological Order | Kahn's Algorithm | $\Theta(V + E)$ | $O(V)$ |
| Strong Connectivity | Kosaraju-Sharir | $\Theta(V + E)$ | $O(V + E)$ |
| Strong Connectivity | Tarjan | $\Theta(V + E)$ | $O(V)$ |
| Eulerian Paths | Hierholzer | $\Theta(V + E)$ | $O(V + E)$ |
| Eulerian Paths | Fleury | $O(E^2)$ | $O(V + E)$ |
| Bipartite Graphs | Two-Coloring (BFS) | $O(V + E)$ | $O(V)$ |
| Bipartite Graphs | Maximum Matching (Hopcroft-Karp) | $O(E \sqrt{V})$ | $O(V)$ |
| Bipartite Graphs | Minimum Vertex Cover (König's Theorem) | $O(E \sqrt{V})$ | $O(V)$ |
| Bipartite Graphs | Maximum Independent Set (Gallai's Identity) | $O(E \sqrt{V})$ | $O(V)$ |
| General Graphs | Maximum Matching (Edmonds' Blossom Algorithm) | $O(V^3)$ | $O(V + E)$ |
| General Graphs | Minimum Edge Cover (Gallai's Theorem) | $O(V^3)$ | $O(V + E)$ |

### Weighted Graphs (`WeightedUndirectedGraph<V>`, `WeightedDigraph<V>`)

| Category | Algorithm | Time Complexity | Space Complexity |
|---|---|---|---|
| Shortest Paths | Dijkstra (Binary Heap) | $O((V + E) \log V)$ | $O(V)$ |
| Shortest Paths | Bellman-Ford | $O(V \cdot E)$ | $O(V)$ |
| Shortest Paths | Floyd-Warshall (All-Pairs Shortest Path) | $O(V^3)$ | $O(V^2)$ |
| Minimum Spanning Tree | Kruskal (Disjoint Set Union) | $O(E \log E)$ | $O(V + E)$ |
| Minimum Spanning Tree | Prim (Binary Heap) | $O((V + E) \log V)$ | $O(V)$ |
| Network Flow | Edmonds-Karp (BFS Augmenting Paths) | $O(V \cdot E^2)$ | $O(V + E)$ |
| Network Flow | Ford-Fulkerson (DFS Augmenting Paths) | $O(E \cdot |f^*|)$ | $O(V + E)$ |
| Network Flow | Min-Cut Extraction (Max-Flow Min-Cut Theorem) | $O(V + E)$ | $O(V)$ |
| Network Flow | Bipartite Matching via Unit Flow | $O(V \cdot E)$ | $O(V + E)$ |

## Verification and Testing

- **Regression Suite**: 33 test procedures verifying structural properties, boundary conditions, and mathematical invariants ($|M| = |C|$, $\alpha'(G) + \beta'(G) = |V|$, $\text{max-flow} = \text{min-cut}$, $W_{\text{Kruskal}} = W_{\text{Prim}}$).
- **Stress Suite**: Scalability verification on graphs containing up to $5 \times 10^3$ vertices and $2 \times 10^4$ edges.