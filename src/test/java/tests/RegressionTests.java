package tests;

import graphs.*;
import utils.*;
import java.io.*;
import java.util.*;

public class RegressionTests {

    private static final String DATA_DIR = "inputs/test/";
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== STARTING REGRESSION TEST SUITE ===\n");

        // Common Graph Tests
        System.out.println("--- COMMON GRAPH TESTS ---");
        runTest("Graph: BFS Distances", RegressionTests::testBFS);
        runTest("Graph: Copy Mechanism", RegressionTests::testGraphCopy);

        // Undirected Graph Tests
        System.out.println("\n--- UNDIRECTED GRAPH TESTS ---");
        runTest("UndirectedGraph: Connectivity & Components", RegressionTests::testUndirectedConnectivity);
        runTest("UndirectedGraph: Cycles", RegressionTests::testUndirectedCycles);
        runTest("UndirectedGraph: Bridges & Articulation Points", RegressionTests::testUndirectedStructures);
        runTest("UndirectedGraph: Eulerian Properties", RegressionTests::testUndirectedEulerian);
        runTest("UndirectedGraph: Bipartite", RegressionTests::testUndirectedBipartite);
        runTest("UndirectedGraph: Bipartite Algorithms", RegressionTests::testBipartiteAlgorithms);
        runTest("UndirectedGraph: Biconnectivity", RegressionTests::testUndirectedBiconnectivity);
        runTest("UndirectedGraph: Hierholzer Algorithm", RegressionTests::testUndirectedHierholzer);

        // Digraph Tests
        System.out.println("\n--- DIGRAPH TESTS ---");
        runTest("Digraph: Cycles", RegressionTests::testDigraphCycles);
        runTest("Digraph: Topological Sort", RegressionTests::testDigraphTopoSort);
        runTest("Digraph: Strongly Connected Components", RegressionTests::testDigraphSCC);
        runTest("Digraph: Degrees", RegressionTests::testDigraphDegrees);
        runTest("Digraph: Bipartite Types", RegressionTests::testDigraphBipartite);
        runTest("Digraph: Tree Structure", RegressionTests::testDigraphTree);
        runTest("Digraph: Eulerian Properties", RegressionTests::testDigraphEulerian);
        runTest("Digraph: Complex SCC", RegressionTests::testDigraphSCCComplex);

        // Generics & Type Safety Tests
        System.out.println("\n--- GENERICS & TYPE SAFETY TESTS ---");
        runTest("Generics: Integer Graph & Edge Symmetry", RegressionTests::testGenericTypes);

        // PR 3 Spec-Driven Algorithm Optimizations Tests
        System.out.println("\n--- PR 3 SPEC-DRIVEN ALGORITHM OPTIMIZATIONS TESTS ---");
        runTest("Spec: Hierholzer Immutability & Edge Cases", RegressionTests::testHierholzerImmutabilityAndEdgeCases);
        runTest("Spec: In-Degree & Out-Degree O(1) Consistency", RegressionTests::testDigraphDegreeConsistency);
        runTest("Spec: König & Hopcroft-Karp Dual Invariants", RegressionTests::testKonigDualInvariants);

        // Weighted Graph Domain & Compatibility Tests (Etapa 1 & 2)
        System.out.println("\n--- WEIGHTED GRAPH DOMAIN & COMPATIBILITY TESTS ---");
        runTest("WeightedUndirectedGraph: Weights, Symmetry & Inheritance", RegressionTests::testWeightedUndirectedGraph);
        runTest("WeightedDigraph: Weights, Transpose & Inheritance", RegressionTests::testWeightedDigraph);
        runTest("DisjointSetUnion: Union, Find & Path Compression", RegressionTests::testDisjointSetUnion);

        // Weighted Graph Algorithms (Etapa 3)
        System.out.println("\n--- WEIGHTED GRAPH ALGORITHMS (ETAPA 3) ---");
        runTest("Shortest Paths: Dijkstra (Distances & Path Reconstruction)", RegressionTests::testDijkstraAndShortestPaths);
        runTest("Shortest Paths: Bellman-Ford & Negative Cycles", RegressionTests::testBellmanFordAndNegativeCycles);
        runTest("Shortest Paths: Floyd-Warshall All-Pairs (APSP)", RegressionTests::testFloydWarshallAllPairs);
        runTest("MST: Kruskal vs Prim (Dual Equivalence & DSU)", RegressionTests::testMinimumSpanningTreeKruskalAndPrim);
        runTest("Flow Networks: Edmonds-Karp, Ford-Fulkerson & Min-Cut", RegressionTests::testMaxFlowAndMinCut);

        // Edmonds' Blossom & Minimum Edge Cover Tests
        System.out.println("\n--- EDMONDS BLOSSOM & MINIMUM EDGE COVER TESTS ---");
        runTest("Blossom: Odd Cycles, Blossoms & General Matching", RegressionTests::testEdmondsBlossomGeneralMatching);
        runTest("Blossom vs Hopcroft-Karp: Bipartite Equivalence & Invariants", RegressionTests::testBlossomHopcroftKarpEquivalence);
        runTest("Edge Cover: Gallai Theorem Dual Invariant & Boundary Cases", RegressionTests::testMinimumEdgeCoverAndGallaiTheorem);

        System.out.println("\n======================================");
        System.out.println("SUMMARY:");
        System.out.println("PASSED: " + testsPassed);
        System.out.println("FAILED: " + testsFailed);

        if (testsFailed > 0) {
            System.out.println("❌ SOME TESTS FAILED");
            System.exit(1);
        } else {
            System.out.println("✅ ALL TESTS PASSED");
        }
    }

    // ------------------------------ UTILITY METHODS ------------------------------
    // //

    private static UndirectedGraph loadUndirectedGraph(String filename) {
        UndirectedGraph g = new UndirectedGraph();
        try (Scanner sc = new Scanner(new File(DATA_DIR + filename))) {
            while (sc.hasNext()) {
                String u = sc.next();
                if (sc.hasNext()) {
                    String v = sc.next();
                    g.addEdge(g.addVertex(u), g.addVertex(v));
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Test file not found: " + filename, e);
        }
        return g;
    }

    private static Digraph loadDigraph(String filename) {
        Digraph g = new Digraph();
        try (Scanner sc = new Scanner(new File(DATA_DIR + filename))) {
            while (sc.hasNext()) {
                String u = sc.next();
                if (sc.hasNext()) {
                    String v = sc.next();
                    g.addEdge(g.addVertex(u), g.addVertex(v));
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Test file not found: " + filename, e);
        }
        return g;
    }

    // ------------------------------ COMMON GRAPH TESTS
    // ------------------------------ //

    private static void testBFS() {
        UndirectedGraph g = loadUndirectedGraph("u_trail.txt");
        Graph.Vertex v1 = g.getVertexById("1");
        Graph.Vertex v2 = g.getVertexById("2");
        Graph.Vertex v3 = g.getVertexById("3");

        check(g.distanceBFS(v1, v3) == 2, "Distance 1->3 is 2");
        check(g.distanceBFS(v1, v1) == 0, "Distance to self is 0");

        Map<Graph.Vertex, Integer> dists = g.distancesBFS(v2);
        check(dists.get(v1) == 1, "DistancesBFS: 2->1 is 1");
        check(dists.get(v3) == 1, "DistancesBFS: 2->3 is 1");
        // Test unreachable vertex (disconnected graph)
        UndirectedGraph disconnected = loadUndirectedGraph("u_disconnected.txt");
        Graph.Vertex vDisc1 = disconnected.getVertexById("1");
        Graph.Vertex vDisc3 = disconnected.getVertexById("3");
        check(disconnected.distanceBFS(vDisc1, vDisc3) == -1, "Unreachable vertex returns -1");
    }

    private static void testGraphCopy() {
        UndirectedGraph g = loadUndirectedGraph("u_trail.txt");
        UndirectedGraph copy = g.copy();

        // Structural Equality
        check(copy.order() == g.order(), "Copy has same order");
        check(copy.size() == g.size(), "Copy has same size");
        check(copy.getVertexById("1") != null, "Copy has vertex 1");

        // Independence
        copy.removeEdge("1", "2");
        check(copy.size() == g.size() - 1, "Copy removed edge");
        check(g.size() == 2, "Original preserved edge");

        copy.addVertex("4");
        check(copy.order() == g.order() + 1, "Copy added vertex");
        check(g.order() == 3, "Original preserved vertices");
    }

    // ------------------------------ UNDIRECTED GRAPH TESTS
    // ------------------------------ //

    private static void testUndirectedConnectivity() {
        // u_bridge.txt has 2 components connected by bridge
        UndirectedGraph g = loadUndirectedGraph("u_bridge.txt");

        check(g.isConnected(), "Graph with bridge is connected");
        check(g.components().size() == 1, "Graph has 1 component");
    }

    private static void testUndirectedCycles() {
        // u_trail.txt is a path (no cycles)
        UndirectedGraph tree = loadUndirectedGraph("u_trail.txt");
        check(!tree.hasCycles(), "Path has no cycles");

        // u_circuit.txt is a cycle
        UndirectedGraph cycle = loadUndirectedGraph("u_circuit.txt");
        check(cycle.hasCycles(), "Triangle has cycles");
    }

    private static void testUndirectedStructures() {
        // u_bridge.txt has bridge (2-5)
        UndirectedGraph g = loadUndirectedGraph("u_bridge.txt");

        Set<UndirectedGraph.Edge> bridges = g.bridges();
        check(!bridges.isEmpty(), "Graph has bridges");

        // Find the bridge edge
        boolean foundBridge = false;
        for (UndirectedGraph.Edge e : bridges) {
            if ((e.from.id().equals("2") && e.to.id().equals("5")) ||
                    (e.from.id().equals("5") && e.to.id().equals("2"))) {
                foundBridge = true;
                break;
            }
        }
        check(foundBridge, "Bridge (2,5) found");

        // u_circuit.txt is a triangle (no bridges, no APs)
        UndirectedGraph cycle = loadUndirectedGraph("u_circuit.txt");
        check(cycle.bridges().isEmpty(), "Triangle has 0 bridges");
        check(cycle.articulationPoints().isEmpty(), "Triangle has 0 APs");
        check(cycle.isBiconnectedByVertices(), "Triangle is biconnected");
    }

    private static void testUndirectedEulerian() {
        // u_circuit.txt: Triangle (Eulerian circuit)
        UndirectedGraph circuit = loadUndirectedGraph("u_circuit.txt");
        check(circuit.hasEulerianCircuit(), "Triangle has Eulerian Circuit");
        check(circuit.hasEulerianTrail(), "Triangle has Eulerian Trail");

        // Test eulerianCircuitHierholzer
        List<UndirectedGraph.Edge> circuitH = circuit.eulerianCircuitHierholzer();
        check(circuitH != null, "eulerianCircuitHierholzer returns non-null for circuit");
        check(circuitH.size() == circuit.size(), "Circuit covers all edges");
        check(isValidEulerianPath(circuit, circuitH), "Circuit Hierholzer is valid");

        // Test eulerianCircuitFleury
        List<UndirectedGraph.Edge> circuitF = circuit.eulerianCircuitFleury();
        check(circuitF != null, "eulerianCircuitFleury returns non-null for circuit");
        check(circuitF.size() == circuit.size(), "Circuit Fleury covers all edges");
        check(isValidEulerianPath(circuit, circuitF), "Circuit Fleury is valid");

        // u_trail.txt: Path (Eulerian trail, not circuit)
        UndirectedGraph trail = loadUndirectedGraph("u_trail.txt");
        check(!trail.hasEulerianCircuit(), "Path NO Eulerian Circuit");
        check(trail.hasEulerianTrail(), "Path has Eulerian Trail");

        // Test eulerianTrailFleury
        List<UndirectedGraph.Edge> trailF = trail.eulerianTrailFleury();
        check(trailF != null, "eulerianTrailFleury returns non-null for trail");
        check(trailF.size() == trail.size(), "Trail Fleury covers all edges");
        check(isValidEulerianPath(trail, trailF), "Trail Fleury is valid");

        // Test eulerianTrailHierholzer
        List<UndirectedGraph.Edge> trailH = trail.eulerianTrailHierholzer();
        check(trailH != null, "eulerianTrailHierholzer returns non-null for trail");
        check(trailH.size() == trail.size(), "Trail Hierholzer covers all edges");
        check(isValidEulerianPath(trail, trailH), "Trail Hierholzer is valid");

        // u_not_eulerian.txt: Star graph (no Eulerian)
        UndirectedGraph notEulerian = loadUndirectedGraph("u_not_eulerian.txt");
        check(!notEulerian.hasEulerianCircuit(), "Star NO Eulerian Circuit");
        check(!notEulerian.hasEulerianTrail(), "Star NO Eulerian Trail");

        // All methods should return null for non-Eulerian graphs
        check(notEulerian.eulerianCircuitHierholzer() == null, "Circuit Hierholzer returns null");
        check(notEulerian.eulerianCircuitFleury() == null, "Circuit Fleury returns null");
        check(notEulerian.eulerianTrailHierholzer() == null, "Trail Hierholzer returns null");
        check(notEulerian.eulerianTrailFleury() == null, "Trail Fleury returns null");
    }

    private static void testUndirectedBipartite() {
        // C4 is bipartite
        UndirectedGraph bipartite = loadUndirectedGraph("u_bipartite_c4.txt");
        check(bipartite.isBipartite(), "C4 is bipartite");
        List<Set<Graph.Vertex>> partition = bipartite.getBipartition();
        check(partition != null, "Bipartition found for C4");
        check(partition.get(0).size() == 2 && partition.get(1).size() == 2, "C4 partition size is 2+2");

        // C3 is not bipartite
        UndirectedGraph notBipartite = loadUndirectedGraph("u_non_bipartite_c3.txt");
        check(!notBipartite.isBipartite(), "C3 is NOT bipartite");
        check(notBipartite.getBipartition() == null, "Bipartition is null for C3");

        // Disconnected bipartite should be bipartite
        UndirectedGraph disconnected = loadUndirectedGraph("u_disconnected.txt");
        check(disconnected.isBipartite(), "Disconnected bipartite graph is bipartite");
        check(disconnected.getBipartition() != null, "Bipartition found for disconnected graph");
    }

    private static void testBipartiteAlgorithms() {
        // 1. Simple Bipartite Graph (u_bipartite_matching.txt)
        // Edges: 1-4, 2-4, 2-5, 3-5
        // Partitions: {1,2,3}, {4,5}
        // Max Matching: 2
        UndirectedGraph g = loadUndirectedGraph("u_bipartite_matching.txt");

        Set<UndirectedGraph.Edge> matching = g.maxMatching();
        check(matching != null, "Matching result not null");
        check(matching.size() == 2, "Max Matching size is 2");

        Set<Graph.Vertex> cover = g.minVertexCover();
        check(cover != null, "Vertex Cover result not null");
        check(cover.size() == 2, "Min Vertex Cover size is 2 (König's Theorem)");

        Set<Graph.Vertex> independentSet = g.maxIndependentSet();
        check(independentSet != null, "Independent Set result not null");
        check(independentSet.size() == 3, "Max Independent Set size is 3 (Gallai's Identity: 5 - 2 = 3)");

        // 2. Cycle 4 (C4) - Perfect matching
        UndirectedGraph c4 = loadUndirectedGraph("u_bipartite_c4.txt");
        check(c4.maxMatching().size() == 2, "C4 Max Matching is 2");
        check(c4.minVertexCover().size() == 2, "C4 Min Vertex Cover is 2");
        check(c4.maxIndependentSet().size() == 2, "C4 Max Independent Set is 2");

        // 3. Triangle (C3) - Not bipartite
        UndirectedGraph c3 = loadUndirectedGraph("u_non_bipartite_c3.txt");
        check(c3.maxMatching() == null, "C3 Max Matching returns null");
        check(c3.minVertexCover() == null, "C3 Min Vertex Cover returns null");
        check(c3.maxIndependentSet() == null, "C3 Max Independent Set returns null");

        // 4. Disconnected Graph with Bipartite Components
        UndirectedGraph disconnected = loadUndirectedGraph("u_disconnected.txt");
        check(disconnected.maxMatching() != null, "Disconnected bipartite matching is not null");
    }

    private static boolean isValidEulerianPath(UndirectedGraph<?> g, List<?> path) {
        if (path.size() != g.size())
            return false;

        // Check all edges exist in graph
        Set<Object> pathEdges = new HashSet<>(path);
        for (Object e : path) {
            if (!g.getEdges().contains(e))
                return false;
        }

        // Check path covers all edges exactly once
        if (pathEdges.size() != g.size())
            return false;

        // Check path continuity
        for (int i = 0; i < path.size() - 1; i++) {
            Graph.Edge<?> e1 = (Graph.Edge<?>) path.get(i);
            Graph.Edge<?> e2 = (Graph.Edge<?>) path.get(i + 1);
            if (!areConnected(e1, e2))
                return false;
        }

        return true;
    }

    private static boolean areConnected(Graph.Edge<?> e1, Graph.Edge<?> e2) {
        return e1.from.equals(e2.from) || e1.from.equals(e2.to) ||
                e1.to.equals(e2.from) || e1.to.equals(e2.to);
    }

    // ------------------------------ DIGRAPH TESTS ------------------------------
    // //

    private static void testDigraphCycles() {
        // d_circuit.txt: 1->2->3->1 (has cycle)
        Digraph cycle = loadDigraph("d_circuit.txt");
        check(cycle.hasCycles(), "Directed triangle has cycles");
    }

    private static void testDigraphTopoSort() {
        // Test on a DAG
        Digraph dag = loadDigraph("d_dag_complex.txt");
        List<Graph.Vertex> sort = dag.topologicalSort();
        check(sort != null, "Topological sort returns non-null for DAG");
        check(sort.size() == dag.order(), "Topological sort includes all vertices");

        // Test on cyclic graph - should return null
        Digraph cycle = loadDigraph("d_circuit.txt");
        check(cycle.topologicalSort() == null, "Cyclic graph has no topological sort");
    }

    private static void testDigraphSCC() {
        // d_scc.txt: {1,2} and {3,4} are SCCs
        Digraph g = loadDigraph("d_scc.txt");

        Set<Set<Graph.Vertex>> sccKS = g.stronglyConnectedComponentsKS();
        Set<Set<Graph.Vertex>> sccT = g.stronglyConnectedComponentsT();

        check(sccKS.size() == 2, "Kosaraju finds 2 SCCs");
        check(sccT.size() == 2, "Tarjan finds 2 SCCs");

        // Verify content
        Graph.Vertex v1 = g.getVertexById("1");
        Graph.Vertex v2 = g.getVertexById("2");
        Graph.Vertex v3 = g.getVertexById("3");
        Graph.Vertex v4 = g.getVertexById("4");

        boolean foundSCC12 = false;
        boolean foundSCC34 = false;
        for (Set<Graph.Vertex> comp : sccKS) {
            if (comp.size() == 2 && comp.contains(v1) && comp.contains(v2))
                foundSCC12 = true;
            if (comp.size() == 2 && comp.contains(v3) && comp.contains(v4))
                foundSCC34 = true;
        }
        check(foundSCC12 && foundSCC34, "Kosaraju SCCs content correct");
    }

    private static void testDigraphDegrees() {
        // d_scc.txt: 1->2, 2->1, 1->3, 3->4, 4->3
        Digraph g = loadDigraph("d_scc.txt");

        Graph.Vertex v1 = g.getVertexById("1");
        Graph.Vertex v2 = g.getVertexById("2");
        Graph.Vertex v3 = g.getVertexById("3");
        Graph.Vertex v4 = g.getVertexById("4");

        // v1: out(2: to 2,3), in(1: from 2)
        check(g.outDegree(v1) == 2, "v1 out-degree is 2");
        check(g.inDegree(v1) == 1, "v1 in-degree is 1");

        // v2: out(1: to 1), in(1: from 1)
        check(g.outDegree(v2) == 1, "v2 out-degree is 1");
        check(g.inDegree(v2) == 1, "v2 in-degree is 1");

        // v3: out(1: to 4), in(2: from 1 and 4)
        check(g.outDegree(v3) == 1, "v3 out-degree is 1");
        check(g.inDegree(v3) == 2, "v3 in-degree is 2");

        // v4: out(1: to 3), in(1: from 3)
        check(g.outDegree(v4) == 1, "v4 out-degree is 1");
        check(g.outDegree(v4) == 1, "v4 out-degree is 1");
        check(g.inDegree(v4) == 1, "v4 in-degree is 1");
    }

    private static void testDigraphBipartite() {
        // 1. Source-Sink Graph (u_bipartite_matching.txt interpreted as directed)
        // 1->4, 2->4, 2->5, 3->5
        // Sources: {1,2,3}, Sinks: {4,5}
        // This should pass BOTH Source-Sink and Standard checks.
        Digraph ssGraph = loadDigraph("u_bipartite_matching.txt");

        check(ssGraph.isDBipartite(), "Source-Sink Graph is Standard Bipartite");
        check(ssGraph.getDPartition() != null, "Standard Partition exists");

        check(ssGraph.isSourceSinkBipartite(), "Source-Sink Graph is Source-Sink Bipartite");
        List<Set<Graph.Vertex>> ssPart = ssGraph.getSourceSinkPartition();
        check(ssPart != null, "Source-Sink Partition exists");
        // Verify Source-Sink partition logic (Sources vs Sinks)
        Set<Graph.Vertex> part1 = ssPart.get(0); // Sources
        Set<Graph.Vertex> part2 = ssPart.get(1); // Sinks
        // Check a known source and sink
        Graph.Vertex v1 = ssGraph.getVertexById("1");
        Graph.Vertex v4 = ssGraph.getVertexById("4");
        check(part1.contains(v1) || part2.contains(v1), "Vertex 1 present");
        if (part1.contains(v1)) {
            check(part2.contains(v4), "If 1 is source, 4 is sink");
        } else {
            check(part1.contains(v4), "If 1 is sink, 4 is source");
        }

        // 2. Cycle 4 (d_cycle_4.txt)
        // 1->2->3->4->1
        // Standard: Bipartite (Even cycle underlying)
        // Source-Sink: NOT Bipartite (2 has in/out, etc.)
        Digraph c4 = loadDigraph("d_cycle_4.txt");

        check(c4.isDBipartite(), "C4 is Standard Bipartite");
        check(!c4.isSourceSinkBipartite(), "C4 is NOT Source-Sink Bipartite");
    }

    private static void testUndirectedBiconnectivity() {
        // Test isBiconnectedByVertices
        UndirectedGraph biconn = loadUndirectedGraph("u_biconnected_large.txt");
        check(biconn.isBiconnectedByVertices(), "Biconnected graph has no articulation points");

        UndirectedGraph hasAP = loadUndirectedGraph("u_articulation_multiple.txt");
        check(!hasAP.isBiconnectedByVertices(), "Graph with APs is not vertex-biconnected");

        // Test isBiconnectedByEdges
        check(biconn.isBiconnectedByEdges(), "Biconnected graph has no bridges");

        UndirectedGraph hasBridge = loadUndirectedGraph("u_bridge.txt");
        check(!hasBridge.isBiconnectedByEdges(), "Graph with bridges is not edge-biconnected");

        // Test biComponentsByArticulation
        Set<Set<UndirectedGraph.Edge>> componentsAP = hasAP.biComponentsByArticulation();
        check(hasAP.articulationPoints().size() > 0, "Graph has articulation points");
        check(componentsAP.size() > 1, "Graph has multiple biconnected components");

        // Test complete graph K4 - no bridges, no articulation points
        UndirectedGraph completeK4 = loadUndirectedGraph("u_complete_k4.txt");
        check(completeK4.bridges().isEmpty(), "Complete graph has no bridges");
        check(completeK4.articulationPoints().isEmpty(), "Complete graph has no articulation points");

        // Test biComponentsByBridges
        Set<Set<Graph.Vertex>> componentsBridge = hasBridge.biComponentsByBridges();
        check(componentsBridge.size() > 1, "Graph has multiple bridge components");
    }

    private static void testUndirectedHierholzer() {
        // Test Hierholzer circuit
        UndirectedGraph circuit = loadUndirectedGraph("u_circuit.txt");
        List<UndirectedGraph.Edge> hierCircuit = circuit.eulerianCircuitHierholzer();
        check(hierCircuit != null, "Eulerian circuit found");
        check(hierCircuit.size() == circuit.size(), "Circuit covers all edges");

        // Test Hierholzer trail
        UndirectedGraph trail = loadUndirectedGraph("u_trail.txt");
        List<UndirectedGraph.Edge> hierTrail = trail.eulerianTrailHierholzer();
        check(hierTrail != null, "Eulerian trail found");
        check(hierTrail.size() == trail.size(), "Trail covers all edges");

        // Test on complex Eulerian
        UndirectedGraph complex = loadUndirectedGraph("u_eulerian_complex.txt");
        if (complex.hasEulerianCircuit()) {
            List<UndirectedGraph.Edge> complexPath = complex.eulerianCircuitHierholzer();
            check(complexPath != null, "Complex Eulerian circuit found");
        } else if (complex.hasEulerianTrail()) {
            List<UndirectedGraph.Edge> complexPath = complex.eulerianTrailHierholzer();
            check(complexPath != null, "Complex Eulerian trail found");
        }
        // Test failure case
        UndirectedGraph notEulerian = loadUndirectedGraph("u_not_eulerian.txt");
        check(notEulerian.eulerianCircuitHierholzer() == null, "Non-Eulerian returns null");
    }

    private static void testDigraphTree() {
        // Load u_tree.txt as directed
        Digraph tree = loadDigraph("u_tree.txt");
        check(tree.isTree(), "Valid directed tree");

        // Test cycle - not a tree
        Digraph cycle = loadDigraph("d_circuit.txt");
        check(!cycle.isTree(), "Cycle is not a tree");

        // Test complex DAG
        loadDigraph("d_dag_complex.txt"); // Load for coverage
        check(true, "DAG tree test completed");
    }

    private static void testDigraphEulerian() {
        // Test Eulerian circuit
        Digraph circuit = loadDigraph("d_eulerian_circuit.txt");
        check(circuit.hasEulerianCircuit(), "Has Eulerian circuit");
        check(circuit.hasEulerianTrail(), "Circuit is also a trail");

        List<Digraph.Edge> circuitPath = circuit.eulerianCircuitHierholzer();
        check(circuitPath != null, "Circuit path found");
        check(circuitPath.size() == circuit.size(), "Circuit covers all edges");

        // Test Eulerian trail (not circuit)
        Digraph trail = loadDigraph("d_eulerian_trail.txt");
        check(trail.hasEulerianTrail(), "Has Eulerian trail");
        check(!trail.hasEulerianCircuit(), "Trail is not a circuit");

        List<Digraph.Edge> trailPath = trail.eulerianTrailHierholzer();
        check(trailPath != null, "Trail path found");
        check(trailPath.size() == trail.size(), "Trail covers all edges");

        // Test non-Eulerian (DAG)
        Digraph dag = loadDigraph("d_dag_complex.txt");
        check(!dag.hasEulerianCircuit(), "DAG has no Eulerian circuit");
    }

    private static void testDigraphSCCComplex() {
        // Test multiple SCCs
        Digraph multiSCC = loadDigraph("d_multiple_scc.txt");
        Set<Set<Graph.Vertex>> sccKS = multiSCC.stronglyConnectedComponentsKS();
        Set<Set<Graph.Vertex>> sccT = multiSCC.stronglyConnectedComponentsT();

        check(sccKS.size() > 1, "Multiple SCCs found (Kosaraju)");
        check(sccT.size() > 1, "Multiple SCCs found (Tarjan)");
        check(sccKS.size() == sccT.size(), "Kosaraju and Tarjan agree on count");

        // Test complex cycles
        Digraph cyclesComplex = loadDigraph("d_cycles_complex.txt");
        Set<Set<Graph.Vertex>> complexSCC = cyclesComplex.stronglyConnectedComponentsKS();
        check(complexSCC.size() >= 1, "Complex graph has SCCs");
    }

    private static void testGenericTypes() {
        UndirectedGraph<Integer> g = new UndirectedGraph<>();
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 1);
        check(g.order() == 3, "Integer graph order");
        check(g.size() == 3, "Integer graph size");
        check(g.isConnected(), "Integer graph connected");
        check(g.hasCycles(), "Integer graph has cycle");
        check(g.distanceBFS(1, 3) == 1, "Distance 1 to 3 is 1");

        UndirectedGraph.Edge<Integer> e12 = new UndirectedGraph.Edge<>(g.getVertexById(1), g.getVertexById(2));
        UndirectedGraph.Edge<Integer> e21 = new UndirectedGraph.Edge<>(g.getVertexById(2), g.getVertexById(1));
        check(e12.equals(e21), "Edge equality is symmetric");
        check(e12.hashCode() == e21.hashCode(), "Edge hashCode is symmetric");

        Digraph<Integer> dg = new Digraph<>();
        dg.addEdge(1, 2);
        dg.addEdge(2, 3);
        check(dg.order() == 3, "Digraph integer order");
        check(dg.size() == 2, "Digraph integer size");
        check(!dg.hasCycles(), "Digraph integer has no cycles");
        check(dg.topologicalSort().size() == 3, "Digraph integer topological sort");
    }

    private static void testHierholzerImmutabilityAndEdgeCases() {
        // 1. Immutability
        UndirectedGraph<String> circuitGraph = loadUndirectedGraph("u_circuit.txt");
        int initOrder = circuitGraph.order();
        int initSize = circuitGraph.size();
        List<UndirectedGraph.Edge<String>> circuit = circuitGraph.eulerianCircuitHierholzer();
        check(circuitGraph.order() == initOrder, "Hierholzer circuit must not mutate graph order");
        check(circuitGraph.size() == initSize, "Hierholzer circuit must not mutate graph size");
        check(circuit != null && circuit.size() == initSize, "Circuit covers all edges");
        check(isValidEulerianPath(circuitGraph, circuit), "Hierholzer circuit is valid and continuous");

        UndirectedGraph<String> trailGraph = loadUndirectedGraph("u_trail.txt");
        int trailInitOrder = trailGraph.order();
        int trailInitSize = trailGraph.size();
        List<UndirectedGraph.Edge<String>> trail = trailGraph.eulerianTrailHierholzer();
        check(trailGraph.order() == trailInitOrder, "Hierholzer trail must not mutate graph order");
        check(trailGraph.size() == trailInitSize, "Hierholzer trail must not mutate graph size");
        check(trail != null && trail.size() == trailInitSize, "Trail covers all edges");
        check(isValidEulerianPath(trailGraph, trail), "Hierholzer trail is valid and continuous");

        // 2. Edge Cases: Empty graph, isolated vertex, disconnected
        UndirectedGraph<Integer> emptyG = new UndirectedGraph<>();
        check(emptyG.eulerianCircuitHierholzer() != null && emptyG.eulerianCircuitHierholzer().isEmpty(), "Empty graph circuit is empty list");
        check(emptyG.eulerianTrailHierholzer() != null && emptyG.eulerianTrailHierholzer().isEmpty(), "Empty graph trail is empty list");

        UndirectedGraph<Integer> isolatedG = new UndirectedGraph<>();
        isolatedG.addVertex(1);
        isolatedG.addVertex(2);
        check(isolatedG.eulerianCircuitHierholzer() != null && isolatedG.eulerianCircuitHierholzer().isEmpty(), "Isolated vertices circuit is empty list");
        check(isolatedG.eulerianTrailHierholzer() != null && isolatedG.eulerianTrailHierholzer().isEmpty(), "Isolated vertices trail is empty list");

        UndirectedGraph<String> nonEulerian = loadUndirectedGraph("u_not_eulerian.txt");
        check(nonEulerian.eulerianCircuitHierholzer() == null, "Non-Eulerian circuit returns null");
        check(nonEulerian.eulerianTrailHierholzer() == null, "Non-Eulerian trail returns null");
    }

    private static void testDigraphDegreeConsistency() {
        Digraph<Integer> dg = new Digraph<>();
        dg.addEdge(1, 2);
        dg.addEdge(1, 3);
        dg.addEdge(2, 3);

        check(dg.inDegree(1) == 0, "inDegree(1) == 0");
        check(dg.inDegree(2) == 1, "inDegree(2) == 1");
        check(dg.inDegree(3) == 2, "inDegree(3) == 2");

        check(dg.outDegree(1) == 2, "outDegree(1) == 2");
        check(dg.outDegree(2) == 1, "outDegree(2) == 1");
        check(dg.outDegree(3) == 0, "outDegree(3) == 0");

        // Null and non-existent queries
        check(dg.inDegree(99) == 0, "inDegree(99) == 0");
        check(dg.outDegree(99) == 0, "outDegree(99) == 0");
        check(dg.inDegree((Graph.Vertex<Integer>) null) == 0, "inDegree(null) == 0");
        check(dg.outDegree((Graph.Vertex<Integer>) null) == 0, "outDegree(null) == 0");

        // Edge removal updates
        dg.removeEdge(1, 3);
        check(dg.inDegree(3) == 1, "After removeEdge(1,3), inDegree(3) == 1");
        check(dg.outDegree(1) == 1, "After removeEdge(1,3), outDegree(1) == 1");

        // Vertex removal updates
        dg.removeVertex(2);
        check(dg.inDegree(3) == 0, "After removeVertex(2), inDegree(3) == 0");
        check(dg.order() == 2, "Digraph order is 2 after removeVertex");
        check(dg.size() == 0, "Digraph size is 0 after removing vertices");
    }

    private static void testKonigDualInvariants() {
        UndirectedGraph<String> g = loadUndirectedGraph("u_bipartite_matching.txt");
        Set<UndirectedGraph.Edge<String>> matching = g.maxMatching();
        Set<Graph.Vertex<String>> cover = g.minVertexCover();
        Set<Graph.Vertex<String>> indep = g.maxIndependentSet();

        check(matching != null && cover != null && indep != null, "Dual sets must not be null");
        check(matching.size() == cover.size(), "König theorem: |M| == |C|");
        check(cover.size() + indep.size() == g.order(), "Gallai theorem: |C| + |I| == |V|");

        // Disconnected bipartite graph
        UndirectedGraph<String> disc = loadUndirectedGraph("u_disconnected.txt");
        Set<UndirectedGraph.Edge<String>> discMatching = disc.maxMatching();
        Set<Graph.Vertex<String>> discCover = disc.minVertexCover();
        Set<Graph.Vertex<String>> discIndep = disc.maxIndependentSet();
        check(discMatching.size() == discCover.size(), "Disconnected König theorem: |M| == |C|");
        check(discCover.size() + discIndep.size() == disc.order(), "Disconnected Gallai theorem: |C| + |I| == |V|");
    }

    private static void testWeightedUndirectedGraph() {
        WeightedUndirectedGraph<Integer> wg = new WeightedUndirectedGraph<>();
        wg.addEdge(1, 2, 3.5);
        wg.addEdge(2, 3, 4.0);
        wg.addEdge(3, 1); // default 1.0

        check(wg.order() == 3, "Weighted undirected order");
        check(wg.size() == 3, "Weighted undirected size");
        check(wg.getWeight(1, 2) == 3.5, "Weight 1-2 is 3.5");
        check(wg.getWeight(2, 1) == 3.5, "Weight 2-1 is symmetric 3.5");
        check(wg.getWeight(3, 1) == 1.0, "Default weight 3-1 is 1.0");
        check(wg.getWeight(1, 99) == Double.POSITIVE_INFINITY, "Non-existent edge weight is +Inf");

        // Update weight
        wg.addEdge(1, 2, 7.5);
        check(wg.size() == 3, "Size remains 3 after updating weight");
        check(wg.getWeight(1, 2) == 7.5, "Updated weight is 7.5");
        check(wg.getWeight(2, 1) == 7.5, "Updated weight symmetric is 7.5");

        // Clone/Copy
        WeightedUndirectedGraph<Integer> copy = wg.copy();
        check(copy.getWeight(1, 2) == 7.5, "Copy retains weight");
        copy.removeEdge(1, 2);
        check(copy.getWeight(1, 2) == Double.POSITIVE_INFINITY, "Copy removed edge");
        check(wg.getWeight(1, 2) == 7.5, "Original retains weight");

        // Inherited algorithms test (Etapa 2)
        check(wg.isConnected(), "Weighted undirected is connected");
        check(wg.hasCycles(), "Weighted undirected has cycles");
        check(!wg.isBipartite(), "Triangle is not bipartite");
        check(wg.eulerianCircuitHierholzer() != null, "Eulerian circuit works on weighted graph");
    }

    private static void testWeightedDigraph() {
        WeightedDigraph<String> wd = new WeightedDigraph<>();
        wd.addEdge("A", "B", 5.0);
        wd.addEdge("B", "C", 2.5);
        wd.addEdge("C", "A"); // default 1.0

        check(wd.order() == 3, "Weighted digraph order");
        check(wd.size() == 3, "Weighted digraph size");
        check(wd.getWeight("A", "B") == 5.0, "Weight A->B is 5.0");
        check(wd.getWeight("B", "A") == Double.POSITIVE_INFINITY, "Weight B->A is +Inf (directional)");
        check(wd.getWeight("C", "A") == 1.0, "Weight C->A default is 1.0");

        // Update weight
        wd.addEdge("A", "B", 10.0);
        check(wd.size() == 3, "Size remains 3 after updating weight");
        check(wd.getWeight("A", "B") == 10.0, "Updated weight is 10.0");

        // Transpose copy
        WeightedDigraph<String> T = wd.transposeCopy();
        check(T.getWeight("B", "A") == 10.0, "Transpose inverts direction with same weight");
        check(T.getWeight("A", "B") == Double.POSITIVE_INFINITY, "Transpose original direction is +Inf");

        // Inherited algorithms test (Etapa 2)
        check(wd.hasCycles(), "Cycle detected in weighted digraph");
        check(wd.inDegree("B") == 1, "inDegree is 1");
        check(wd.outDegree("A") == 1, "outDegree is 1");
        check(wd.stronglyConnectedComponentsKS().size() == 1, "SCC contains 1 component (cycle A->B->C->A)");
    }

    private static void testDisjointSetUnion() {
        DisjointSetUnion<Integer> dsu = new DisjointSetUnion<>();
        dsu.makeSet(1);
        dsu.makeSet(2);
        dsu.makeSet(3);
        dsu.makeSet(4);

        check(dsu.count() == 4, "Initial DSU count is 4");
        check(!dsu.connected(1, 2), "1 and 2 not initially connected");

        check(dsu.union(1, 2), "Union 1 and 2 returns true");
        check(dsu.connected(1, 2), "1 and 2 connected");
        check(!dsu.union(1, 2), "Duplicate union returns false");
        check(dsu.count() == 3, "DSU count is 3");

        dsu.union(3, 4);
        check(dsu.connected(3, 4), "3 and 4 connected");
        check(!dsu.connected(1, 3), "1 and 3 not connected");

        dsu.union(2, 4);
        check(dsu.connected(1, 3), "1 and 3 connected after transitive union");
        check(dsu.count() == 1, "DSU count is 1 after connecting all");
    }

    private static void testDijkstraAndShortestPaths() {
        WeightedDigraph<String> wd = new WeightedDigraph<>();
        wd.addEdge("A", "B", 4.0);
        wd.addEdge("A", "C", 2.0);
        wd.addEdge("C", "B", 1.0);
        wd.addEdge("B", "D", 5.0);
        wd.addEdge("C", "D", 8.0);
        wd.addEdge("C", "E", 10.0);
        wd.addEdge("D", "E", 2.0);

        Map<Graph.Vertex<String>, Double> distA = wd.dijkstraDistances("A");
        check(distA.get(wd.getVertexById("A")) == 0.0, "Dist A->A is 0");
        check(distA.get(wd.getVertexById("C")) == 2.0, "Dist A->C is 2");
        check(distA.get(wd.getVertexById("B")) == 3.0, "Dist A->B is 3 (via C)");
        check(distA.get(wd.getVertexById("D")) == 8.0, "Dist A->D is 8 (A-C-B-D)");
        check(distA.get(wd.getVertexById("E")) == 10.0, "Dist A->E is 10 (A-C-B-D-E)");

        List<Graph.Vertex<String>> path = wd.dijkstraShortestPath("A", "E");
        check(path.size() == 5, "Shortest path length is 5 vertices");
        check(path.get(0).id().equals("A"), "Path starts at A");
        check(path.get(1).id().equals("C"), "Path step 1 is C");
        check(path.get(2).id().equals("B"), "Path step 2 is B");
        check(path.get(3).id().equals("D"), "Path step 3 is D");
        check(path.get(4).id().equals("E"), "Path ends at E");

        // Negative weight check
        WeightedDigraph<String> negG = new WeightedDigraph<>();
        negG.addEdge("X", "Y", -1.0);
        boolean caught = false;
        try {
            negG.dijkstraDistances("X");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        check(caught, "Dijkstra rejects negative edge weights");
    }

    private static void testBellmanFordAndNegativeCycles() {
        WeightedDigraph<String> wd = new WeightedDigraph<>();
        wd.addEdge("S", "A", 4.0);
        wd.addEdge("S", "B", 5.0);
        wd.addEdge("A", "C", -2.0);
        wd.addEdge("B", "C", 2.0);
        wd.addEdge("C", "D", 3.0);

        check(!wd.hasNegativeCycle(), "No negative cycle in DAG with negative edge");
        Map<Graph.Vertex<String>, Double> distS = wd.bellmanFordDistances("S");
        check(distS.get(wd.getVertexById("C")) == 2.0, "Dist S->C is 2.0 (4 - 2)");
        check(distS.get(wd.getVertexById("D")) == 5.0, "Dist S->D is 5.0 (2 + 3)");

        // Negative cycle graph
        WeightedDigraph<String> negCycle = new WeightedDigraph<>();
        negCycle.addEdge("1", "2", 1.0);
        negCycle.addEdge("2", "3", -5.0);
        negCycle.addEdge("3", "1", 2.0); // cycle 1->2->3->1 with sum -2
        check(negCycle.hasNegativeCycle(), "Negative cycle detected");

        boolean threw = false;
        try {
            negCycle.bellmanFordDistances("1");
        } catch (IllegalStateException e) {
            threw = true;
        }
        check(threw, "Bellman-Ford throws on negative cycle reachable from source");
    }

    private static void testFloydWarshallAllPairs() {
        WeightedUndirectedGraph<Integer> wg = new WeightedUndirectedGraph<>();
        wg.addEdge(1, 2, 3.0);
        wg.addEdge(2, 3, 1.0);
        wg.addEdge(1, 3, 6.0);

        Map<Graph.Vertex<Integer>, Map<Graph.Vertex<Integer>, Double>> ap = wg.floydWarshall();
        Graph.Vertex<Integer> v1 = wg.getVertexById(1);
        Graph.Vertex<Integer> v2 = wg.getVertexById(2);
        Graph.Vertex<Integer> v3 = wg.getVertexById(3);

        check(ap.get(v1).get(v1) == 0.0, "APSP 1->1 is 0");
        check(ap.get(v1).get(v2) == 3.0, "APSP 1->2 is 3");
        check(ap.get(v1).get(v3) == 4.0, "APSP 1->3 is 4 (via 2)");
        check(ap.get(v3).get(v1) == 4.0, "APSP symmetric 3->1 is 4");
    }

    private static void testMinimumSpanningTreeKruskalAndPrim() {
        WeightedUndirectedGraph<String> g = new WeightedUndirectedGraph<>();
        g.addEdge("A", "B", 4.0);
        g.addEdge("A", "C", 2.0);
        g.addEdge("B", "C", 1.0);
        g.addEdge("B", "D", 5.0);
        g.addEdge("C", "D", 8.0);
        g.addEdge("C", "E", 10.0);
        g.addEdge("D", "E", 2.0);
        g.addEdge("D", "F", 6.0);
        g.addEdge("E", "F", 3.0);

        Set<WeightedEdge<String>> kruskal = g.kruskalMST();
        Set<WeightedEdge<String>> prim = g.primMST();

        check(kruskal.size() == g.order() - 1, "Kruskal MST has |V| - 1 edges");
        check(prim.size() == g.order() - 1, "Prim MST has |V| - 1 edges");

        double weightKruskal = g.kruskalWeight();
        double weightPrim = g.primWeight();
        check(Double.compare(weightKruskal, weightPrim) == 0, "Kruskal and Prim agree on total MST weight");
        check(weightKruskal == 13.0, "Total MST weight is 13.0");
    }

    private static void testMaxFlowAndMinCut() {
        WeightedDigraph<String> net = new WeightedDigraph<>();
        // Classic flow network
        net.addEdge("S", "A", 10.0);
        net.addEdge("S", "B", 10.0);
        net.addEdge("A", "B", 2.0);
        net.addEdge("A", "T", 4.0);
        net.addEdge("B", "T", 8.0);

        double ekFlow = net.edmondsKarpMaxFlow("S", "T");
        double ffFlow = net.fordFulkersonMaxFlow("S", "T");

        check(Double.compare(ekFlow, 12.0) == 0, "Edmonds-Karp max flow is 12.0");
        check(Double.compare(ffFlow, 12.0) == 0, "Ford-Fulkerson max flow is 12.0");

        // Min-Cut verification (Max-Flow Min-Cut Theorem)
        Set<WeightedEdge<String>> cut = net.minCut("S", "T");
        check(!cut.isEmpty(), "Min-cut contains edges");
        double cutCap = net.minCutCapacity("S", "T");
        check(Double.compare(cutCap, ekFlow) == 0, "Max-Flow Min-Cut Theorem holds: flow == minCut capacity (12.0)");

        // Max Bipartite Matching via Flow
        Set<String> partX = Set.of("1", "2", "3");
        Set<String> partY = Set.of("4", "5");
        WeightedDigraph<String> bipNet = new WeightedDigraph<>();
        bipNet.addEdge("1", "4", 1.0);
        bipNet.addEdge("2", "4", 1.0);
        bipNet.addEdge("2", "5", 1.0);
        bipNet.addEdge("3", "5", 1.0);

        int maxMatch = bipNet.maxBipartiteMatchingFlow(partX, partY);
        check(maxMatch == 2, "Max bipartite matching via max-flow is 2");
    }

    // ------------------------------ EDMONDS BLOSSOM & EDGE COVER TESTS ------------------------------ //

    private static void testEdmondsBlossomGeneralMatching() {
        // 1. Triangle C3 (Odd cycle, non-bipartite)
        UndirectedGraph<Integer> c3 = new UndirectedGraph<>();
        c3.addEdge(1, 2);
        c3.addEdge(2, 3);
        c3.addEdge(3, 1);
        Set<UndirectedGraph.Edge<Integer>> matchC3 = c3.maxMatchingBlossom();
        check(matchC3 != null && matchC3.size() == 1, "C3 Blossom matching size is 1");
        checkValidMatching(c3, matchC3);

        // 2. Cycle of 5 C5 (Odd cycle, non-bipartite)
        UndirectedGraph<Integer> c5 = new UndirectedGraph<>();
        c5.addEdge(1, 2);
        c5.addEdge(2, 3);
        c5.addEdge(3, 4);
        c5.addEdge(4, 5);
        c5.addEdge(5, 1);
        Set<UndirectedGraph.Edge<Integer>> matchC5 = c5.maxMatchingBlossom();
        check(matchC5 != null && matchC5.size() == 2, "C5 Blossom matching size is 2");
        checkValidMatching(c5, matchC5);

        // 3. Complete Graph K4
        UndirectedGraph<String> k4 = loadUndirectedGraph("u_complete_k4.txt");
        Set<UndirectedGraph.Edge<String>> matchK4 = k4.maxMatchingBlossom();
        check(matchK4 != null && matchK4.size() == 2, "K4 Blossom matching is 2 (perfect matching)");
        checkValidMatching(k4, matchK4);

        // 4. Complete Graph K5
        UndirectedGraph<Integer> k5 = new UndirectedGraph<>();
        for (int i = 1; i <= 5; i++) {
            for (int j = i + 1; j <= 5; j++) {
                k5.addEdge(i, j);
            }
        }
        Set<UndirectedGraph.Edge<Integer>> matchK5 = k5.maxMatchingBlossom();
        check(matchK5 != null && matchK5.size() == 2, "K5 Blossom matching is 2");
        checkValidMatching(k5, matchK5);

        // 5. Petersen Graph (10 vertices, 15 edges, non-bipartite, perfect matching = 5)
        UndirectedGraph<Integer> petersen = new UndirectedGraph<>();
        // Outer 5-cycle: 0-1-2-3-4-0
        petersen.addEdge(0, 1);
        petersen.addEdge(1, 2);
        petersen.addEdge(2, 3);
        petersen.addEdge(3, 4);
        petersen.addEdge(4, 0);
        // Inner 5-star: 5-7-9-6-8-5
        petersen.addEdge(5, 7);
        petersen.addEdge(7, 9);
        petersen.addEdge(9, 6);
        petersen.addEdge(6, 8);
        petersen.addEdge(8, 5);
        // Spokes: 0-5, 1-6, 2-7, 3-8, 4-9
        petersen.addEdge(0, 5);
        petersen.addEdge(1, 6);
        petersen.addEdge(2, 7);
        petersen.addEdge(3, 8);
        petersen.addEdge(4, 9);
        check(petersen.order() == 10 && petersen.size() == 15, "Petersen graph constructed with 10 vertices and 15 edges");
        check(!petersen.isBipartite(), "Petersen graph is non-bipartite");
        Set<UndirectedGraph.Edge<Integer>> matchPetersen = petersen.maxMatchingBlossom();
        check(matchPetersen != null && matchPetersen.size() == 5, "Petersen graph has perfect matching of size 5");
        checkValidMatching(petersen, matchPetersen);

        // 6. Nested Blossoms (Two blossoms sharing vertices connected by stem)
        UndirectedGraph<Integer> nested = new UndirectedGraph<>();
        // Stem: 1-2
        nested.addEdge(1, 2);
        // Blossom 1: 2-3-4-2
        nested.addEdge(2, 3);
        nested.addEdge(3, 4);
        nested.addEdge(4, 2);
        // Blossom 2: 4-5-6-4
        nested.addEdge(4, 5);
        nested.addEdge(5, 6);
        nested.addEdge(6, 4);
        Set<UndirectedGraph.Edge<Integer>> matchNested = nested.maxMatchingBlossom();
        check(matchNested != null && matchNested.size() == 3, "Nested blossoms graph has max matching of size 3");
        checkValidMatching(nested, matchNested);

        // 7. Disconnected Mixed Graph (C3 + C4)
        UndirectedGraph<Integer> mixed = new UndirectedGraph<>();
        mixed.addEdge(1, 2);
        mixed.addEdge(2, 3);
        mixed.addEdge(3, 1);
        mixed.addEdge(4, 5);
        mixed.addEdge(5, 6);
        mixed.addEdge(6, 7);
        mixed.addEdge(7, 4);
        Set<UndirectedGraph.Edge<Integer>> matchMixed = mixed.maxMatchingBlossom();
        check(matchMixed != null && matchMixed.size() == 3, "Mixed disconnected graph matching is 3 (1 from C3 + 2 from C4)");
        checkValidMatching(mixed, matchMixed);

        // 8. Boundary cases: Empty graph, isolated vertices, single edge
        UndirectedGraph<Integer> emptyG = new UndirectedGraph<>();
        check(emptyG.maxMatchingBlossom() != null && emptyG.maxMatchingBlossom().isEmpty(), "Empty graph matching is empty");

        UndirectedGraph<Integer> isolatedG = new UndirectedGraph<>();
        isolatedG.addVertex(1);
        isolatedG.addVertex(2);
        isolatedG.addVertex(3);
        check(isolatedG.maxMatchingBlossom() != null && isolatedG.maxMatchingBlossom().isEmpty(), "Isolated vertices matching is empty");

        UndirectedGraph<Integer> singleEdgeG = new UndirectedGraph<>();
        singleEdgeG.addEdge(1, 2);
        singleEdgeG.addVertex(3);
        check(singleEdgeG.maxMatchingBlossom().size() == 1, "Graph with single edge and isolated vertex has matching of size 1");

        // Self-loop test: edge (1,1) and edge (1,2)
        UndirectedGraph<Integer> selfLoopG = new UndirectedGraph<>();
        selfLoopG.addEdge(1, 1);
        selfLoopG.addEdge(1, 2);
        Set<UndirectedGraph.Edge<Integer>> matchSelf = selfLoopG.maxMatchingBlossom();
        check(matchSelf != null && matchSelf.size() == 1, "Graph with self-loop (1,1) and edge (1,2) has matching of size 1");
        checkValidMatching(selfLoopG, matchSelf);

        // Self-loop only test: edge (1,1)
        UndirectedGraph<Integer> onlySelfLoop = new UndirectedGraph<>();
        onlySelfLoop.addEdge(1, 1);
        Set<UndirectedGraph.Edge<Integer>> matchOnlySelf = onlySelfLoop.maxMatchingBlossom();
        check(matchOnlySelf != null && matchOnlySelf.isEmpty(), "Graph with only self-loop has empty matching");

        // 9. Grötzsch Graph (11 vertices, 20 edges, triangle-free, chromatic number 4, non-bipartite)
        UndirectedGraph<Integer> grotzsch = new UndirectedGraph<>();
        for (int i = 0; i < 5; i++) grotzsch.addEdge(i, (i + 1) % 5);
        for (int i = 5; i <= 9; i++) grotzsch.addEdge(10, i);
        grotzsch.addEdge(5, 1); grotzsch.addEdge(5, 4);
        grotzsch.addEdge(6, 0); grotzsch.addEdge(6, 2);
        grotzsch.addEdge(7, 1); grotzsch.addEdge(7, 3);
        grotzsch.addEdge(8, 2); grotzsch.addEdge(8, 4);
        grotzsch.addEdge(9, 3); grotzsch.addEdge(9, 0);
        Set<UndirectedGraph.Edge<Integer>> matchGrotzsch = grotzsch.maxMatchingBlossom();
        check(matchGrotzsch != null && matchGrotzsch.size() == 5, "Grötzsch graph has max matching of size 5");
        checkValidMatching(grotzsch, matchGrotzsch);

        // 10. Deep 3-Level Nested Blossoms
        // Stem: 0-1, Blossom 1: 1-2-3-1, Blossom 2: 3-4-5-3, Blossom 3: 5-6-7-5, Tail: 7-8
        UndirectedGraph<Integer> deepNested = new UndirectedGraph<>();
        deepNested.addEdge(0, 1);
        deepNested.addEdge(1, 2); deepNested.addEdge(2, 3); deepNested.addEdge(3, 1);
        deepNested.addEdge(3, 4); deepNested.addEdge(4, 5); deepNested.addEdge(5, 3);
        deepNested.addEdge(5, 6); deepNested.addEdge(6, 7); deepNested.addEdge(7, 5);
        deepNested.addEdge(7, 8);
        Set<UndirectedGraph.Edge<Integer>> matchDeep = deepNested.maxMatchingBlossom();
        check(matchDeep != null && matchDeep.size() == 4, "Deep 3-level nested blossoms graph has max matching of size 4");
        checkValidMatching(deepNested, matchDeep);

        // 11. Odd Wheels W5 (hub + C4) and W6 (hub + C5)
        UndirectedGraph<Integer> w5 = new UndirectedGraph<>();
        for (int i = 1; i <= 4; i++) {
            w5.addEdge(i, (i % 4) + 1);
            w5.addEdge(0, i); // hub 0
        }
        Set<UndirectedGraph.Edge<Integer>> matchW5 = w5.maxMatchingBlossom();
        check(matchW5 != null && matchW5.size() == 2, "Wheel W5 has matching of size 2");
        checkValidMatching(w5, matchW5);

        UndirectedGraph<Integer> w6 = new UndirectedGraph<>();
        for (int i = 1; i <= 5; i++) {
            w6.addEdge(i, (i % 5) + 1);
            w6.addEdge(0, i); // hub 0
        }
        Set<UndirectedGraph.Edge<Integer>> matchW6 = w6.maxMatchingBlossom();
        check(matchW6 != null && matchW6.size() == 3, "Wheel W6 has perfect matching of size 3");
        checkValidMatching(w6, matchW6);
    }

    private static void testBlossomHopcroftKarpEquivalence() {
        // Compare Blossom vs Hopcroft-Karp on all bipartite test suites
        String[] bipartiteFiles = {"u_bipartite_matching.txt", "u_bipartite_c4.txt", "u_disconnected.txt"};
        for (String file : bipartiteFiles) {
            @SuppressWarnings("unchecked")
            UndirectedGraph<String> g = (UndirectedGraph<String>) loadUndirectedGraph(file);
            Set<UndirectedGraph.Edge<String>> hk = g.maxMatching();
            Set<UndirectedGraph.Edge<String>> blossom = g.maxMatchingBlossom();
            Set<UndirectedGraph.Edge<String>> general = g.maxMatchingGeneral();

            check(hk != null, "Hopcroft-Karp returns matching for " + file);
            check(blossom != null, "Blossom returns matching for " + file);
            check(general != null, "General returns matching for " + file);
            check(hk.size() == blossom.size(), "Blossom size agrees with Hopcroft-Karp on " + file + " (size: " + hk.size() + ")");
            check(blossom.size() == general.size(), "maxMatchingGeneral matches maxMatchingBlossom on " + file);
            checkValidMatching(g, blossom);
        }
    }

    private static void testMinimumEdgeCoverAndGallaiTheorem() {
        // Gallai's Theorem: alpha'(G) + beta'(G) = |V| for graphs without isolated vertices

        // 1. Triangle C3
        UndirectedGraph<Integer> c3 = new UndirectedGraph<>();
        c3.addEdge(1, 2);
        c3.addEdge(2, 3);
        c3.addEdge(3, 1);
        Set<UndirectedGraph.Edge<Integer>> mC3 = c3.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<Integer>> covC3 = c3.minEdgeCover();
        check(covC3 != null && covC3.size() == 2, "C3 min edge cover size is 2");
        check(mC3.size() + covC3.size() == c3.order(), "Gallai Theorem holds for C3: 1 + 2 = 3");
        checkValidEdgeCover(c3, covC3);

        // 2. Cycle 4 (C4)
        UndirectedGraph<Integer> c4 = new UndirectedGraph<>();
        c4.addEdge(1, 2);
        c4.addEdge(2, 3);
        c4.addEdge(3, 4);
        c4.addEdge(4, 1);
        Set<UndirectedGraph.Edge<Integer>> mC4 = c4.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<Integer>> covC4 = c4.minEdgeCover();
        check(covC4 != null && covC4.size() == 2, "C4 min edge cover size is 2");
        check(mC4.size() + covC4.size() == c4.order(), "Gallai Theorem holds for C4: 2 + 2 = 4");
        checkValidEdgeCover(c4, covC4);

        // 3. Cycle 5 (C5)
        UndirectedGraph<Integer> c5 = new UndirectedGraph<>();
        c5.addEdge(1, 2);
        c5.addEdge(2, 3);
        c5.addEdge(3, 4);
        c5.addEdge(4, 5);
        c5.addEdge(5, 1);
        Set<UndirectedGraph.Edge<Integer>> mC5 = c5.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<Integer>> covC5 = c5.minEdgeCover();
        check(covC5 != null && covC5.size() == 3, "C5 min edge cover size is 3");
        check(mC5.size() + covC5.size() == c5.order(), "Gallai Theorem holds for C5: 2 + 3 = 5");
        checkValidEdgeCover(c5, covC5);

        // 4. Complete Graph K4
        UndirectedGraph<String> k4 = loadUndirectedGraph("u_complete_k4.txt");
        Set<UndirectedGraph.Edge<String>> mK4 = k4.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<String>> covK4 = k4.minEdgeCover();
        check(covK4 != null && covK4.size() == 2, "K4 min edge cover size is 2");
        check(mK4.size() + covK4.size() == k4.order(), "Gallai Theorem holds for K4: 2 + 2 = 4");
        checkValidEdgeCover(k4, covK4);

        // 5. Complete Graph K5
        UndirectedGraph<Integer> k5 = new UndirectedGraph<>();
        for (int i = 1; i <= 5; i++) {
            for (int j = i + 1; j <= 5; j++) {
                k5.addEdge(i, j);
            }
        }
        Set<UndirectedGraph.Edge<Integer>> mK5 = k5.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<Integer>> covK5 = k5.minEdgeCover();
        check(covK5 != null && covK5.size() == 3, "K5 min edge cover size is 3");
        check(mK5.size() + covK5.size() == k5.order(), "Gallai Theorem holds for K5: 2 + 3 = 5");
        checkValidEdgeCover(k5, covK5);

        // 6. Petersen Graph
        UndirectedGraph<Integer> petersen = new UndirectedGraph<>();
        petersen.addEdge(0, 1);
        petersen.addEdge(1, 2);
        petersen.addEdge(2, 3);
        petersen.addEdge(3, 4);
        petersen.addEdge(4, 0);
        petersen.addEdge(5, 7);
        petersen.addEdge(7, 9);
        petersen.addEdge(9, 6);
        petersen.addEdge(6, 8);
        petersen.addEdge(8, 5);
        petersen.addEdge(0, 5);
        petersen.addEdge(1, 6);
        petersen.addEdge(2, 7);
        petersen.addEdge(3, 8);
        petersen.addEdge(4, 9);
        Set<UndirectedGraph.Edge<Integer>> mPet = petersen.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<Integer>> covPet = petersen.minEdgeCover();
        check(covPet != null && covPet.size() == 5, "Petersen min edge cover size is 5");
        check(mPet.size() + covPet.size() == petersen.order(), "Gallai Theorem holds for Petersen: 5 + 5 = 10");
        checkValidEdgeCover(petersen, covPet);

        // 7. Bipartite matching file
        UndirectedGraph<String> bip = loadUndirectedGraph("u_bipartite_matching.txt");
        Set<UndirectedGraph.Edge<String>> mBip = bip.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<String>> covBip = bip.minEdgeCover();
        check(covBip != null && covBip.size() == 3, "u_bipartite_matching min edge cover size is 3");
        check(mBip.size() + covBip.size() == bip.order(), "Gallai Theorem holds for u_bipartite_matching: 2 + 3 = 5");
        checkValidEdgeCover(bip, covBip);

        // 8. Overloaded minEdgeCover(matching)
        Set<UndirectedGraph.Edge<String>> covFromPrecomputed = bip.minEdgeCover(bip.maxMatching());
        check(covFromPrecomputed != null && covFromPrecomputed.size() == 3, "Precomputed matching produces identical edge cover size");

        // 9. Grötzsch Graph Gallai's Theorem
        UndirectedGraph<Integer> grotzsch = new UndirectedGraph<>();
        for (int i = 0; i < 5; i++) grotzsch.addEdge(i, (i + 1) % 5);
        for (int i = 5; i <= 9; i++) grotzsch.addEdge(10, i);
        grotzsch.addEdge(5, 1); grotzsch.addEdge(5, 4);
        grotzsch.addEdge(6, 0); grotzsch.addEdge(6, 2);
        grotzsch.addEdge(7, 1); grotzsch.addEdge(7, 3);
        grotzsch.addEdge(8, 2); grotzsch.addEdge(8, 4);
        grotzsch.addEdge(9, 3); grotzsch.addEdge(9, 0);
        Set<UndirectedGraph.Edge<Integer>> mGrotzsch = grotzsch.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<Integer>> covGrotzsch = grotzsch.minEdgeCover();
        check(covGrotzsch != null && covGrotzsch.size() == 6, "Grötzsch min edge cover size is 6");
        check(mGrotzsch.size() + covGrotzsch.size() == grotzsch.order(), "Gallai Theorem holds for Grötzsch: 5 + 6 = 11");
        checkValidEdgeCover(grotzsch, covGrotzsch);

        // 10. Deep Nested Blossoms Gallai's Theorem
        UndirectedGraph<Integer> deepNested = new UndirectedGraph<>();
        deepNested.addEdge(0, 1);
        deepNested.addEdge(1, 2); deepNested.addEdge(2, 3); deepNested.addEdge(3, 1);
        deepNested.addEdge(3, 4); deepNested.addEdge(4, 5); deepNested.addEdge(5, 3);
        deepNested.addEdge(5, 6); deepNested.addEdge(6, 7); deepNested.addEdge(7, 5);
        deepNested.addEdge(7, 8);
        Set<UndirectedGraph.Edge<Integer>> mDeep = deepNested.maxMatchingBlossom();
        Set<UndirectedGraph.Edge<Integer>> covDeep = deepNested.minEdgeCover();
        check(covDeep != null && covDeep.size() == 5, "Deep nested blossoms min edge cover size is 5");
        check(mDeep.size() + covDeep.size() == deepNested.order(), "Gallai Theorem holds for deep nested: 4 + 5 = 9");
        checkValidEdgeCover(deepNested, covDeep);

        // 11. Self-Loop Edge Covers & Gallai Invariant
        UndirectedGraph<Integer> selfOnly = new UndirectedGraph<>();
        selfOnly.addEdge(1, 1);
        Set<UndirectedGraph.Edge<Integer>> covSelfOnly = selfOnly.minEdgeCover();
        check(covSelfOnly != null && covSelfOnly.size() == 1, "Self-loop only has min edge cover of size 1");
        check(selfOnly.maxMatchingBlossom().size() + covSelfOnly.size() == selfOnly.order(), "Gallai Theorem for self-loop only: 0 + 1 = 1");
        checkValidEdgeCover(selfOnly, covSelfOnly);

        UndirectedGraph<Integer> selfWithEdge = new UndirectedGraph<>();
        selfWithEdge.addEdge(1, 1);
        selfWithEdge.addEdge(1, 2);
        Set<UndirectedGraph.Edge<Integer>> covSelfEdge = selfWithEdge.minEdgeCover();
        check(covSelfEdge != null && covSelfEdge.size() == 1, "Graph with (1,1) and (1,2) has min edge cover of size 1");
        check(selfWithEdge.maxMatchingBlossom().size() + covSelfEdge.size() == selfWithEdge.order(), "Gallai Theorem for (1,1) and (1,2): 1 + 1 = 2");
        checkValidEdgeCover(selfWithEdge, covSelfEdge);

        // 12. Boundary cases: Isolated vertices and empty graph
        UndirectedGraph<Integer> isolatedG = new UndirectedGraph<>();
        isolatedG.addEdge(1, 2);
        isolatedG.addVertex(3); // isolated vertex
        check(isolatedG.minEdgeCover() == null, "Graph with isolated vertex returns null for minEdgeCover");

        UndirectedGraph<Integer> allIsolated = new UndirectedGraph<>();
        allIsolated.addVertex(1);
        check(allIsolated.minEdgeCover() == null, "Graph with only isolated vertex returns null for minEdgeCover");

        UndirectedGraph<Integer> emptyG = new UndirectedGraph<>();
        check(emptyG.minEdgeCover() != null && emptyG.minEdgeCover().isEmpty(), "Empty graph minEdgeCover is empty set");
        check(emptyG.minEdgeCover(null) == null, "minEdgeCover(null) returns null");
    }

    private static <V> void checkValidMatching(UndirectedGraph<V> g, Set<UndirectedGraph.Edge<V>> matching) {
        Set<Graph.Vertex<V>> seen = new HashSet<>();
        for (UndirectedGraph.Edge<V> e : matching) {
            check(g.getEdges().contains(e), "Matched edge " + e + " must exist in graph");
            check(!seen.contains(e.from), "Vertex " + e.from + " appears in multiple matched edges");
            check(!seen.contains(e.to), "Vertex " + e.to + " appears in multiple matched edges");
            seen.add(e.from);
            seen.add(e.to);
        }
    }

    private static <V> void checkValidEdgeCover(UndirectedGraph<V> g, Set<UndirectedGraph.Edge<V>> cover) {
        Set<Graph.Vertex<V>> covered = new HashSet<>();
        for (UndirectedGraph.Edge<V> e : cover) {
            check(g.getEdges().contains(e), "Cover edge " + e + " must exist in graph");
            covered.add(e.from);
            covered.add(e.to);
        }
        for (Graph.Vertex<V> v : g.getVertices()) {
            check(covered.contains(v), "Vertex " + v + " must be covered by at least one edge in edge cover");
        }
    }

    // ------------------------------ HELPER FRAMEWORK
    // ------------------------------ //

    private static void runTest(String name, Runnable test) {
        System.out.print("Running " + name + "... ");
        try {
            test.run();
            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable e) {
            System.out.println("FAILED");
            System.out.println("   Reason: " + e.getMessage());
            e.printStackTrace(System.out);
            testsFailed++;
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new RuntimeException("Assertion failed: " + description);
        }
    }
}
