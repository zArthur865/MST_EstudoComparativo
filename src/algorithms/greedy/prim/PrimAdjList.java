package algorithms.greedy.prim;

import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import datastructures.MinPriorityQueue;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de Prim utilizando
 * lista de adjacência e fila de prioridade.
 */
public class PrimAdjList implements MSTAlgorithm {

    @Override
    public MSTResult execute(Graph graph) {

        if (graph == null) {
            throw new IllegalArgumentException(
                    "O grafo não pode ser nulo."
            );
        }

        int vertices = graph.getVertices();

        /*
         * Um grafo com 0 vértices não possui MST.
         */
        if (vertices == 0) {
            throw new IllegalArgumentException(
                    "O grafo deve possuir pelo menos um vértice."
            );
        }

        boolean[] visited = new boolean[vertices];

        List<Edge> mstEdges = new ArrayList<>();

        MinPriorityQueue priorityQueue =
                new MinPriorityQueue();

        double totalWeight = 0.0;

        /*
         * Podemos começar por qualquer vértice.
         *
         * Utilizamos o vértice 0 como padrão.
         */
        int startVertex = 0;

        visited[startVertex] = true;

        /*
         * Coloca na fila todas as arestas que partem
         * do vértice inicial.
         */
        for (Edge edge : graph.getNeighbors(startVertex)) {

            priorityQueue.add(edge);
        }

        /*
         * Uma MST de V vértices possui exatamente V - 1 arestas.
         */
        while (!priorityQueue.isEmpty()
                && mstEdges.size() < vertices - 1) {

            Edge currentEdge =
                    priorityQueue.extractMin();

            int source = currentEdge.getSource();
            int destination =
                    currentEdge.getDestination();

            /*
             * Como o grafo é não direcionado, a mesma aresta
             * aparece nas listas dos dois vértices.
             *
             * Descobrimos qual extremidade ainda não foi visitada.
             */
            int newVertex;

            if (visited[source] && !visited[destination]) {

                newVertex = destination;

            } else if (!visited[source] && visited[destination]) {

                newVertex = source;

            } else {

                /*
                 * Dois casos:
                 *
                 * 1. Ambos já foram visitados:
                 *    a aresta criaria um ciclo.
                 *
                 * 2. Nenhum foi visitado:
                 *    essa aresta ainda não conecta a árvore
                 *    ao componente construído.
                 */
                continue;
            }

            /*
             * A aresta conecta a árvore atual a um novo vértice.
             */
            visited[newVertex] = true;

            mstEdges.add(currentEdge);

            totalWeight += currentEdge.getWeight();

            /*
             * Adiciona as novas arestas candidatas.
             */
            for (Edge edge : graph.getNeighbors(newVertex)) {

                int neighbor;

                if (edge.getSource() == newVertex) {
                    neighbor = edge.getDestination();
                } else {
                    neighbor = edge.getSource();
                }

                if (!visited[neighbor]) {
                    priorityQueue.add(edge);
                }
            }
        }

        /*
         * Se a quantidade de arestas não for V - 1,
         * o grafo não é conexo.
         */
        if (mstEdges.size() != vertices - 1) {

            throw new IllegalArgumentException(
                    "Não foi possível construir uma MST. "
                            + "O grafo provavelmente não é conexo."
            );
        }

        return new MSTResult(
                mstEdges,
                totalWeight
        );
    }

    @Override
    public String getName() {
        return "Prim - Lista de Adjacência";
    }
}