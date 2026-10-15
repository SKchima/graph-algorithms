package graphs;

import java.util.*;

public class WeightedDigraph<V> extends Digraph<V> {

    public static class Edge<V> extends WeightedEdge<V> {
        public Edge(Vertex<V> from, Vertex<V> to, double weight) {
            super(from, to, weight);
        }

        @Override
        public String toString() {
            return "(" + from + " -> " + to + " : " + weight + ")";
        }
    }

    protected final Map<Vertex<V>, Map<Vertex<V>, Double>> weightMap = new HashMap<>();

    public WeightedDigraph() {
        super();
    }

    public WeightedDigraph(WeightedDigraph<V> other) {
        for (Vertex<V> v : other.vertices) {
            this.addVertex(v.id());
        }
        for (Graph.Edge<V> e : other.edges) {
            double w = other.getWeight(e.from, e.to);
            this.addEdge(this.getVertexById(e.from.id()), this.getVertexById(e.to.id()), w);
        }
    }

    public WeightedDigraph(Digraph<V> other) {
        for (Vertex<V> v : other.vertices) {
            this.addVertex(v.id());
        }
        for (Graph.Edge<V> e : other.edges) {
            this.addEdge(this.getVertexById(e.from.id()), this.getVertexById(e.to.id()), 1.0);
        }
    }

    @Override
    public WeightedDigraph<V> copy() {
        return new WeightedDigraph<>(this);
    }

    @Override
    public WeightedDigraph<V> transposeCopy() {
        WeightedDigraph<V> T = new WeightedDigraph<>();
        for (Vertex<V> v : this.vertices) {
            T.addVertex(v.id());
        }
        for (Graph.Edge<V> e : this.edges) {
            double w = getWeight(e.from, e.to);
            T.addEdge(T.getVertexById(e.to.id()), T.getVertexById(e.from.id()), w);
        }
        return T;
    }

    // -------------------------- EDGE & WEIGHT OPERATIONS --- //

    @Override
    public void addEdge(Vertex<V> from, Vertex<V> to) {
        addEdge(from, to, 1.0);
    }

    public void addEdge(Vertex<V> from, Vertex<V> to, double weight) {
        this.addVertex(from);
        this.addVertex(to);

        if (this.adjacencyMap.get(from).contains(to)) {
            this.edges.removeIf(e -> e.from.equals(from) && e.to.equals(to));
        }

        Edge<V> edge = new Edge<>(from, to, weight);
        this.edges.add(edge);
        this.adjacencyMap.get(from).add(to);
        this.inAdjacencyMap.get(to).add(from);

        this.weightMap.computeIfAbsent(from, k -> new HashMap<>()).put(to, weight);
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
            Map<Vertex<V>, Double> m = this.weightMap.get(vFrom);
            if (m != null) m.remove(vTo);
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

    // -------------------------- FLOW NETWORKS -------------- //

    /**
     * Algorithm: Edmonds-Karp (Max Flow using BFS shortest augmenting path)
     * Complexity: O(V * E²) time, O(V + E) space
     */
    public double edmondsKarpMaxFlow(V sourceId, V sinkId) {
        Vertex<V> s = getVertexById(sourceId);
        Vertex<V> t = getVertexById(sinkId);
        if (s == null || t == null || s.equals(t)) return 0.0;

        Map<Vertex<V>, Map<Vertex<V>, Double>> resCap = buildResidualGraph();
        double maxFlow = 0.0;
        Map<Vertex<V>, Vertex<V>> parent = new HashMap<>();

        double pushFlow;
        while ((pushFlow = bfsAugmentingPath(s, t, resCap, parent)) > 1e-9) {
            maxFlow += pushFlow;
            Vertex<V> curr = t;
            while (!curr.equals(s)) {
                Vertex<V> prev = parent.get(curr);
                resCap.get(prev).put(curr, resCap.get(prev).get(curr) - pushFlow);
                resCap.get(curr).put(prev, resCap.get(curr).get(prev) + pushFlow);
                curr = prev;
            }
        }
        return maxFlow;
    }

    private double bfsAugmentingPath(Vertex<V> s, Vertex<V> t,
                                     Map<Vertex<V>, Map<Vertex<V>, Double>> resCap,
                                     Map<Vertex<V>, Vertex<V>> parent) {
        parent.clear();
        Queue<Map.Entry<Vertex<V>, Double>> q = new ArrayDeque<>();
        q.add(new AbstractMap.SimpleEntry<>(s, Double.POSITIVE_INFINITY));
        parent.put(s, s);

        while (!q.isEmpty()) {
            Map.Entry<Vertex<V>, Double> entry = q.poll();
            Vertex<V> u = entry.getKey();
            double flow = entry.getValue();

            Map<Vertex<V>, Double> neighbors = resCap.get(u);
            if (neighbors != null) {
                for (Map.Entry<Vertex<V>, Double> edge : neighbors.entrySet()) {
                    Vertex<V> v = edge.getKey();
                    double cap = edge.getValue();
                    if (!parent.containsKey(v) && cap > 1e-9) {
                        parent.put(v, u);
                        double newFlow = Math.min(flow, cap);
                        if (v.equals(t)) {
                            return newFlow;
                        }
                        q.add(new AbstractMap.SimpleEntry<>(v, newFlow));
                    }
                }
            }
        }
        return 0.0;
    }

    /**
     * Algorithm: Ford-Fulkerson (Max Flow using DFS augmenting paths)
     * Complexity: O(E * |f*|) time, O(V + E) space
     */
    public double fordFulkersonMaxFlow(V sourceId, V sinkId) {
        Vertex<V> s = getVertexById(sourceId);
        Vertex<V> t = getVertexById(sinkId);
        if (s == null || t == null || s.equals(t)) return 0.0;

        Map<Vertex<V>, Map<Vertex<V>, Double>> resCap = buildResidualGraph();
        double maxFlow = 0.0;

        double pushFlow;
        while ((pushFlow = dfsAugmentingPath(s, t, Double.POSITIVE_INFINITY, resCap, new HashSet<>())) > 1e-9) {
            maxFlow += pushFlow;
        }
        return maxFlow;
    }

    private double dfsAugmentingPath(Vertex<V> u, Vertex<V> t, double bottleneck,
                                     Map<Vertex<V>, Map<Vertex<V>, Double>> resCap,
                                     Set<Vertex<V>> visited) {
        if (u.equals(t)) return bottleneck;
        visited.add(u);

        Map<Vertex<V>, Double> neighbors = resCap.get(u);
        if (neighbors != null) {
            for (Map.Entry<Vertex<V>, Double> edge : neighbors.entrySet()) {
                Vertex<V> v = edge.getKey();
                double cap = edge.getValue();
                if (!visited.contains(v) && cap > 1e-9) {
                    double push = dfsAugmentingPath(v, t, Math.min(bottleneck, cap), resCap, visited);
                    if (push > 1e-9) {
                        resCap.get(u).put(v, cap - push);
                        resCap.get(v).put(u, resCap.get(v).get(u) + push);
                        return push;
                    }
                }
            }
        }
        return 0.0;
    }

    /**
     * Algorithm: Max-Flow Min-Cut Theorem
     * Identifies the minimum capacity cut edges separating source from sink.
     */
    public Set<WeightedEdge<V>> minCut(V sourceId, V sinkId) {
        Vertex<V> s = getVertexById(sourceId);
        Vertex<V> t = getVertexById(sinkId);
        if (s == null || t == null || s.equals(t)) return Collections.emptySet();

        Map<Vertex<V>, Map<Vertex<V>, Double>> resCap = buildResidualGraph();
        Map<Vertex<V>, Vertex<V>> parent = new HashMap<>();

        double pushFlow;
        while ((pushFlow = bfsAugmentingPath(s, t, resCap, parent)) > 1e-9) {
            Vertex<V> curr = t;
            while (!curr.equals(s)) {
                Vertex<V> prev = parent.get(curr);
                resCap.get(prev).put(curr, resCap.get(prev).get(curr) - pushFlow);
                resCap.get(curr).put(prev, resCap.get(curr).get(prev) + pushFlow);
                curr = prev;
            }
        }

        Set<Vertex<V>> S = new HashSet<>();
        Queue<Vertex<V>> q = new ArrayDeque<>();
        q.add(s);
        S.add(s);

        while (!q.isEmpty()) {
            Vertex<V> u = q.poll();
            Map<Vertex<V>, Double> neighbors = resCap.get(u);
            if (neighbors != null) {
                for (Map.Entry<Vertex<V>, Double> edge : neighbors.entrySet()) {
                    if (edge.getValue() > 1e-9 && !S.contains(edge.getKey())) {
                        S.add(edge.getKey());
                        q.add(edge.getKey());
                    }
                }
            }
        }

        Set<WeightedEdge<V>> cutEdges = new HashSet<>();
        for (Vertex<V> u : S) {
            for (Vertex<V> v : this.adjacencyMap.get(u)) {
                if (!S.contains(v)) {
                    cutEdges.add(new Edge<>(u, v, getWeight(u, v)));
                }
            }
        }
        return cutEdges;
    }

    public double minCutCapacity(V sourceId, V sinkId) {
        return minCut(sourceId, sinkId).stream().mapToDouble(WeightedEdge::weight).sum();
    }

    /**
     * Max Bipartite Matching reduced to Max Flow on unit capacity network
     */
    public int maxBipartiteMatchingFlow(Set<V> partitionX, Set<V> partitionY) {
        WeightedDigraph<Object> net = new WeightedDigraph<>();
        Object source = "__src__";
        Object sink = "__snk__";

        for (V x : partitionX) {
            net.addEdge(source, x, 1.0);
        }
        for (V y : partitionY) {
            net.addEdge(y, sink, 1.0);
        }
        for (V x : partitionX) {
            Vertex<V> vx = getVertexById(x);
            if (vx != null) {
                for (Vertex<V> vy : this.adjacencyMap.get(vx)) {
                    if (partitionY.contains(vy.id())) {
                        net.addEdge(x, vy.id(), 1.0);
                    }
                }
            }
        }
        return (int) net.edmondsKarpMaxFlow(source, sink);
    }

    private Map<Vertex<V>, Map<Vertex<V>, Double>> buildResidualGraph() {
        Map<Vertex<V>, Map<Vertex<V>, Double>> resCap = new HashMap<>();
        for (Vertex<V> u : this.vertices) {
            resCap.put(u, new HashMap<>());
        }
        for (Vertex<V> u : this.vertices) {
            for (Vertex<V> v : this.adjacencyMap.get(u)) {
                double cap = getWeight(u, v);
                resCap.get(u).put(v, cap);
                resCap.get(v).putIfAbsent(u, 0.0);
            }
        }
        return resCap;
    }
}
