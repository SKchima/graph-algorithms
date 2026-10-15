package tests;

import graphs.*;
import utils.GraphGenerator;
import java.io.*;
import java.util.*;

public class StressTests {

    private static final String DATA_DIR = "inputs/test/";

    public static void main(String[] args) {
        System.out.println("=== STARTING STRESS TEST SUITE ===");
        System.out.println("Generating data files...");

        // 1. Large Bipartite for Matching (2k vertices, 10k edges)
        GraphGenerator.generateAndSaveBipartite("stress_bipartite.txt", 1000, 1000, 10000);

        // 2. Large Digraph for SCC (5k vertices, 20k edges)
        GraphGenerator.generateAndSaveDigraph("stress_digraph.txt", 5000, 20000);

        // 3. Large Undirected for Bridges/Cycles (5k vertices, 20k edges)
        GraphGenerator.generateAndSaveUndirected("stress_undirected.txt", 5000, 20000);

        // 4. Large Weighted Undirected for MST & Dijkstra (5k vertices, 20k edges)
        GraphGenerator.generateAndSaveWeightedUndirected("stress_weighted_undirected.txt", 5000, 20000);

        // 5. Large Flow Network (500 vertices, 3000 edges)
        GraphGenerator.generateAndSaveFlowNetwork("stress_flow_network.txt", 500, 3000);

        System.out.println("Data generation complete.\n");

        runTest("Stress: Max Matching & Cover (Hopcroft-Karp)", StressTests::testMatchingStress);
        runTest("Stress: Strongly Connected Components (Kosaraju/Tarjan)", StressTests::testSCCStress);
        runTest("Stress: Undirected Connectivity & Bridges", StressTests::testUndirectedStress);
        runTest("Stress: Minimum Spanning Tree & Dijkstra (Kruskal/Prim)", StressTests::testWeightedStress);
        runTest("Stress: Max Flow & Min Cut (Edmonds-Karp vs Ford-Fulkerson)", StressTests::testFlowStress);
        runTest("Stress: General Matching & Edge Cover (Edmonds Blossom & Gallai)", StressTests::testBlossomStress);
        runTest("Stress: Blossom Differential Fuzzing vs Exact Bitmask-DP (500 graphs & gadgets)", StressTests::testBlossomDifferentialFuzz);

        System.out.println("\n✅ STRESS TESTS COMPLETED");
    }

    private static void testBlossomStress() {
        UndirectedGraph g = loadUndirectedGraph("stress_bipartite.txt");
        System.out.println("   [Graph Order: " + g.order() + ", Size: " + g.size() + "]");

        long start = System.currentTimeMillis();
        Set<UndirectedGraph.Edge> matchingBlossom = g.maxMatchingBlossom();
        long tMatch = System.currentTimeMillis() - start;
        System.out.println("   MaxMatchingBlossom Time: " + tMatch + "ms (Size: " + matchingBlossom.size() + ")");

        Set<UndirectedGraph.Edge> matchingHK = g.maxMatching();
        if (matchingBlossom.size() != matchingHK.size()) {
            throw new RuntimeException("Blossom and Hopcroft-Karp size mismatch: Blossom="
                    + matchingBlossom.size() + ", HK=" + matchingHK.size());
        }

        // Test on non-bipartite dense / random graph
        UndirectedGraph<Integer> nonBip = new UndirectedGraph<>();
        int n = 500;
        Random rng = new Random(42);
        for (int i = 0; i < n; i++) {
            nonBip.addVertex(i);
        }
        for (int i = 0; i < 2500; i++) {
            int u = rng.nextInt(n);
            int v = rng.nextInt(n);
            if (u != v) {
                nonBip.addEdge(u, v);
            }
        }
        // Ensure connected / no isolated vertices
        for (int i = 0; i < n; i++) {
            if (nonBip.degreeOf(i) == 0) {
                nonBip.addEdge(i, (i + 1) % n);
            }
        }
        start = System.currentTimeMillis();
        Set<UndirectedGraph.Edge<Integer>> genMatch = nonBip.maxMatchingBlossom();
        long tGen = System.currentTimeMillis() - start;
        System.out.println("   General Non-Bipartite Blossom Time (500 vertices, ~2500 edges): " + tGen + "ms (Matching: " + genMatch.size() + ")");

        start = System.currentTimeMillis();
        Set<UndirectedGraph.Edge<Integer>> edgeCover = nonBip.minEdgeCover(genMatch);
        long tCover = System.currentTimeMillis() - start;
        System.out.println("   MinEdgeCover Time: " + tCover + "ms (Cover: " + edgeCover.size() + ")");

        if (genMatch.size() + edgeCover.size() != nonBip.order()) {
            throw new RuntimeException("Gallai Identity Failed: |M|=" + genMatch.size()
                    + ", |EdgeCover|=" + edgeCover.size() + ", |V|=" + nonBip.order());
        }
    }

    private static void testBlossomDifferentialFuzz() {
        Random rng = new Random(1337);
        int totalGraphs = 500;
        int maxVertices = 12;

        long start = System.currentTimeMillis();
        for (int test = 0; test < totalGraphs; test++) {
            int n = 2 + rng.nextInt(maxVertices - 1); // 2 to 12
            double p = 0.05 + rng.nextDouble() * 0.90; // density 5% to 95%

            UndirectedGraph<Integer> g = new UndirectedGraph<>();
            for (int i = 0; i < n; i++) {
                g.addVertex(i);
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (rng.nextDouble() < p) {
                        g.addEdge(i, j);
                    }
                }
            }

            // Occasionally add self-loops (15% chance per vertex)
            for (int i = 0; i < n; i++) {
                if (rng.nextDouble() < 0.15) {
                    g.addEdge(i, i);
                }
            }

            Set<UndirectedGraph.Edge<Integer>> blossomMatch = g.maxMatchingBlossom();
            Set<UndirectedGraph.Edge<Integer>> generalMatch = g.maxMatchingGeneral();

            if (blossomMatch.size() != generalMatch.size()) {
                throw new RuntimeException("Fuzz Test #" + test + ": maxMatchingBlossom size "
                        + blossomMatch.size() + " != maxMatchingGeneral size " + generalMatch.size());
            }

            // Verify matching validity: no vertex repeated, every edge exists in g, no self-loops
            Set<Graph.Vertex<Integer>> matchedVertices = new HashSet<>();
            for (UndirectedGraph.Edge<Integer> e : blossomMatch) {
                if (e.from.equals(e.to)) {
                    throw new RuntimeException("Fuzz Test #" + test + ": Matching contains self-loop " + e);
                }
                if (!g.getEdges().contains(e)) {
                    throw new RuntimeException("Fuzz Test #" + test + ": Matched edge " + e + " not in graph");
                }
                if (matchedVertices.contains(e.from) || matchedVertices.contains(e.to)) {
                    throw new RuntimeException("Fuzz Test #" + test + ": Vertex matched more than once in " + blossomMatch);
                }
                matchedVertices.add(e.from);
                matchedVertices.add(e.to);
            }

            // Differential comparison against exact Bitmask-DP
            int exactMatch = bruteForceMaxMatchingBitmask(g);
            if (blossomMatch.size() != exactMatch) {
                throw new RuntimeException("Fuzz Test #" + test + " (n=" + n + ", p=" + p + "): Blossom size "
                        + blossomMatch.size() + " != Exact Bitmask-DP size " + exactMatch);
            }

            // Check Min Edge Cover & Gallai Identity
            boolean hasIsolated = false;
            for (Graph.Vertex<Integer> v : g.getVertices()) {
                if (g.degreeOf(v) == 0) {
                    hasIsolated = true;
                    break;
                }
            }

            Set<UndirectedGraph.Edge<Integer>> cover = g.minEdgeCover();
            Set<UndirectedGraph.Edge<Integer>> coverFromMatch = g.minEdgeCover(blossomMatch);

            if (hasIsolated) {
                if (cover != null || coverFromMatch != null) {
                    throw new RuntimeException("Fuzz Test #" + test + ": Edge cover should be null for graph with isolated vertices");
                }
            } else {
                if (cover == null || coverFromMatch == null) {
                    throw new RuntimeException("Fuzz Test #" + test + ": Edge cover is null for graph without isolated vertices");
                }
                if (cover.size() != coverFromMatch.size()) {
                    throw new RuntimeException("Fuzz Test #" + test + ": minEdgeCover() size " + cover.size()
                            + " != minEdgeCover(match) size " + coverFromMatch.size());
                }
                if (blossomMatch.size() + cover.size() != g.order()) {
                    throw new RuntimeException("Fuzz Test #" + test + ": Gallai Identity failed: |M|="
                            + blossomMatch.size() + " + |Cover|=" + cover.size() + " != |V|=" + g.order());
                }
                // Verify cover covers every vertex
                Set<Graph.Vertex<Integer>> covered = new HashSet<>();
                for (UndirectedGraph.Edge<Integer> e : cover) {
                    if (!g.getEdges().contains(e)) {
                        throw new RuntimeException("Fuzz Test #" + test + ": Cover edge " + e + " not in graph");
                    }
                    covered.add(e.from);
                    covered.add(e.to);
                }
                for (Graph.Vertex<Integer> v : g.getVertices()) {
                    if (!covered.contains(v)) {
                        throw new RuntimeException("Fuzz Test #" + test + ": Vertex " + v + " not covered by edge cover");
                    }
                }
            }
        }

        // Test Grötzsch Graph (11 vertices, 20 edges, chromatic number 4, triangle-free)
        UndirectedGraph<Integer> grotzsch = new UndirectedGraph<>();
        // 5-cycle: 0-1-2-3-4-0
        for (int i = 0; i < 5; i++) grotzsch.addEdge(i, (i + 1) % 5);
        // Hub 10 connected to 5,6,7,8,9
        for (int i = 5; i <= 9; i++) grotzsch.addEdge(10, i);
        // Spokes
        grotzsch.addEdge(5, 1); grotzsch.addEdge(5, 4);
        grotzsch.addEdge(6, 0); grotzsch.addEdge(6, 2);
        grotzsch.addEdge(7, 1); grotzsch.addEdge(7, 3);
        grotzsch.addEdge(8, 2); grotzsch.addEdge(8, 4);
        grotzsch.addEdge(9, 3); grotzsch.addEdge(9, 0);

        int grotzschExact = bruteForceMaxMatchingBitmask(grotzsch);
        Set<UndirectedGraph.Edge<Integer>> grotzschMatch = grotzsch.maxMatchingBlossom();
        if (grotzschMatch.size() != grotzschExact || grotzschExact != 5) {
            throw new RuntimeException("Grötzsch Graph matching failed: Blossom=" + grotzschMatch.size()
                    + ", Exact=" + grotzschExact + " (expected 5)");
        }
        Set<UndirectedGraph.Edge<Integer>> grotzschCover = grotzsch.minEdgeCover();
        if (grotzschCover.size() + grotzschMatch.size() != grotzsch.order()) {
            throw new RuntimeException("Grötzsch Graph Gallai identity failed: 5 + " + grotzschCover.size() + " != 11");
        }

        // Test Multi-Level Nested Blossoms Gadget
        // Stem: 0-1
        // Blossom 1: 1-2, 2-3, 3-1
        // Blossom 2: 3-4, 4-5, 5-3
        // Blossom 3: 5-6, 6-7, 7-5
        // Tail: 7-8
        UndirectedGraph<Integer> deepNested = new UndirectedGraph<>();
        deepNested.addEdge(0, 1);
        deepNested.addEdge(1, 2); deepNested.addEdge(2, 3); deepNested.addEdge(3, 1);
        deepNested.addEdge(3, 4); deepNested.addEdge(4, 5); deepNested.addEdge(5, 3);
        deepNested.addEdge(5, 6); deepNested.addEdge(6, 7); deepNested.addEdge(7, 5);
        deepNested.addEdge(7, 8);

        int deepExact = bruteForceMaxMatchingBitmask(deepNested);
        Set<UndirectedGraph.Edge<Integer>> deepMatch = deepNested.maxMatchingBlossom();
        if (deepMatch.size() != deepExact || deepExact != 4) {
            throw new RuntimeException("Deep Nested Blossoms matching failed: Blossom=" + deepMatch.size()
                    + ", Exact=" + deepExact + " (expected 4)");
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("   Differential Fuzzing Complete: 500 random graphs + Grötzsch + Deep Nested in " + elapsed + "ms");
    }

    private static int bruteForceMaxMatchingBitmask(UndirectedGraph<Integer> g) {
        List<Graph.Vertex<Integer>> vList = new ArrayList<>(g.getVertices());
        int n = vList.size();
        if (n <= 1) return 0;
        Map<Graph.Vertex<Integer>, Integer> vToIdx = new HashMap<>();
        for (int i = 0; i < n; i++) vToIdx.put(vList.get(i), i);
        int[] adjMask = new int[n];
        for (Graph.Edge<Integer> e : g.getEdges()) {
            if (!e.from.equals(e.to)) {
                int u = vToIdx.get(e.from);
                int v = vToIdx.get(e.to);
                adjMask[u] |= (1 << v);
                adjMask[v] |= (1 << u);
            }
        }
        int[] memo = new int[1 << n];
        Arrays.fill(memo, -1);
        return solveBitmaskDP(0, n, adjMask, memo);
    }

    private static int solveBitmaskDP(int mask, int n, int[] adjMask, int[] memo) {
        if (mask == (1 << n) - 1) return 0;
        if (memo[mask] != -1) return memo[mask];
        int u = -1;
        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) == 0) {
                u = i;
                break;
            }
        }
        if (u == -1) return 0;
        int best = solveBitmaskDP(mask | (1 << u), n, adjMask, memo);
        int candidates = adjMask[u] & ~mask;
        while (candidates != 0) {
            int v = Integer.numberOfTrailingZeros(candidates);
            best = Math.max(best, 1 + solveBitmaskDP(mask | (1 << u) | (1 << v), n, adjMask, memo));
            candidates &= candidates - 1;
        }
        memo[mask] = best;
        return best;
    }

    private static void testMatchingStress() {
        UndirectedGraph g = loadUndirectedGraph("stress_bipartite.txt");
        System.out.println("   [Graph Order: " + g.order() + ", Size: " + g.size() + "]");

        long start = System.currentTimeMillis();
        Set<UndirectedGraph.Edge> matching = g.maxMatching();
        long tMatch = System.currentTimeMillis() - start;
        System.out.println("   MaxMatching Time: " + tMatch + "ms");

        start = System.currentTimeMillis();
        Set<Graph.Vertex> cover = g.minVertexCover();
        long tCover = System.currentTimeMillis() - start;
        System.out.println("   MinVertexCover Time: " + tCover + "ms");

        start = System.currentTimeMillis();
        Set<Graph.Vertex> independent = g.maxIndependentSet();
        long tIndep = System.currentTimeMillis() - start;
        System.out.println("   MaxIndependentSet Time: " + tIndep + "ms");

        // Verification
        if (matching.size() != cover.size())
            throw new RuntimeException("König's Theorem Failed: |M|=" + matching.size() + ", |VC|=" + cover.size());

        if (cover.size() + independent.size() != g.order()) {
            // Note: Independent Set only considers vertices in the specific graph context
            // but here our 'random bipartite' generates disconnected components
            // potentially.
            // However, Gallai's Identity alpha + beta = n holds for ANY graph.
            throw new RuntimeException("Gallai Identity Failed: |VC|=" + cover.size() + ", |IS|=" + independent.size()
                    + ", |V|=" + g.order());
        }
    }

    private static void testSCCStress() {
        Digraph g = loadDigraph("stress_digraph.txt");
        System.out.println("   [Graph Order: " + g.order() + ", Size: " + g.size() + "]");

        long start = System.currentTimeMillis();
        Set<Set<Graph.Vertex>> sccKS = g.stronglyConnectedComponentsKS();
        long tKS = System.currentTimeMillis() - start;
        System.out.println("   Kosaraju Time: " + tKS + "ms");

        start = System.currentTimeMillis();
        Set<Set<Graph.Vertex>> sccT = g.stronglyConnectedComponentsT();
        long tT = System.currentTimeMillis() - start;
        System.out.println("   Tarjan Time: " + tT + "ms");

        if (sccKS.size() != sccT.size())
            throw new RuntimeException("SCC Mismatch: Kosaraju=" + sccKS.size() + ", Tarjan=" + sccT.size());
    }

    private static void testUndirectedStress() {
        UndirectedGraph g = loadUndirectedGraph("stress_undirected.txt");
        System.out.println("   [Graph Order: " + g.order() + ", Size: " + g.size() + "]");

        long start = System.currentTimeMillis();
        Set<UndirectedGraph.Edge> bridges = g.bridges();
        long tBridge = System.currentTimeMillis() - start;
        System.out.println("   Bridges Time: " + tBridge + "ms (" + bridges.size() + " bridges)");

        start = System.currentTimeMillis();
        Set<Graph.Vertex> aps = g.articulationPoints();
        long tAP = System.currentTimeMillis() - start;
        System.out.println("   Articulation Points Time: " + tAP + "ms (" + aps.size() + " points)");
    }

    private static void testWeightedStress() {
        WeightedUndirectedGraph<String> g = loadWeightedUndirectedGraph("stress_weighted_undirected.txt");
        System.out.println("   [Graph Order: " + g.order() + ", Size: " + g.size() + "]");

        long start = System.currentTimeMillis();
        double wKruskal = g.kruskalWeight();
        long tKruskal = System.currentTimeMillis() - start;
        System.out.println("   Kruskal MST Time: " + tKruskal + "ms (Weight: " + wKruskal + ")");

        start = System.currentTimeMillis();
        double wPrim = g.primWeight();
        long tPrim = System.currentTimeMillis() - start;
        System.out.println("   Prim MST Time: " + tPrim + "ms (Weight: " + wPrim + ")");

        if (Math.abs(wKruskal - wPrim) > 1e-4) {
            throw new RuntimeException("MST Weight Mismatch: Kruskal=" + wKruskal + ", Prim=" + wPrim);
        }

        start = System.currentTimeMillis();
        Map<Graph.Vertex<String>, Double> dijkstra = g.dijkstraDistances("1");
        long tDijkstra = System.currentTimeMillis() - start;
        System.out.println("   Dijkstra Time (from '1'): " + tDijkstra + "ms (Reached: " + dijkstra.size() + " nodes)");
    }

    private static void testFlowStress() {
        WeightedDigraph<String> net = loadWeightedDigraph("stress_flow_network.txt");
        System.out.println("   [Network Order: " + net.order() + ", Size: " + net.size() + "]");

        String source = "1";
        String sink = String.valueOf(net.order());

        long start = System.currentTimeMillis();
        double ekFlow = net.edmondsKarpMaxFlow(source, sink);
        long tEK = System.currentTimeMillis() - start;
        System.out.println("   Edmonds-Karp Time: " + tEK + "ms (Max Flow: " + ekFlow + ")");

        start = System.currentTimeMillis();
        double ffFlow = net.fordFulkersonMaxFlow(source, sink);
        long tFF = System.currentTimeMillis() - start;
        System.out.println("   Ford-Fulkerson Time: " + tFF + "ms (Max Flow: " + ffFlow + ")");

        if (Math.abs(ekFlow - ffFlow) > 1e-4) {
            throw new RuntimeException("Flow Mismatch: EK=" + ekFlow + ", FF=" + ffFlow);
        }

        start = System.currentTimeMillis();
        double cutCap = net.minCutCapacity(source, sink);
        long tCut = System.currentTimeMillis() - start;
        System.out.println("   Min-Cut Time: " + tCut + "ms (Capacity: " + cutCap + ")");

        if (Math.abs(ekFlow - cutCap) > 1e-4) {
            throw new RuntimeException("Max-Flow Min-Cut Duality Failed: Flow=" + ekFlow + ", Cut=" + cutCap);
        }
    }

    // --- Helpers ---

    private static WeightedUndirectedGraph<String> loadWeightedUndirectedGraph(String filename) {
        WeightedUndirectedGraph<String> g = new WeightedUndirectedGraph<>();
        try (Scanner sc = new Scanner(new File(DATA_DIR + filename))) {
            while (sc.hasNext()) {
                String u = sc.next();
                if (sc.hasNext()) {
                    String v = sc.next();
                    double w = sc.hasNextDouble() ? sc.nextDouble() : 1.0;
                    g.addEdge(u, v, w);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return g;
    }

    private static WeightedDigraph<String> loadWeightedDigraph(String filename) {
        WeightedDigraph<String> g = new WeightedDigraph<>();
        try (Scanner sc = new Scanner(new File(DATA_DIR + filename))) {
            while (sc.hasNext()) {
                String u = sc.next();
                if (sc.hasNext()) {
                    String v = sc.next();
                    double w = sc.hasNextDouble() ? sc.nextDouble() : 1.0;
                    g.addEdge(u, v, w);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return g;
    }

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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
        return g;
    }

    private static void runTest(String name, Runnable test) {
        System.out.println("Running " + name + "...");
        try {
            test.run();
        } catch (Throwable e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("PASSED\n");
    }
}
