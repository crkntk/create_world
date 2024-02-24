package byow.Core;

import java.io.Serializable;

/** BACKGROUND ON UNION FIND AND EXAMPLES FROM STACK OVERFLOW: https://stackoverflow.com/questions/41837311/understanding-union-find
 * Found the class when sifting through explanations about how to create hallways/connect rooms
 */
public class UnionFind implements Serializable {
    int[] parent;

    public UnionFind(int n) {
        parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = -1;
        }
    }

    private void validate(int v1) {
        if (v1 >= parent.length) {
            throw new IllegalArgumentException();
        }
    }

    public int sizeOf(int v1) {
        int root = find(v1);
        return -1 * parent[root];
    }

    public int parent(int v1) {
        return parent[v1];
    }

    public boolean isIsolated(int v1) {
        return parent(v1) >= 0;
    }

    public boolean isConnected(int v1, int v2) {
        validate(v1);
        validate(v2);
        return find(v1) == find(v2);
    }

    public void connect(int v1, int v2) {
        validate(v1);
        validate(v2);
        if (isConnected(v1, v2)) {
            return;
        } else if (sizeOf(v1) > sizeOf(v2)) {
            parent[find(v1)] += parent[find(v2)];
            parent[find(v2)] = find(v1);
        } else {
            parent[find(v2)] += parent[find(v1)];
            parent[find(v1)] = find(v2);
        }
    }

    public int find(int v1) {
        validate(v1);
        while (parent(v1) >= 0) {
            v1 = parent(v1);
        }
        return v1;
    }
}