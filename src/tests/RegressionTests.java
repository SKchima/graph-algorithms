package tests;

import graphs.*;
import java.io.*;
import java.util.*;

public class RegressionTests {

    private static final String DATA_DIR = "src/tests/data/";
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== STARTING REGRESSION TEST SUITE ===\n");

        // Common Graph Tests
        System.out.println("--- COMMON GRAPH TESTS ---");
        runTest("Graph: BFS Distances", RegressionTests::testBFS);
        runTest("Graph: Copy Mechanism", RegressionTests::testGraphCopy);

        // UndirectedGraph Tests
        System.out.println("\n--- UNDIRECTED GRAPH TESTS ---");
        runTest("UndirectedGraph: Connectivity & Components", RegressionTests::testUndirectedConnectivity);
        runTest("UndirectedGraph: Cycles", RegressionTests::testUndirectedCycles);
        runTest("UndirectedGraph: Bridges & Articulation Points", RegressionTests::testUndirectedStructures);
        runTest("UndirectedGraph: Eulerian Properties", RegressionTests::testUndirectedEulerian);
        runTest("UndirectedGraph: Bipartite", RegressionTests::testUndirectedBipartite);

        // Digraph Tests
        System.out.println("\n--- DIGRAPH TESTS ---");
        runTest("Digraph: Cycles", RegressionTests::testDigraphCycles);
        runTest("Digraph: Topological Sort", RegressionTests::testDigraphTopoSort);
        runTest("Digraph: Strongly Connected Components", RegressionTests::testDigraphSCC);
        runTest("Digraph: Degrees", RegressionTests::testDigraphDegrees);

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

    private static boolean isValidEulerianPath(UndirectedGraph g, List<UndirectedGraph.Edge> path) {
        if (path.size() != g.size())
            return false;

        // Check all edges exist in graph
        Set<UndirectedGraph.Edge> pathEdges = new HashSet<>(path);
        for (UndirectedGraph.Edge e : path) {
            if (!g.edges.contains(e))
                return false;
        }

        // Check path covers all edges exactly once
        if (pathEdges.size() != g.size())
            return false;

        // Check path continuity
        for (int i = 0; i < path.size() - 1; i++) {
            UndirectedGraph.Edge e1 = path.get(i);
            UndirectedGraph.Edge e2 = path.get(i + 1);
            if (!areConnected(e1, e2))
                return false;
        }

        return true;
    }

    private static boolean areConnected(UndirectedGraph.Edge e1, UndirectedGraph.Edge e2) {
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
        // d_circuit.txt has a cycle, so topo sort should return null
        Digraph cycle = loadDigraph("d_circuit.txt");
        List<Graph.Vertex> cycleSort = cycle.topologicalSort();
        check(cycleSort == null, "TopoSort returns null for Cyclic Graph");
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
        check(g.inDegree(v4) == 1, "v4 in-degree is 1");
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
