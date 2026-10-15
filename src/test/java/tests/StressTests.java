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

        System.out.println("Data generation complete.\n");

        runTest("Stress: Max Matching & Cover (Hopcroft-Karp)", StressTests::testMatchingStress);
        runTest("Stress: Strongly Connected Components (Kosaraju/Tarjan)", StressTests::testSCCStress);
        runTest("Stress: Undirected Connectivity & Bridges", StressTests::testUndirectedStress);

        System.out.println("\n✅ STRESS TESTS COMPLETED");
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

    // --- Helpers ---

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
