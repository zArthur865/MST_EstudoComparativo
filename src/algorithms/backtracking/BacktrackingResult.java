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
<<<<<<< HEAD
<<<<<<< HEAD
    private final int completedSolutions;
=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920

    /**
     * Cria um resultado do algoritmo de Backtracking.
     *
     * @param edges arestas pertencentes à MST
     * @param totalWeight peso total da árvore
     * @param exploredStates quantidade de estados (nós) explorados
     * @param prunedStates quantidade de estados podados
     * @param maxRecursionDepth profundidade máxima atingida na recursão
<<<<<<< HEAD
<<<<<<< HEAD
     * @param completedSolutions quantidade de árvores geradoras completas encontradas
=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
     */
    public BacktrackingResult(
            List<Edge> edges,
            double totalWeight,
            long exploredStates,
            long prunedStates,
<<<<<<< HEAD
<<<<<<< HEAD
            int maxRecursionDepth,
            int completedSolutions
=======
            int maxRecursionDepth
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
=======
            int maxRecursionDepth
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
    ) {
        super(edges, totalWeight);
        this.exploredStates = exploredStates;
        this.prunedStates = prunedStates;
        this.maxRecursionDepth = maxRecursionDepth;
<<<<<<< HEAD
<<<<<<< HEAD
        this.completedSolutions = completedSolutions;
=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
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

<<<<<<< HEAD
<<<<<<< HEAD
    /**
     * Retorna a quantidade de árvores geradoras completas
     * encontradas durante a busca.
     */
    public int getCompletedSolutions() {
        return completedSolutions;
    }

=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
=======
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
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
<<<<<<< HEAD
<<<<<<< HEAD
                .append(maxRecursionDepth)
                .append("\n");
        result.append("Soluções completas encontradas: ")
                .append(completedSolutions);
=======
                .append(maxRecursionDepth);
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920
=======
                .append(maxRecursionDepth);
>>>>>>> 9ed9fa75fb897f8162ac5bb3d7abec031ff01920

        return result.toString();
    }
}