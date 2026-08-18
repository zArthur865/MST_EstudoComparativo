package algorithms.greedy.kruskal;

import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import datastructures.UnionFind;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de Kruskal.
 *
 * Estratégia:
 * 1. Ordenar as arestas pelo peso.
 * 2. Percorrer as arestas em ordem crescente.
 * 3. Adicionar uma aresta somente se ela não formar ciclo.
 * 4. Utilizar Union-Find para detectar ciclos.
 */
public class Kruskal implements MSTAlgorithm {

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
         * Criamos uma cópia para não modificar
         * a lista interna do Graph.
         */
        List<Edge> sortedEdges =
                new ArrayList<>(graph.getEdges());

        /*
         * Edge já implementa Comparable<Edge>,
         * portanto podemos utilizar sort().
         */
        sortedEdges.sort(null);

        UnionFind unionFind =
                new UnionFind(vertices);

        List<Edge> mstEdges =
                new ArrayList<>();

        double totalWeight = 0.0;

        /*
         * Percorre as arestas em ordem crescente de peso.
         */
        for (Edge edge : sortedEdges) {

            int source = edge.getSource();
            int destination =
                    edge.getDestination();

            /*
             * Se conseguir unir os conjuntos,
             * significa que a aresta não cria ciclo.
             */
            if (unionFind.union(
                    source,
                    destination)) {

                mstEdges.add(edge);

                totalWeight +=
                        edge.getWeight();

                /*
                 * MST possui V - 1 arestas.
                 */
                if (mstEdges.size()
                        == vertices - 1) {

                    break;
                }
            }
        }

        /*
         * Se não conseguimos V - 1 arestas,
         * o grafo não é conexo.
         */
        if (mstEdges.size()
                != vertices - 1) {

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
        return "Kruskal";
    }
}