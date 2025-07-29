package graphs;

public class UndirectedGraph extends Graph {

    @Override
    public void removeVertex(String id) {
        Vertex v = getVertexById(id);
        if (v == null) return;

        for (Vertex neighbor : adjacencyMap.get(v)) {
            adjacencyMap.get(neighbor).remove(v);
        }
        this.vertices.remove(v);
        this.adjacencyMap.remove(v);
        this.edges.removeIf(e -> e.from().equals(id) || e.to().equals(id));
    }

    @Override
    public void addEdge(String from, String to) {
        Vertex vFrom = this.addVertex(from);
        Vertex vTo = this.addVertex(to);
        if (this.edges.contains(new Edge(from, to)) || this.edges.contains(new Edge(to, from))) return;
        this.adjacencyMap.get(vFrom).add(vTo);
        this.adjacencyMap.get(vTo).add(vFrom);
    }

    @Override
    public void removeEdge(String from, String to) {
        Vertex vFrom = getVertexById(from);
        Vertex vTo = getVertexById(to);
        if (vFrom != null && vTo != null && (this.edges.remove(new Edge(from, to)) || this.edges.remove(new Edge(to, from)))) {
            this.adjacencyMap.get(vFrom).remove(vTo);
            this.adjacencyMap.get(vTo).remove(vFrom);
        }
    }
}
