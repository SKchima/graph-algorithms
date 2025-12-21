package utils;

import java.io.*;
import java.util.*;

public class GraphGenerator {

    private static final String DATA_DIR = "inputs/test/";

    public static void generateAndSaveUndirected(String filename, int V, int E) {
        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_DIR + filename))) {
            Random rand = new Random();
            Set<String> edges = new HashSet<>();
            int generatedEdges = 0;

            // Ensure connectivity (spanning tree)
            for (int i = 2; i <= V; i++) {
                int u = rand.nextInt(i - 1) + 1; // Connect current node to a previous node
                out.println(i + " " + u);
                generatedEdges++;
            }

            // Fill remaining edges
            while (generatedEdges < E) {
                int u = rand.nextInt(V) + 1;
                int v = rand.nextInt(V) + 1;
                if (u == v)
                    continue;

                String key = u < v ? u + "-" + v : v + "-" + u;
                if (!edges.contains(key)) {
                    edges.add(key);
                    out.println(u + " " + v);
                    generatedEdges++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateAndSaveBipartite(String filename, int nX, int nY, int E) {
        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_DIR + filename))) {
            Random rand = new Random();
            Set<String> edges = new HashSet<>();
            int generatedEdges = 0;

            while (generatedEdges < E) {
                int u = rand.nextInt(nX) + 1; // 1 to nX
                int v = rand.nextInt(nY) + 1 + nX; // nX+1 to nX+nY

                String key = u + "-" + v;
                if (!edges.contains(key)) {
                    edges.add(key);
                    out.println(u + " " + v);
                    generatedEdges++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateAndSaveDigraph(String filename, int V, int E) {
        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_DIR + filename))) {
            Random rand = new Random();
            Set<String> edges = new HashSet<>();
            int generatedEdges = 0;

            // Ensure some connectivity/structure if needed, but random is fine for stress
            while (generatedEdges < E) {
                int u = rand.nextInt(V) + 1;
                int v = rand.nextInt(V) + 1;
                if (u == v)
                    continue;

                String key = u + "->" + v;
                if (!edges.contains(key)) {
                    edges.add(key);
                    out.println(u + " " + v);
                    generatedEdges++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
