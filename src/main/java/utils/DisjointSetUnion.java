package utils;

import java.util.*;

public class DisjointSetUnion<V> {
    private final Map<V, V> parent = new HashMap<>();
    private final Map<V, Integer> rank = new HashMap<>();
    private int count = 0;

    public void makeSet(V item) {
        if (!parent.containsKey(item)) {
            parent.put(item, item);
            rank.put(item, 0);
            count++;
        }
    }

    public V find(V item) {
        if (!parent.containsKey(item)) {
            makeSet(item);
            return item;
        }
        V root = item;
        while (!root.equals(parent.get(root))) {
            root = parent.get(root);
        }
        // Path compression
        V curr = item;
        while (!curr.equals(root)) {
            V next = parent.get(curr);
            parent.put(curr, root);
            curr = next;
        }
        return root;
    }

    public boolean union(V x, V y) {
        V rootX = find(x);
        V rootY = find(y);
        if (rootX.equals(rootY)) {
            return false;
        }

        int rankX = rank.get(rootX);
        int rankY = rank.get(rootY);
        if (rankX < rankY) {
            parent.put(rootX, rootY);
        } else if (rankX > rankY) {
            parent.put(rootY, rootX);
        } else {
            parent.put(rootY, rootX);
            rank.put(rootX, rankX + 1);
        }
        count--;
        return true;
    }

    public boolean connected(V x, V y) {
        return find(x).equals(find(y));
    }

    public int count() {
        return count;
    }
}
