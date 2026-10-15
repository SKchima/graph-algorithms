package graphs;

import java.util.*;

public class UndirectedGraph<V> extends Graph<V> {

    public static class Edge<V> extends Graph.Edge<V> {
        public Edge(Vertex<V> from, Vertex<V> to) {
            super(from, to);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass())
                return false;
            Edge<V> other = (Edge<V>) o;

            return (Objects.equals(this.from, other.from) && Objects.equals(this.to, other.to))
                    || (Objects.equals(this.from, other.to) && Objects.equals(this.to, other.from));
        }

        @Override
        public int hashCode() {
            return from.hashCode() ^ to.hashCode();
        }
    }

    public UndirectedGraph() {
        super();
    }

    public UndirectedGraph(UndirectedGraph<V> other) {
        for (Vertex<V> v : other.vertices) {
            this.addVertex(v.id());
        }
        for (Graph.Edge<V> e : other.edges) {
            this.addEdge(this.getVertexById(e.from.id()), this.getVertexById(e.to.id()));
        }
    }

    public UndirectedGraph<V> copy() {
        return new UndirectedGraph<>(this);
    }

    // -------------------------- INHERITED METHODS ---------- //

    @Override
    public void removeVertex(V id) {
        Vertex<V> v = getVertexById(id);
        if (v == null)
            return;

        for (Vertex<V> neighbor : this.adjacencyMap.get(v)) {
            this.adjacencyMap.get(neighbor).remove(v);
        }
        this.vertices.remove(v);
        this.adjacencyMap.remove(v);
        this.edges.removeIf(e -> e.from.equals(v) || e.to.equals(v));
    }

    @Override
    public void addEdge(Vertex<V> from, Vertex<V> to) {
        this.addVertex(from);
        this.addVertex(to);
        Edge<V> edge = new Edge<>(from, to);
        if (!this.edges.add(edge))
            return;
        this.adjacencyMap.get(from).add(to);
        this.adjacencyMap.get(to).add(from);
    }

    @Override
    public void removeEdge(V from, V to) {
        Vertex<V> vFrom = getVertexById(from);
        Vertex<V> vTo = getVertexById(to);
        if (vFrom != null && vTo != null && this.edges.remove(new Edge<>(vFrom, vTo))) {
            this.adjacencyMap.get(vFrom).remove(vTo);
            this.adjacencyMap.get(vTo).remove(vFrom);
        }
    }

    @Override
    public UndirectedGraph<V> treeBFS(Vertex<V> root) {
        UndirectedGraph<V> tree = new UndirectedGraph<>();
        Set<Vertex<V>> visited = new HashSet<>();
        Deque<Vertex<V>> queue = new ArrayDeque<>();

        queue.add(root);
        visited.add(root);

        while (!queue.isEmpty()) {
            Vertex<V> current = queue.poll();
            tree.addVertex(current);
            for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
                if (!visited.contains(neighbor)) {
                    tree.addEdge(current, neighbor);
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return tree;
    }

    // -------------------------- ALGORITHMS ----------------- //

    // ----- BASIC STRUCTURES ----- //

    /**
     * Algorithm: DFS-based cycle detection
     * Complexity: O(V + E) time, O(V) space
     */
    public boolean isTree() {
        if (this.vertices.isEmpty())
            return true;

        return isConnected() && !hasCycles();
    }

    /**
     * Algorithm: Depth-First Search
     * Complexity: O(V + E) time, O(V) space
     */
    public boolean hasCycles() {
        Set<Vertex<V>> visited = new HashSet<>();
        for (Vertex<V> v : this.vertices) {
            if (!visited.contains(v)) {
                if (hasCyclesDFS(visited, v, null))
                    return true;
            }
        }
        return false;
    }

    // ----- CONNECTIVITY ----- //

    /**
     * Algorithm: Tarjan's Bridge-Finding Algorithm
     * Complexity: Θ(V + E) time, O(V) space
     */
    public Set<Edge<V>> bridges() {
        Set<Vertex<V>> visited = new HashSet<>();
        Map<Vertex<V>, Integer> discovery = new HashMap<>();
        Map<Vertex<V>, Integer> lowpoint = new HashMap<>();
        Set<Edge<V>> answer = new HashSet<>();
        int[] counter = { 0 };

        for (Vertex<V> v : this.vertices)
            if (!visited.contains(v))
                bridgesDFS(visited, v, null, discovery, lowpoint, counter, answer);
        return answer;
    }

    /**
     * Algorithm: Tarjan's Articulation Points Algorithm
     * Complexity: Θ(V + E) time, O(V) space
     */
    public Set<Vertex<V>> articulationPoints() {
        Set<Vertex<V>> visited = new HashSet<>();
        Map<Vertex<V>, Integer> discovery = new HashMap<>();
        Map<Vertex<V>, Integer> lowpoint = new HashMap<>();
        Set<Vertex<V>> answer = new HashSet<>();
        int[] counter = { 0 };

        for (Vertex<V> v : this.vertices)
            if (!visited.contains(v))
                articulationPointsDFS(visited, v, null, discovery, lowpoint, counter, answer);
        return answer;
    }

    /**
     * Algorithm: Depth-First Search
     * Complexity: Θ(V + E) time, O(V) space
     */
    public boolean isConnected() {
        if (this.vertices.isEmpty())
            return true;

        Set<Vertex<V>> visited = new HashSet<>();
        Vertex<V> start = this.vertices.iterator().next();
        isConnectedDFS(start, visited);

        return visited.size() == this.vertices.size();
    }

    /**
     * Algorithm: Depth-First Search
     * Complexity: Θ(V + E) time, O(V) space
     */
    public Set<Set<Vertex<V>>> components() {
        Set<Vertex<V>> visited = new HashSet<>();
        Set<Set<Vertex<V>>> answer = new HashSet<>();

        for (Vertex<V> v : this.vertices) {
            if (!visited.contains(v)) {
                Set<Vertex<V>> component = new HashSet<>();
                componentsDFS(v, visited, component);
                answer.add(component);
            }
        }
        return answer;
    }

    // ----- BICONNECTIVITY ----- //

    /**
     * Algorithm: Tarjan's Articulation Points + connectivity check
     * Complexity: Θ(V + E) time, O(V) space
     */
    public boolean isBiconnectedByVertices() {
        return isConnected() && articulationPoints().isEmpty();
    }

    /**
     * Algorithm: Tarjan's Bridge-Finding + connectivity check
     * Complexity: Θ(V + E) time, O(V) space
     */
    public boolean isBiconnectedByEdges() {
        return isConnected() && bridges().isEmpty();
    }

    /**
     * Algorithm: Tarjan's Biconnected Components (articulation-based)
     * Complexity: Θ(V + E) time, O(V + E) space
     */
    public Set<Set<Edge<V>>> biComponentsByArticulation() {
        Set<Vertex<V>> visited = new HashSet<>();
        Map<Vertex<V>, Integer> discovery = new HashMap<>();
        Map<Vertex<V>, Integer> lowpoint = new HashMap<>();
        Set<Set<Edge<V>>> answer = new HashSet<>();
        Deque<Edge<V>> stack = new ArrayDeque<>();
        int[] counter = { 0 };

        for (Vertex<V> v : this.vertices)
            if (!visited.contains(v))
                biconnectedComponentsDFS(visited, v, null, discovery, lowpoint, counter, stack, answer);
        return answer;
    }

    /**
     * Algorithm: Bridge-based component decomposition
     * Complexity: Θ(V + E) time, O(V + E) space
     */
    public Set<Set<Vertex<V>>> biComponentsByBridges() {
        Set<Vertex<V>> visited = new HashSet<>();
        Set<Set<Vertex<V>>> answer = new HashSet<>();
        Set<Edge<V>> bridges = bridges();

        for (Vertex<V> v : this.vertices) {
            if (!visited.contains(v)) {
                Set<Vertex<V>> component = new HashSet<>();
                markComponentDFS(v, visited, component, bridges);
                answer.add(component);
            }
        }
        return answer;
    }

    // ----- EULERIAN TRAILS ----- //

    /**
     * Algorithm: Degree check (Eulerian condition)
     * Complexity: Θ(V) time, O(1) space
     */
    public boolean hasEulerianTrail() {
        if (!isConnectedExcludingIsolated())
            return false;
        int oddDegreeVertices = 0;
        for (Vertex<V> v : this.vertices) {
            if (degreeOf(v) % 2 != 0)
                oddDegreeVertices++;
        }
        return oddDegreeVertices == 0 || oddDegreeVertices == 2;
    }

    /**
     * Algorithm: Degree check (Eulerian condition)
     * Complexity: Θ(V) time, O(1) space
     */
    public boolean hasEulerianCircuit() {
        if (!isConnectedExcludingIsolated())
            return false;
        for (Vertex<V> v : this.vertices) {
            if (degreeOf(v) % 2 != 0)
                return false;
        }
        return true;
    }

    /**
     * Algorithm: Fleury's Algorithm
     * Complexity: O(E²) time, O(V + E) space
     */
    public List<Edge<V>> eulerianTrailFleury() {
        if (!hasEulerianTrail())
            return null;

        List<Edge<V>> answer = new ArrayList<>();
        UndirectedGraph<V> copy = this.copy();

        Vertex<V> start = null;
        for (Vertex<V> v : copy.vertices) {
            if (copy.degreeOf(v) % 2 != 0) {
                start = v;
                break;
            }
        }
        if (start == null) {
            for (Vertex<V> v : copy.vertices) {
                if (copy.degreeOf(v) > 0) {
                    start = v;
                    break;
                }
            }
        }

        if (start == null)
            return answer;

        Vertex<V> current = start;
        while (copy.size() > 0) {
            Vertex<V> next = null;

            for (Vertex<V> candidate : copy.adjacencyMap.get(current)) {
                if (copy.isValidEdgeFleury(current, candidate)) {
                    next = candidate;
                    break;
                }
            }
            answer.add(new Edge<>(current, next));
            copy.removeEdge(current.id(), next.id());
            current = next;
        }
        return answer;
    }

    /**
     * Algorithm: Fleury's Algorithm
     * Complexity: O(E²) time, O(V + E) space
     */
    public List<Edge<V>> eulerianCircuitFleury() {
        if (!hasEulerianCircuit())
            return null;

        List<Edge<V>> answer = new ArrayList<>();
        UndirectedGraph<V> copy = this.copy();

        Vertex<V> start = null;
        for (Vertex<V> v : copy.vertices) {
            if (copy.degreeOf(v) > 0) {
                start = v;
                break;
            }
        }

        if (start == null)
            return answer;

        Vertex<V> current = start;
        while (copy.size() > 0) {
            Vertex<V> next = null;

            for (Vertex<V> candidate : copy.adjacencyMap.get(current)) {
                if (copy.isValidEdgeFleury(current, candidate)) {
                    next = candidate;
                    break;
                }
            }
            answer.add(new Edge<>(current, next));
            copy.removeEdge(current.id(), next.id());
            current = next;
        }
        return answer;
    }

    /**
     * Algorithm: Hierholzer's Algorithm
     * Complexity: Θ(V + E) time, O(V + E) space
     */
    public List<Edge<V>> eulerianTrailHierholzer() {
        if (!hasEulerianTrail())
            return null;

        if (this.hasEulerianCircuit())
            return eulerianCircuitHierholzer();

        List<Edge<V>> answer = new ArrayList<>();
        UndirectedGraph<V> copy = this.copy();

        Vertex<V> start = null;
        Vertex<V> end = null;
        for (Vertex<V> v : copy.vertices) {
            if (copy.degreeOf(v) % 2 != 0) {
                if (start == null)
                    start = v;
                else
                    end = v;
            }
        }

        if (start == null || end == null)
            return answer;

        copy.addEdge(end, start);

        Deque<Edge<V>> stack = new ArrayDeque<>();
        Vertex<V> current = start;

        while (!stack.isEmpty() || copy.degreeOf(current) > 0) {
            if (copy.degreeOf(current) > 0) {
                Vertex<V> next = copy.adjacencyMap.get(current).iterator().next();
                Edge<V> e = new Edge<>(current, next);

                stack.push(e);
                copy.removeEdge(current.id(), next.id());
                current = next;
            } else {
                Edge<V> e = stack.pop();
                answer.add(e);
                current = e.from.equals(current) ? e.to : e.from;
            }
        }

        Collections.reverse(answer);

        for (int i = 0; i < answer.size(); i++) {
            Edge<V> e = answer.get(i);
            if ((e.from.equals(end) && e.to.equals(start)) ||
                    (e.from.equals(start) && e.to.equals(end))) {
                List<Edge<V>> trail = new ArrayList<>();
                for (int j = i + 1; j < answer.size(); j++) {
                    trail.add(answer.get(j));
                }
                for (int j = 0; j < i; j++) {
                    trail.add(answer.get(j));
                }
                return trail;
            }
        }

        return answer;
    }

    /**
     * Algorithm: Hierholzer's Algorithm
     * Complexity: Θ(V + E) time, O(V + E) space
     */
    public List<Edge<V>> eulerianCircuitHierholzer() {
        if (!hasEulerianCircuit())
            return null;

        List<Edge<V>> answer = new ArrayList<>();
        UndirectedGraph<V> copy = this.copy();

        Vertex<V> start = null;
        for (Vertex<V> v : copy.vertices) {
            if (copy.degreeOf(v) > 0) {
                start = v;
                break;
            }
        }

        if (start == null)
            return answer;

        Deque<Edge<V>> stack = new ArrayDeque<>();
        Vertex<V> current = start;

        while (!stack.isEmpty() || copy.degreeOf(current) > 0) {

            if (copy.degreeOf(current) > 0) {
                Vertex<V> next = copy.adjacencyMap.get(current).iterator().next();
                Edge<V> e = new Edge<>(current, next);

                stack.push(e);
                copy.removeEdge(current.id(), next.id());
                current = next;
            } else {
                Edge<V> e = stack.pop();
                answer.add(e);
                current = e.from.equals(current) ? e.to : e.from;
            }
        }

        Collections.reverse(answer);
        return answer;
    }

    // ----- BIPARTITION AND MATCHING ----- //

    /**
     * Algorithm: BFS-based 2-coloring
     * Complexity: O(V + E) time, O(V) space
     */
    public boolean isBipartite() {
        Map<Vertex<V>, Integer> colors = new HashMap<>();

        for (Vertex<V> v : this.vertices) {
            if (colors.containsKey(v))
                continue;

            colors.put(v, 0);
            Deque<Vertex<V>> queue = new ArrayDeque<>();
            queue.add(v);

            while (!queue.isEmpty()) {
                Vertex<V> current = queue.pop();
                int color = colors.get(current);

                for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
                    if (colors.containsKey(neighbor)) {
                        if (colors.get(neighbor) == color)
                            return false;
                    } else {
                        colors.put(neighbor, 1 - color);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return true;
    }

    /**
     * Algorithm: BFS-based 2-coloring
     * Complexity: O(V + E) time, O(V) space
     */
    public List<Set<Vertex<V>>> getBipartition() {
        List<Set<Vertex<V>>> answer = new ArrayList<>();
        answer.add(new HashSet<>());
        answer.add(new HashSet<>());

        Map<Vertex<V>, Integer> colors = new HashMap<>();

        for (Vertex<V> v : this.vertices) {
            if (colors.containsKey(v))
                continue;

            colors.put(v, 0);
            answer.get(0).add(v);
            Deque<Vertex<V>> queue = new ArrayDeque<>();
            queue.add(v);

            while (!queue.isEmpty()) {
                Vertex<V> current = queue.poll();
                int color = colors.get(current);

                for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
                    if (colors.containsKey(neighbor)) {
                        if (colors.get(neighbor) == color)
                            return null;
                    } else {
                        int nextColor = 1 - color;
                        colors.put(neighbor, nextColor);
                        answer.get(nextColor).add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return answer;
    }

    /**
     * Algorithm: Hopcroft-Karp
     * Complexity: O(E√V) time, O(V) space
     */
    public Set<Edge<V>> maxMatching() {
        List<Set<Vertex<V>>> partitions = getBipartition();
        if (partitions == null)
            return null;

        Set<Vertex<V>> X = partitions.get(0);
        Set<Vertex<V>> Y = partitions.get(1);

        Map<Vertex<V>, Vertex<V>> matching = new HashMap<>();
        Map<Vertex<V>, Integer> distances = new HashMap<>();
        int distToFreeY;

        while ((distToFreeY = maxMatchingBFS(matching, X, Y, distances)) != Integer.MAX_VALUE) {
            for (Vertex<V> x : X)
                if (!matching.containsKey(x))
                    maxMatchingDFS(x, matching, distances, distToFreeY);
        }

        Set<Edge<V>> answer = new HashSet<>();
        for (Vertex<V> x : X)
            if (matching.containsKey(x))
                answer.add(new Edge<>(x, matching.get(x)));
        return answer;
    }

    /**
     * Algorithm: König's Theorem (matching-based vertex cover)
     * Complexity: O(E√V) time, O(V) space
     */
    public Set<Vertex<V>> minVertexCover() {
        Set<Edge<V>> matchingEdges = maxMatching();
        if (matchingEdges == null)
            return null;

        List<Set<Vertex<V>>> partitions = getBipartition();
        Set<Vertex<V>> X = partitions.get(0);
        Set<Vertex<V>> Y = partitions.get(1);

        Map<Vertex<V>, Vertex<V>> matchMap = new HashMap<>();
        for (Edge<V> e : matchingEdges) {
            matchMap.put(e.from, e.to);
            matchMap.put(e.to, e.from);
        }

        Set<Vertex<V>> marked = new HashSet<>();
        Deque<Vertex<V>> queue = new ArrayDeque<>();

        for (Vertex<V> x : X) {
            if (!matchMap.containsKey(x)) {
                marked.add(x);
                queue.add(x);
            }
        }

        while (!queue.isEmpty()) {
            Vertex<V> u = queue.poll();
            if (X.contains(u)) {
                for (Vertex<V> v : this.adjacencyMap.get(u)) {
                    if (matchMap.get(u) != v && !marked.contains(v)) {
                        marked.add(v);
                        queue.add(v);
                    }
                }
            } else {
                if (matchMap.containsKey(u)) {
                    Vertex<V> v = matchMap.get(u);
                    if (!marked.contains(v)) {
                        marked.add(v);
                        queue.add(v);
                    }
                }
            }
        }

        Set<Vertex<V>> answer = new HashSet<>();
        for (Vertex<V> x : X)
            if (!marked.contains(x))
                answer.add(x);
        for (Vertex<V> y : Y)
            if (marked.contains(y))
                answer.add(y);
        return answer;
    }

    /**
     * Algorithm: Gallai's Identity (complement of min vertex cover)
     * Complexity: O(E√V) time, O(V) space
     */
    public Set<Vertex<V>> maxIndependentSet() {
        Set<Vertex<V>> cover = minVertexCover();
        if (cover == null)
            return null;

        Set<Vertex<V>> answer = new HashSet<>(this.vertices);
        answer.removeAll(cover);
        return answer;
    }

    // -------------------------- HELPERS -------------------- //

    private boolean hasCyclesDFS(Set<Vertex<V>> visited, Vertex<V> current, Vertex<V> previous) {
        visited.add(current);
        for (Vertex<V> v : this.adjacencyMap.get(current)) {
            if (v.equals(previous))
                continue;
            if (visited.contains(v))
                return true;
            if (hasCyclesDFS(visited, v, current))
                return true;
        }
        return false;
    }

    private void bridgesDFS(Set<Vertex<V>> visited, Vertex<V> current, Vertex<V> previous, Map<Vertex<V>, Integer> discovery,
            Map<Vertex<V>, Integer> lowpoint, int[] counter, Set<Edge<V>> answer) {
        visited.add(current);
        discovery.put(current, counter[0]);
        lowpoint.put(current, counter[0]);
        counter[0]++;

        for (Vertex<V> v : this.adjacencyMap.get(current)) {
            if (v.equals(previous))
                continue;

            if (!visited.contains(v)) {
                bridgesDFS(visited, v, current, discovery, lowpoint, counter, answer);
                lowpoint.put(current, Math.min(lowpoint.get(current), lowpoint.get(v)));

                if (discovery.get(current) < lowpoint.get(v))
                    answer.add(new Edge<>(current, v));

            } else
                lowpoint.put(current, Math.min(lowpoint.get(current), discovery.get(v)));
        }
    }

    private void articulationPointsDFS(Set<Vertex<V>> visited, Vertex<V> current, Vertex<V> previous,
            Map<Vertex<V>, Integer> discovery, Map<Vertex<V>, Integer> lowpoint, int[] counter, Set<Vertex<V>> answer) {
        visited.add(current);
        discovery.put(current, counter[0]);
        lowpoint.put(current, counter[0]);
        counter[0]++;

        int children = 0;
        for (Vertex<V> v : this.adjacencyMap.get(current)) {
            if (v.equals(previous))
                continue;

            if (!visited.contains(v)) {
                children++;
                articulationPointsDFS(visited, v, current, discovery, lowpoint, counter, answer);
                lowpoint.put(current, Math.min(lowpoint.get(current), lowpoint.get(v)));

                if (previous != null && discovery.get(current) <= lowpoint.get(v))
                    answer.add(current);

            } else
                lowpoint.put(current, Math.min(lowpoint.get(current), discovery.get(v)));
        }
        if (previous == null && children >= 2) {
            answer.add(current);
        }
    }

    private void isConnectedDFS(Vertex<V> current, Set<Vertex<V>> visited) {
        visited.add(current);

        for (Vertex<V> v : this.adjacencyMap.get(current))
            if (!visited.contains(v))
                isConnectedDFS(v, visited);
    }

    private void componentsDFS(Vertex<V> current, Set<Vertex<V>> visited, Set<Vertex<V>> component) {
        visited.add(current);
        component.add(current);

        for (Vertex<V> v : this.adjacencyMap.get(current)) {
            if (!visited.contains(v))
                componentsDFS(v, visited, component);
        }
    }

    private void biconnectedComponentsDFS(Set<Vertex<V>> visited, Vertex<V> current, Vertex<V> previous,
            Map<Vertex<V>, Integer> discovery, Map<Vertex<V>, Integer> lowpoint, int[] counter, Deque<Edge<V>> stack,
            Set<Set<Edge<V>>> answer) {
        visited.add(current);
        discovery.put(current, counter[0]);
        lowpoint.put(current, counter[0]);
        counter[0]++;

        for (Vertex<V> v : this.adjacencyMap.get(current)) {
            if (v.equals(previous))
                continue;

            if (!visited.contains(v)) {
                stack.push(new Edge<>(current, v));
                biconnectedComponentsDFS(visited, v, current, discovery, lowpoint, counter, stack, answer);

                lowpoint.put(current, Math.min(lowpoint.get(current), lowpoint.get(v)));

                if (lowpoint.get(v) >= discovery.get(current)) {
                    Set<Edge<V>> component = new HashSet<>();
                    Edge<V> target = new Edge<>(current, v);
                    Edge<V> e;
                    do {
                        e = stack.pop();
                        component.add(e);
                    } while (!e.equals(target));
                    answer.add(component);
                }

            } else if (discovery.get(v) < discovery.get(current)) {
                stack.push(new Edge<>(current, v));
                lowpoint.put(current, Math.min(lowpoint.get(current), discovery.get(v)));
            }
        }

        if (previous == null && !stack.isEmpty()) {
            Set<Edge<V>> component = new HashSet<>();
            while (!stack.isEmpty())
                component.add(stack.pop());
            answer.add(component);
        }
    }

    private void markComponentDFS(Vertex<V> current, Set<Vertex<V>> visited, Set<Vertex<V>> component, Set<Edge<V>> bridges) {
        visited.add(current);
        component.add(current);

        for (Vertex<V> v : this.adjacencyMap.get(current)) {
            Edge<V> e = new Edge<>(current, v);
            if (visited.contains(v) || bridges.contains(e))
                continue;
            markComponentDFS(v, visited, component, bridges);
        }
    }

    private boolean isConnectedExcludingIsolated() {
        Vertex<V> startNode = null;
        int nonIsolatedCount = 0;

        for (Vertex<V> v : this.vertices) {
            if (degreeOf(v) > 0) {
                nonIsolatedCount++;
                if (startNode == null)
                    startNode = v;
            }
        }

        if (nonIsolatedCount == 0)
            return true;

        return countReachable(startNode, new HashSet<>()) == nonIsolatedCount;
    }

    private int countReachable(Vertex<V> v, Set<Vertex<V>> visited) {
        visited.add(v);
        int count = 1;
        for (Vertex<V> neighbor : this.adjacencyMap.get(v)) {
            if (!visited.contains(neighbor)) {
                count += countReachable(neighbor, visited);
            }
        }
        return count;
    }

    private boolean isValidEdgeFleury(Vertex<V> u, Vertex<V> v) {
        if (degreeOf(u) == 1)
            return true;

        return !isBridge(u, v);
    }

    private boolean isBridge(Vertex<V> u, Vertex<V> v) {
        int before = countReachable(u, new HashSet<>());
        removeEdge(u.id(), v.id());
        int after = countReachable(u, new HashSet<>());
        addEdge(u, v);

        return after < before;
    }

    private int maxMatchingBFS(Map<Vertex<V>, Vertex<V>> matching, Set<Vertex<V>> X, Set<Vertex<V>> Y,
            Map<Vertex<V>, Integer> distances) {
        Deque<Vertex<V>> queue = new ArrayDeque<>();
        for (Vertex<V> x : X) {
            if (!matching.containsKey(x)) {
                distances.put(x, 0);
                queue.add(x);
            } else
                distances.put(x, Integer.MAX_VALUE);
        }
        distances.put(null, Integer.MAX_VALUE);

        while (!queue.isEmpty()) {
            Vertex<V> x = queue.poll();
            if (distances.get(x) < distances.get(null)) {
                for (Vertex<V> y : this.adjacencyMap.get(x)) {
                    Vertex<V> mate = matching.get(y);
                    if (mate == null) {
                        if (distances.get(null) == Integer.MAX_VALUE)
                            distances.put(null, distances.get(x) + 1);
                    } else {
                        if (distances.get(mate) == Integer.MAX_VALUE) {
                            distances.put(mate, distances.get(x) + 1);
                            queue.add(mate);
                        }
                    }
                }
            }
        }
        return distances.get(null);
    }

    private boolean maxMatchingDFS(Vertex<V> u, Map<Vertex<V>, Vertex<V>> matching, Map<Vertex<V>, Integer> distances,
            int distToFreeY) {
        int distU = distances.get(u);

        for (Vertex<V> v : this.adjacencyMap.get(u)) {
            Vertex<V> pairOfV = matching.get(v);

            if (pairOfV == null) {
                if (distToFreeY == distU + 1) {
                    matching.put(u, v);
                    matching.put(v, u);
                    return true;
                }
            } else if (distances.get(pairOfV) == distU + 1) {
                if (maxMatchingDFS(pairOfV, matching, distances, distToFreeY)) {
                    matching.put(u, v);
                    matching.put(v, u);
                    return true;
                }
            }
        }

        distances.put(u, Integer.MAX_VALUE);
        return false;
    }
}