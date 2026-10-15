package graphs;

import java.util.*;

public class Digraph<V> extends Graph<V> {

        public static class Edge<V> extends Graph.Edge<V> {
        public Edge(Vertex<V> from, Vertex<V> to) {
            super(from, to);
        }
    }

    protected final Map<Vertex<V>, Set<Vertex<V>>> inAdjacencyMap = new HashMap<>();

    public Digraph() {
        super();
    }

    public Digraph(Digraph<V> other) {
        for (Vertex<V> v : other.vertices) {
            this.addVertex(v.id());
        }
        for (Graph.Edge<V> e : other.edges) {
            this.addEdge(this.getVertexById(e.from.id()), this.getVertexById(e.to.id()));
        }
    }

    public Digraph<V> copy() {
        return new Digraph<>(this);
    }

    // -------------------------- INHERITED METHODS ---------- //

    @Override
    public Vertex<V> addVertex(V id) {
        Vertex<V> v = super.addVertex(id);
        this.inAdjacencyMap.putIfAbsent(v, new HashSet<>());
        return v;
    }

    @Override
    public void addVertex(Vertex<V> v) {
        super.addVertex(v);
        if (v != null) {
            this.inAdjacencyMap.putIfAbsent(v, new HashSet<>());
        }
    }

    @Override
    public void removeVertex(V id) {
        Vertex<V> v = getVertexById(id);
        if (v == null)
            return;

        for (Set<Vertex<V>> neighbors : this.adjacencyMap.values()) {
            neighbors.remove(v);
        }
        for (Set<Vertex<V>> predecessors : this.inAdjacencyMap.values()) {
            predecessors.remove(v);
        }
        this.vertices.remove(v);
        this.vertexMap.remove(id);
        this.adjacencyMap.remove(v);
        this.inAdjacencyMap.remove(v);
        this.edges.removeIf(e -> e.from.equals(v) || e.to.equals(v));
    }

    @Override
    public void addEdge(Vertex<V> from, Vertex<V> to) {
        Edge<V> edge = new Edge<>(from, to);

        this.addVertex(from);
        this.addVertex(to);
        if (this.edges.add(edge)) {
            this.adjacencyMap.get(from).add(to);
            this.inAdjacencyMap.get(to).add(from);
        }
    }

    @Override
    public void removeEdge(V from, V to) {
        Vertex<V> vFrom = getVertexById(from);
        Vertex<V> vTo = getVertexById(to);
        if (vFrom != null && vTo != null && this.edges.remove(new Edge<>(vFrom, vTo))) {
            this.adjacencyMap.get(vFrom).remove(vTo);
            Set<Vertex<V>> inSet = this.inAdjacencyMap.get(vTo);
            if (inSet != null) {
                inSet.remove(vFrom);
            }
        }
    }

    public int inDegree(Vertex<V> v) {
        if (v == null || !this.vertices.contains(v))
            return 0;
        Set<Vertex<V>> inSet = this.inAdjacencyMap.get(v);
        return inSet != null ? inSet.size() : 0;
    }

    public int inDegree(V id) {
        return inDegree(getVertexById(id));
    }

    public int outDegree(Vertex<V> v) {
        if (v == null || !this.vertices.contains(v))
            return 0;
        Set<Vertex<V>> outSet = this.adjacencyMap.get(v);
        return outSet != null ? outSet.size() : 0;
    }

    public int outDegree(V id) {
        return outDegree(getVertexById(id));
    }

    public Set<Vertex<V>> getPredecessors(Vertex<V> v) {
        if (v == null)
            return Collections.emptySet();
        Set<Vertex<V>> preds = this.inAdjacencyMap.get(v);
        return preds != null ? Collections.unmodifiableSet(preds) : Collections.emptySet();
    }

    public Set<Vertex<V>> getPredecessors(V id) {
        return getPredecessors(getVertexById(id));
    }

    @Override
    public Digraph<V> treeBFS(Vertex<V> root) {
        Digraph<V> tree = new Digraph<>();
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

    // ----- STRUCTURES AND ORDERING ----- //

    /**
     * Algorithm: DFS cycle detection + degree constraints
     * Complexity: O(V + E) time, O(V) space
     */
    public boolean isTree() {
        if (this.vertices.isEmpty())
            return true;

        if (this.vertices.size() - 1 != this.edges.size())
            return false;

        if (hasCycles())
            return false;

        int rootCount = 0;
        for (Vertex<V> v : this.vertices) {
            int in = inDegree(v);
            if (in == 0) {
                rootCount++;
            } else if (in > 1) {
                return false;
            }
        }

        return rootCount == 1;
    }

    /**
     * Algorithm: Depth-First Search (white-gray-black)
     * Complexity: O(V + E) time, O(V) space
     */
    public boolean hasCycles() {
        Set<Vertex<V>> start = new HashSet<>();
        Set<Vertex<V>> finish = new HashSet<>();
        for (Vertex<V> v : this.vertices) {
            if (!start.contains(v)) {
                if (hasCyclesDFS(start, finish, v))
                    return true;
            }
        }
        return false;
    }

    /**
     * Algorithm: Kahn's Algorithm (BFS-based)
     * Complexity: Θ(V + E) time, O(V) space
     */
    public List<Vertex<V>> topologicalSort() {
        Map<Vertex<V>, Integer> inDegree = new HashMap<>();
        Deque<Vertex<V>> q = new ArrayDeque<>();

        for (Vertex<V> v : this.vertices) {
            int deg = inDegree(v);
            inDegree.put(v, deg);
            if (deg == 0)
                q.add(v);
        }

        List<Vertex<V>> sort = new ArrayList<>();
        int count = 0;
        while (!q.isEmpty()) {
            Vertex<V> current = q.poll();
            sort.add(current);
            count++;
            for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
                int newDeg = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDeg);
                if (newDeg == 0)
                    q.add(neighbor);
            }
        }

        if (count != this.vertices.size())
            return null;
        return sort;
    }

    /**
     * Algorithm: Edge<V> reversal
     * Complexity: Θ(V + E) time, Θ(V + E) space
     */
    public Digraph<V> transposeCopy() {
        Digraph<V> T = new Digraph<>();
        for (Graph.Edge<V> e : this.edges) {
            T.addEdge(T.addVertex(e.to.id()), T.addVertex(e.from.id()));
        }
        for (Vertex<V> v : this.vertices) {
            if (T.getVertexById(v.id()) == null)
                T.addVertex(v.id());
        }
        return T;
    }

    // ----- STRONG CONNECTIVITY ----- //

    /**
     * Algorithm: Kosaraju-Sharir (two-pass DFS)
     * Complexity: Θ(V + E) time, O(V) space
     */
    public Set<Set<Vertex<V>>> stronglyConnectedComponentsKS() {
        Set<Vertex<V>> visited = new HashSet<>();
        Deque<Vertex<V>> stack = new ArrayDeque<>();

        for (Vertex<V> v : this.vertices) {
            if (!visited.contains(v))
                kosarajuFirstPass(visited, stack, v);
        }

        Digraph<V> T = transposeAux();

        visited.clear();
        Set<Set<Vertex<V>>> components = new HashSet<>();

        while (!stack.isEmpty()) {
            Vertex<V> current = stack.pop();
            if (!visited.contains(current)) {
                Set<Vertex<V>> component = new HashSet<>();
                T.kosarajuSecondPass(visited, component, current);
                components.add(component);
            }
        }
        return components;
    }

    /**
     * Algorithm: Tarjan's Algorithm (single-pass DFS)
     * Complexity: Θ(V + E) time, O(V) space
     */
    public Set<Set<Vertex<V>>> stronglyConnectedComponentsT() {
        Map<Vertex<V>, Integer> index = new HashMap<>();
        Map<Vertex<V>, Integer> lowlink = new HashMap<>();
        Deque<Vertex<V>> stack = new ArrayDeque<>();
        Set<Vertex<V>> onStack = new HashSet<>();
        Set<Set<Vertex<V>>> components = new HashSet<>();
        int[] count = { 0 };

        for (Vertex<V> v : this.vertices) {
            if (!index.containsKey(v))
                tarjanDFS(v, index, lowlink, stack, onStack, count, components);
        }
        return components;
    }

    // ----- EULERIAN TRAILS ----- //

    /**
     * Algorithm: Degree-based Eulerian condition
     * Complexity: Θ(V) time, O(1) space
     */
    public boolean hasEulerianTrail() {
        if (!isWeaklyConnectedExcludingIsolated())
            return false;
        int startNodes = 0;
        int endNodes = 0;
        for (Vertex<V> v : this.vertices) {
            int in = inDegree(v);
            int out = outDegree(v);
            if (out - in == 1)
                startNodes++;
            else if (in - out == 1)
                endNodes++;
            else if (in != out)
                return false;
        }
        return (startNodes == 0 && endNodes == 0) || (startNodes == 1 && endNodes == 1);
    }

    /**
     * Algorithm: Degree-based Eulerian condition
     * Complexity: Θ(V) time, O(1) space
     */
    public boolean hasEulerianCircuit() {
        if (!isWeaklyConnectedExcludingIsolated())
            return false;
        for (Vertex<V> v : this.vertices) {
            if (inDegree(v) != outDegree(v))
                return false;
        }
        return true;
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

        Vertex<V> start = null;
        Vertex<V> end = null;
        for (Vertex<V> v : this.vertices) {
            int in = inDegree(v);
            int out = outDegree(v);
            if (out - in == 1)
                start = v;
            else if (in - out == 1)
                end = v;
        }

        Digraph<V> temp = this.copy();
        temp.addEdge(end, start);

        List<Edge<V>> circuit = temp.eulerianCircuitHierholzer();
        List<Edge<V>> trail = new ArrayList<>();
        int dummyIdx = -1;

        for (int i = 0; i < circuit.size(); i++) {
            Edge<V> e = circuit.get(i);
            if (e.from.equals(end) && e.to.equals(start)) {
                dummyIdx = i;
                break;
            }
        }

        for (int i = 1; i < circuit.size(); i++) {
            trail.add(circuit.get((dummyIdx + i) % circuit.size()));
        }

        return trail;
    }

    /**
     * Algorithm: Hierholzer's Algorithm
     * Complexity: Θ(V + E) time, O(V + E) space
     */
    public List<Edge<V>> eulerianCircuitHierholzer() {
        if (!hasEulerianCircuit())
            return null;

        List<Edge<V>> answer = new ArrayList<>();
        Digraph<V> copy = this.copy();

        Vertex<V> start = null;
        for (Vertex<V> v : copy.vertices) {
            if (copy.outDegree(v) > 0) {
                start = v;
                break;
            }
        }

        if (start == null)
            return answer;

        Deque<Edge<V>> stack = new ArrayDeque<>();
        Vertex<V> current = start;

        while (!stack.isEmpty() || copy.outDegree(current) > 0) {
            if (copy.outDegree(current) > 0) {
                Vertex<V> next = copy.adjacencyMap.get(current).iterator().next();
                Edge<V> e = new Edge<>(current, next);
                stack.push(e);
                copy.removeEdge(current.id(), next.id());
                current = next;
            } else {
                Edge<V> e = stack.pop();
                answer.add(e);
                current = e.from;
            }
        }

        Collections.reverse(answer);
        return answer;
    }

    // ----- BIPARTITION AND MATCHING ----- //

    /**
     * Algorithm: BFS 2-coloring on underlying undirected graph
     * Complexity: O(V + E) time, O(V) space
     */
    public boolean isDBipartite() {
        return getDPartition() != null;
    }

    /**
     * Algorithm: BFS 2-coloring on underlying undirected graph
     * Complexity: O(V + E) time, O(V) space
     */
    public List<Set<Vertex<V>>> getDPartition() {
        List<Set<Vertex<V>>> answer = new ArrayList<>();
        answer.add(new HashSet<>());
        answer.add(new HashSet<>());

        Map<Vertex<V>, Integer> colors = new HashMap<>();

        Map<Vertex<V>, List<Vertex<V>>> undirectedAdj = new HashMap<>();
        for (Vertex<V> v : this.vertices)
            undirectedAdj.put(v, new ArrayList<>());
        for (Graph.Edge<V> e : this.edges) {
            undirectedAdj.get(e.from).add(e.to);
            undirectedAdj.get(e.to).add(e.from);
        }

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
                int nextColor = 1 - color;

                for (Vertex<V> neighbor : undirectedAdj.get(current)) {
                    if (colors.containsKey(neighbor)) {
                        if (colors.get(neighbor) == color)
                            return null;
                    } else {
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
     * Algorithm: Set intersection check (source-sink exclusivity)
     * Complexity: Θ(E) time, O(V) space
     */
    public boolean isSourceSinkBipartite() {
        Set<Vertex<V>> X = new HashSet<>();
        Set<Vertex<V>> Y = new HashSet<>();

        for (Graph.Edge<V> e : this.edges) {
            X.add(e.from);
            Y.add(e.to);
        }

        for (Vertex<V> v : X)
            if (Y.contains(v))
                return false;
        return true;
    }

    /**
     * Algorithm: Source-sink partition by edge endpoints
     * Complexity: Θ(V + E) time, O(V) space
     */
    public List<Set<Vertex<V>>> getSourceSinkPartition() {
        if (!isSourceSinkBipartite())
            return null;

        List<Set<Vertex<V>>> answer = new ArrayList<>();
        Set<Vertex<V>> X = new HashSet<>();
        Set<Vertex<V>> Y = new HashSet<>();

        for (Graph.Edge<V> e : this.edges) {
            X.add(e.from);
            Y.add(e.to);
        }

        for (Vertex<V> v : this.vertices) {
            if (!X.contains(v) && !Y.contains(v))
                X.add(v);
        }

        answer.add(X);
        answer.add(Y);
        return answer;
    }

    // -------------------------- HELPERS -------------------- //

    private boolean isWeaklyConnectedExcludingIsolated() {
        Vertex<V> startNode = null;
        int nonIsolatedCount = 0;
        for (Vertex<V> v : this.vertices) {
            if (inDegree(v) > 0 || outDegree(v) > 0) {
                nonIsolatedCount++;
                if (startNode == null)
                    startNode = v;
            }
        }
        if (nonIsolatedCount == 0)
            return true;

        Set<Vertex<V>> visited = new HashSet<>();
        Deque<Vertex<V>> q = new ArrayDeque<>();
        q.add(startNode);
        visited.add(startNode);

        while (!q.isEmpty()) {
            Vertex<V> curr = q.poll();
            for (Vertex<V> n : this.adjacencyMap.get(curr)) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    q.add(n);
                }
            }
            for (Graph.Edge<V> e : this.edges) {
                if (e.to.equals(curr) && !visited.contains(e.from)) {
                    visited.add(e.from);
                    q.add(e.from);
                }
            }
        }
        return visited.size() == nonIsolatedCount;
    }

    private boolean hasCyclesDFS(Set<Vertex<V>> start, Set<Vertex<V>> finish, Vertex<V> current) {
        start.add(current);
        for (Vertex<V> v : this.adjacencyMap.get(current)) {
            if (start.contains(v) && !finish.contains(v))
                return true;
            if (hasCyclesDFS(start, finish, v))
                return true;
        }
        finish.add(current);
        return false;
    }

    private Digraph<V> transposeAux() {
        Digraph<V> T = new Digraph<>();
        for (Vertex<V> v : this.vertices) {
            T.addVertex(v);
        }
        for (Graph.Edge<V> e : this.edges) {
            T.addEdge(e.to, e.from);
        }
        return T;
    }

    private void kosarajuFirstPass(Set<Vertex<V>> visited, Deque<Vertex<V>> stack, Vertex<V> current) {
        visited.add(current);
        for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
            if (!visited.contains(neighbor))
                kosarajuFirstPass(visited, stack, neighbor);
        }
        stack.push(current);
    }

    private void kosarajuSecondPass(Set<Vertex<V>> visited, Set<Vertex<V>> component, Vertex<V> current) {
        visited.add(current);
        component.add(current);

        for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
            if (!visited.contains(neighbor))
                kosarajuSecondPass(visited, component, neighbor);
        }
    }

    private void tarjanDFS(Vertex<V> current, Map<Vertex<V>, Integer> index, Map<Vertex<V>, Integer> lowlink,
            Deque<Vertex<V>> stack, Set<Vertex<V>> onStack, int[] counter, Set<Set<Vertex<V>>> components) {
        index.put(current, counter[0]);
        lowlink.put(current, counter[0]);
        stack.push(current);
        onStack.add(current);
        counter[0]++;

        for (Vertex<V> neighbor : this.adjacencyMap.get(current)) {
            if (!index.containsKey(neighbor)) {
                tarjanDFS(neighbor, index, lowlink, stack, onStack, counter, components);
                lowlink.put(current, Math.min(lowlink.get(current), lowlink.get(neighbor)));
            } else if (onStack.contains(neighbor))
                lowlink.put(current, Math.min(lowlink.get(current), index.get(neighbor)));
        }

        if (lowlink.get(current).equals(index.get(current))) {
            Set<Vertex<V>> component = new HashSet<>();
            Vertex<V> v;
            do {
                v = stack.pop();
                onStack.remove(v);
                component.add(v);
            } while (v != current);
            components.add(component);
        }
    }
}