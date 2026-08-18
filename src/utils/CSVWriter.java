package utils;

import benchmark.Metrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Utilitário para exportar os resultados do benchmark para um arquivo CSV.
 */
public class CSVWriter {

    /**
     * Salva a lista de métricas em um arquivo CSV.
     *
     * @param metricsList Lista de resultados do benchmark.
     * @param filePath    Caminho do arquivo (ex: "resultados_mst.csv").
     */
    public static void write(List<Metrics> metricsList, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Escreve o cabeçalho do CSV
            writer.println("Algoritmo,Vertices,Arestas,Tempo_ns,Memoria_bytes,Sucesso,Erro");

            for (Metrics m : metricsList) {
                // Trata a mensagem de erro para não quebrar o formato do CSV caso tenha vírgulas
                String errorMsg = m.getErrorMessage() == null ? "" : m.getErrorMessage().replace(",", ";");
                
                writer.printf("%s,%d,%d,%d,%d,%b,%s%n",
                        m.getAlgorithmName(),
                        m.getVertices(),
                        m.getEdges(),
                        m.getExecutionTime(),
                        m.getMemoryUsed(),
                        m.isSuccess(),
                        errorMsg
                );
            }
            System.out.println("Resultados exportados com sucesso para: " + filePath);
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo CSV: " + e.getMessage());
        }
    }
}