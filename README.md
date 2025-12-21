# Graph Algorithms Library

A comprehensive Java library implementing classic graph algorithms with complete test coverage and performance benchmarks.

> [!NOTE]
> **Project Purpose**: This is a **recreational and educational library** focused on clean implementations of fundamental graph algorithms. Auxiliary tasks—such as documentation, comprehensive testing, and refactoring—are automated via **AI assistants**, allowing the developer to concentrate on algorithmic logic.

## 🏗 System Architecture

The library follows **object-oriented design** with a focus on **extensibility** and **type safety**.

*   **`graphs.Graph` (Base Abstract Class)**: Manages core structure (Vertices, Edges, Adjacency).
*   **Encapsulation**: Internal attributes (`vertices`, `edges`, `adjacencyMap`) are `protected`.
    *   *Internal Use*: Subclasses access directly for optimal performance.
    *   *External Use*: Clients use `getVertices()` and `getEdges()`, which return **immutable views** for safety.
*   **Subclasses**:
    *   `UndirectedGraph`: Undirected graphs with bipartite matching, Eulerian paths, biconnectivity.
    *   `Digraph`: Directed graphs with topological sort, strong connectivity, Eulerian circuits.

## 📂 Project Structure

```
graph-algorithms/
├── src/
│   ├── graphs/          # Core algorithms (Graph, UndirectedGraph, Digraph)
│   ├── utils/           # Utilities (GraphGenerator, Builders)
│   └── tests/           # Comprehensive test suite (RegressionTests, StressTests)
├── inputs/
│   ├── test/            # Graph data files for automated tests (.txt format)
│   └── user/            # Your custom graph files (place .txt files here)
├── scripts/             # Build and test scripts
├── out/                 # Compiled bytecode (gitignored)
└── README.md
```

## 🚀 Quick Start

### Installation
```bash
git clone <repository-url>
cd graph-algorithms
```

No external dependencies required—just Java 17+.

### Usage Example

**Option 1: Manual Creation**
```java
import graphs.*;

public class Main {
    public static void main(String[] args) {
        UndirectedGraph g = new UndirectedGraph();
        Graph.Vertex a = g.addVertex("A");
        Graph.Vertex b = g.addVertex("B");
        Graph.Vertex c = g.addVertex("C");
        
        g.addEdge(a, b);
        g.addEdge(b, c);
        
        System.out.println("Vertices: " + g.getVertices());
        System.out.println("Is Connected: " + g.isConnected());
    }
}
```

**Option 2: Load from File (Builders)**
```java
import graphs.*;
import utils.Builders;

public class Main {
    public static void main(String[] args) {
        // Load from file (edge-list format)
        UndirectedGraph g = Builders.undirectedGraphBuilder("inputs/user/my_graph.txt");
        Digraph d = Builders.digraphBuilder("inputs/user/my_digraph.txt");
        
        // Run algorithms
        System.out.println("Bipartite: " + g.isBipartite());
        System.out.println("Has Cycle: " + d.hasCycles());
        
        // Max matching (Hopcroft-Karp O(E√V))
        var matching = g.maxMatching();
        System.out.println("Max Matching Size: " + matching.size());
    }
}
```

> **📁 Where to put your graph files**: Place your `.txt` files in `inputs/user/` (created for you). This directory is separate from test data and gitignored for your custom graphs.

### Compile & Run
```bash
javac -d out -cp src src/Main.java
java -cp out Main
```

## 📊 Graph Data Format

Graph files in `inputs/test/` use a simple edge-list format compatible with [CS Academy Graph Editor](https://csacademy.com/app/graph_editor/):

**Undirected graph** (`u_*.txt`):
```
1 2
2 3
3 1
```

**Directed graph** (`d_*.txt`):
```
1 2
2 3
3 1
```

> **Tip**: Visualize any `.txt` file by copying its contents to the CS Academy Graph Editor!

## ✅ Running Tests

### Regression Tests (20 test methods, 41 algorithms covered)
```bash
./scripts/run_tests.sh
```

### Stress Tests (Large graphs, performance benchmarks)
```bash
./scripts/run_stress_tests.sh
```

**Note**: Stress tests run with `-Xss32m -Xmx2G` for deep recursion and large graphs.

## 📚 Implemented Algorithms

> [!NOTE]
> All algorithms currently implemented in this library are **deterministic** and run in **polynomial time** relative to the number of vertices ($V$) and edges ($E$).

### Graph (Base Class)
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| BFS | Θ(V + E) | Breadth-First Search for distances |
| Tree BFS | Θ(V + E) | BFS spanning tree |

### UndirectedGraph

#### Connectivity & Structure
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| Connected Components (DFS) | Θ(V + E) | Find all connected components |
| Bridges (Tarjan) | Θ(V + E) | Find critical edges |
| Articulation Points (Tarjan) | Θ(V + E) | Find critical vertices |
| Biconnectivity | Θ(V + E) | Check 2-edge/2-vertex connectivity |
| Biconnected Components | Θ(V + E) | Decompose into biconnected parts |

#### Paths & Cycles
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| Cycle Detection (DFS) | O(V + E) | Check for cycles |
| Eulerian Circuit/Trail (Hierholzer) | Θ(V + E) | Find Eulerian paths efficiently |
| Eulerian Circuit/Trail (Fleury) | O(E²) | Classical approach |

#### Bipartite & Matching
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| Bipartite Check (BFS) | O(V + E) | 2-coloring verification |
| Max Matching (Hopcroft-Karp) | O(E√V) | Maximum bipartite matching |
| Min Vertex Cover (König) | O(E√V) | Minimum vertex cover via matching |
| Max Independent Set (Gallai) | O(E√V) | Maximum independent set |

### Digraph (Directed Graphs)

#### Structure & Ordering
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| Cycle Detection (DFS) | O(V + E) | Detect directed cycles |
| Topological Sort (Kahn) | Θ(V + E) | Linear ordering of DAG |
| Tree Check | O(V + E) | Verify directed tree structure |

#### Strong Connectivity
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| SCC (Kosaraju-Sharir) | Θ(V + E) | Two-pass DFS approach |
| SCC (Tarjan) | Θ(V + E) | Single-pass DFS with lowlink |

#### Eulerian Paths
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| Eulerian Circuit/Trail (Hierholzer) | Θ(V + E) | Directed Eulerian paths |

#### Bipartite
| Algorithm | Complexity | Description |
|-----------|------------|-------------|
| Standard Bipartite (2-coloring) | O(V + E) | Underlying undirected bipartition |
| Source-Sink Bipartite | Θ(E) | Strict in/out degree separation |

## 🧪 Test Coverage

- **20 test methods** covering all 41 public algorithms
- **24 graph files** including edge cases (disconnected, complete, cycles, DAGs)
- **100% method coverage** with both success and failure cases
- **Stress tests** on graphs with 5,000+ vertices

## 🎓 Educational Features

- **Javadoc complexity annotations**: Every public method documents its algorithm name and Big-O complexity
- **Multiple implementations**: Compare Hierholzer vs Fleury, Kosaraju vs Tarjan
- **Theoretical validations**: König's Theorem, Gallai's Identity verified in tests

## 🛠 Development

### Adding New Algorithms
1. Implement in appropriate class (`UndirectedGraph` or `Digraph`)
2. Add Javadoc with complexity: `@complexity Θ(V + E) time, O(V) space`
3. Add test in `RegressionTests.java`
4. Run test suite: `./scripts/run_tests.sh`

### Code Style
- Clean implementations without verbose comments
- Complexity documentation via Javadoc only
- Immutable external interfaces, mutable internal for performance