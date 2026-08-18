package algorithms.common;

import graph.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa o resultado obtido por um algoritmo de MST.
 */
public class MSTResult {

    private final List<Edge> edges;
    private final double totalWeight;

    /**
     * Cria um resultado de MST.
     *
     * @param edges arestas pertencentes à MST
     * @param totalWeight peso total da árvore
     */
    public MSTResult(
            List<Edge> edges,
            double totalWeight
    ) {

        this.edges = new ArrayList<>(edges);
        this.totalWeight = totalWeight;
    }

    /**
     * Retorna as arestas da MST.
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Retorna o peso total da MST.
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Retorna a quantidade de arestas da MST.
     */
    public int getEdgeCount() {
        return edges.size();
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        result.append("Arestas da MST:\n");

        for (Edge edge : edges) {
            result.append(edge).append("\n");
        }

        result.append("Peso total: ")
                .append(totalWeight);

        return result.toString();
    }
}