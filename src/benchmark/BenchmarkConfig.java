package benchmark;

/**
 * Configurações para a execução do Benchmark.
 */
public class BenchmarkConfig {
    
    // Valores padrão
    private int[] vertexCounts = {10, 50, 100, 500, 1000};
    private double[] densities = {0.1, 0.5, 0.9}; // 10% (esparso), 50% (médio), 90% (denso)

    public BenchmarkConfig() {
    }

    public int[] getVertexCounts() {
        return vertexCounts;
    }

    public void setVertexCounts(int[] vertexCounts) {
        this.vertexCounts = vertexCounts;
    }

    public double[] getDensities() {
        return densities;
    }

    public void setDensities(double[] densities) {
        this.densities = densities;
    }
}