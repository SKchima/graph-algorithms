package graphs;

import java.util.Objects;

public class WeightedEdge<V> extends Graph.Edge<V> implements Comparable<WeightedEdge<V>> {
    protected final double weight;

    public WeightedEdge(Graph.Vertex<V> from, Graph.Vertex<V> to, double weight) {
        super(from, to);
        this.weight = weight;
    }

    public double weight() {
        return weight;
    }

    @Override
    public int compareTo(WeightedEdge<V> o) {
        return Double.compare(this.weight, o.weight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightedEdge<?> other = (WeightedEdge<?>) o;
        return Objects.equals(from, other.from) && Objects.equals(to, other.to)
                && Double.compare(weight, other.weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, weight);
    }

    @Override
    public String toString() {
        return "(" + from + " -> " + to + " : " + weight + ")";
    }
}
