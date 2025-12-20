package graphs;

import java.util.*;

public class Digraph extends Graph {

    public Digraph() {
        super();
    }

    public Digraph(Digraph other) {
        for (Vertex v : other.vertices) {
            this.addVertex(v.id());
        }
        for (Edge e : other.edges) {
            this.addEdge(this.getVertexById(e.from.id()), this.getVertexById(e.to.id()));
        }
    }

    public Digraph copy() {
        return new Digraph(this);
    }

    // -------------------------- INHERITED METHODS ---------- //

    @Override
    public void removeVertex(String id) {
        Vertex v = getVertexById(id);
        if (v == null)
            return;

        for (Set<Vertex> neighbors : this.adjacencyMap.values()) {
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
        if (this.edges.add(edge))
            this.adjacencyMap.get(from).add(to);
    }

    @Override
    public void removeEdge(String from, String to) {
        Vertex vFrom = getVertexById(from);
        Vertex vTo = getVertexById(to);
        if (vFrom != null && vTo != null && this.edges.remove(new Edge(vFrom, vTo))) {
            this.adjacencyMap.get(vFrom).remove(vTo);
        }
    }

    public int inDegree(Vertex v) {
        if (!this.vertices.contains(v))
            return 0;
        int count = 0;
        for (Edge e : this.edges) {
            if (e.to.equals(v)) {
                count++;
            }
        }
        return count;
    }

    public int outDegree(Vertex v) {
        if (!this.vertices.contains(v))
            return 0;
        int count = 0;
        for (Edge e : this.edges) {
            if (e.from.equals(v)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Digraph treeBFS(Vertex root) {
        Digraph tree = new Digraph();
        Set<Vertex> visited = new HashSet<>();
        Deque<Vertex> queue = new ArrayDeque<>();

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

    // -------------------------- ALGORITHMS ----------------- //

    // ----- STRUCTURES AND ORDERING ----- //

    public boolean isTree() {
        if (this.vertices.isEmpty())
            return true;

        if (this.vertices.size() - 1 != this.edges.size())
            return false;

        if (hasCycles())
            return false;

        int rootCount = 0;
        for (Vertex v : this.vertices) {
            int in = inDegree(v);
            if (in == 0) {
                rootCount++;
            } else if (in > 1) {
                return false;
            }
        }

        return rootCount == 1;
    }

    public boolean hasCycles() {
        Set<Vertex> start = new HashSet<>();
        Set<Vertex> finish = new HashSet<>();
        for (Vertex v : this.vertices) {
            if (!start.contains(v)) {
                if (hasCyclesDFS(start, finish, v))
                    return true;
            }
        }
        return false;
    }

    public List<Vertex> topologicalSort() {
        Map<Vertex, Integer> inDegree = new HashMap<>();
        for (Vertex v : this.vertices) {
            inDegree.put(v, 0);
        }

        for (Vertex v : this.vertices) {
            for (Vertex neighbor : this.adjacencyMap.get(v)) {
                inDegree.put(neighbor, inDegree.get(neighbor) + 1);
            }
        }

        Deque<Vertex> q = new ArrayDeque<>();
        for (Map.Entry<Vertex, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0)
                q.add(entry.getKey());
        }

        List<Vertex> sort = new ArrayList<>();
        int count = 0;
        while (!q.isEmpty()) {
            Vertex current = q.poll();
            sort.add(current);
            count++;
            for (Vertex neighbor : this.adjacencyMap.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0)
                    q.add(neighbor);
            }
        }

        if (count != this.vertices.size())
            return null;
        return sort;
    }

    public Digraph transposeCopy() {
        Digraph T = new Digraph();
        for (Edge e : this.edges) {
            T.addEdge(T.addVertex(e.to.id()), T.addVertex(e.from.id()));
        }
        for (Vertex v : this.vertices) {
            if (T.getVertexById(v.id()) == null)
                T.addVertex(v.id());
        }
        return T;
    }

    // ----- STRONG CONNECTIVITY ----- //

    public Set<Set<Vertex>> stronglyConnectedComponentsKS() {
        Set<Vertex> visited = new HashSet<>();
        Deque<Vertex> stack = new ArrayDeque<>();

        for (Vertex v : this.vertices) {
            if (!visited.contains(v))
                kosarajuFirstPass(visited, stack, v);
        }

        Digraph T = transposeAux();

        visited.clear();
        Set<Set<Vertex>> components = new HashSet<>();

        while (!stack.isEmpty()) {
            Vertex current = stack.pop();
            if (!visited.contains(current)) {
                Set<Vertex> component = new HashSet<>();
                T.kosarajuSecondPass(visited, component, current);
                components.add(component);
            }
        }
        return components;
    }

    public Set<Set<Vertex>> stronglyConnectedComponentsT() {
        Map<Vertex, Integer> index = new HashMap<>();
        Map<Vertex, Integer> lowlink = new HashMap<>();
        Deque<Vertex> stack = new ArrayDeque<>();
        Set<Vertex> onStack = new HashSet<>();
        Set<Set<Vertex>> components = new HashSet<>();
        int[] count = { 0 };

        for (Vertex v : this.vertices) {
            if (!index.containsKey(v))
                tarjanDFS(v, index, lowlink, stack, onStack, count, components);
        }
        return components;
    }

    // ----- EULERIAN TRAILS ----- //

    public boolean hasEulerianTrail() {
        if (!isWeaklyConnectedExcludingIsolated())
            return false;
        int startNodes = 0;
        int endNodes = 0;
        for (Vertex v : this.vertices) {
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

    public boolean hasEulerianCircuit() {
        if (!isWeaklyConnectedExcludingIsolated())
            return false;
        for (Vertex v : this.vertices) {
            if (inDegree(v) != outDegree(v))
                return false;
        }
        return true;
    }

    public List<Edge> eulerianTrailHierholzer() {
        if (!hasEulerianTrail())
            return null;

        if (this.hasEulerianCircuit())
            return eulerianCircuitHierholzer();

        Vertex start = null;
        Vertex end = null;
        for (Vertex v : this.vertices) {
            int in = inDegree(v);
            int out = outDegree(v);
            if (out - in == 1)
                start = v;
            else if (in - out == 1)
                end = v;
        }

        Digraph temp = this.copy();
        temp.addEdge(end, start);

        List<Edge> circuit = temp.eulerianCircuitHierholzer();
        List<Edge> trail = new ArrayList<>();
        int dummyIdx = -1;

        for (int i = 0; i < circuit.size(); i++) {
            Edge e = circuit.get(i);
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

    public List<Edge> eulerianCircuitHierholzer() {
        if (!hasEulerianCircuit())
            return null;

        List<Edge> answer = new ArrayList<>();
        Digraph copy = this.copy();

        Vertex start = null;
        for (Vertex v : copy.vertices) {
            if (copy.outDegree(v) > 0) {
                start = v;
                break;
            }
        }

        if (start == null)
            return answer;

        Deque<Edge> stack = new ArrayDeque<>();
        Vertex current = start;

        while (!stack.isEmpty() || copy.outDegree(current) > 0) {
            if (copy.outDegree(current) > 0) {
                Vertex next = copy.adjacencyMap.get(current).iterator().next();
                Edge e = new Edge(current, next);
                stack.push(e);
                copy.removeEdge(current.id(), next.id());
                current = next;
            } else {
                Edge e = stack.pop();
                answer.add(e);
                current = e.from;
            }
        }

        Collections.reverse(answer);
        return answer;
    }

    // ----- BIPARTITION AND MATCHING ----- //

    public boolean isDBipartite() {
        // Algorithm: D-Bipartition Test
        return false;
    }

    public Map<Vertex, Integer> getDPartitions() {
        // Algorithm: Source-Sink Analysis
        return null;
    }

    public Set<Edge> maxMatching() {
        // Algorithm: Max Flow using Edmonds-Karp
        return null;
    }

    // -------------------------- HELPERS -------------------- //

    private boolean isWeaklyConnectedExcludingIsolated() {
        Vertex startNode = null;
        int nonIsolatedCount = 0;
        for (Vertex v : this.vertices) {
            if (inDegree(v) > 0 || outDegree(v) > 0) {
                nonIsolatedCount++;
                if (startNode == null)
                    startNode = v;
            }
        }
        if (nonIsolatedCount == 0)
            return true;

        Set<Vertex> visited = new HashSet<>();
        Deque<Vertex> q = new ArrayDeque<>();
        q.add(startNode);
        visited.add(startNode);

        while (!q.isEmpty()) {
            Vertex curr = q.poll();
            for (Vertex n : this.adjacencyMap.get(curr)) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    q.add(n);
                }
            }
            for (Edge e : this.edges) {
                if (e.to.equals(curr) && !visited.contains(e.from)) {
                    visited.add(e.from);
                    q.add(e.from);
                }
            }
        }
        return visited.size() == nonIsolatedCount;
    }

    private boolean hasCyclesDFS(Set<Vertex> start, Set<Vertex> finish, Vertex current) {
        start.add(current);
        for (Vertex v : this.adjacencyMap.get(current)) {
            if (start.contains(v) && !finish.contains(v))
                return true;
            if (hasCyclesDFS(start, finish, v))
                return true;
        }
        finish.add(current);
        return false;
    }

    private Digraph transposeAux() {
        Digraph T = new Digraph();
        for (Vertex v : this.vertices) {
            T.addVertex(v);
        }
        for (Edge e : this.edges) {
            T.addEdge(e.to, e.from);
        }
        return T;
    }

    private void kosarajuFirstPass(Set<Vertex> visited, Deque<Vertex> stack, Vertex current) {
        visited.add(current);
        for (Vertex neighbor : this.adjacencyMap.get(current)) {
            if (!visited.contains(neighbor))
                kosarajuFirstPass(visited, stack, neighbor);
        }
        stack.push(current);
    }

    private void kosarajuSecondPass(Set<Vertex> visited, Set<Vertex> component, Vertex current) {
        visited.add(current);
        component.add(current);

        for (Vertex neighbor : this.adjacencyMap.get(current)) {
            if (!visited.contains(neighbor))
                kosarajuSecondPass(visited, component, neighbor);
        }
    }

    private void tarjanDFS(Vertex current, Map<Vertex, Integer> index, Map<Vertex, Integer> lowlink,
            Deque<Vertex> stack, Set<Vertex> onStack, int[] counter, Set<Set<Vertex>> components) {
        index.put(current, counter[0]);
        lowlink.put(current, counter[0]);
        stack.push(current);
        onStack.add(current);
        counter[0]++;

        for (Vertex neighbor : this.adjacencyMap.get(current)) {
            if (!index.containsKey(neighbor)) {
                tarjanDFS(neighbor, index, lowlink, stack, onStack, counter, components);
                lowlink.put(current, Math.min(lowlink.get(current), lowlink.get(neighbor)));
            } else if (onStack.contains(neighbor))
                lowlink.put(current, Math.min(lowlink.get(current), index.get(neighbor)));
        }

        if (lowlink.get(current).equals(index.get(current))) {
            Set<Vertex> component = new HashSet<>();
            Vertex v;
            do {
                v = stack.pop();
                onStack.remove(v);
                component.add(v);
            } while (v != current);
            components.add(component);
        }
    }
}