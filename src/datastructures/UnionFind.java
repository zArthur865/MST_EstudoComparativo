package datastructures;

/**
 * Estrutura Union-Find (Disjoint Set Union).
 *
 * Permite manter conjuntos disjuntos e verificar
 * rapidamente se dois vértices pertencem ao mesmo conjunto.
 *
 * Utiliza:
 * - Path Compression
 * - Union by Rank
 */
public class UnionFind {

    private final int[] parent;
    private final int[] rank;

    /**
     * Cria uma estrutura contendo inicialmente
     * um conjunto separado para cada elemento.
     *
     * @param size quantidade de elementos
     */
    public UnionFind(int size) {

        if (size < 0) {
            throw new IllegalArgumentException(
                    "O tamanho não pode ser negativo."
            );
        }

        parent = new int[size];
        rank = new int[size];

        for (int i = 0; i < size; i++) {

            parent[i] = i;
            rank[i] = 0;
        }
    }

    /**
     * Retorna o representante do conjunto de x.
     *
     * Utiliza Path Compression.
     */
    public int find(int x) {

        validate(x);

        if (parent[x] != x) {

            parent[x] =
                    find(parent[x]);
        }

        return parent[x];
    }

    /**
     * Une os conjuntos que contêm x e y.
     *
     * Utiliza Union by Rank.
     *
     * @return true se os conjuntos foram unidos;
     *         false se já pertenciam ao mesmo conjunto.
     */
    public boolean union(int x, int y) {

        validate(x);
        validate(y);

        int rootX = find(x);
        int rootY = find(y);

        /*
         * Já pertencem ao mesmo conjunto.
         * Portanto, adicionar a aresta criaria um ciclo.
         */
        if (rootX == rootY) {
            return false;
        }

        /*
         * Coloca a árvore de menor rank
         * abaixo da árvore de maior rank.
         */
        if (rank[rootX] < rank[rootY]) {

            parent[rootX] = rootY;

        } else if (rank[rootX] > rank[rootY]) {

            parent[rootY] = rootX;

        } else {

            parent[rootY] = rootX;
            rank[rootX]++;
        }

        return true;
    }

    private void validate(int x) {

        if (x < 0 || x >= parent.length) {

            throw new IllegalArgumentException(
                    "Elemento inválido: " + x
            );
        }
    }
}