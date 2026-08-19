package utils;

import benchmark.Metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * Responsável por exportar métricas do benchmark
 * para arquivos CSV.
 */
public final class CSVWriter {

    private CSVWriter() {
        /*
         * Classe utilitária.
         */
    }

    /**
     * Escreve todas as métricas em um arquivo CSV.
     *
     * @param metrics lista de métricas
     * @param filePath caminho do arquivo de saída
     */
    public static void write(
            List<Metrics> metrics,
            String filePath
    ) {

        if (metrics == null) {
            throw new IllegalArgumentException(
                    "A lista de métricas não pode ser nula."
            );
        }

        if (filePath == null
                || filePath.isBlank()) {

            throw new IllegalArgumentException(
                    "O caminho do arquivo não pode ser vazio."
            );
        }

        Path path =
                Paths.get(filePath);

        try {

            Path parent =
                    path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 path,
                                 StandardCharsets.UTF_8
                         )) {

                writeHeader(writer);

                for (Metrics metric :
                        metrics) {

                    writeMetric(
                            writer,
                            metric
                    );
                }
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao escrever o arquivo CSV: "
                            + filePath,
                    e
            );
        }
    }

    /**
     * Escreve o cabeçalho do CSV.
     */
    private static void writeHeader(
            BufferedWriter writer
    ) throws IOException {

        writer.write(
                "algorithm,"
                        + "vertices,"
                        + "edges,"
                        + "density,"
                        + "requested_density,"
                        + "actual_density,"
                        + "seed,"
                        + "repetition,"
                        + "execution_time_ns,"
                        + "memory_bytes,"
                        + "mst_weight,"
                        + "success,"
                        + "error_message,"
                        + "states_explored,"
                        + "recursive_calls,"
                        + "prunings,"
                        + "max_depth"
        );

        writer.newLine();
    }

    /**
     * Escreve uma linha de métricas.
     */
    private static void writeMetric(
            BufferedWriter writer,
            Metrics metric
    ) throws IOException {

        if (metric == null) {
            return;
        }

        String line =
                csv(metric.getAlgorithmName())
                        + ","
                        + metric.getVertices()
                        + ","
                        + metric.getEdges()
                        + ","
                        + csv(metric.getDensityName())
                        + ","
                        + formatDouble(
                        metric.getRequestedDensity()
                )
                        + ","
                        + formatDouble(
                        metric.getActualDensity()
                )
                        + ","
                        + metric.getSeed()
                        + ","
                        + metric.getRepetition()
                        + ","
                        + metric.getExecutionTimeNanos()
                        + ","
                        + metric.getMemoryUsedBytes()
                        + ","
                        + formatDouble(
                        metric.getMstWeight()
                )
                        + ","
                        + metric.isSuccess()
                        + ","
                        + csv(
                        metric.getErrorMessage()
                )
                        + ","
                        + metric.getStatesExplored()
                        + ","
                        + metric.getRecursiveCalls()
                        + ","
                        + metric.getPrunings()
                        + ","
                        + metric.getMaxDepth();

        writer.write(line);
        writer.newLine();
    }

    /**
     * Formata números de ponto flutuante usando ponto
     * como separador decimal.
     */
    private static String formatDouble(
            double value
    ) {

        if (Double.isNaN(value)) {
            return "";
        }

        if (Double.isInfinite(value)) {
            return "";
        }

        return String.format(
                Locale.US,
                "%.10f",
                value
        );
    }

    /**
     * Escapa um valor para o formato CSV.
     *
     * Caso contenha vírgula, aspas ou quebra de linha,
     * o valor é colocado entre aspas.
     */
    private static String csv(
            String value
    ) {

        if (value == null) {
            return "";
        }

        if (value.contains("\"")
                || value.contains(",")
                || value.contains("\n")
                || value.contains("\r")) {

            return "\""
                    + value.replace(
                    "\"",
                    "\"\""
            )
                    + "\"";
        }

        return value;
    }
}