package algorithms.backtracking;

import algorithms.common.MSTResult;
import graph.Edge;

import java.util.List;

/**
 * Resultado específico do algoritmo de Backtracking para MST.
 *
 * Estende {@link MSTResult} adicionando métricas de exploração
 * do espaço de busca, úteis para analisar o comportamento do
 * backtracking em relação aos algoritmos gulosos.
 */
public class BacktrackingResult extends MSTResult {

    private final long exploredStates;
    private final long prunedStates;
    private final int maxRecursionDepth;

    /**
     * Cria um resultado do algoritmo de Backtracking.
     *
     * @param edges arestas pertencentes à MST
     * @param totalWeight peso total da árvore
     * @param exploredStates quantidade de estados (nós) explorados
     * @param prunedStates quantidade de estados podados
     * @param maxRecursionDepth profundidade máxima atingida na recursão
     */
    public BacktrackingResult(
            List<Edge> edges,
            double totalWeight,
            long exploredStates,
            long prunedStates,
            int maxRecursionDepth
    ) {
        super(edges, totalWeight);
        this.exploredStates = exploredStates;
        this.prunedStates = prunedStates;
        this.maxRecursionDepth = maxRecursionDepth;
    }

    /**
     * Retorna a quantidade de estados explorados durante a busca.
     */
    public long getExploredStates() {
        return exploredStates;
    }

    /**
     * Retorna a quantidade de estados podados (descartados) durante a busca.
     */
    public long getPrunedStates() {
        return prunedStates;
    }

    /**
     * Retorna a profundidade máxima atingida pela recursão.
     */
    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(super.toString());
        result.append("\n\nMétricas do Backtracking:\n");
        result.append("Estados explorados: ")
                .append(exploredStates)
                .append("\n");
        result.append("Estados podados: ")
                .append(prunedStates)
                .append("\n");
        result.append("Profundidade máxima de recursão: ")
                .append(maxRecursionDepth);

        return result.toString();
    }
}