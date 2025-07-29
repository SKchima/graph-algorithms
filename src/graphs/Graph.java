package graphs;
import java.util.*;

public abstract class Graph {

    public record Vertex(String id) {

        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) return false;
            Vertex other = (Vertex) o;
            return Objects.equals(this.id, other.id);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public record Edge(String from, String to) {

        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) return false;
            Edge other = (Edge) o;
            return Objects.equals(this.from, other.from) && Objects.equals(this.to, other.to);
        }

        @Override
        public String toString() {
            return "(" + from + ", " + to + ")";
        }
    }

    public final Set<Vertex> vertices = new HashSet<>();
    public final Set<Edge> edges = new HashSet<>();
    public final Map<Vertex, Set<Vertex>> adjacencyMap = new HashMap<>();

    public Vertex getVertexById(String id) {
        return vertices.stream().filter(v -> v.id().equals(id)).findFirst().orElse(null);
    }

    public Vertex addVertex(String id) {
        Vertex v = new Vertex(id);
        if (this.vertices.add(v)) {
            this.adjacencyMap.put(v, new HashSet<>());
        }
        return v;
    }

    public abstract void removeVertex(String id);

    public abstract void addEdge(String from, String to);

    public abstract void removeEdge(String from, String to);

    // ------------------------------ AUXILIARY METHODS ------------------------------ //

    public int getDegreeOf(Vertex v) {
        if (!vertices.contains(v)) throw new IllegalArgumentException();
        return this.adjacencyMap.get(v).size();
    }

    public int getMinDegree() {
        int minimum = Integer.MAX_VALUE;
        for (Vertex v : this.vertices) {
            minimum = Math.min(minimum, adjacencyMap.get(v).size());
        }
        return minimum;
    }

    public int getMaxDegree() {
        int maximum = Integer.MIN_VALUE;
        for (Vertex v : this.vertices) {
            maximum = Math.max(maximum, adjacencyMap.get(v).size());
        }
        return maximum;
    }
}