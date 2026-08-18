package algorithms.common;

import graph.Graph;

/**
 * Interface comum para algoritmos de Árvore Geradora Mínima (MST).
 */
public interface MSTAlgorithm {

    /**
     * Executa o algoritmo sobre o grafo informado.
     *
     * @param graph grafo ponderado e conexo
     * @return resultado da MST encontrada
     */
    MSTResult execute(Graph graph);

    /**
     * Retorna o nome da implementação do algoritmo.
     *
     * @return nome do algoritmo
     */
    String getName();
}