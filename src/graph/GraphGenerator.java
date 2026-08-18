package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Responsável pela geração de grafos ponderados e conexos
 * utilizados nos experimentos.
 */
public class GraphGenerator {

    /**
     * Gera um grafo ponderado, não direcionado e conexo.
     *
     * O método primeiro cria uma árvore aleatória conectando
     * todos os vértices. Depois adiciona arestas extras
     * aleatoriamente até atingir a quantidade solicitada.
     *
     * @param vertices quantidade de vértices
     * @param edges quantidade de arestas
     * @param minWeight peso mínimo
     * @param maxWeight peso máximo
     * @param seed semente utilizada para reprodução do experimento
     *
     * @return grafo gerado
     */
    public static Graph generate(
            int vertices,
            int edges,
            double minWeight,
            double maxWeight,
            long seed
    ) {

        validateParameters(
                vertices,
                edges,
                minWeight,
                maxWeight
        );

        Graph graph = new Graph(vertices);

        Random random = new Random(seed);

        Set<String> existingEdges = new HashSet<>();

        /*
         * Primeiro criamos uma árvore.
         *
         * Isso garante que o grafo será conexo.
         *
         * Uma árvore com V vértices possui V - 1 arestas.
         */
        for (int vertex = 1; vertex < vertices; vertex++) {

            int parent = random.nextInt(vertex);

            double weight = randomWeight(
                    random,
                    minWeight,
                    maxWeight
            );

            graph.addEdge(
                    parent,
                    vertex,
                    weight
            );

            existingEdges.add(
                    edgeKey(parent, vertex)
            );
        }

        /*
         * Depois adicionamos arestas aleatórias
         * até atingir a quantidade desejada.
         */
        while (graph.getEdgeCount() < edges) {

            int source = random.nextInt(vertices);
            int destination = random.nextInt(vertices);

            if (source == destination) {
                continue;
            }

            String key = edgeKey(source, destination);

            if (existingEdges.contains(key)) {
                continue;
            }

            double weight = randomWeight(
                    random,
                    minWeight,
                    maxWeight
            );

            graph.addEdge(
                    source,
                    destination,
                    weight
            );

            existingEdges.add(key);
        }

        return graph;
    }

    /**
     * Gera um peso aleatório dentro do intervalo especificado.
     */
    private static double randomWeight(
            Random random,
            double minWeight,
            double maxWeight
    ) {

        return minWeight
                + (maxWeight - minWeight) * random.nextDouble();
    }

    /**
     * Cria uma chave única para uma aresta não direcionada.
     *
     * Assim:
     *
     * (2, 5)
     *
     * e
     *
     * (5, 2)
     *
     * são consideradas a mesma aresta.
     */
    private static String edgeKey(
            int source,
            int destination
    ) {

        int min = Math.min(source, destination);
        int max = Math.max(source, destination);

        return min + "-" + max;
    }

    /**
     * Valida os parâmetros da geração.
     */
    private static void validateParameters(
            int vertices,
            int edges,
            double minWeight,
            double maxWeight
    ) {

        if (vertices < 1) {
            throw new IllegalArgumentException(
                    "O grafo deve possuir pelo menos um vértice."
            );
        }

        long maxPossibleEdges =
                ((long) vertices * (vertices - 1)) / 2;

        if (edges < vertices - 1) {
            throw new IllegalArgumentException(
                    "Um grafo conexo precisa possuir pelo menos V - 1 arestas."
            );
        }

        if (edges > maxPossibleEdges) {
            throw new IllegalArgumentException(
                    "Quantidade de arestas excede o máximo possível para "
                            + "um grafo simples não direcionado."
            );
        }

        if (minWeight < 0) {
            throw new IllegalArgumentException(
                    "O peso mínimo não pode ser negativo."
            );
        }

        if (maxWeight < minWeight) {
            throw new IllegalArgumentException(
                    "O peso máximo deve ser maior ou igual ao peso mínimo."
            );
        }
    }
}