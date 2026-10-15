package graphs;

import java.util.*;

public class WeightedUndirectedGraph<V> extends UndirectedGraph<V> {

    public static class Edge<V> extends WeightedEdge<V> {
        public Edge(Vertex<V> from, Vertex<V> to, double weight) {
            super(from, to, weight);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge<?> other = (Edge<?>) o;
            boolean endpointsMatch = (Objects.equals(from, other.from) && Objects.equals(to, other.to))
                    || (Objects.equals(from, other.to) && Objects.equals(to, other.from));
            return endpointsMatch && Double.compare(weight, other.weight) == 0;
        }

        @Override
        public int hashCode() {
            return (from.hashCode() ^ to.hashCode()) + Double.hashCode(weight);
        }

        @Override
        public String toString() {
            return "(" + from + " -- " + to + " : " + weight + ")";
        }
    }

    protected final Map<Vertex<V>, Map<Vertex<V>, Double>> weightMap = new HashMap<>();

    public WeightedUndirectedGraph() {
        super();
    }

    public WeightedUndirectedGraph(WeightedUndirectedGraph<V> other) {
        for (Vertex<V> v : other.vertices) {
            this.addVertex(v.id());
        }
        for (Graph.Edge<V> e : other.edges) {
            double w = other.getWeight(e.from, e.to);
            this.addEdge(this.getVertexById(e.from.id()), this.getVertexById(e.to.id()), w);
        }
    }

    public WeightedUndirectedGraph(UndirectedGraph<V> other) {
        for (Vertex<V> v : other.vertices) {
            this.addVertex(v.id());
        }
        for (Graph.Edge<V> e : other.edges) {
            this.addEdge(this.getVertexById(e.from.id()), this.getVertexById(e.to.id()), 1.0);
        }
    }

    @Override
    public WeightedUndirectedGraph<V> copy() {
        return new WeightedUndirectedGraph<>(this);
    }

    // -------------------------- EDGE & WEIGHT OPERATIONS --- //

    @Override
    public void addEdge(Vertex<V> from, Vertex<V> to) {
        addEdge(from, to, 1.0);
    }

    public void addEdge(Vertex<V> from, Vertex<V> to, double weight) {
        this.addVertex(from);
        this.addVertex(to);

        // Update if already exists
        if (this.adjacencyMap.get(from).contains(to)) {
            this.edges.removeIf(e -> (e.from.equals(from) && e.to.equals(to))
                    || (e.from.equals(to) && e.to.equals(from)));
        }

        Edge<V> edge = new Edge<>(from, to, weight);
        this.edges.add(edge);
        this.adjacencyMap.get(from).add(to);
        this.adjacencyMap.get(to).add(from);

        this.weightMap.computeIfAbsent(from, k -> new HashMap<>()).put(to, weight);
        this.weightMap.computeIfAbsent(to, k -> new HashMap<>()).put(from, weight);
    }

    public void addEdge(V fromId, V toId, double weight) {
        Vertex<V> u = addVertex(fromId);
        Vertex<V> v = addVertex(toId);
        addEdge(u, v, weight);
    }

    @Override
    public void removeEdge(V from, V to) {
        Vertex<V> vFrom = getVertexById(from);
        Vertex<V> vTo = getVertexById(to);
        if (vFrom != null && vTo != null) {
            super.removeEdge(from, to);
            Map<Vertex<V>, Double> mFrom = this.weightMap.get(vFrom);
            if (mFrom != null) mFrom.remove(vTo);
            Map<Vertex<V>, Double> mTo = this.weightMap.get(vTo);
            if (mTo != null) mTo.remove(vFrom);
        }
    }

    @Override
    public void removeVertex(V id) {
        Vertex<V> v = getVertexById(id);
        if (v == null) return;
        super.removeVertex(id);
        this.weightMap.remove(v);
        for (Map<Vertex<V>, Double> m : this.weightMap.values()) {
            m.remove(v);
        }
    }

    public double getWeight(Vertex<V> from, Vertex<V> to) {
        if (from == null || to == null) return Double.POSITIVE_INFINITY;
        Map<Vertex<V>, Double> neighbors = this.weightMap.get(from);
        if (neighbors == null) return Double.POSITIVE_INFINITY;
        Double w = neighbors.get(to);
        return w != null ? w : Double.POSITIVE_INFINITY;
    }

    public double getWeight(V fromId, V toId) {
        return getWeight(getVertexById(fromId), getVertexById(toId));
    }

    public Set<WeightedEdge<V>> getIncidentWeightedEdges(Vertex<V> v) {
        if (v == null || !this.vertices.contains(v)) return Collections.emptySet();
        Set<WeightedEdge<V>> result = new HashSet<>();
        Map<Vertex<V>, Double> neighbors = this.weightMap.get(v);
        if (neighbors != null) {
            for (Map.Entry<Vertex<V>, Double> entry : neighbors.entrySet()) {
                result.add(new Edge<>(v, entry.getKey(), entry.getValue()));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public Set<WeightedEdge<V>> getIncidentWeightedEdges(V id) {
        return getIncidentWeightedEdges(getVertexById(id));
    }

    public Set<WeightedEdge<V>> getWeightedEdges() {
        Set<WeightedEdge<V>> result = new HashSet<>();
        for (Graph.Edge<V> e : this.edges) {
            if (e instanceof WeightedEdge<V> we) {
                result.add(we);
            } else {
                result.add(new Edge<>(e.from, e.to, getWeight(e.from, e.to)));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    // -------------------------- SHORTEST PATHS ------------- //

    /**
     * Algorithm: Dijkstra's Algorithm (Single Source Shortest Path)
     * Complexity: O((V + E) log V) time, O(V) space
     */
    public Map<Vertex<V>, Double> dijkstraDistances(Vertex<V> start) {
        if (start == null || !this.vertices.contains(start)) {
            return Collections.emptyMap();
        }
        for (Graph.Edge<V> e : this.edges) {
            if (getWeight(e.from, e.to) < 0) {
                throw new IllegalArgumentException("Dijkstra does not support negative edge weights: " + e);
            }
        }

        Map<Vertex<V>, Double> distances = new HashMap<>();
        for (Vertex<V> v : this.vertices) {
            distances.put(v, Double.POSITIVE_INFINITY);
        }
        distances.put(start, 0.0);

        PriorityQueue<Map.Entry<Vertex<V>, Double>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue));
        pq.add(new AbstractMap.SimpleEntry<>(start, 0.0));
        Set<Vertex<V>> settled = new HashSet<>();

        while (!pq.isEmpty()) {
            Map.Entry<Vertex<V>, Double> curr = pq.poll();
            Vertex<V> u = curr.getKey();

            if (settled.contains(u)) continue;
            settled.add(u);

            for (Vertex<V> v : this.adjacencyMap.get(u)) {
                if (!settled.contains(v)) {
                    double w = getWeight(u, v);
                    if (distances.get(u) + w < distances.get(v)) {
                        distances.put(v, distances.get(u) + w);
                        pq.add(new AbstractMap.SimpleEntry<>(v, distances.get(v)));
                    }
                }
            }
        }
        return distances;
    }

    public Map<Vertex<V>, Double> dijkstraDistances(V startId) {
        return dijkstraDistances(getVertexById(startId));
    }

    public List<Vertex<V>> dijkstraShortestPath(Vertex<V> start, Vertex<V> target) {
        if (start == null || target == null || !this.vertices.contains(start) || !this.vertices.contains(target)) {
            return Collections.emptyList();
        }
        for (Graph.Edge<V> e : this.edges) {
            if (getWeight(e.from, e.to) < 0) {
                throw new IllegalArgumentException("Dijkstra does not support negative edge weights: " + e);
            }
        }

        Map<Vertex<V>, Double> distances = new HashMap<>();
        Map<Vertex<V>, Vertex<V>> predecessor = new HashMap<>();
        for (Vertex<V> v : this.vertices) {
            distances.put(v, Double.POSITIVE_INFINITY);
        }
        distances.put(start, 0.0);

        PriorityQueue<Map.Entry<Vertex<V>, Double>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue));
        pq.add(new AbstractMap.SimpleEntry<>(start, 0.0));
        Set<Vertex<V>> settled = new HashSet<>();

        while (!pq.isEmpty()) {
            Map.Entry<Vertex<V>, Double> curr = pq.poll();
            Vertex<V> u = curr.getKey();

            if (u.equals(target)) break;
            if (settled.contains(u)) continue;
            settled.add(u);

            for (Vertex<V> v : this.adjacencyMap.get(u)) {
                if (!settled.contains(v)) {
                    double w = getWeight(u, v);
                    if (distances.get(u) + w < distances.get(v)) {
                        distances.put(v, distances.get(u) + w);
                        predecessor.put(v, u);
                        pq.add(new AbstractMap.SimpleEntry<>(v, distances.get(v)));
                    }
                }
            }
        }

        if (distances.get(target) == Double.POSITIVE_INFINITY) {
            return Collections.emptyList();
        }

        List<Vertex<V>> path = new ArrayList<>();
        Vertex<V> curr = target;
        while (curr != null) {
            path.add(curr);
            curr = predecessor.get(curr);
        }
        Collections.reverse(path);
        return path;
    }

    public List<Vertex<V>> dijkstraShortestPath(V startId, V targetId) {
        return dijkstraShortestPath(getVertexById(startId), getVertexById(targetId));
    }

    /**
     * Algorithm: Bellman-Ford Algorithm (Handles negative weights, detects negative cycles)
     * Complexity: O(V * E) time, O(V) space
     */
    public Map<Vertex<V>, Double> bellmanFordDistances(Vertex<V> start) {
        if (start == null || !this.vertices.contains(start)) {
            return Collections.emptyMap();
        }

        Map<Vertex<V>, Double> distances = new HashMap<>();
        for (Vertex<V> v : this.vertices) {
            distances.put(v, Double.POSITIVE_INFINITY);
        }
        distances.put(start, 0.0);

        int n = this.vertices.size();
        for (int i = 1; i < n; i++) {
            boolean updated = false;
            for (Vertex<V> u : this.vertices) {
                double distU = distances.get(u);
                if (distU == Double.POSITIVE_INFINITY) continue;
                for (Vertex<V> v : this.adjacencyMap.get(u)) {
                    double w = getWeight(u, v);
                    if (distU + w < distances.get(v)) {
                        distances.put(v, distU + w);
                        updated = true;
                    }
                }
            }
            if (!updated) break;
        }

        // N-th iteration checks for negative cycles
        for (Vertex<V> u : this.vertices) {
            double distU = distances.get(u);
            if (distU == Double.POSITIVE_INFINITY) continue;
            for (Vertex<V> v : this.adjacencyMap.get(u)) {
                double w = getWeight(u, v);
                if (distU + w < distances.get(v)) {
                    throw new IllegalStateException("Negative-weight cycle detected reachable from " + start);
                }
            }
        }

        return distances;
    }

    public Map<Vertex<V>, Double> bellmanFordDistances(V startId) {
        return bellmanFordDistances(getVertexById(startId));
    }

    public boolean hasNegativeCycle() {
        Map<Vertex<V>, Double> distances = new HashMap<>();
        for (Vertex<V> v : this.vertices) {
            distances.put(v, 0.0);
        }

        int n = this.vertices.size();
        for (int i = 1; i < n; i++) {
            boolean updated = false;
            for (Vertex<V> u : this.vertices) {
                for (Vertex<V> v : this.adjacencyMap.get(u)) {
                    double w = getWeight(u, v);
                    if (distances.get(u) + w < distances.get(v)) {
                        distances.put(v, distances.get(u) + w);
                        updated = true;
                    }
                }
            }
            if (!updated) return false;
        }

        for (Vertex<V> u : this.vertices) {
            for (Vertex<V> v : this.adjacencyMap.get(u)) {
                double w = getWeight(u, v);
                if (distances.get(u) + w < distances.get(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Algorithm: Floyd-Warshall Algorithm (All-Pairs Shortest Paths)
     * Complexity: Θ(V³) time, Θ(V²) space
     */
    public Map<Vertex<V>, Map<Vertex<V>, Double>> floydWarshall() {
        Map<Vertex<V>, Map<Vertex<V>, Double>> dist = new HashMap<>();
        for (Vertex<V> u : this.vertices) {
            dist.put(u, new HashMap<>());
            for (Vertex<V> v : this.vertices) {
                if (u.equals(v)) {
                    dist.get(u).put(v, 0.0);
                } else if (this.adjacencyMap.get(u).contains(v)) {
                    dist.get(u).put(v, getWeight(u, v));
                } else {
                    dist.get(u).put(v, Double.POSITIVE_INFINITY);
                }
            }
        }

        for (Vertex<V> k : this.vertices) {
            for (Vertex<V> i : this.vertices) {
                double distIK = dist.get(i).get(k);
                if (distIK == Double.POSITIVE_INFINITY) continue;
                for (Vertex<V> j : this.vertices) {
                    double distKJ = dist.get(k).get(j);
                    if (distKJ == Double.POSITIVE_INFINITY) continue;
                    if (distIK + distKJ < dist.get(i).get(j)) {
                        dist.get(i).put(j, distIK + distKJ);
                    }
                }
            }
        }

        for (Vertex<V> i : this.vertices) {
            if (dist.get(i).get(i) < 0) {
                throw new IllegalStateException("Graph contains a negative-weight cycle");
            }
        }

        return dist;
    }

    // -------------------------- MINIMUM SPANNING TREE ------ //

    /**
     * Algorithm: Kruskal's Algorithm (Minimum Spanning Tree with DSU)
     * Complexity: O(E log E) time, O(V + E) space
     */
    public Set<WeightedEdge<V>> kruskalMST() {
        List<WeightedEdge<V>> allEdges = new ArrayList<>(getWeightedEdges());
        Collections.sort(allEdges);

        utils.DisjointSetUnion<Vertex<V>> dsu = new utils.DisjointSetUnion<>();
        for (Vertex<V> v : this.vertices) {
            dsu.makeSet(v);
        }

        Set<WeightedEdge<V>> mst = new HashSet<>();
        for (WeightedEdge<V> e : allEdges) {
            if (dsu.union(e.from, e.to)) {
                mst.add(e);
                if (mst.size() == this.vertices.size() - 1) {
                    break;
                }
            }
        }
        return mst;
    }

    public double kruskalWeight() {
        return kruskalMST().stream().mapToDouble(WeightedEdge::weight).sum();
    }

    /**
     * Algorithm: Prim's Algorithm (Minimum Spanning Tree with PriorityQueue)
     * Complexity: O((V + E) log V) time, O(V + E) space
     */
    public Set<WeightedEdge<V>> primMST() {
        if (this.vertices.isEmpty()) return Collections.emptySet();

        Set<WeightedEdge<V>> mst = new HashSet<>();
        Set<Vertex<V>> visited = new HashSet<>();
        PriorityQueue<WeightedEdge<V>> pq = new PriorityQueue<>();

        Vertex<V> start = this.vertices.iterator().next();
        visited.add(start);
        pq.addAll(getIncidentWeightedEdges(start));

        while (!pq.isEmpty() && mst.size() < this.vertices.size() - 1) {
            WeightedEdge<V> edge = pq.poll();
            Vertex<V> next = null;
            if (visited.contains(edge.from) && !visited.contains(edge.to)) {
                next = edge.to;
            } else if (visited.contains(edge.to) && !visited.contains(edge.from)) {
                next = edge.from;
            }

            if (next != null) {
                visited.add(next);
                mst.add(edge);
                for (WeightedEdge<V> inc : getIncidentWeightedEdges(next)) {
                    Vertex<V> other = inc.from.equals(next) ? inc.to : inc.from;
                    if (!visited.contains(other)) {
                        pq.add(inc);
                    }
                }
            }
        }
        return mst;
    }

    public double primWeight() {
        return primMST().stream().mapToDouble(WeightedEdge::weight).sum();
    }
}
