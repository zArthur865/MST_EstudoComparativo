package benchmark;

import algorithms.backtracking.BacktrackingMetrics;
import algorithms.backtracking.MSTBacktracking;
import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import algorithms.greedy.kruskal.Kruskal;
import algorithms.greedy.prim.PrimAdjList;
import algorithms.greedy.prim.PrimMatrix;

import graph.Graph;
import graph.GraphLoader;

import utils.CSVWriter;
import utils.MSTValidator;
import utils.MemoryMonitor;
import utils.Timer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Executa os experimentos comparativos dos algoritmos
 * de Árvore Geradora Mínima.
 *
 * As instâncias utilizadas pelos experimentos são carregadas
 * dos arquivos previamente gerados e armazenados em instances/.
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
         * Prim + Lista de Adjacência.
         */
        this.algorithms.add(
                new PrimAdjList()
        );

        /*
         * Estratégia Gulosa:
         * Prim + Matriz de Adjacência.
         */
        this.algorithms.add(
                new PrimMatrix()
        );

        /*
         * Estratégia Gulosa:
         * Kruskal + Union-Find.
         */
        this.algorithms.add(
                new Kruskal()
        );

        /*
         * Estratégia de Backtracking.
         */
        this.algorithms.add(
                new MSTBacktracking()
        );
    }

    /**
     * Executa todos os experimentos configurados.
     *
     * As instâncias são carregadas dos arquivos existentes
     * no diretório instances/.
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
         * Junta os tamanhos gerais com os tamanhos
         * específicos do Backtracking.
         *
         * LinkedHashSet evita duplicações.
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
         * Percorre cada tamanho de entrada.
         */
        for (int vertices : allVertexCounts) {

            /*
             * Percorre cada categoria de densidade.
             */
            for (Map.Entry<String, Double> densityEntry :
                    config.getDensityLevels().entrySet()) {

                String densityName =
                        densityEntry.getKey();

                double requestedDensity =
                        densityEntry.getValue();

                /*
                 * Calcula a quantidade de arestas esperada
                 * para localizar o arquivo correspondente.
                 */
                int edges =
                        calculateEdgeCount(
                                vertices,
                                requestedDensity
                        );

                /*
                 * Localiza a instância previamente gerada.
                 */
                Path instancePath =
                        buildInstancePath(
                                vertices,
                                densityName,
                                edges,
                                config.getSeed()
                        );

                System.out.println(
                        "\n----------------------------------------------"
                );

                System.out.printf(
                        "Instância: V=%d | E=%d | "
                                + "densidade=%s | arquivo=%s%n",
                        vertices,
                        edges,
                        densityName,
                        instancePath
                );

                /*
                 * Carrega a instância salva.
                 */
                Graph graph;

                try {

                    graph =
                            GraphLoader.load(
                                    instancePath
                            );

                } catch (IOException e) {

                    System.err.println(
                            "Não foi possível carregar a instância:"
                    );

                    System.err.println(
                            instancePath
                    );

                    System.err.println(
                            "Execute primeiro o comando "
                                    + "'generate' do Main."
                    );

                    throw new RuntimeException(
                            "Falha ao carregar instância.",
                            e
                    );
                }

                /*
                 * Verificação de segurança:
                 * o arquivo carregado deve corresponder
                 * à configuração do experimento.
                 */
                validateLoadedGraph(
                        graph,
                        vertices,
                        edges,
                        instancePath
                );

                /*
                 * Calcula a densidade REAL da instância carregada.
                 */
                double actualDensity =
                        calculateActualDensity(
                                graph.getVertices(),
                                graph.getEdgeCount()
                        );

                System.out.printf(
                        "Densidade solicitada: %.2f%%%n",
                        requestedDensity * 100.0
                );

                System.out.printf(
                        "Densidade real: %.2f%%%n",
                        actualDensity * 100.0
                );

                /*
                 * Calcula uma MST de referência.
                 *
                 * Essa execução não entra na medição.
                 */
                MSTResult referenceResult =
                        calculateReferenceMST(
                                graph
                        );

                /*
                 * Executa cada algoritmo.
                 */
                for (MSTAlgorithm algorithm :
                        algorithms) {

                    /*
                     * Backtracking possui um limite específico
                     * devido ao crescimento do espaço de busca.
                     */
                    if (isBacktracking(algorithm)
                            && vertices
                            > config.getMaxBacktrackingVertices()) {

                        System.out.printf(
                                "[%s] V=%d -> "
                                        + "ignorado: acima do limite "
                                        + "de Backtracking (%d)%n",
                                algorithm.getName(),
                                vertices,
                                config.getMaxBacktrackingVertices()
                        );

                        continue;
                    }

                    /*
                     * Aquecimento da JVM.
                     *
                     * Não entra nos resultados.
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
     * Calcula a quantidade de arestas utilizada para uma
     * determinada densidade.
     *
     * density = E / Emax
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

        long requestedEdges =
                Math.round(
                        maxEdges * density
                );

        /*
         * Um grafo conexo precisa de pelo menos V - 1
         * arestas.
         */
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
     * Calcula a densidade efetivamente presente no grafo.
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
     * Constrói o caminho da instância.
     *
     * A estrutura é a mesma criada pelo Main.generate().
     */
    private Path buildInstancePath(
            int vertices,
            String densityName,
            int edges,
            long seed
    ) {

        String category;

        if (vertices <= 20) {

            category = "small";

        } else if (vertices <= 100) {

            category = "medium";

        } else if (vertices <= 1000) {

            category = "large";

        } else {

            category = "very_large";
        }

        String filename =
                String.format(
                        "graph_V%d_E%d_seed%d.graph",
                        vertices,
                        edges,
                        seed
                );

        return Path.of(
                "instances",
                category,
                densityName,
                filename
        );
    }

    /**
     * Verifica se a instância carregada corresponde
     * às dimensões esperadas.
     */
    private void validateLoadedGraph(
            Graph graph,
            int expectedVertices,
            int expectedEdges,
            Path path
    ) {

        if (graph == null) {

            throw new IllegalStateException(
                    "O arquivo "
                            + path
                            + " não produziu um grafo válido."
            );
        }

        if (graph.getVertices()
                != expectedVertices) {

            throw new IllegalStateException(
                    "A instância "
                            + path
                            + " possui "
                            + graph.getVertices()
                            + " vértices, mas eram esperados "
                            + expectedVertices
                            + "."
            );
        }

        if (graph.getEdgeCount()
                != expectedEdges) {

            throw new IllegalStateException(
                    "A instância "
                            + path
                            + " possui "
                            + graph.getEdgeCount()
                            + " arestas, mas eram esperadas "
                            + expectedEdges
                            + "."
            );
        }
    }

    /**
     * Calcula uma MST de referência utilizando Kruskal.
     *
     * Essa execução ocorre fora das medições do benchmark.
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

        if (!MSTValidator.isValidMST(
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
     * Realiza as execuções de warm-up.
     *
     * Não produz métricas.
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
     * Executa uma única medição.
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
             * O MemoryMonitor é iniciado primeiro porque
             * pode chamar System.gc().
             *
             * Assim, essa preparação não entra no tempo
             * medido do algoritmo.
             */
            memoryMonitor.start();
            memoryMonitorStarted = true;

            /*
             * Início efetivo da medição de tempo.
             */
            timer.start();
            timerStarted = true;

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

            if (timerStarted) {
                timer.stop();
            }

            if (memoryMonitorStarted) {
                memoryMonitor.stop();
            }
        }

        /*
         * Registra o tempo.
         */
        if (timerStarted) {

            metrics.setExecutionTime(
                    timer.getElapsedTimeNanos()
            );
        }

        /*
         * Registra a memória.
         */
        if (memoryMonitorStarted) {

            metrics.setMemoryUsed(
                    memoryMonitor.getUsedMemoryBytes()
            );
        }

        /*
         * Se o algoritmo falhou, não há resultado
         * a ser validado.
         */
        if (result == null) {
            return metrics;
        }

        /*
         * Armazena o peso encontrado.
         */
        metrics.setMstWeight(
                result.getTotalWeight()
        );

        try {

            /*
             * Validação completa:
             * estrutura + peso armazenado.
             */
            if (!MSTValidator.isValidMST(
                    graph,
                    result
            )) {

                throw new IllegalStateException(
                        "A solução produzida "
                                + "não é uma MST válida."
                );
            }

            /*
             * A solução deve possuir o mesmo peso
             * da MST de referência.
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
             * Coleta as métricas específicas
             * do Backtracking.
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
     * para Metrics.
     */
    private void collectBacktrackingMetrics(
            MSTAlgorithm algorithm,
            Metrics metrics
    ) {

        if (!(algorithm
                instanceof MSTBacktracking)) {

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
     * Verifica se determinado algoritmo é Backtracking.
     */
    private boolean isBacktracking(
            MSTAlgorithm algorithm
    ) {

        return algorithm
                instanceof MSTBacktracking;
    }

    /**
     * Exibe uma execução no console.
     */
    private void printMetrics(
            Metrics metrics
    ) {

        if (!metrics.isSuccess()) {

            System.out.printf(
                    "[%s] V=%d E=%d "
                            + "densidade=%s "
                            + "rep=%d "
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
         * Exibe informações extras do Backtracking.
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
}