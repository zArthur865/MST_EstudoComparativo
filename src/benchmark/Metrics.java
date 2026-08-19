package benchmark;

/**
 * Armazena as métricas de uma execução de um algoritmo de MST.
 *
 * Cada objeto representa uma execução individual.
 */
public class Metrics {

    private final String algorithmName;

    private final int vertices;

    private final int edges;

    /**
     * Nome da categoria de densidade.
     */
    private final String densityName;

    /**
     * Densidade solicitada na geração.
     */
    private final double requestedDensity;

    /**
     * Densidade efetivamente obtida.
     *
     * Pode ser diferente da solicitada para grafos muito
     * pequenos, pois um grafo conexo precisa de pelo
     * menos V - 1 arestas.
     */
    private final double actualDensity;

    /**
     * Seed utilizada na instância.
     */
    private final long seed;

    /**
     * Número da repetição.
     */
    private final int repetition;

    /**
     * Tempo em nanossegundos.
     */
    private long executionTimeNanos;

    /**
     * Memória estimada utilizada durante a execução.
     */
    private long memoryUsedBytes;

    /**
     * Peso total da MST encontrada.
     */
    private double mstWeight;

    /**
     * Indica se a execução foi concluída com sucesso.
     */
    private boolean success;

    /**
     * Mensagem de erro, quando aplicável.
     */
    private String errorMessage;

    /*
     * Métricas específicas do Backtracking.
     *
     * Para Prim e Kruskal permanecem em zero.
     */
    private long statesExplored;
    private long recursiveCalls;
    private long prunings;
    private int maxDepth;

    public Metrics(
            String algorithmName,
            int vertices,
            int edges,
            String densityName,
            double requestedDensity,
            double actualDensity,
            long seed,
            int repetition
    ) {

        if (algorithmName == null
                || algorithmName.isBlank()) {

            throw new IllegalArgumentException(
                    "O nome do algoritmo não pode ser vazio."
            );
        }

        this.algorithmName = algorithmName;
        this.vertices = vertices;
        this.edges = edges;
        this.densityName = densityName;
        this.requestedDensity = requestedDensity;
        this.actualDensity = actualDensity;
        this.seed = seed;
        this.repetition = repetition;

        this.executionTimeNanos = 0L;
        this.memoryUsedBytes = 0L;
        this.mstWeight = Double.NaN;

        this.success = false;
        this.errorMessage = null;

        this.statesExplored = 0L;
        this.recursiveCalls = 0L;
        this.prunings = 0L;
        this.maxDepth = 0;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public int getVertices() {
        return vertices;
    }

    public int getEdges() {
        return edges;
    }

    public String getDensityName() {
        return densityName;
    }

    public double getRequestedDensity() {
        return requestedDensity;
    }

    public double getActualDensity() {
        return actualDensity;
    }

    public long getSeed() {
        return seed;
    }

    public int getRepetition() {
        return repetition;
    }

    public long getExecutionTime() {
        return executionTimeNanos;
    }

    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    public void setExecutionTime(long executionTimeNanos) {

        if (executionTimeNanos < 0) {

            throw new IllegalArgumentException(
                    "O tempo de execução não pode ser negativo."
            );
        }

        this.executionTimeNanos =
                executionTimeNanos;
    }

    public long getMemoryUsed() {
        return memoryUsedBytes;
    }

    public long getMemoryUsedBytes() {
        return memoryUsedBytes;
    }

    public void setMemoryUsed(long memoryUsedBytes) {

        if (memoryUsedBytes < 0) {

            throw new IllegalArgumentException(
                    "A memória utilizada não pode ser negativa."
            );
        }

        this.memoryUsedBytes =
                memoryUsedBytes;
    }

    public double getMstWeight() {
        return mstWeight;
    }

    public void setMstWeight(double mstWeight) {
        this.mstWeight = mstWeight;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getStatesExplored() {
        return statesExplored;
    }

    public void setStatesExplored(long statesExplored) {
        this.statesExplored = statesExplored;
    }

    public long getRecursiveCalls() {
        return recursiveCalls;
    }

    public void setRecursiveCalls(long recursiveCalls) {
        this.recursiveCalls = recursiveCalls;
    }

    public long getPrunings() {
        return prunings;
    }

    public void setPrunings(long prunings) {
        this.prunings = prunings;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}