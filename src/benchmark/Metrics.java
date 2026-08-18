package benchmark;

/**
 * Armazena as métricas coletadas durante a execução de um algoritmo.
 */
public class Metrics {
    
    private final String algorithmName;
    private final int vertices;
    private final int edges;
    
    private long executionTime; // em nanossegundos
    private long memoryUsed;    // em bytes
    
    private boolean success;
    private String errorMessage;

    public Metrics(String algorithmName, int vertices, int edges) {
        this.algorithmName = algorithmName;
        this.vertices = vertices;
        this.edges = edges;
    }

    public String getAlgorithmName() { return algorithmName; }
    public int getVertices() { return vertices; }
    public int getEdges() { return edges; }
    
    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    
    public long getMemoryUsed() { return memoryUsed; }
    public void setMemoryUsed(long memoryUsed) { this.memoryUsed = memoryUsed; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}