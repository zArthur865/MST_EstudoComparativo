import analysis.ChartGenerator;
import analysis.ResultProcessor;
import benchmark.Benchmark;
import benchmark.BenchmarkConfig;
import graph.Graph;
import graph.GraphGenerator;
import graph.GraphLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ponto de entrada principal do projeto de estudo comparativo
 * de algoritmos de Árvore Geradora Mínima (MST).
 *
 * Comandos disponíveis:
 *
 * generate
 *     Gera e salva as instâncias de teste.
 *
 * benchmark
 *     Executa os experimentos utilizando as instâncias salvas.
 *
 * analyze
 *     Processa os resultados brutos e gera os gráficos.
 *
 * help
 *     Exibe os comandos disponíveis.
 */
public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            return;
        }

        String command =
                args[0].toLowerCase();

        switch (command) {

            case "generate":
                generateInstances();
                break;

            case "benchmark":
                runBenchmark();
                break;

            case "analyze":
                analyzeResults();
                break;

            case "help":
                printUsage();
                break;

            default:

                System.out.println(
                        "Comando desconhecido: "
                                + args[0]
                );

                System.out.println();

                printUsage();
        }
    }

    /**
     * Gera e salva as instâncias oficiais utilizadas
     * nos experimentos.
     */
    private static void generateInstances() {

        /*
         * Tamanhos utilizados pelo projeto.
         *
         * Os menores também contemplam o Backtracking.
         */
        int[] vertexCounts = {
                5,
                6,
                8,
                10,
                12,
                15,
                50,
                100,
                500,
                1000
        };

        /*
         * Categorias de densidade.
         *
         * Os nomes precisam ser iguais aos utilizados
         * no BenchmarkConfig:
         *
         * sparse
         * medium
         * dense
         */
        Map<String, Double> densities =
                new LinkedHashMap<>();

        densities.put(
                "sparse",
                0.01
        );

        densities.put(
                "medium",
                0.10
        );

        densities.put(
                "dense",
                0.50
        );

        long seed = 42L;

        double minWeight = 1.0;
        double maxWeight = 100.0;

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "        GERAÇÃO DE INSTÂNCIAS MST"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println();

        for (int vertices :
                vertexCounts) {

            for (Map.Entry<String, Double> densityEntry :
                    densities.entrySet()) {

                String densityName =
                        densityEntry.getKey();

                double density =
                        densityEntry.getValue();

                long maxEdges =
                        ((long) vertices
                                * (vertices - 1))
                                / 2;

                long requestedEdges =
                        Math.round(
                                maxEdges * density
                        );

                /*
                 * Um grafo conexo precisa possuir
                 * pelo menos V - 1 arestas.
                 */
                int edges =
                        (int) Math.max(
                                vertices - 1L,
                                requestedEdges
                        );

                /*
                 * Não pode ultrapassar o número máximo
                 * de arestas de um grafo simples.
                 */
                edges =
                        (int) Math.min(
                                edges,
                                maxEdges
                        );

                Graph graph =
                        GraphGenerator.generate(
                                vertices,
                                edges,
                                minWeight,
                                maxWeight,
                                seed
                        );

                Path path =
                        buildInstancePath(
                                vertices,
                                densityName,
                                edges,
                                seed
                        );

                try {

                    GraphLoader.save(
                            graph,
                            path
                    );

                    System.out.println(
                            "Gerado: "
                                    + path
                    );

                } catch (IOException e) {

                    System.err.println(
                            "Erro ao salvar "
                                    + path
                                    + ": "
                                    + e.getMessage()
                    );

                    throw new RuntimeException(
                            "Falha ao gerar instância.",
                            e
                    );
                }
            }
        }

        System.out.println();

        System.out.println(
                "Geração concluída."
        );
    }

    /**
     * Constrói o caminho onde uma instância será salva.
     *
     * Estrutura:
     *
     * instances/
     * ├── small/
     * ├── medium/
     * ├── large/
     * └── very_large/
     */
    private static Path buildInstancePath(
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
     * Executa o benchmark utilizando as instâncias
     * previamente salvas.
     */
    private static void runBenchmark() {

        BenchmarkConfig config =
                new BenchmarkConfig();

        /*
         * Tamanhos gerais para Prim e Kruskal.
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

        /*
         * Tamanhos menores para o Backtracking.
         */
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

        /*
         * Quantidade de execuções medidas.
         */
        config.setRepetitions(10);

        /*
         * Aquecimento da JVM.
         */
        config.setWarmupRuns(2);

        /*
         * Seed das instâncias.
         */
        config.setSeed(42L);

        /*
         * Intervalo dos pesos.
         */
        config.setMinWeight(1.0);

        config.setMaxWeight(100.0);

        /*
         * Limite de tamanho para Backtracking.
         */
        config.setMaxBacktrackingVertices(15);

        /*
         * Arquivo de saída dos dados brutos.
         */
        config.setOutputFile(
                "results/raw/resultado_benchmark.csv"
        );

        Benchmark benchmark =
                new Benchmark(config);

        benchmark.run();
    }

    /**
     * Processa os resultados brutos e gera os gráficos.
     *
     * Fluxo:
     *
     * raw CSV
     *     ↓
     * summary.csv
     *     ↓
     * gráficos SVG
     */
    private static void analyzeResults() {

        String rawFile =
                "results/raw/resultado_benchmark.csv";

        String processedFile =
                "results/processed/summary.csv";

        String graphsDirectory =
                "results/graphs";

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       ANÁLISE DOS RESULTADOS"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println();

        /*
         * Processa o CSV bruto.
         */
        System.out.println(
                "Processando resultados..."
        );

        ResultProcessor.process(
                rawFile,
                processedFile
        );

        System.out.println();

        /*
         * Gera os gráficos.
         */
        System.out.println(
                "Gerando gráficos..."
        );

        ChartGenerator.generateAll(
                processedFile,
                graphsDirectory
        );

        System.out.println();

        System.out.println(
                "Análise concluída."
        );
    }

    /**
     * Exibe os comandos disponíveis.
     */
    private static void printUsage() {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "        MST - ESTUDO COMPARATIVO"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println();

        System.out.println(
                "Comandos:"
        );

        System.out.println();

        System.out.println(
                "  generate"
        );

        System.out.println(
                "      Gera e salva as instâncias de teste."
        );

        System.out.println();

        System.out.println(
                "  benchmark"
        );

        System.out.println(
                "      Executa os experimentos."
        );

        System.out.println();

        System.out.println(
                "  analyze"
        );

        System.out.println(
                "      Processa os resultados e gera os gráficos."
        );

        System.out.println();

        System.out.println(
                "  help"
        );

        System.out.println(
                "      Exibe esta mensagem."
        );
    }
}