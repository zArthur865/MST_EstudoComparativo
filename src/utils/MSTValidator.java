package utils;

import algorithms.common.MSTResult;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por validar soluções de MST.
 */
public final class MSTValidator {

    private static final double EPSILON = 1e-9;

    private MSTValidator() {
        /*
         * Classe utilitária.
         */
    }

    /**
     * Verifica se uma solução representa uma árvore
     * geradora válida para o grafo informado.
     *
     * Condições:
     * - exatamente V - 1 arestas;
     * - nenhuma aresta inválida;
     * - sem ciclos;
     * - todos os vértices conectados.
     *
     * @param graph grafo original
     * @param result resultado produzido pelo algoritmo
     * @return true se a estrutura for uma árvore geradora válida
     */
    public static boolean isValidStructure(
            Graph graph,
            MSTResult result
    ) {

        if (graph == null || result == null) {
            return false;
        }

        int vertices = graph.getVertices();

        List<Edge> mstEdges =
                result.getEdges();

        /*
         * Um grafo com um único vértice possui MST vazia.
         */
        if (vertices == 1) {
            return mstEdges.isEmpty();
        }

        /*
         * Uma árvore com V vértices possui V - 1 arestas.
         */
        if (mstEdges.size() != vertices - 1) {
            return false;
        }

        /*
         * DSU simples para detectar ciclos.
         */
        DisjointSet set =
                new DisjointSet(vertices);

        for (Edge edge : mstEdges) {

            if (edge == null) {
                return false;
            }

            int source =
                    edge.getSource();

            int destination =
                    edge.getDestination();

            /*
             * Os vértices precisam pertencer ao grafo.
             */
            if (source < 0
                    || source >= vertices
                    || destination < 0
                    || destination >= vertices) {

                return false;
            }

            /*
             * A solução não pode possuir laços.
             */
            if (source == destination) {
                return false;
            }

            /*
             * A aresta precisa realmente existir no grafo.
             */
            if (!graph.hasEdge(
                    source,
                    destination
            )) {

                return false;
            }

            /*
             * Se os dois vértices já pertencem ao mesmo
             * conjunto, a nova aresta cria um ciclo.
             */
            if (!set.union(
                    source,
                    destination
            )) {

                return false;
            }
        }

        /*
         * Se temos V - 1 arestas e nenhuma cria ciclo,
         * a estrutura é uma árvore.
         *
         * A presença de V - 1 arestas sem ciclos em V vértices
         * implica conectividade.
         */
        return set.getNumberOfSets() == 1;
    }

    /**
     * Verifica se duas soluções possuem o mesmo peso total
     * considerando uma tolerância para ponto flutuante.
     */
    public static boolean areWeightsEqual(
            MSTResult first,
            MSTResult second
    ) {

        if (first == null || second == null) {
            return false;
        }

        double difference =
                Math.abs(
                        first.getTotalWeight()
                                - second.getTotalWeight()
                );

        return difference <= EPSILON;
    }

    /**
     * Verifica se o peso armazenado no resultado corresponde
     * à soma das arestas da solução.
     */
    public static boolean hasCorrectTotalWeight(
            MSTResult result
    ) {

        if (result == null) {
            return false;
        }

        double calculatedWeight = 0.0;

        for (Edge edge :
                result.getEdges()) {

            calculatedWeight +=
                    edge.getWeight();
        }

        return Math.abs(
                calculatedWeight
                        - result.getTotalWeight()
        ) <= EPSILON;
    }

    /**
     * Validação completa:
     * estrutura + peso armazenado.
     */
    public static boolean isValidMST(
            Graph graph,
            MSTResult result
    ) {

        return isValidStructure(
                graph,
                result
        )
                && hasCorrectTotalWeight(result);
    }

    /**
     * Estrutura simples de conjuntos disjuntos usada apenas
     * para a validação.
     */
    private static class DisjointSet {

        private final int[] parent;
        private final int[] rank;
        private int numberOfSets;

        private DisjointSet(int size) {

            parent = new int[size];
            rank = new int[size];

            numberOfSets = size;

            for (int i = 0;
                 i < size;
                 i++) {

                parent[i] = i;
                rank[i] = 0;
            }
        }

        private int find(int value) {

            if (parent[value] != value) {

                parent[value] =
                        find(parent[value]);
            }

            return parent[value];
        }

        private boolean union(
                int first,
                int second
        ) {

            int rootFirst =
                    find(first);

            int rootSecond =
                    find(second);

            if (rootFirst == rootSecond) {
                return false;
            }

            if (rank[rootFirst]
                    < rank[rootSecond]) {

                parent[rootFirst] =
                        rootSecond;

            } else if (
                    rank[rootFirst]
                            > rank[rootSecond]) {

                parent[rootSecond] =
                        rootFirst;

            } else {

                parent[rootSecond] =
                        rootFirst;

                rank[rootFirst]++;
            }

            numberOfSets--;

            return true;
        }

        private int getNumberOfSets() {
            return numberOfSets;
        }
    }
}