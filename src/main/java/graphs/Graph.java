package graphs;

import java.util.*;

public abstract class Graph<V> {

    public record Vertex<V>(V id) {
        public Vertex {
            Objects.requireNonNull(id, "Vertex id cannot be null");
        }

        @Override
        public String toString() {
            return String.valueOf(id);
        }
    }

    public static class Edge<V> {
        public final Vertex<V> from;
        public final Vertex<V> to;

        public Edge(Vertex<V> from, Vertex<V> to) {
            this.from = Objects.requireNonNull(from, "from vertex cannot be null");
            this.to = Objects.requireNonNull(to, "to vertex cannot be null");
        }

        public Vertex<V> from() {
            return from;
        }

        public Vertex<V> to() {
            return to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge<?> edge = (Edge<?>) o;
            return Objects.equals(from, edge.from) && Objects.equals(to, edge.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }

        @Override
        public String toString() {
            return "(" + from + ", " + to + ")";
        }
    }

    // -------------------------- ATTRIBUTES ----------------- //

    protected final Set<Vertex<V>> vertices = new HashSet<>();
    protected final Map<V, Vertex<V>> vertexMap = new HashMap<>();
    protected final Set<Edge<V>> edges = new HashSet<>();
    protected final Map<Vertex<V>, Set<Vertex<V>>> adjacencyMap = new HashMap<>();

    public Set<Vertex<V>> getVertices() {
        return Collections.unmodifiableSet(this.vertices);
    }

    public Set<Edge<V>> getEdges() {
        return Collections.unmodifiableSet(this.edges);
    }

    public Set<Vertex<V>> getNeighbors(Vertex<V> v) {
        Set<Vertex<V>> neighbors = this.adjacencyMap.get(v);
        return neighbors != null ? Collections.unmodifiableSet(neighbors) : Collections.emptySet();
    }

    public Set<Vertex<V>> getNeighbors(V id) {
        Vertex<V> v = getVertexById(id);
        return v != null ? getNeighbors(v) : Collections.emptySet();
    }

    // -------------------------- COMMON METHODS ------------- //

    public Vertex<V> getVertexById(V id) {
        return this.vertexMap.get(id);
    }

    public Vertex<V> addVertex(V id) {
        if (id == null) {
            throw new IllegalArgumentException("Vertex id cannot be null");
        }
        Vertex<V> existing = getVertexById(id);
        if (existing != null)
            return existing;
        Vertex<V> v = new Vertex<>(id);
        this.vertices.add(v);
        this.vertexMap.put(id, v);
        this.adjacencyMap.put(v, new HashSet<>());
        return v;
    }

    public void addVertex(Vertex<V> v) {
        if (v == null || v.id() == null) {
            throw new IllegalArgumentException("Vertex cannot be null");
        }
        if (getVertexById(v.id()) != null)
            return;
        this.vertices.add(v);
        this.vertexMap.put(v.id(), v);
        this.adjacencyMap.put(v, new HashSet<>());
    }

    // -------------------------- ABSTRACT METHODS ----------- //

    public abstract void removeVertex(V id);

    public abstract void addEdge(Vertex<V> from, Vertex<V> to);

    public void addEdge(V fromId, V toId) {
        addEdge(addVertex(fromId), addVertex(toId));
    }

    public abstract void removeEdge(V from, V to);

    // -------------------------- AUXILIARY METHODS ---------- //

    public int order() {
        return this.vertices.size();
    }

    public int size() {
        return this.edges.size();
    }

    public int degreeOf(Vertex<V> v) {
        if (!this.vertices.contains(v))
            throw new IllegalArgumentException("Vertex not found in graph: " + v);
        return this.adjacencyMap.get(v).size();
    }

    public int degreeOf(V id) {
        Vertex<V> v = getVertexById(id);
        if (v == null) {
            throw new IllegalArgumentException("Vertex not found in graph: " + id);
        }
        return degreeOf(v);
    }

    public int minDegree() {
        if (this.vertices.isEmpty())
            return 0;
        int minimum = Integer.MAX_VALUE;
        for (Vertex<V> v : this.vertices) {
            minimum = Math.min(minimum, this.adjacencyMap.get(v).size());
        }
        return minimum;
    }

    public int maxDegree() {
        if (this.vertices.isEmpty())
            return 0;
        int maximum = Integer.MIN_VALUE;
        for (Vertex<V> v : this.vertices) {
            maximum = Math.max(maximum, this.adjacencyMap.get(v).size());
        }
        return maximum;
    }

    // -------------------------- COMMON ALGORITHMS ---------- //

    /**
     * Algorithm: Breadth-First Search
     * Complexity: O(V + E) time, O(V) space
     */
    public int distanceBFS(Vertex<V> from, Vertex<V> to) {
        if (!this.vertices.contains(from) || !this.vertices.contains(to))
            return -1;
        Set<Vertex<V>> visited = new HashSet<>();
        Deque<Vertex<V>> queue = new ArrayDeque<>();
        queue.add(from);
        visited.add(from);

        int distance = 0;
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Vertex<V> current = queue.poll();
                if (current.equals(to))
                    return distance;
                for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
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

    public int distanceBFS(V fromId, V toId) {
        return distanceBFS(getVertexById(fromId), getVertexById(toId));
    }

    /**
     * Algorithm: Breadth-First Search
     * Complexity: Θ(V + E) time, O(V) space
     */
    public Map<Vertex<V>, Integer> distancesBFS(Vertex<V> from) {
        Map<Vertex<V>, Integer> distances = new HashMap<>();
        if (!this.vertices.contains(from))
            return distances;
        distances.put(from, 0);

        Deque<Vertex<V>> queue = new ArrayDeque<>();
        queue.add(from);

        while (!queue.isEmpty()) {
            Vertex<V> current = queue.poll();
            for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, distances.get(current) + 1);
                    queue.add(neighbor);
                }
            }
        }
        return distances;
    }

    // -------------------------- ABSTRACT ALGORITHMS -------- //

    public abstract boolean hasCycles();

    public abstract boolean isTree();

    public abstract Graph<V> treeBFS(Vertex<V> root);
}