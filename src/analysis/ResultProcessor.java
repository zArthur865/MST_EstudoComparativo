package analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Processa os resultados brutos produzidos pelo benchmark.
 *
 * Entrada:
 * results/raw/resultado_benchmark.csv
 *
 * Saída:
 * results/processed/summary.csv
 */
public final class ResultProcessor {

    private ResultProcessor() {
    }

    /**
     * Processa o CSV bruto e gera o resumo agregado.
     *
     * @param inputPath caminho do CSV bruto
     * @param outputPath caminho do CSV processado
     */
    public static void process(
            String inputPath,
            String outputPath
    ) {

        Path input = Path.of(inputPath);
        Path output = Path.of(outputPath);

        if (!Files.exists(input)) {
            throw new IllegalArgumentException(
                    "Arquivo de resultados não encontrado: "
                            + input
            );
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            input,
                            StandardCharsets.UTF_8
                    );

            if (lines.isEmpty()) {
                throw new IllegalStateException(
                        "O arquivo de resultados está vazio."
                );
            }

            List<String> headers =
                    parseCsvLine(lines.get(0));

            Map<String, Integer> columnIndex =
                    createColumnIndex(headers);

            validateRequiredColumns(columnIndex);

            Map<String, Aggregate> aggregates =
                    new LinkedHashMap<>();

            for (int i = 1; i < lines.size(); i++) {

                if (lines.get(i).isBlank()) {
                    continue;
                }

                List<String> values =
                        parseCsvLine(lines.get(i));

                Map<String, String> row =
                        createRow(
                                columnIndex,
                                values
                        );

                String algorithm =
                        row.get("algorithm");

                int vertices =
                        Integer.parseInt(
                                row.get("vertices")
                        );

                int edges =
                        Integer.parseInt(
                                row.get("edges")
                        );

                String density =
                        row.get("density");

                String key =
                        algorithm
                                + "|"
                                + vertices
                                + "|"
                                + edges
                                + "|"
                                + density;

                Aggregate aggregate =
                        aggregates.computeIfAbsent(
                                key,
                                ignored ->
                                        new Aggregate(
                                                algorithm,
                                                vertices,
                                                edges,
                                                density,
                                                parseDouble(
                                                        row.get(
                                                                "requested_density"
                                                        )
                                                ),
                                                parseDouble(
                                                        row.get(
                                                                "actual_density"
                                                        )
                                                )
                                        )
                        );

                aggregate.add(row);
            }

            Path parent =
                    output.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            writeSummary(
                    aggregates,
                    output
            );

            System.out.println(
                    "Resultados processados com sucesso:"
            );

            System.out.println(
                    output
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao processar resultados.",
                    e
            );
        }
    }

    /**
     * Escreve o CSV agregado.
     */
    private static void writeSummary(
            Map<String, Aggregate> aggregates,
            Path output
    ) throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "algorithm,"
                            + "vertices,"
                            + "edges,"
                            + "density,"
                            + "requested_density,"
                            + "actual_density,"
                            + "executions,"
                            + "successful,"
                            + "failed,"
                            + "avg_time_ns,"
                            + "stddev_time_ns,"
                            + "avg_memory_bytes,"
                            + "avg_mst_weight,"
                            + "avg_states_explored,"
                            + "avg_recursive_calls,"
                            + "avg_prunings,"
                            + "avg_max_depth"
            );

            writer.newLine();

            for (Aggregate aggregate :
                    aggregates.values()) {

                writer.write(
                        csv(aggregate.algorithm)
                                + ","
                                + aggregate.vertices
                                + ","
                                + aggregate.edges
                                + ","
                                + csv(aggregate.density)
                                + ","
                                + format(
                                aggregate.requestedDensity
                        )
                                + ","
                                + format(
                                aggregate.actualDensity
                        )
                                + ","
                                + aggregate.executions
                                + ","
                                + aggregate.successful
                                + ","
                                + aggregate.failed
                                + ","
                                + format(
                                aggregate.getAverageTime()
                        )
                                + ","
                                + format(
                                aggregate.getStdDevTime()
                        )
                                + ","
                                + format(
                                aggregate.getAverageMemory()
                        )
                                + ","
                                + format(
                                aggregate.getAverageMstWeight()
                        )
                                + ","
                                + format(
                                aggregate.getAverageStates()
                        )
                                + ","
                                + format(
                                aggregate.getAverageRecursiveCalls()
                        )
                                + ","
                                + format(
                                aggregate.getAveragePrunings()
                        )
                                + ","
                                + format(
                                aggregate.getAverageDepth()
                        )
                );

                writer.newLine();
            }
        }
    }

    private static void validateRequiredColumns(
            Map<String, Integer> columns
    ) {

        String[] required = {
                "algorithm",
                "vertices",
                "edges",
                "density",
                "requested_density",
                "actual_density",
                "execution_time_ns",
                "memory_bytes",
                "mst_weight",
                "success",
                "states_explored",
                "recursive_calls",
                "prunings",
                "max_depth"
        };

        for (String column : required) {

            if (!columns.containsKey(column)) {

                throw new IllegalArgumentException(
                        "Coluna obrigatória ausente no CSV: "
                                + column
                );
            }
        }
    }

    private static Map<String, Integer> createColumnIndex(
            List<String> headers
    ) {

        Map<String, Integer> result =
                new HashMap<>();

        for (int i = 0;
             i < headers.size();
             i++) {

            result.put(
                    headers.get(i).trim(),
                    i
            );
        }

        return result;
    }

    private static Map<String, String> createRow(
            Map<String, Integer> columns,
            List<String> values
    ) {

        Map<String, String> result =
                new HashMap<>();

        for (Map.Entry<String, Integer> entry :
                columns.entrySet()) {

            int index = entry.getValue();

            String value =
                    index < values.size()
                            ? values.get(index)
                            : "";

            result.put(
                    entry.getKey(),
                    value
            );
        }

        return result;
    }

    private static double parseDouble(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return Double.NaN;
        }

        return Double.parseDouble(value);
    }

    private static String format(
            double value
    ) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {

            return "";
        }

        return String.format(
                Locale.US,
                "%.6f",
                value
        );
    }

    private static String csv(
            String value
    ) {

        if (value == null) {
            return "";
        }

        if (value.contains(",")
                || value.contains("\"")
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

    /**
     * Parser simples de CSV que suporta valores entre aspas.
     */
    private static List<String> parseCsvLine(
            String line
    ) {

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0;
             i < line.length();
             i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {

                    insideQuotes = !insideQuotes;
                }

            } else if (
                    c == ','
                            && !insideQuotes
            ) {

                values.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(c);
            }
        }

        values.add(
                current.toString()
        );

        return values;
    }

    /**
     * Acumula várias execuções da mesma configuração.
     */
    private static class Aggregate {

        private final String algorithm;
        private final int vertices;
        private final int edges;
        private final String density;
        private final double requestedDensity;
        private final double actualDensity;

        private int executions;
        private int successful;
        private int failed;

        private double timeSum;
        private double timeSquaredSum;

        private double memorySum;
        private int memoryCount;

        private double mstWeightSum;
        private int mstWeightCount;

        private double statesSum;
        private int statesCount;

        private double recursiveCallsSum;
        private int recursiveCallsCount;

        private double pruningsSum;
        private int pruningsCount;

        private double depthSum;
        private int depthCount;

        private final List<Double> executionTimes =
                new ArrayList<>();

        private Aggregate(
                String algorithm,
                int vertices,
                int edges,
                String density,
                double requestedDensity,
                double actualDensity
        ) {

            this.algorithm = algorithm;
            this.vertices = vertices;
            this.edges = edges;
            this.density = density;
            this.requestedDensity =
                    requestedDensity;
            this.actualDensity =
                    actualDensity;
        }

        private void add(
                Map<String, String> row
        ) {

            executions++;

            boolean success =
                    Boolean.parseBoolean(
                            row.get("success")
                    );

            if (success) {
                successful++;
            } else {
                failed++;
            }

            /*
             * Tempo.
             */
            double time =
                    parseDouble(
                            row.get(
                                    "execution_time_ns"
                            )
                    );

            if (!Double.isNaN(time)) {

                timeSum += time;
                timeSquaredSum +=
                        time * time;

                executionTimes.add(time);
            }

            /*
             * Memória.
             */
            double memory =
                    parseDouble(
                            row.get(
                                    "memory_bytes"
                            )
                    );

            if (success
                    && !Double.isNaN(memory)) {

                memorySum += memory;
                memoryCount++;
            }

            /*
             * Peso da MST.
             */
            double mstWeight =
                    parseDouble(
                            row.get(
                                    "mst_weight"
                            )
                    );

            if (success
                    && !Double.isNaN(mstWeight)) {

                mstWeightSum += mstWeight;
                mstWeightCount++;
            }

            /*
             * Métricas de Backtracking.
             */
            double states =
                    parseDouble(
                            row.get(
                                    "states_explored"
                            )
                    );

            if (success
                    && !Double.isNaN(states)) {

                statesSum += states;
                statesCount++;
            }

            double recursiveCalls =
                    parseDouble(
                            row.get(
                                    "recursive_calls"
                            )
                    );

            if (success
                    && !Double.isNaN(
                    recursiveCalls
            )) {

                recursiveCallsSum +=
                        recursiveCalls;

                recursiveCallsCount++;
            }

            double prunings =
                    parseDouble(
                            row.get(
                                    "prunings"
                            )
                    );

            if (success
                    && !Double.isNaN(prunings)) {

                pruningsSum += prunings;
                pruningsCount++;
            }

            double depth =
                    parseDouble(
                            row.get(
                                    "max_depth"
                            )
                    );

            if (success
                    && !Double.isNaN(depth)) {

                depthSum += depth;
                depthCount++;
            }
        }

        private double getAverageTime() {

            if (executionTimes.isEmpty()) {
                return Double.NaN;
            }

            return timeSum
                    / executionTimes.size();
        }

        private double getStdDevTime() {

            if (executionTimes.size() <= 1) {
                return 0.0;
            }

            double mean =
                    getAverageTime();

            double variance =
                    0.0;

            for (double value :
                    executionTimes) {

                double difference =
                        value - mean;

                variance +=
                        difference
                                * difference;
            }

            variance /=
                    executionTimes.size() - 1;

            return Math.sqrt(variance);
        }

        private double getAverageMemory() {

            if (memoryCount == 0) {
                return Double.NaN;
            }

            return memorySum
                    / memoryCount;
        }

        private double getAverageMstWeight() {

            if (mstWeightCount == 0) {
                return Double.NaN;
            }

            return mstWeightSum
                    / mstWeightCount;
        }

        private double getAverageStates() {

            if (statesCount == 0) {
                return Double.NaN;
            }

            return statesSum
                    / statesCount;
        }

        private double getAverageRecursiveCalls() {

            if (recursiveCallsCount == 0) {
                return Double.NaN;
            }

            return recursiveCallsSum
                    / recursiveCallsCount;
        }

        private double getAveragePrunings() {

            if (pruningsCount == 0) {
                return Double.NaN;
            }

            return pruningsSum
                    / pruningsCount;
        }

        private double getAverageDepth() {

            if (depthCount == 0) {
                return Double.NaN;
            }

            return depthSum
                    / depthCount;
        }
    }
}