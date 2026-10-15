package utils;

import graphs.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Builders {

    public static UndirectedGraph<String> undirectedGraphBuilder(String filePath) {
        try {
            return undirectedGraphBuilder(new File(filePath));
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filePath);
            return new UndirectedGraph<>();
        }
    }

    public static UndirectedGraph<String> undirectedGraphBuilder(File file) throws FileNotFoundException {
        UndirectedGraph<String> graph = new UndirectedGraph<>();

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty())
                    continue;
                String[] tokens = line.split("\\s+");
                if (tokens.length >= 2) {
                    graph.addEdge(tokens[0], tokens[1]);
                } else if (tokens.length == 1) {
                    graph.addVertex(tokens[0]);
                }
            }
        }
        return graph;
    }

    public static Digraph<String> digraphBuilder(String filePath) {
        try {
            return digraphBuilder(new File(filePath));
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filePath);
            return new Digraph<>();
        }
    }

    public static Digraph<String> digraphBuilder(File file) throws FileNotFoundException {
        Digraph<String> graph = new Digraph<>();

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty())
                    continue;
                String[] tokens = line.split("\\s+");
                if (tokens.length >= 2) {
                    graph.addEdge(tokens[0], tokens[1]);
                } else if (tokens.length == 1) {
                    graph.addVertex(tokens[0]);
                }
            }
        }
        return graph;
    }

    public static UndirectedGraph<Integer> undirectedIntegerGraphBuilder(String filePath) {
        try (Scanner sc = new Scanner(new File(filePath))) {
            UndirectedGraph<Integer> graph = new UndirectedGraph<>();
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty())
                    continue;
                String[] tokens = line.split("\\s+");
                if (tokens.length >= 2) {
                    graph.addEdge(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                } else if (tokens.length == 1) {
                    graph.addVertex(Integer.parseInt(tokens[0]));
                }
            }
            return graph;
        } catch (Exception e) {
            System.err.println("Error reading integer graph: " + e.getMessage());
            return new UndirectedGraph<>();
        }
    }

    public static Digraph<Integer> digraphIntegerBuilder(String filePath) {
        try (Scanner sc = new Scanner(new File(filePath))) {
            Digraph<Integer> graph = new Digraph<>();
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty())
                    continue;
                String[] tokens = line.split("\\s+");
                if (tokens.length >= 2) {
                    graph.addEdge(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
                } else if (tokens.length == 1) {
                    graph.addVertex(Integer.parseInt(tokens[0]));
                }
            }
            return graph;
        } catch (Exception e) {
            System.err.println("Error reading integer digraph: " + e.getMessage());
            return new Digraph<>();
        }
    }
}
