package graphs;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Builders {
    public static UndirectedGraph undirectedGraphBuilder(File file) throws FileNotFoundException {
        UndirectedGraph graph = new UndirectedGraph();

        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] tokens = line.split(" ");
            if (tokens.length == 2) {
                String from = tokens[0];
                String to = tokens[1];
                graph.addEdge(new Graph.Vertex(from), new Graph.Vertex(to));
            } else {
                String vertex = tokens[0];
                graph.addVertex(vertex);
            }
        }
        sc.close();

        return graph;
    }

    public Digraph digraphBuilder(File file) throws FileNotFoundException {
        Digraph graph = new Digraph();

        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] tokens = line.split(" ");
            if (tokens.length == 2) {
                String from = tokens[0];
                String to = tokens[1];
                graph.addEdge(new Graph.Vertex(from), new Graph.Vertex(to));
            } else {
                String vertex = tokens[0];
                graph.addVertex(vertex);
            }
        }
        sc.close();

        return graph;
    }

/*
    public MultiGraph multiGraphBuilder(File file) throws FileNotFoundException {
        MultiGraph graph = new MultiGraph();

        return graph;
    }

    public WeightedGraph weightedGraphBuilder(File file) throws FileNotFoundException {
        WeightedGraph graph = new WeightedGraph();

        return graph;
    }
*/
}
