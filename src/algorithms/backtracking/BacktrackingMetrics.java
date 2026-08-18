package algorithms.backtracking;

/**
 * Métricas coletadas durante a execução do algoritmo de Backtracking.
 *
 * Diferente das métricas genéricas do benchmark ({@code Metrics}),
 * esta classe concentra os contadores internos da busca exaustiva:
 *
 * <ul>
 *   <li>estados (nós) explorados na árvore de decisão;</li>
 *   <li>estados podados pelas regras de corte (branches descartados);</li>
 *   <li>profundidade máxima atingida pela recursão;</li>
 *   <li>quantidade de soluções completas (árvores geradoras) encontradas.</li>
 * </ul>
 *
 * Esses dados são utilizados pelo {@link MSTBacktracking} para medir o
 * esforço computacional da busca e podem ser incorporados ao
 * {@link BacktrackingResult} para análise comparativa com os algoritmos
 * gulosos (Prim e Kruskal).
 */
public class BacktrackingMetrics {

    private long exploredStates;
    private long prunedStates;
    private int maxRecursionDepth;
    private int completedSolutions;

    /**
     * Cria um conjunto de métricas zerado.
     */
    public BacktrackingMetrics() {
    }

    /**
     * Cria um conjunto de métricas com valores iniciais definidos.
     *
     * @param exploredStates    quantidade inicial de estados explorados
     * @param prunedStates      quantidade inicial de estados podados
     * @param maxRecursionDepth profundidade máxima inicial
     * @param completedSolutions quantidade inicial de soluções completas
     */
    public BacktrackingMetrics(
            long exploredStates,
            long prunedStates,
            int maxRecursionDepth,
            int completedSolutions
    ) {
        this.exploredStates = exploredStates;
        this.prunedStates = prunedStates;
        this.maxRecursionDepth = maxRecursionDepth;
        this.completedSolutions = completedSolutions;
    }

    /**
     * Retorna a quantidade de estados explorados até o momento.
     */
    public long getExploredStates() {
        return exploredStates;
    }

    /**
     * Retorna a quantidade de estados podados até o momento.
     */
    public long getPrunedStates() {
        return prunedStates;
    }

    /**
     * Retorna a profundidade máxima de recursão atingida.
     */
    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    /**
     * Retorna a quantidade de soluções completas encontradas.
     */
    public int getCompletedSolutions() {
        return completedSolutions;
    }

    /**
     * Incrementa o contador de estados explorados.
     */
    public void incrementExploredStates() {
        exploredStates++;
    }

    /**
     * Incrementa o contador de estados podados.
     */
    public void incrementPrunedStates() {
        prunedStates++;
    }

    /**
     * Atualiza a profundidade máxima de recursão, se a profundidade
     * informada for maior que o valor corrente.
     *
     * @param depth profundidade atingida na recursão atual
     */
    public void updateMaxRecursionDepth(int depth) {
        maxRecursionDepth = Math.max(maxRecursionDepth, depth);
    }

    /**
     * Incrementa o contador de soluções completas encontradas.
     */
    public void incrementCompletedSolutions() {
        completedSolutions++;
    }

    /**
     * Zera todos os contadores.
     */
    public void reset() {
        exploredStates = 0;
        prunedStates = 0;
        maxRecursionDepth = 0;
        completedSolutions = 0;
    }

    @Override
    public String toString() {
        return "BacktrackingMetrics{"
                + "exploredStates=" + exploredStates
                + ", prunedStates=" + prunedStates
                + ", maxRecursionDepth=" + maxRecursionDepth
                + ", completedSolutions=" + completedSolutions
                + '}';
    }
}