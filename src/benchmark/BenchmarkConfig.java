package benchmark;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configura os experimentos do benchmark.
 *
 * A classe não executa os algoritmos.
 * Ela apenas define como os experimentos devem ser realizados.
 */
public class BenchmarkConfig {

    /**
     * Tamanhos utilizados pelos algoritmos gerais:
     * Prim e Kruskal.
     */
    private int[] vertexCounts;

    /**
     * Tamanhos específicos utilizados pelo Backtracking.
     */
    private int[] backtrackingVertexCounts;

    /**
     * Categorias de densidade.
     *
     * O valor representa:
     *
     * E / Emax
     *
     * onde:
     *
     * Emax = V(V - 1) / 2
     */
    private Map<String, Double> densityLevels;

    /**
     * Quantidade de execuções medidas por configuração.
     */
    private int repetitions;

    /**
     * Quantidade de execuções de aquecimento da JVM.
     */
    private int warmupRuns;

    /**
     * Seed utilizada para reproduzir as instâncias.
     */
    private long seed;

    /**
     * Peso mínimo das arestas.
     */
    private double minWeight;

    /**
     * Peso máximo das arestas.
     */
    private double maxWeight;

    /**
     * Limite máximo de vértices para o Backtracking.
     */
    private int maxBacktrackingVertices;

    /**
     * Arquivo CSV de saída.
     */
    private String outputFile;

    public BenchmarkConfig() {

        this.vertexCounts = new int[]{
                10,
                50,
                100,
                500,
                1000
        };

        this.backtrackingVertexCounts = new int[]{
                5,
                6,
                8,
                10,
                12,
                15
        };

        /*
         * IMPORTANTE:
         *
         * Esses nomes precisam ser exatamente os mesmos
         * utilizados pelo Main.generateInstances().
         */
        this.densityLevels = new LinkedHashMap<>();

        densityLevels.put("sparse", 0.01);
        densityLevels.put("medium", 0.10);
        densityLevels.put("dense", 0.50);

        this.repetitions = 10;

        this.warmupRuns = 2;

        this.seed = 42L;

        this.minWeight = 1.0;

        this.maxWeight = 100.0;

        this.maxBacktrackingVertices = 15;

        this.outputFile =
                "results/raw/resultado_benchmark.csv";
    }

    public int[] getVertexCounts() {
        return Arrays.copyOf(
                vertexCounts,
                vertexCounts.length
        );
    }

    public void setVertexCounts(
            int[] vertexCounts
    ) {

        if (vertexCounts == null
                || vertexCounts.length == 0) {

            throw new IllegalArgumentException(
                    "É necessário informar pelo menos um "
                            + "tamanho de grafo."
            );
        }

        for (int vertices : vertexCounts) {

            if (vertices <= 0) {

                throw new IllegalArgumentException(
                        "A quantidade de vértices deve ser positiva."
                );
            }
        }

        this.vertexCounts =
                Arrays.copyOf(
                        vertexCounts,
                        vertexCounts.length
                );
    }

    public int[] getBacktrackingVertexCounts() {
        return Arrays.copyOf(
                backtrackingVertexCounts,
                backtrackingVertexCounts.length
        );
    }

    public void setBacktrackingVertexCounts(
            int[] backtrackingVertexCounts
    ) {

        if (backtrackingVertexCounts == null
                || backtrackingVertexCounts.length == 0) {

            throw new IllegalArgumentException(
                    "É necessário informar pelo menos um "
                            + "tamanho para Backtracking."
            );
        }

        for (int vertices :
                backtrackingVertexCounts) {

            if (vertices <= 0) {

                throw new IllegalArgumentException(
                        "A quantidade de vértices deve ser positiva."
                );
            }
        }

        this.backtrackingVertexCounts =
                Arrays.copyOf(
                        backtrackingVertexCounts,
                        backtrackingVertexCounts.length
                );
    }

    public Map<String, Double> getDensityLevels() {

        return Collections.unmodifiableMap(
                densityLevels
        );
    }

    public void setDensityLevels(
            Map<String, Double> densityLevels
    ) {

        if (densityLevels == null
                || densityLevels.isEmpty()) {

            throw new IllegalArgumentException(
                    "É necessário informar pelo menos "
                            + "uma densidade."
            );
        }

        for (Map.Entry<String, Double> entry :
                densityLevels.entrySet()) {

            String name = entry.getKey();
            Double value = entry.getValue();

            if (name == null
                    || name.isBlank()) {

                throw new IllegalArgumentException(
                        "O nome da densidade não pode ser vazio."
                );
            }

            if (value == null
                    || value <= 0.0
                    || value > 1.0) {

                throw new IllegalArgumentException(
                        "A densidade deve estar no intervalo (0, 1]."
                );
            }
        }

        this.densityLevels =
                new LinkedHashMap<>(
                        densityLevels
                );
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(
            int repetitions
    ) {

        if (repetitions <= 0) {

            throw new IllegalArgumentException(
                    "O número de repetições deve ser positivo."
            );
        }

        this.repetitions = repetitions;
    }

    public int getWarmupRuns() {
        return warmupRuns;
    }

    public void setWarmupRuns(
            int warmupRuns
    ) {

        if (warmupRuns < 0) {

            throw new IllegalArgumentException(
                    "O número de warm-ups não pode ser negativo."
            );
        }

        this.warmupRuns = warmupRuns;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(
            double minWeight
    ) {

        if (minWeight < 0) {

            throw new IllegalArgumentException(
                    "O peso mínimo não pode ser negativo."
            );
        }

        if (minWeight > maxWeight) {

            throw new IllegalArgumentException(
                    "O peso mínimo não pode ser maior "
                            + "que o peso máximo."
            );
        }

        this.minWeight = minWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(
            double maxWeight
    ) {

        if (maxWeight < minWeight) {

            throw new IllegalArgumentException(
                    "O peso máximo deve ser maior ou igual "
                            + "ao peso mínimo."
            );
        }

        this.maxWeight = maxWeight;
    }

    public int getMaxBacktrackingVertices() {
        return maxBacktrackingVertices;
    }

    public void setMaxBacktrackingVertices(
            int maxBacktrackingVertices
    ) {

        if (maxBacktrackingVertices <= 0) {

            throw new IllegalArgumentException(
                    "O limite de Backtracking deve ser positivo."
            );
        }

        this.maxBacktrackingVertices =
                maxBacktrackingVertices;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(
            String outputFile
    ) {

        if (outputFile == null
                || outputFile.isBlank()) {

            throw new IllegalArgumentException(
                    "O arquivo de saída não pode ser vazio."
            );
        }

        this.outputFile = outputFile;
    }
}