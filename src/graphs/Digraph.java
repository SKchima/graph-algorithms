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

    public boolean hasCycles() {
        Set<Vertex> start = new HashSet<>();
        Set<Vertex> finish = new HashSet<>();
        for (Vertex v : this.vertices) {
            if (!start.contains(v)) {
                if (hasCyclesR(start, finish, v)) return true;
            }
        }
        return false;
    }

    private boolean hasCyclesR(Set<Vertex> start, Set<Vertex> finish, Vertex current) {
        start.add(current);
        for (Vertex v : this.adjacencyMap.get(current)) {
            if (start.contains(v) && !finish.contains(v)) return true;
            if (hasCyclesR(start, finish, v)) return true;
        }
        finish.add(current);
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

        Queue<Vertex> q = new LinkedList<>();
        for (Map.Entry<Vertex, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) q.add(entry.getKey());
        }

        List<Vertex> sort = new ArrayList<>();
        int count = 0;
        while (!q.isEmpty()) {
            Vertex current = q.poll();
            sort.add(current);
            count++;
            for (Vertex neighbor : this.adjacencyMap.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) q.add(neighbor);
            }
        }

        if (count != vertices.size()) return null;
        return sort;
    }

    public Digraph transposeCopy() {
        Digraph T = new Digraph();
        for (Edge e : this.edges) {
            T.addEdge(T.addVertex(e.to.id()), T.addVertex(e.from.id()));
        }
        for (Vertex v : this.vertices) {
            if (T.getVertexById(v.id()) == null) T.addVertex(v.id());
        }
        return T;
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

    public Set<Set<Vertex>> stronglyConnectedComponentsKS() {
        Set<Vertex> visited = new HashSet<>();
        Deque<Vertex> stack = new ArrayDeque<>();

        for (Vertex v : this.vertices) {
            if (!visited.contains(v)) SCCKSAuxOrder(visited, stack, v);
        }

        Digraph T = this.transposeAux();

        visited.clear();
        Set<Set<Vertex>> components = new HashSet<>();

        while (!stack.isEmpty()) {
            Vertex current = stack.pop();
            if (!visited.contains(current)) {
                Set<Vertex> component = new HashSet<>();
                T.SCCKSAuxCollect(visited, component, current);
                components.add(component);
            }
        }
        return components;
    }

    private void SCCKSAuxOrder(Set<Vertex> visited, Deque<Vertex> stack, Vertex current) {
        visited.add(current);
        for (Vertex neighbor : this.adjacencyMap.get(current)) {
            if (!visited.contains(neighbor)) SCCKSAuxOrder(visited, stack, neighbor);
        }
        stack.push(current);
    }

    private void SCCKSAuxCollect(Set<Vertex> visited, Set<Vertex> component, Vertex current) {
        visited.add(current);
        component.add(current);

        for (Vertex neighbor : this.adjacencyMap.get(current)) {
            if (!visited.contains(neighbor)) SCCKSAuxCollect(visited, component, neighbor);
        }
    }

    public Set<Set<Vertex>> stronglyConnectedComponentsT() {
        Map<Vertex, Integer> index = new HashMap<>();
        Map<Vertex, Integer> lowlink = new HashMap<>();
        Deque<Vertex> stack = new ArrayDeque<>();
        Set<Vertex> onStack = new HashSet<>();
        Set<Set<Vertex>> components = new HashSet<>();
        int[] count = {0};

        for (Vertex v : this.vertices) {
            if (!index.containsKey(v)) SCCTAuxR(v, index, lowlink, stack, onStack, count, components);
        }
        return components;
    }

    private void SCCTAuxR(Vertex current, Map<Vertex, Integer> index, Map<Vertex, Integer> lowlink, Deque<Vertex> stack, Set<Vertex> onStack, int[] counter, Set<Set<Vertex>> components) {
        index.put(current, counter[0]);
        lowlink.put(current, counter[0]);
        stack.push(current);
        onStack.add(current);
        counter[0]++;

        for (Vertex neighbor : this.adjacencyMap.get(current)) {
            if (!index.containsKey(neighbor)) {
                SCCTAuxR(neighbor, index, lowlink, stack, onStack, counter, components);
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