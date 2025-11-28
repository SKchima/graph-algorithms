package graphs;

import java.util.*;

public abstract class Graph {

    public record Vertex(String id) {
        @Override
        public String toString() {
            return id;
        }
    }

    public static class Edge {

        public final Vertex from;
        public final Vertex to;

        Edge(Vertex from, Vertex to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) return false;
            Edge other = (Edge) o;
            return Objects.equals(this.from, other.from) && Objects.equals(this.to, other.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }

        @Override
        public String toString() {
            return "(" + from.toString() + ", " + to.toString() + ")";
        }
    }


    // ------------------------------ ATTRIBUTES ------------------------------ //

    public final Set<Vertex> vertices = new HashSet<>();
    public final Map<String, Vertex> vertexMap = new HashMap<>();
    public final Set<Edge> edges = new HashSet<>();
    public final Map<Vertex, Set<Vertex>> adjacencyMap = new HashMap<>();


    // ------------------------------ COMMON METHODS ------------------------------ //

    public Vertex getVertexById(String id) {
        return vertexMap.get(id);
    }

    public Vertex addVertex(String id) {
        if (getVertexById(id) != null) return getVertexById(id);
        Vertex v = new Vertex(id);
        this.vertices.add(v);
        this.vertexMap.put(id, v);
        this.adjacencyMap.put(v, new HashSet<>());
        return v;
    }

    public void addVertex(Vertex v) {
        if (getVertexById(v.id) != null) return;
        this.vertices.add(v);
        this.vertexMap.put(v.id, v);
        this.adjacencyMap.put(v, new HashSet<>());
    }


    // ------------------------------ ABSTRACT METHODS ------------------------------ //

    public abstract void removeVertex(String id);

    public abstract void addEdge(Vertex from, Vertex to);

    public abstract void removeEdge(String from, String to);

    // ------------------------------ AUXILIARY METHODS ------------------------------ //

    public int order() {
        return vertices.size();
    }

    public int size() {
        return edges.size();
    }

    public int degreeOf(Vertex v) {
        if (!vertices.contains(v)) throw new IllegalArgumentException();
        return this.adjacencyMap.get(v).size();
    }

    public int minDegree() {
        if (vertices.isEmpty()) return 0;
        int minimum = Integer.MAX_VALUE;
        for (Vertex v : this.vertices) {
            minimum = Math.min(minimum, adjacencyMap.get(v).size());
        }
        return minimum;
    }

    public int maxDegree() {
        if (vertices.isEmpty()) return 0;
        int maximum = Integer.MIN_VALUE;
        for (Vertex v : this.vertices) {
            maximum = Math.max(maximum, adjacencyMap.get(v).size());
        }
        return maximum;
    }


    // ------------------------------ COMMON ALGORITHMS ------------------------------ //

    public int distanceBFS(Vertex from, Vertex to) {
        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from);

        int distance = 0;
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Vertex current = queue.poll();
                if (current.equals(to)) return distance;
                for (Vertex neighbor : this.adjacencyMap.get(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            distance++;
        }
        return -1;
    }

    public Map<Vertex, Integer> distancesBFS(Vertex from) {
        Map<Vertex, Integer> distances = new HashMap<>();
        distances.put(from, 0);

        Queue<Vertex> queue = new LinkedList<>();
        queue.add(from);

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            for (Vertex neighbor : this.adjacencyMap.get(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, distances.get(current) + 1);
                    queue.add(neighbor);
                }
            }
        }
        return distances;
    }

/*
    public boolean isTree() {
        return (this.vertices.size() - 1 == this.edges.size() && !this.hasCycles());
    }
*/
    // ------------------------------ ABSTRACT ALGORITHMS ------------------------------ //


    public abstract boolean hasCycles();

    public abstract Graph treeBFS(Vertex root);
}