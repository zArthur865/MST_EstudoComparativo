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
 * Ponto de entrada principal do projeto MST.
 *
 * Comandos:
 *
 * generate
 *     Gera e salva as instâncias de teste.
 *
 * benchmark
 *     Executa os experimentos utilizando as instâncias salvas.
 *
 * help
 *     Exibe os comandos disponíveis.
 */
public class Main {

    public static void main(
            String[] args
    ) {

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
     * Gera e salva as instâncias oficiais
     * utilizadas pelos experimentos.
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
         * IMPORTANTE:
         *
         * Os nomes devem ser iguais aos nomes usados
         * no BenchmarkConfig.
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
                                maxEdges
                                        * density
                        );

                int edges =
                        (int) Math.max(
                                vertices - 1L,
                                requestedEdges
                        );

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
     * Executa o benchmark.
     *
     * O Benchmark irá carregar as instâncias
     * previamente salvas pelo comando generate.
     */
    private static void runBenchmark() {

        BenchmarkConfig config =
                new BenchmarkConfig();

        /*
         * Tamanhos gerais.
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
         * Tamanhos para Backtracking.
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

        config.setRepetitions(10);

        config.setWarmupRuns(2);

        config.setSeed(42L);

        config.setMinWeight(1.0);

        config.setMaxWeight(100.0);

        config.setMaxBacktrackingVertices(
                15
        );

        config.setOutputFile(
                "results/raw/resultado_benchmark.csv"
        );

        Benchmark benchmark =
                new Benchmark(config);

        benchmark.run();
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
                "  help"
        );

        System.out.println(
                "      Exibe esta mensagem."
        );
    }
}