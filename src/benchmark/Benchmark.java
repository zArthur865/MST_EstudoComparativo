package benchmark;

import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import algorithms.greedy.kruskal.Kruskal;
import algorithms.greedy.prim.PrimAdjList;
// import algorithms.backtracking.MSTBacktracking; // matheus Descomenta isso aqui quando implementar

import graph.Graph;
import graph.GraphGenerator;
import utils.CSVWriter;
import utils.MSTValidator;
import utils.MemoryMonitor;
import utils.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por executar os benchmarks dos algoritmos de MST.
 */
public class Benchmark {

    private final List<MSTAlgorithm> algorithms;
    private final BenchmarkConfig config;

    public Benchmark(BenchmarkConfig config) {
        this.config = config;
        this.algorithms = new ArrayList<>();
        
        // Adiciona os algoritmos a serem testados
        this.algorithms.add(new Kruskal());
        this.algorithms.add(new PrimAdjList());
        // this.algorithms.add(new MSTBacktracking()); // Adiciona o backtracking aqui viss
    }

    /**
     * Executa o benchmark para as configurações fornecidas.
     */
    public void run() {
        System.out.println("Iniciando Benchmark de Algoritmos MST...");
        System.out.println("=========================================");

        List<Metrics> allMetrics = new ArrayList<>();

        for (int vertices : config.getVertexCounts()) {
            
            for (double density : config.getDensities()) {
                System.out.printf("\nTestando para %d vértices e densidade de %.1f%%\n", vertices, (density * 100));
                
                // Cálculo das arestas
                long maxEdges = ((long) vertices * (vertices - 1)) / 2;
                int requestedEdges = (int) (maxEdges * density);
                
                // Um grafo conexo precisa de pelo menos V - 1 arestas.
                // Também não pode exceder o maxEdges.
                int edges = Math.max(vertices - 1, requestedEdges);
                edges = (int) Math.min(edges, maxEdges);

                // Geração do Grafo (Pesos de 1.0 a 100.0, seed fixa em 42 para reprodutibilidade)
                Graph graph = GraphGenerator.generate(vertices, edges, 1.0, 100.0, 42L);

                // Referência para validar se os algoritmos seguintes encontram o mesmo peso
                MSTResult referenceResult = null;

                for (MSTAlgorithm algorithm : algorithms) {
                    MSTResult result = runAlgorithmAndRecordMetrics(algorithm, graph, allMetrics, referenceResult);
                    
                    // Salva o primeiro resultado bem-sucedido como referência para os próximos
                    if (referenceResult == null && result != null) {
                        referenceResult = result; 
                    }
                }
            }
        }
        
        System.out.println("\nBenchmark finalizado. Exportando dados...");
        CSVWriter.write(allMetrics, "resultado_benchmark.csv");
    }

    /**
     * Executa um algoritmo, grava as métricas e retorna o resultado da MST.
     */
    private MSTResult runAlgorithmAndRecordMetrics(
            MSTAlgorithm algorithm, 
            Graph graph, 
            List<Metrics> metricsList,
            MSTResult referenceResult) {
            
        Metrics metrics = new Metrics(algorithm.getName(), graph.getVertices(), graph.getEdges().size());
        
        MemoryMonitor memoryMonitor = new MemoryMonitor();
        Timer timer = new Timer();
        MSTResult result = null;
        
        try {
            // Inicializa os monitores
            memoryMonitor.start();
            timer.start();
            
            // Executa o algoritmo
            result = algorithm.execute(graph);
            
            // Para os monitores
            timer.stop();
            memoryMonitor.stop();
            
            // Salva os dados brutos de tempo e memória
            metrics.setExecutionTime(timer.getElapsedTimeNanos());
            metrics.setMemoryUsed(memoryMonitor.getUsedMemoryBytes());
            
            // Validações
            if (!MSTValidator.isValidStructure(graph, result)) {
                throw new IllegalStateException("Estrutura da MST inválida (quantidade de arestas incorreta).");
            }
            
            if (referenceResult != null && !MSTValidator.areWeightsEqual(referenceResult, result)) {
                throw new IllegalStateException("O peso encontrado (" + result.getTotalWeight() + 
                                                ") difere da referência (" + referenceResult.getTotalWeight() + ").");
            }

            metrics.setSuccess(true);
            
        } catch (Exception e) {
            timer.stop();
            memoryMonitor.stop();
            metrics.setSuccess(false);
            metrics.setErrorMessage(e.getMessage());
        }
        
        metricsList.add(metrics);
        printMetrics(metrics);
        
        return result;
    }
    
    /**
     * Exibe as métricas parciais no console.
     */
    private void printMetrics(Metrics metrics) {
        if (metrics.isSuccess()) {
            System.out.printf("[%s] V: %d, E: %d | Tempo: %.2f ms | Memória: %d KB%n",
                    metrics.getAlgorithmName(),
                    metrics.getVertices(),
                    metrics.getEdges(),
                    metrics.getExecutionTime() / 1_000_000.0, 
                    metrics.getMemoryUsed() / 1024);
        } else {
             System.out.printf("[%s] V: %d, E: %d | FALHOU: %s%n",
                    metrics.getAlgorithmName(),
                    metrics.getVertices(),
                    metrics.getEdges(),
                    metrics.getErrorMessage());
        }
    }
    
    // Método principal
    public static void main(String[] args) {
        BenchmarkConfig config = new BenchmarkConfig();
        
        // Exemplo: Testando configurações com grafos variando de 10 a 1000 vértices
        config.setVertexCounts(new int[]{10, 50, 100, 500, 1000});
        
        // Exemplo: 10% (esparso), 50% (médio) e 90% (denso)
        config.setDensities(new double[]{0.1, 0.5, 0.9});
        
        Benchmark benchmark = new Benchmark(config);
        benchmark.run();
    }
}