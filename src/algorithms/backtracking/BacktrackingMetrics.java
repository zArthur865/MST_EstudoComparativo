package algorithms.backtracking;

/**
 * Métricas específicas da execução do algoritmo
 * de MST por Backtracking.
 */
public class BacktrackingMetrics {

    private long statesExplored;
    private long recursiveCalls;
    private long prunings;
    private int maxDepth;

    public BacktrackingMetrics() {
        this.statesExplored = 0;
        this.recursiveCalls = 0;
        this.prunings = 0;
        this.maxDepth = 0;
    }

    public void incrementStatesExplored() {
        statesExplored++;
    }

    public void incrementRecursiveCalls() {
        recursiveCalls++;
    }

    public void incrementPrunings() {
        prunings++;
    }

    public void updateMaxDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    public long getStatesExplored() {
        return statesExplored;
    }

    public long getRecursiveCalls() {
        return recursiveCalls;
    }

    public long getPrunings() {
        return prunings;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public String toString() {
        return "Estados explorados: " + statesExplored
                + "\nChamadas recursivas: " + recursiveCalls
                + "\nPodas: " + prunings
                + "\nProfundidade máxima: " + maxDepth;
    }
}