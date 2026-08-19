package algorithms.greedy.prim;

import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de Prim utilizando
 * uma matriz de adjacência.
 *
 * Nesta implementação, a matriz é construída a partir
 * das arestas do Graph no início da execução.
 *
 * Complexidade:
 *
 * Construção da matriz:
 * O(E)
 *
 * Execução de Prim:
 * O(V²)
 *
 * Complexidade espacial:
 * O(V²)
 */
public class PrimMatrix implements MSTAlgorithm {

    @Override
    public MSTResult execute(Graph graph) {

        if (graph == null) {
            throw new IllegalArgumentException(
                    "O grafo não pode ser nulo."
            );
        }

        int vertices = graph.getVertices();

        if (vertices == 0) {
            throw new IllegalArgumentException(
                    "O grafo deve possuir pelo menos um vértice."
            );
        }

        /*
         * Caso especial: grafo com um único vértice.
         *
         * A MST é composta por zero arestas
         * e possui peso total igual a zero.
         */
        if (vertices == 1) {
            return new MSTResult(
                    new ArrayList<>(),
                    0.0
            );
        }

        /*
         * Constrói a matriz de adjacência.
         *
         * Double.POSITIVE_INFINITY representa
         * ausência de uma aresta.
         */
        double[][] matrix =
                new double[vertices][vertices];

        for (int i = 0; i < vertices; i++) {

            for (int j = 0; j < vertices; j++) {

                matrix[i][j] =
                        Double.POSITIVE_INFINITY;
            }
        }

        /*
         * Como o grafo é não direcionado,
         * matrix[u][v] = matrix[v][u].
         */
        for (Edge edge : graph.getEdges()) {

            int source = edge.getSource();
            int destination = edge.getDestination();
            double weight = edge.getWeight();

            /*
             * Caso existam arestas paralelas entre
             * os mesmos vértices, mantemos a menor.
             *
             * Isso torna a matriz consistente com o
             * problema de MST.
             */
            if (weight < matrix[source][destination]) {

                matrix[source][destination] =
                        weight;

                matrix[destination][source] =
                        weight;
            }
        }

        /*
         * visited[v] indica se o vértice já pertence
         * à MST construída.
         */
        boolean[] visited =
                new boolean[vertices];

        /*
         * menorWeight[v] representa o peso da menor
         * aresta conhecida que conecta v à árvore atual.
         */
        double[] menorWeight =
                new double[vertices];

        /*
         * parent[v] armazena o vértice que conecta v
         * à MST.
         */
        int[] parent =
                new int[vertices];

        /*
         * Inicialização.
         */
        for (int i = 0; i < vertices; i++) {

            menorWeight[i] =
                    Double.POSITIVE_INFINITY;

            parent[i] = -1;
        }

        /*
         * Começamos pelo vértice 0.
         */
        menorWeight[0] = 0.0;

        List<Edge> mstEdges =
                new ArrayList<>();

        double totalWeight = 0.0;

        /*
         * Adicionamos um vértice por iteração.
         */
        for (int iteration = 0;
             iteration < vertices;
             iteration++) {

            int current =
                    findMinimumVertex(
                            menorWeight,
                            visited
                    );

            /*
             * Se não existe vértice alcançável,
             * o grafo não é conexo.
             */
            if (current == -1) {

                throw new IllegalArgumentException(
                        "Não foi possível construir uma MST. "
                                + "O grafo provavelmente não é conexo."
                );
            }

            visited[current] = true;

            /*
             * Se current possui um parent,
             * a aresta correspondente entra na MST.
             */
            if (parent[current] != -1) {

                Edge edge =
                        new Edge(
                                parent[current],
                                current,
                                matrix[parent[current]][current]
                        );

                mstEdges.add(edge);

                totalWeight +=
                        edge.getWeight();
            }

            /*
             * Atualiza os pesos mínimos dos vizinhos.
             */
            for (int neighbor = 0;
                 neighbor < vertices;
                 neighbor++) {

                double edgeWeight =
                        matrix[current][neighbor];

                if (!visited[neighbor]
                        && edgeWeight
                        < menorWeight[neighbor]) {

                    menorWeight[neighbor] =
                            edgeWeight;

                    parent[neighbor] =
                            current;
                }
            }
        }

        /*
         * Uma MST válida para V vértices possui
         * exatamente V - 1 arestas.
         */
        if (mstEdges.size()
                != vertices - 1) {

            throw new IllegalArgumentException(
                    "A solução produzida não possui "
                            + "V - 1 arestas."
            );
        }

        return new MSTResult(
                mstEdges,
                totalWeight
        );
    }

    /**
     * Encontra o vértice não visitado com menor
     * valor em menorWeight.
     *
     * Complexidade:
     * O(V)
     */
    private int findMinimumVertex(
            double[] menorWeight,
            boolean[] visited
    ) {

        double minimum =
                Double.POSITIVE_INFINITY;

        int minimumVertex = -1;

        for (int vertex = 0;
             vertex < menorWeight.length;
             vertex++) {

            if (!visited[vertex]
                    && menorWeight[vertex]
                    < minimum) {

                minimum =
                        menorWeight[vertex];

                minimumVertex =
                        vertex;
            }
        }

        return minimumVertex;
    }

    @Override
    public String getName() {
        return "Prim - Matriz de Adjacência";
    }
}