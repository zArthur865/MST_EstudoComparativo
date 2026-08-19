package benchmark;

import algorithms.backtracking.BacktrackingMetrics;
import algorithms.backtracking.MSTBacktracking;
import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import algorithms.greedy.kruskal.Kruskal;
import algorithms.greedy.prim.PrimAdjList;

import graph.Graph;
import graph.GraphGenerator;

import utils.CSVWriter;
import utils.MSTValidator;
import utils.MemoryMonitor;
import utils.Timer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Executa os experimentos comparativos dos algoritmos
 * de Árvore Geradora Mínima.
 */
public class Benchmark {

    private final List<MSTAlgorithm> algorithms;

    private final BenchmarkConfig config;

    public Benchmark(BenchmarkConfig config) {

        if (config == null) {

            throw new IllegalArgumentException(
                    "A configuração do benchmark não pode ser nula."
            );
        }

        this.config = config;

        this.algorithms = new ArrayList<>();

        /*
         * Estratégia Gulosa:
         * Prim + Lista de Adjacência
         */
        this.algorithms.add(
                new PrimAdjList()
        );

        /*
         * Estratégia Gulosa:
         * Kruskal + Union-Find
         */
        this.algorithms.add(
                new Kruskal()
        );

        /*
         * Backtracking.
         *
         * Será executado somente nos tamanhos
         * permitidos pela configuração.
         */
        this.algorithms.add(
                new MSTBacktracking()
        );
    }

    /**
     * Executa todos os experimentos.
     */
    public void run() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "   BENCHMARK - ÁRVORE GERADORA MÍNIMA"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "Repetições: "
                        + config.getRepetitions()
        );

        System.out.println(
                "Warm-up: "
                        + config.getWarmupRuns()
        );

        System.out.println(
                "Seed: "
                        + config.getSeed()
        );

        System.out.println();

        List<Metrics> allMetrics =
                new ArrayList<>();

        /*
         * Junta os tamanhos gerais e os tamanhos
         * específicos do Backtracking.
         *
         * LinkedHashSet evita duplicações mantendo
         * a ordem de inserção.
         */
        Set<Integer> allVertexCounts =
                new LinkedHashSet<>();

        for (int vertices :
                config.getVertexCounts()) {

            allVertexCounts.add(vertices);
        }

        for (int vertices :
                config.getBacktrackingVertexCounts()) {

            allVertexCounts.add(vertices);
        }

        /*
         * Para cada tamanho de entrada.
         */
        for (int vertices : allVertexCounts) {

            /*
             * Para cada categoria de densidade.
             */
            for (Map.Entry<String, Double> densityEntry :
                    config.getDensityLevels().entrySet()) {

                String densityName =
                        densityEntry.getKey();

                double requestedDensity =
                        densityEntry.getValue();

                /*
                 * Calcula a quantidade de arestas.
                 */
                int edges =
                        calculateEdgeCount(
                                vertices,
                                requestedDensity
                        );

                /*
                 * Geração da instância.
                 *
                 * A mesma instância será usada por
                 * todos os algoritmos.
                 */
                Graph graph =
                        GraphGenerator.generate(
                                vertices,
                                edges,
                                config.getMinWeight(),
                                config.getMaxWeight(),
                                config.getSeed()
                        );

                double actualDensity =
                        calculateActualDensity(
                                vertices,
                                edges
                        );

                System.out.println(
                        "\n----------------------------------------------"
                );

                System.out.printf(
                        "V = %d | E = %d | "
                                + "densidade solicitada = %.2f%% | "
                                + "densidade real = %.2f%%%n",
                        vertices,
                        edges,
                        requestedDensity * 100.0,
                        actualDensity * 100.0
                );

                /*
                 * Calculamos uma MST de referência antes
                 * das execuções medidas.
                 *
                 * Essa execução NÃO entra nas métricas.
                 */
                MSTResult referenceResult =
                        calculateReferenceMST(graph);

                /*
                 * Executa cada algoritmo.
                 */
                for (MSTAlgorithm algorithm :
                        algorithms) {

                    /*
                     * Evita executar Backtracking em tamanhos
                     * que o próprio benchmark considera
                     * impraticáveis.
                     */
                    if (isBacktracking(algorithm)
                            && vertices
                            > config.getMaxBacktrackingVertices()) {

                        System.out.printf(
                                "[%s] V = %d -> "
                                        + "ignorado: acima do limite "
                                        + "de Backtracking (%d)%n",
                                algorithm.getName(),
                                vertices,
                                config.getMaxBacktrackingVertices()
                        );

                        continue;
                    }

                    /*
                     * Executa warm-up.
                     */
                    performWarmup(
                            algorithm,
                            graph
                    );

                    /*
                     * Execuções medidas.
                     */
                    for (int repetition = 1;
                         repetition <= config.getRepetitions();
                         repetition++) {

                        Metrics metrics =
                                runMeasuredExecution(
                                        algorithm,
                                        graph,
                                        referenceResult,
                                        densityName,
                                        requestedDensity,
                                        actualDensity,
                                        repetition
                                );

                        allMetrics.add(metrics);

                        printMetrics(metrics);
                    }
                }
            }
        }

        System.out.println(
                "\n=============================================="
        );

        System.out.println(
                "Benchmark finalizado."
        );

        System.out.println(
                "Exportando resultados..."
        );

        CSVWriter.write(
                allMetrics,
                config.getOutputFile()
        );

        System.out.println(
                "Resultados salvos em: "
                        + config.getOutputFile()
        );
    }

    /**
     * Calcula a quantidade de arestas a partir da densidade.
     */
    private int calculateEdgeCount(
            int vertices,
            double density
    ) {

        if (vertices <= 0) {

            throw new IllegalArgumentException(
                    "O número de vértices deve ser positivo."
            );
        }

        long maxEdges =
                ((long) vertices * (vertices - 1))
                        / 2;

        /*
         * Um grafo conexo precisa possuir pelo menos V - 1
         * arestas.
         */
        long requestedEdges =
                Math.round(
                        maxEdges * density
                );

        long edges =
                Math.max(
                        vertices - 1L,
                        requestedEdges
                );

        edges =
                Math.min(
                        edges,
                        maxEdges
                );

        return (int) edges;
    }

    /**
     * Calcula a densidade efetivamente utilizada.
     */
    private double calculateActualDensity(
            int vertices,
            int edges
    ) {

        if (vertices <= 1) {
            return 1.0;
        }

        long maxEdges =
                ((long) vertices * (vertices - 1))
                        / 2;

        return (double) edges
                / maxEdges;
    }

    /**
     * Calcula uma MST de referência.
     *
     * A execução fica fora da medição.
     *
     * Kruskal é utilizado como referência porque
     * produz uma MST ótima.
     */
    private MSTResult calculateReferenceMST(
            Graph graph
    ) {

        MSTAlgorithm referenceAlgorithm =
                new Kruskal();

        MSTResult referenceResult =
                referenceAlgorithm.execute(
                        graph
                );

        if (!MSTValidator.isValidStructure(
                graph,
                referenceResult
        )) {

            throw new IllegalStateException(
                    "A MST de referência é inválida."
            );
        }

        return referenceResult;
    }

    /**
     * Executa uma quantidade de warm-ups.
     *
     * Os resultados não são registrados.
     */
    private void performWarmup(
            MSTAlgorithm algorithm,
            Graph graph
    ) {

        for (int i = 0;
             i < config.getWarmupRuns();
             i++) {

            algorithm.execute(graph);
        }
    }

    /**
     * Executa uma medição individual.
     */
    private Metrics runMeasuredExecution(
            MSTAlgorithm algorithm,
            Graph graph,
            MSTResult referenceResult,
            String densityName,
            double requestedDensity,
            double actualDensity,
            int repetition
    ) {

        Metrics metrics =
                new Metrics(
                        algorithm.getName(),
                        graph.getVertices(),
                        graph.getEdgeCount(),
                        densityName,
                        requestedDensity,
                        actualDensity,
                        config.getSeed(),
                        repetition
                );

        Timer timer =
                new Timer();

        MemoryMonitor memoryMonitor =
                new MemoryMonitor();

        MSTResult result = null;

        boolean timerStarted = false;
        boolean memoryMonitorStarted = false;

        try {

            /*
             * Início das medições.
             */
            timer.start();
            timerStarted = true;

            memoryMonitor.start();
            memoryMonitorStarted = true;

            /*
             * Executa apenas o algoritmo.
             */
            result =
                    algorithm.execute(graph);

        } catch (Exception e) {

            metrics.setSuccess(false);

            metrics.setErrorMessage(
                    e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
            );

        } finally {

            /*
             * Finaliza as medições exatamente uma vez.
             */
            if (timerStarted) {
                timer.stop();
            }

            if (memoryMonitorStarted) {
                memoryMonitor.stop();
            }
        }

        /*
         * Registra tempo e memória apenas se a execução
         * foi efetivamente iniciada.
         */
        if (timerStarted) {

            metrics.setExecutionTime(
                    timer.getElapsedTimeNanos()
            );
        }

        if (memoryMonitorStarted) {

            metrics.setMemoryUsed(
                    memoryMonitor.getUsedMemoryBytes()
            );
        }

        /*
         * Se o algoritmo lançou exceção, não há resultado
         * para validar.
         */
        if (result == null) {

            return metrics;
        }

        /*
         * Guarda o peso encontrado.
         */
        metrics.setMstWeight(
                result.getTotalWeight()
        );

        /*
         * Valida a estrutura da MST.
         */
        try {

            if (!MSTValidator.isValidStructure(
                    graph,
                    result
            )) {

                throw new IllegalStateException(
                        "A estrutura da MST é inválida."
                );
            }

            /*
             * Compara o peso com a solução de referência.
             */
            if (!MSTValidator.areWeightsEqual(
                    referenceResult,
                    result
            )) {

                throw new IllegalStateException(
                        "O peso encontrado ("
                                + result.getTotalWeight()
                                + ") difere da referência ("
                                + referenceResult.getTotalWeight()
                                + ")."
                );
            }

            /*
             * Coleta métricas específicas do Backtracking.
             */
            collectBacktrackingMetrics(
                    algorithm,
                    metrics
            );

            metrics.setSuccess(true);

        } catch (Exception e) {

            metrics.setSuccess(false);

            metrics.setErrorMessage(
                    e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
            );
        }

        return metrics;
    }

    /**
     * Copia as métricas específicas do Backtracking
     * para o objeto geral de benchmark.
     */
    private void collectBacktrackingMetrics(
            MSTAlgorithm algorithm,
            Metrics metrics
    ) {

        if (!(algorithm instanceof MSTBacktracking)) {
            return;
        }

        MSTBacktracking backtracking =
                (MSTBacktracking) algorithm;

        BacktrackingMetrics backtrackingMetrics =
                backtracking.getMetrics();

        metrics.setStatesExplored(
                backtrackingMetrics
                        .getStatesExplored()
        );

        metrics.setRecursiveCalls(
                backtrackingMetrics
                        .getRecursiveCalls()
        );

        metrics.setPrunings(
                backtrackingMetrics
                        .getPrunings()
        );

        metrics.setMaxDepth(
                backtrackingMetrics
                        .getMaxDepth()
        );
    }

    /**
     * Verifica se determinado algoritmo é o Backtracking.
     */
    private boolean isBacktracking(
            MSTAlgorithm algorithm
    ) {

        return algorithm instanceof MSTBacktracking;
    }

    /**
     * Exibe os resultados de uma execução.
     */
    private void printMetrics(
            Metrics metrics
    ) {

        if (!metrics.isSuccess()) {

            System.out.printf(
                    "[%s] V=%d E=%d "
                            + "densidade=%s "
                            + "repetição=%d "
                            + "| FALHOU: %s%n",

                    metrics.getAlgorithmName(),
                    metrics.getVertices(),
                    metrics.getEdges(),
                    metrics.getDensityName(),
                    metrics.getRepetition(),
                    metrics.getErrorMessage()
            );

            return;
        }

        System.out.printf(
                "[%s] V=%d E=%d "
                        + "densidade=%s "
                        + "rep=%d "
                        + "| Tempo=%.4f ms "
                        + "| Memória=%d KB "
                        + "| MST=%.4f%n",

                metrics.getAlgorithmName(),
                metrics.getVertices(),
                metrics.getEdges(),
                metrics.getDensityName(),
                metrics.getRepetition(),
                metrics.getExecutionTimeNanos()
                        / 1_000_000.0,
                metrics.getMemoryUsedBytes()
                        / 1024,
                metrics.getMstWeight()
        );

        /*
         * Exibe métricas extras somente para Backtracking.
         */
        if (metrics.getStatesExplored() > 0
                || metrics.getRecursiveCalls() > 0) {

            System.out.printf(
                    "    Backtracking: "
                            + "estados=%d | "
                            + "recursões=%d | "
                            + "podas=%d | "
                            + "profundidade=%d%n",

                    metrics.getStatesExplored(),
                    metrics.getRecursiveCalls(),
                    metrics.getPrunings(),
                    metrics.getMaxDepth()
            );
        }
    }

    /**
     * Permite executar o benchmark diretamente.
     */
    public static void main(String[] args) {

        BenchmarkConfig config =
                new BenchmarkConfig();

        /*
         * Configuração dos experimentos.
         */
        config.setVertexCounts(
                new int[]{
                        10,
                        50,
                        100,
                        500,
                        1000
                }
        );

        config.setBacktrackingVertexCounts(
                new int[]{
                        5,
                        6,
                        8,
                        10,
                        12,
                        15
                }
        );

        config.setRepetitions(10);

        config.setWarmupRuns(2);

        config.setSeed(42L);

        config.setMinWeight(1.0);

        config.setMaxWeight(100.0);

        config.setMaxBacktrackingVertices(15);

        config.setOutputFile(
                "results/raw/resultado_benchmark.csv"
        );

        Benchmark benchmark =
                new Benchmark(config);

        benchmark.run();
    }
}