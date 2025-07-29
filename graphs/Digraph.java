package graphs;
import java.util.*;

public class Digraph extends Graph {

    @Override
    public void removeVertex(String id) {
        Vertex v = getVertexById(id);
        if (v == null) return;

        for (Set<Vertex> neighbors : adjacencyMap.values()) {
            neighbors.remove(v);
        }
        this.vertices.remove(v);
        this.adjacencyMap.remove(v);
        this.edges.removeIf(e -> e.from().equals(id) || e.to().equals(id));
    }

    @Override
    public void addEdge(String from, String to) {
        Edge edge = new Edge(from, to);

        Vertex vFrom = this.addVertex(from);
        Vertex vTo = this.addVertex(to);
        if (this.edges.add(edge)) this.adjacencyMap.get(vFrom).add(vTo);
    }

    @Override
    public void removeEdge(String from, String to) {
        Vertex vFrom = getVertexById(from);
        Vertex vTo = getVertexById(to);
        if (vFrom != null && vTo != null && this.edges.remove(new Edge(from, to))) {
            this.adjacencyMap.get(vFrom).remove(vTo);
        }
    }
}
