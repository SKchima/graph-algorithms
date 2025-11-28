import graphs.*;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
//        File file = new File(args[0]);
//        Graph graph = Builders.undirectedGraphBuilder(file);

        Digraph g = new Digraph();

        // Criando vértices
        Graph.Vertex A = g.addVertex("A");
        Graph.Vertex B = g.addVertex("B");
        Graph.Vertex C = g.addVertex("C");
        Graph.Vertex D = g.addVertex("D");
        Graph.Vertex E = g.addVertex("E");

        // Adicionando arestas
        g.addEdge(A, B);
        g.addEdge(B, C);
        g.addEdge(A, D);  // cruzamento
        g.addEdge(D, E);
        g.addEdge(C, E);  // cross edge que NÃO forma ciclo real

        // Teste de hasCycles()
        boolean hasCycle = g.hasCycles();
        System.out.println("O grafo tem ciclo? " + hasCycle);
    }
}
