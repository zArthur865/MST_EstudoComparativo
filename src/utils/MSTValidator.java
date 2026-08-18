package utils;

import algorithms.common.MSTResult;
import graph.Graph;

/**
 * Utilitário para validar as Árvores Geradoras Mínimas geradas pelos algoritmos.
 */
public class MSTValidator {

    public static boolean isValidStructure(Graph graph, MSTResult result) {
        if (graph == null || result == null) {
            return false;
        }
        return result.getEdgeCount() == graph.getVertices() - 1;
    }

    /**
     * Compara dois resultados de MST para garantir que encontraram o mesmo peso mínimo.
     * Útil para validar se a sua implementação de backtracking chegou na mesma resposta que os gulosos.
     */
    public static boolean areWeightsEqual(MSTResult expected, MSTResult actual) {
        if (expected == null || actual == null) {
            return false;
        }
        // Margem de erro 
        double epsilon = 1e-9;
        return Math.abs(expected.getTotalWeight() - actual.getTotalWeight()) < epsilon;
    }
}