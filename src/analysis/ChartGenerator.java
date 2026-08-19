package analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gera gráficos SVG a partir do summary.csv.
 *
 * Não utiliza bibliotecas externas.
 */
public final class ChartGenerator {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;

    private static final int LEFT = 90;
    private static final int RIGHT = 40;
    private static final int TOP = 70;
    private static final int BOTTOM = 90;

    private ChartGenerator() {
    }

    /**
     * Gera todos os gráficos definidos para o projeto.
     */
    public static void generateAll(
            String summaryPath,
            String outputDirectory
    ) {

        Path input =
                Path.of(summaryPath);

        Path output =
                Path.of(outputDirectory);

        if (!Files.exists(input)) {

            throw new IllegalArgumentException(
                    "Arquivo processado não encontrado: "
                            + input
            );
        }

        try {

            Files.createDirectories(output);

            List<Row> rows =
                    readSummary(input);

            generateTimeCharts(
                    rows,
                    output
            );

            generateMemoryCharts(
                    rows,
                    output
            );

            generateBacktrackingCharts(
                    rows,
                    output
            );

            System.out.println(
                    "Gráficos gerados em:"
            );

            System.out.println(
                    output
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao gerar gráficos.",
                    e
            );
        }
    }

    private static void generateTimeCharts(
            List<Row> rows,
            Path output
    ) throws IOException {

        String[] densities = {
                "sparse",
                "medium",
                "dense"
        };

        for (String density :
                densities) {

            List<Series> greedy =
                    createSeries(
                            rows,
                            density,
                            false,
                            "avg_time_ns"
                    );

            if (!greedy.isEmpty()) {

                writeChart(
                        output.resolve(
                                "tempo_vs_vertices_"
                                        + density
                                        + "_greedy.svg"
                        ),
                        "Tempo de execução - "
                                + density,
                        "Vértices",
                        "Tempo médio (ms)",
                        convertNanosecondsToMilliseconds(
                                greedy
                        ),
                        true
                );
            }

            List<Series> backtracking =
                    createSeries(
                            rows,
                            density,
                            true,
                            "avg_time_ns"
                    );

            if (!backtracking.isEmpty()) {

                writeChart(
                        output.resolve(
                                "tempo_vs_vertices_"
                                        + density
                                        + "_backtracking.svg"
                        ),
                        "Tempo de execução - Backtracking - "
                                + density,
                        "Vértices",
                        "Tempo médio (ms)",
                        convertNanosecondsToMilliseconds(
                                backtracking
                        ),
                        true
                );
            }
        }
    }

    private static void generateMemoryCharts(
            List<Row> rows,
            Path output
    ) throws IOException {

        String[] densities = {
                "sparse",
                "medium",
                "dense"
        };

        for (String density :
                densities) {

            List<Series> greedy =
                    createSeries(
                            rows,
                            density,
                            false,
                            "avg_memory_bytes"
                    );

            if (!greedy.isEmpty()) {

                writeChart(
                        output.resolve(
                                "memoria_vs_vertices_"
                                        + density
                                        + "_greedy.svg"
                        ),
                        "Memória - "
                                + density,
                        "Vértices",
                        "Memória média (KB)",
                        convertBytesToKilobytes(
                                greedy
                        ),
                        true
                );
            }

            List<Series> backtracking =
                    createSeries(
                            rows,
                            density,
                            true,
                            "avg_memory_bytes"
                    );

            if (!backtracking.isEmpty()) {

                writeChart(
                        output.resolve(
                                "memoria_vs_vertices_"
                                        + density
                                        + "_backtracking.svg"
                        ),
                        "Memória - Backtracking - "
                                + density,
                        "Vértices",
                        "Memória média (KB)",
                        convertBytesToKilobytes(
                                backtracking
                        ),
                        true
                );
            }
        }
    }

    private static void generateBacktrackingCharts(
            List<Row> rows,
            Path output
    ) throws IOException {

        String[] metrics = {
                "avg_states_explored",
                "avg_recursive_calls",
                "avg_prunings",
                "avg_max_depth"
        };

        String[] titles = {
                "Estados explorados pelo Backtracking",
                "Chamadas recursivas do Backtracking",
                "Podas realizadas pelo Backtracking",
                "Profundidade máxima do Backtracking"
        };

        String[] filenames = {
                "backtracking_estados.svg",
                "backtracking_recursivo.svg",
                "backtracking_podas.svg",
                "backtracking_profundidade.svg"
        };

        String[] yLabels = {
                "Estados",
                "Chamadas",
                "Podas",
                "Profundidade"
        };

        for (int i = 0;
             i < metrics.length;
             i++) {

            List<Series> series =
                    new ArrayList<>();

            for (String density :
                    new String[]{
                            "sparse",
                            "medium",
                            "dense"
                    }) {

                List<Series> current =
                        createSeries(
                                rows,
                                density,
                                true,
                                metrics[i]
                        );

                series.addAll(current);
            }

            if (!series.isEmpty()) {

                writeChart(
                        output.resolve(
                                filenames[i]
                        ),
                        titles[i],
                        "Vértices",
                        yLabels[i],
                        series,
                        true
                );
            }
        }
    }

    private static List<Series> createSeries(
            List<Row> rows,
            String density,
            boolean backtrackingOnly,
            String metric
    ) {

        Map<String, Series> map =
                new LinkedHashMap<>();

        for (Row row : rows) {

            boolean isBacktracking =
                    row.algorithm
                            .toLowerCase()
                            .contains(
                                    "backtracking"
                            );

            if (backtrackingOnly
                    != isBacktracking) {

                continue;
            }

            if (!row.density.equals(
                    density
            )) {

                continue;
            }

            double value =
                    row.get(metric);

            if (Double.isNaN(value)) {
                continue;
            }

            Series series =
                    map.computeIfAbsent(
                            row.algorithm
                                    + " - "
                                    + density,
                            ignored ->
                                    new Series(
                                            row.algorithm
                                    )
                    );

            series.points.put(
                    row.vertices,
                    value
            );
        }

        return new ArrayList<>(
                map.values()
        );
    }

    private static List<Series> convertNanosecondsToMilliseconds(
            List<Series> source
    ) {

        for (Series series : source) {

            for (Map.Entry<Integer, Double> entry :
                    series.points.entrySet()) {

                entry.setValue(
                        entry.getValue()
                                / 1_000_000.0
                );
            }
        }

        return source;
    }

    private static List<Series> convertBytesToKilobytes(
            List<Series> source
    ) {

        for (Series series : source) {

            for (Map.Entry<Integer, Double> entry :
                    series.points.entrySet()) {

                entry.setValue(
                        entry.getValue()
                                / 1024.0
                );
            }
        }

        return source;
    }

    private static void writeChart(
            Path output,
            String title,
            String xLabel,
            String yLabel,
            List<Series> series,
            boolean showLegend
    ) throws IOException {

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (Series current :
                series) {

            for (Map.Entry<Integer, Double> point :
                    current.points.entrySet()) {

                minX = Math.min(
                        minX,
                        point.getKey()
                );

                maxX = Math.max(
                        maxX,
                        point.getKey()
                );

                minY = Math.min(
                        minY,
                        point.getValue()
                );

                maxY = Math.max(
                        maxY,
                        point.getValue()
                );
            }
        }

        if (!Double.isFinite(minX)
                || !Double.isFinite(maxX)
                || !Double.isFinite(minY)
                || !Double.isFinite(maxY)) {
            return;
        }

        if (minX == maxX) {
            maxX += 1;
            minX -= 1;
        }

        if (minY == maxY) {
            maxY += 1;
            minY -= 1;
        }

        double yMargin =
                (maxY - minY) * 0.10;

        minY = Math.max(
                0,
                minY - yMargin
        );

        maxY += yMargin;

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             output,
                             StandardCharsets.UTF_8
                     )) {

            writer.write(
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" "
                            + "width=\""
                            + WIDTH
                            + "\" height=\""
                            + HEIGHT
                            + "\" "
                            + "viewBox=\"0 0 "
                            + WIDTH
                            + " "
                            + HEIGHT
                            + "\">"
            );

            writer.newLine();

            writer.write(
                    "<rect x=\"0\" y=\"0\" width=\""
                            + WIDTH
                            + "\" height=\""
                            + HEIGHT
                            + "\" fill=\"white\"/>"
            );

            writer.newLine();

            /*
             * Título.
             */
            writer.write(
                    "<text x=\""
                            + WIDTH / 2
                            + "\" y=\"30\" "
                            + "text-anchor=\"middle\" "
                            + "font-size=\"20\" "
                            + "font-family=\"Arial\">"
                            + escape(title)
                            + "</text>"
            );

            writer.newLine();

            int chartWidth =
                    WIDTH - LEFT - RIGHT;

            int chartHeight =
                    HEIGHT - TOP - BOTTOM;

            int x0 = LEFT;
            int y0 =
                    HEIGHT - BOTTOM;

            int x1 =
                    WIDTH - RIGHT;

            int y1 = TOP;

            /*
             * Eixos.
             */
            writer.write(
                    "<line x1=\""
                            + x0
                            + "\" y1=\""
                            + y0
                            + "\" x2=\""
                            + x1
                            + "\" y2=\""
                            + y0
                            + "\" stroke=\"black\"/>"
            );

            writer.newLine();

            writer.write(
                    "<line x1=\""
                            + x0
                            + "\" y1=\""
                            + y0
                            + "\" x2=\""
                            + x0
                            + "\" y2=\""
                            + y1
                            + "\" stroke=\"black\"/>"
            );

            writer.newLine();

            /*
             * Rótulos dos eixos.
             */
            writer.write(
                    "<text x=\""
                            + (x0 + x1) / 2
                            + "\" y=\""
                            + (HEIGHT - 25)
                            + "\" text-anchor=\"middle\" "
                            + "font-size=\"14\" "
                            + "font-family=\"Arial\">"
                            + escape(xLabel)
                            + "</text>"
            );

            writer.newLine();

            writer.write(
                    "<text x=\"20\" y=\""
                            + (y0 + y1) / 2
                            + "\" "
                            + "transform=\"rotate(-90 20 "
                            + (y0 + y1) / 2
                            + ")\" "
                            + "text-anchor=\"middle\" "
                            + "font-size=\"14\" "
                            + "font-family=\"Arial\">"
                            + escape(yLabel)
                            + "</text>"
            );

            writer.newLine();

            /*
             * Grades horizontais.
             */
            for (int i = 0; i <= 5; i++) {

                double value =
                        minY
                                + (maxY - minY)
                                * i
                                / 5.0;

                double y =
                        y0
                                - (value - minY)
                                / (maxY - minY)
                                * chartHeight;

                writer.write(
                        "<line x1=\""
                                + x0
                                + "\" y1=\""
                                + formatNumber(y)
                                + "\" x2=\""
                                + x1
                                + "\" y2=\""
                                + formatNumber(y)
                                + "\" stroke=\"#dddddd\"/>"
                );

                writer.newLine();

                writer.write(
                        "<text x=\""
                                + (x0 - 10)
                                + "\" y=\""
                                + formatNumber(y + 5)
                                + "\" text-anchor=\"end\" "
                                + "font-size=\"11\" "
                                + "font-family=\"Arial\">"
                                + formatNumber(value)
                                + "</text>"
                );

                writer.newLine();
            }

            /*
             * Linhas.
             */
            for (Series current :
                    series) {

                List<Map.Entry<Integer, Double>>
                        points =
                        new ArrayList<>(
                                current.points.entrySet()
                        );

                points.sort(
                        Map.Entry.comparingByKey()
                );

                StringBuilder path =
                        new StringBuilder();

                boolean first = true;

                for (Map.Entry<Integer, Double> point :
                        points) {

                    double x =
                            x0
                                    + (point.getKey()
                                    - minX)
                                    / (maxX - minX)
                                    * chartWidth;

                    double y =
                            y0
                                    - (point.getValue()
                                    - minY)
                                    / (maxY - minY)
                                    * chartHeight;

                    if (first) {

                        path.append("M ");

                        first = false;

                    } else {

                        path.append(" L ");
                    }

                    path.append(
                            formatNumber(x)
                    );

                    path.append(" ");

                    path.append(
                            formatNumber(y)
                    );

                    writer.write(
                            "<circle cx=\""
                                    + formatNumber(x)
                                    + "\" cy=\""
                                    + formatNumber(y)
                                    + "\" r=\"4\" "
                                    + "fill=\"black\"/>"
                    );

                    writer.newLine();
                }

                if (!path.isEmpty()) {

                    writer.write(
                            "<path d=\""
                                    + path
                                    + "\" "
                                    + "fill=\"none\" "
                                    + "stroke=\"black\" "
                                    + "stroke-width=\"2\"/>"
                    );

                    writer.newLine();
                }
            }

            /*
             * Legenda.
             */
            if (showLegend) {

                int legendX =
                        WIDTH - 260;

                int legendY = 65;

                for (int i = 0;
                     i < series.size();
                     i++) {

                    Series current =
                            series.get(i);

                    int y =
                            legendY
                                    + i * 22;

                    writer.write(
                            "<text x=\""
                                    + legendX
                                    + "\" y=\""
                                    + y
                                    + "\" "
                                    + "font-size=\"12\" "
                                    + "font-family=\"Arial\">"
                                    + escape(
                                    current.name
                            )
                                    + "</text>"
                    );

                    writer.newLine();
                }
            }

            writer.write("</svg>");
        }
    }

    private static List<Row> readSummary(
            Path input
    ) throws IOException {

        List<String> lines =
                Files.readAllLines(
                        input,
                        StandardCharsets.UTF_8
                );

        if (lines.isEmpty()) {
            return List.of();
        }

        List<String> headers =
                parseCsvLine(
                        lines.get(0)
                );

        Map<String, Integer> indexes =
                new HashMap<>();

        for (int i = 0;
             i < headers.size();
             i++) {

            indexes.put(
                    headers.get(i),
                    i
            );
        }

        List<Row> rows =
                new ArrayList<>();

        for (int i = 1;
             i < lines.size();
             i++) {

            if (lines.get(i).isBlank()) {
                continue;
            }

            List<String> values =
                    parseCsvLine(
                            lines.get(i)
                    );

            Row row =
                    new Row(
                            value(
                                    values,
                                    indexes,
                                    "algorithm"
                            ),
                            Integer.parseInt(
                                    value(
                                            values,
                                            indexes,
                                            "vertices"
                                    )
                            ),
                            value(
                                    values,
                                    indexes,
                                    "density"
                            )
                    );

            String[] numericColumns = {
                    "avg_time_ns",
                    "avg_memory_bytes",
                    "avg_states_explored",
                    "avg_recursive_calls",
                    "avg_prunings",
                    "avg_max_depth"
            };

            for (String column :
                    numericColumns) {

                row.values.put(
                        column,
                        parseDouble(
                                value(
                                        values,
                                        indexes,
                                        column
                                )
                        )
                );
            }

            rows.add(row);
        }

        return rows;
    }

    private static String value(
            List<String> values,
            Map<String, Integer> indexes,
            String column
    ) {

        Integer index =
                indexes.get(column);

        if (index == null
                || index >= values.size()) {

            return "";
        }

        return values.get(index);
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

                    insideQuotes =
                            !insideQuotes;
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

    private static String formatNumber(
            double value
    ) {

        return String.format(
                java.util.Locale.US,
                "%.2f",
                value
        );
    }

    private static String escape(
            String value
    ) {

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    /**
     * Uma série de dados de um gráfico.
     */
    private static class Series {

        private final String name;

        private final Map<Integer, Double> points =
                new LinkedHashMap<>();

        private Series(String name) {
            this.name = name;
        }
    }

    /**
     * Uma linha do summary.csv.
     */
    private static class Row {

        private final String algorithm;
        private final int vertices;
        private final String density;

        private final Map<String, Double> values =
                new HashMap<>();

        private Row(
                String algorithm,
                int vertices,
                String density
        ) {

            this.algorithm = algorithm;
            this.vertices = vertices;
            this.density = density;
        }

        private double get(String column) {

            return values.getOrDefault(
                    column,
                    Double.NaN
            );
        }
    }
}