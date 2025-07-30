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
        this.edges.removeIf(e -> e.from.equals(v) || e.to.equals(v));
    }

    @Override
    public void addEdge(Vertex from, Vertex to) {
        Edge edge = new Edge(from, to);

        this.addVertex(from);
        this.addVertex(to);
        if (this.edges.add(edge)) this.adjacencyMap.get(from).add(to);
    }

    @Override
    public void removeEdge(String from, String to) {
        Vertex vFrom = getVertexById(from);
        Vertex vTo = getVertexById(to);
        if (vFrom != null && vTo != null && this.edges.remove(new Edge(vFrom, vTo))) {
            this.adjacencyMap.get(vFrom).remove(vTo);
        }
    }

    @Override
    public Digraph treeBFS(Vertex root) {
        Digraph tree = new Digraph();
        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> queue = new LinkedList<>();

        queue.add(root);
        visited.add(root);

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            tree.addVertex(current);
            for (Vertex neighbor : this.adjacencyMap.get(current)) {
                if (!visited.contains(neighbor)) {
                    tree.addEdge(current, neighbor);
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return tree;
    }
}
