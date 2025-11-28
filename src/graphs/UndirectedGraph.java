package graphs;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UndirectedGraph extends Graph {

    public static class Edge extends Graph.Edge {
        public Edge(Vertex from, Vertex to) {
            super(from, to);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) return false;
            Edge other = (Edge) o;

            return (Objects.equals(this.from, other.from) && Objects.equals(this.to, other.to))
                    || (Objects.equals(this.from, other.to) && Objects.equals(this.to, other.from));
        }

        @Override
        public int hashCode() {
            return from.hashCode() ^ to.hashCode();
        }
    }

    @Override
    public void removeVertex(String id) {
        Vertex v = getVertexById(id);
        if (v == null) return;

        for (Vertex neighbor : adjacencyMap.get(v)) {
            adjacencyMap.get(neighbor).remove(v);
        }
        this.vertices.remove(v);
        this.adjacencyMap.remove(v);
        this.edges.removeIf(e -> e.from.equals(v) || e.to.equals(v));
    }

    @Override
    public void addEdge(Vertex from, Vertex to) {
        this.addVertex(from);
        this.addVertex(to);
        Edge edge = new Edge(from, to);
        if (!this.edges.add(edge)) return;
        this.adjacencyMap.get(from).add(to);
        this.adjacencyMap.get(to).add(from);
    }

    @Override
    public void removeEdge(String from, String to) {
        Vertex vFrom = getVertexById(from);
        Vertex vTo = getVertexById(to);
        if (vFrom != null && vTo != null && this.edges.remove(new Edge(vFrom, vTo))) {
            this.adjacencyMap.get(vFrom).remove(vTo);
            this.adjacencyMap.get(vTo).remove(vFrom);
        }
    }

    public boolean hasCycles() {
        Set<Vertex> visited = new HashSet<>();
        for (Vertex v : this.vertices) {
            if (!visited.contains(v)) {
                if (hasCyclesR(visited, v, null)) return true;
            }
        }
        return false;
    }

    private boolean hasCyclesR(Set<Vertex> visited, Vertex current, Vertex previous) {
        visited.add(current);
        for (Vertex v : this.adjacencyMap.get(current)) {
            if (v == previous) continue;
            if (visited.contains(v)) return true;
            if (hasCyclesR(visited, v, current)) return true;
        }
        return false;
    }

    @Override
    public UndirectedGraph treeBFS(Vertex root) {
        return null;
    }
}
