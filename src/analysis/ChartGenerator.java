package analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gera gráficos SVG a partir dos resultados processados.
 *
 * Os gráficos são gerados sem bibliotecas externas.
 *
 * Algoritmos gulosos:
 * - Prim - Lista de Adjacência
 * - Prim - Matriz de Adjacência
 * - Kruskal
 *
 * Cada algoritmo possui uma cor própria para facilitar
 * a comparação visual.
 */
public final class ChartGenerator {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;

    private static final int LEFT = 90;
    private static final int RIGHT = 180;
    private static final int TOP = 70;
    private static final int BOTTOM = 90;

    /*
     * Cores utilizadas nos algoritmos gulosos.
     *
     * Mantemos as cores fixas em todos os gráficos para que
     * o leitor associe sempre a mesma cor ao mesmo algoritmo.
     */
    private static final String COLOR_PRIM_LIST =
            "#2563EB";

    private static final String COLOR_PRIM_MATRIX =
            "#DC2626";

    private static final String COLOR_KRUSKAL =
            "#16A34A";

    private static final String COLOR_BACKTRACKING =
            "#7C3AED";

    private ChartGenerator() {
        /*
         * Classe utilitária.
         */
    }

    /**
     * Gera todos os gráficos do projeto.
     *
     * @param summaryPath caminho do summary.csv
     * @param outputDirectory diretório onde os SVGs serão salvos
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

            /*
             * Gráficos comparativos dos três algoritmos gulosos.
             */
            generateGreedyTimeCharts(
                    rows,
                    output
            );

            generateGreedyMemoryCharts(
                    rows,
                    output
            );

            /*
             * Gráficos específicos do Backtracking.
             */
            generateBacktrackingTimeCharts(
                    rows,
                    output
            );

            generateBacktrackingMemoryCharts(
                    rows,
                    output
            );

            generateBacktrackingMetricCharts(
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

    // ============================================================
    // GRÁFICOS DOS ALGORITMOS GULOSOS
    // ============================================================

    /**
     * Gera gráficos de tempo comparando:
     *
     * Prim - Lista de Adjacência
     * Prim - Matriz de Adjacência
     * Kruskal
     *
     * Cada gráfico corresponde a uma densidade.
     */
    private static void generateGreedyTimeCharts(
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

            List<Series> series =
                    createGreedySeries(
                            rows,
                            density,
                            "avg_time_ns"
                    );

            if (series.isEmpty()) {
                continue;
            }

            convertNanosecondsToMilliseconds(
                    series
            );

            writeChart(
                    output.resolve(
                            "tempo_vs_vertices_"
                                    + density
                                    + "_greedy.svg"
                    ),
                    "Tempo de execução - "
                            + density,
                    "Número de vértices",
                    "Tempo médio (ms)",
                    series
            );
        }
    }

    /**
     * Gera gráficos de memória comparando os três algoritmos gulosos.
     */
    private static void generateGreedyMemoryCharts(
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

            List<Series> series =
                    createGreedySeries(
                            rows,
                            density,
                            "avg_memory_bytes"
                    );

            if (series.isEmpty()) {
                continue;
            }

            convertBytesToKilobytes(
                    series
            );

            writeChart(
                    output.resolve(
                            "memoria_vs_vertices_"
                                    + density
                                    + "_greedy.svg"
                    ),
                    "Consumo de memória - "
                            + density,
                    "Número de vértices",
                    "Memória média (KB)",
                    series
            );
        }
    }

    /**
     * Cria as séries dos três algoritmos gulosos.
     */
    private static List<Series> createGreedySeries(
            List<Row> rows,
            String density,
            String metric
    ) {

        Map<String, Series> seriesMap =
                new LinkedHashMap<>();

        /*
         * Ordem fixa para que a legenda seja sempre:
         *
         * Prim Lista
         * Prim Matriz
         * Kruskal
         */
        String[] algorithms = {
                "Prim - Lista de Adjacência",
                "Prim - Matriz de Adjacência",
                "Kruskal"
        };

        for (String algorithm :
                algorithms) {

            String color =
                    getAlgorithmColor(
                            algorithm
                    );

            seriesMap.put(
                    algorithm,
                    new Series(
                            algorithm,
                            color
                    )
            );
        }

        for (Row row : rows) {

            if (!row.density.equals(density)) {
                continue;
            }

            if (!isGreedyAlgorithm(
                    row.algorithm
            )) {
                continue;
            }

            double value =
                    row.get(metric);

            if (Double.isNaN(value)) {
                continue;
            }

            Series series =
                    findSeries(
                            seriesMap,
                            row.algorithm
                    );

            if (series == null) {
                continue;
            }

            series.points.put(
                    row.vertices,
                    value
            );
        }

        /*
         * Remove séries que não possuem dados.
         */
        List<Series> result =
                new ArrayList<>();

        for (Series series :
                seriesMap.values()) {

            if (!series.points.isEmpty()) {
                result.add(series);
            }
        }

        return result;
    }

    /**
     * Identifica se o algoritmo pertence ao grupo guloso.
     */
    private static boolean isGreedyAlgorithm(
            String algorithm
    ) {

        return normalizeAlgorithmName(algorithm)
                .equals(
                        normalizeAlgorithmName(
                                "Prim - Lista de Adjacência"
                        )
                )
                || normalizeAlgorithmName(algorithm)
                .equals(
                        normalizeAlgorithmName(
                                "Prim - Matriz de Adjacência"
                        )
                )
                || normalizeAlgorithmName(algorithm)
                .equals(
                        normalizeAlgorithmName(
                                "Kruskal"
                        )
                );
    }

    /**
     * Localiza a série correspondente.
     *
     * O CSV precisa conter os mesmos nomes produzidos
     * pelos métodos getName() dos algoritmos.
     */
    private static Series findSeries(
            Map<String, Series> seriesMap,
            String algorithm
    ) {

        for (Map.Entry<String, Series> entry :
                seriesMap.entrySet()) {

            if (normalizeAlgorithmName(
                    entry.getKey()
            ).equals(
                    normalizeAlgorithmName(
                            algorithm
                    )
            )) {

                return entry.getValue();
            }
        }

        /*
         * Pequena tolerância para eventuais nomes diferentes.
         */
        String normalized =
                normalizeAlgorithmName(
                        algorithm
                );

        if (normalized.contains(
                "prim - lista"
        )) {

            return findByName(
                    seriesMap,
                    "Prim - Lista de Adjacência"
            );
        }

        if (normalized.contains(
                "prim - matriz"
        )) {

            return findByName(
                    seriesMap,
                    "Prim - Matriz de Adjacência"
            );
        }

        if (normalized.equals(
                "kruskal"
        )) {

            return findByName(
                    seriesMap,
                    "Kruskal"
            );
        }

        return null;
    }

    private static Series findByName(
            Map<String, Series> seriesMap,
            String name
    ) {

        return seriesMap.get(name);
    }

    /**
     * Retorna a cor padronizada de cada algoritmo.
     */
    private static String getAlgorithmColor(
            String algorithm
    ) {

        String normalized =
                normalizeAlgorithmName(
                        algorithm
                );

        if (normalized.contains(
                "prim - lista"
        )) {

            return COLOR_PRIM_LIST;
        }

        if (normalized.contains(
                "prim - matriz"
        )) {

            return COLOR_PRIM_MATRIX;
        }

        if (normalized.equals(
                "kruskal"
        )) {

            return COLOR_KRUSKAL;
        }

        return "#000000";
    }

    /**
     * Normaliza nomes para comparação.
     */
    private static String normalizeAlgorithmName(
            String name
    ) {

        if (name == null) {
            return "";
        }

        return name
                .trim()
                .toLowerCase()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    // ============================================================
    // GRÁFICOS DO BACKTRACKING
    // ============================================================

    /**
     * Gera gráficos de tempo do Backtracking separados
     * por densidade.
     */
    private static void generateBacktrackingTimeCharts(
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

            List<Series> series =
                    createBacktrackingSeries(
                            rows,
                            density,
                            "avg_time_ns"
                    );

            if (series.isEmpty()) {
                continue;
            }

            convertNanosecondsToMilliseconds(
                    series
            );

            writeChart(
                    output.resolve(
                            "tempo_vs_vertices_"
                                    + density
                                    + "_backtracking.svg"
                    ),
                    "Tempo de execução - Backtracking - "
                            + density,
                    "Número de vértices",
                    "Tempo médio (ms)",
                    series
            );
        }
    }

    /**
     * Gera gráficos de memória do Backtracking.
     */
    private static void generateBacktrackingMemoryCharts(
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

            List<Series> series =
                    createBacktrackingSeries(
                            rows,
                            density,
                            "avg_memory_bytes"
                    );

            if (series.isEmpty()) {
                continue;
            }

            convertBytesToKilobytes(
                    series
            );

            writeChart(
                    output.resolve(
                            "memoria_vs_vertices_"
                                    + density
                                    + "_backtracking.svg"
                    ),
                    "Consumo de memória - Backtracking - "
                            + density,
                    "Número de vértices",
                    "Memória média (KB)",
                    series
            );
        }
    }

    /**
     * Cria uma série específica do Backtracking.
     */
    private static List<Series> createBacktrackingSeries(
            List<Row> rows,
            String density,
            String metric
    ) {

        /*
         * Cada densidade possui sua própria série.
         *
         * Como este método é chamado individualmente por densidade,
         * haverá uma única série no gráfico.
         */
        Series series =
                new Series(
                        "Backtracking - "
                                + density,
                        COLOR_BACKTRACKING
                );

        for (Row row : rows) {

            if (!row.density.equals(density)) {
                continue;
            }

            if (!isBacktracking(
                    row.algorithm
            )) {
                continue;
            }

            double value =
                    row.get(metric);

            if (Double.isNaN(value)) {
                continue;
            }

            series.points.put(
                    row.vertices,
                    value
            );
        }

        if (series.points.isEmpty()) {
            return List.of();
        }

        return List.of(series);
    }

    /**
     * Gera os quatro gráficos específicos do Backtracking.
     */
    private static void generateBacktrackingMetricCharts(
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
                "Estados explorados - Backtracking",
                "Chamadas recursivas - Backtracking",
                "Podas realizadas - Backtracking",
                "Profundidade máxima - Backtracking"
        };

        String[] filenames = {
                "backtracking_estados.svg",
                "backtracking_recursivo.svg",
                "backtracking_podas.svg",
                "backtracking_profundidade.svg"
        };

        String[] yLabels = {
                "Estados explorados",
                "Chamadas recursivas",
                "Podas",
                "Profundidade máxima"
        };

        /*
         * Para cada métrica, geramos uma série para cada densidade.
         *
         * Aqui as cores não representam algoritmos diferentes;
         * elas representam densidades diferentes.
         */
        String[] densities = {
                "sparse",
                "medium",
                "dense"
        };

        String[] densityColors = {
                "#2563EB",
                "#DC2626",
                "#16A34A"
        };

        for (int i = 0;
             i < metrics.length;
             i++) {

            List<Series> series =
                    new ArrayList<>();

            for (int d = 0;
                 d < densities.length;
                 d++) {

                String density =
                        densities[d];

                Series current =
                        new Series(
                                "Backtracking - "
                                        + density,
                                densityColors[d]
                        );

                for (Row row :
                        rows) {

                    if (!row.density.equals(
                            density
                    )) {
                        continue;
                    }

                    if (!isBacktracking(
                            row.algorithm
                    )) {
                        continue;
                    }

                    double value =
                            row.get(metrics[i]);

                    if (Double.isNaN(value)) {
                        continue;
                    }

                    current.points.put(
                            row.vertices,
                            value
                    );
                }

                if (!current.points.isEmpty()) {
                    series.add(current);
                }
            }

            if (!series.isEmpty()) {

                writeChart(
                        output.resolve(
                                filenames[i]
                        ),
                        titles[i],
                        "Número de vértices",
                        yLabels[i],
                        series
                );
            }
        }
    }

    private static boolean isBacktracking(
            String algorithm
    ) {

        return algorithm != null
                && algorithm
                .toLowerCase()
                .contains(
                        "backtracking"
                );
    }

    // ============================================================
    // CONVERSÕES
    // ============================================================

    private static void convertNanosecondsToMilliseconds(
            List<Series> series
    ) {

        for (Series current :
                series) {

            for (Map.Entry<Integer, Double> entry :
                    current.points.entrySet()) {

                entry.setValue(
                        entry.getValue()
                                / 1_000_000.0
                );
            }
        }
    }

    private static void convertBytesToKilobytes(
            List<Series> series
    ) {

        for (Series current :
                series) {

            for (Map.Entry<Integer, Double> entry :
                    current.points.entrySet()) {

                entry.setValue(
                        entry.getValue()
                                / 1024.0
                );
            }
        }
    }

    // ============================================================
    // GERAÇÃO DO SVG
    // ============================================================

    private static void writeChart(
            Path output,
            String title,
            String xLabel,
            String yLabel,
            List<Series> series
    ) throws IOException {

        double minX =
                Double.POSITIVE_INFINITY;

        double maxX =
                Double.NEGATIVE_INFINITY;

        double minY =
                Double.POSITIVE_INFINITY;

        double maxY =
                Double.NEGATIVE_INFINITY;

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

        /*
         * Margem vertical.
         */
        double yMargin =
                (maxY - minY)
                        * 0.10;

        minY =
                Math.max(
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

            /*
             * Fundo.
             */
            writer.write(
                    "<rect x=\"0\" y=\"0\" width=\""
                            + WIDTH
                            + "\" height=\""
                            + HEIGHT
                            + "\" "
                            + "fill=\"white\"/>"
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
                            + "font-family=\"Arial\" "
                            + "font-weight=\"bold\">"
                            + escape(title)
                            + "</text>"
            );

            writer.newLine();

            int chartWidth =
                    WIDTH
                            - LEFT
                            - RIGHT;

            int chartHeight =
                    HEIGHT
                            - TOP
                            - BOTTOM;

            int x0 = LEFT;

            int y0 =
                    HEIGHT
                            - BOTTOM;

            int x1 =
                    WIDTH
                            - RIGHT;

            int y1 =
                    TOP;

            /*
             * Eixo X.
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
                            + "\" "
                            + "stroke=\"#222222\" "
                            + "stroke-width=\"2\"/>"
            );

            writer.newLine();

            /*
             * Eixo Y.
             */
            writer.write(
                    "<line x1=\""
                            + x0
                            + "\" y1=\""
                            + y0
                            + "\" x2=\""
                            + x0
                            + "\" y2=\""
                            + y1
                            + "\" "
                            + "stroke=\"#222222\" "
                            + "stroke-width=\"2\"/>"
            );

            writer.newLine();

            /*
             * Rótulo X.
             */
            writer.write(
                    "<text x=\""
                            + (x0 + x1) / 2
                            + "\" y=\""
                            + (HEIGHT - 25)
                            + "\" "
                            + "text-anchor=\"middle\" "
                            + "font-size=\"14\" "
                            + "font-family=\"Arial\">"
                            + escape(xLabel)
                            + "</text>"
            );

            writer.newLine();

            /*
             * Rótulo Y.
             */
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
             * Grade horizontal e escala Y.
             */
            for (int i = 0;
                 i <= 5;
                 i++) {

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
                                + "\" "
                                + "stroke=\"#E5E7EB\"/>"
                );

                writer.newLine();

                writer.write(
                        "<text x=\""
                                + (x0 - 10)
                                + "\" y=\""
                                + formatNumber(y + 5)
                                + "\" "
                                + "text-anchor=\"end\" "
                                + "font-size=\"11\" "
                                + "font-family=\"Arial\">"
                                + formatNumber(value)
                                + "</text>"
                );

                writer.newLine();
            }

            /*
             * Linhas das séries.
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

                    /*
                     * Ponto.
                     */
                    writer.write(
                            "<circle cx=\""
                                    + formatNumber(x)
                                    + "\" cy=\""
                                    + formatNumber(y)
                                    + "\" "
                                    + "r=\"4\" "
                                    + "fill=\""
                                    + current.color
                                    + "\"/>"
                    );

                    writer.newLine();
                }

                /*
                 * Linha da série.
                 */
                if (!path.isEmpty()) {

                    writer.write(
                            "<path d=\""
                                    + path
                                    + "\" "
                                    + "fill=\"none\" "
                                    + "stroke=\""
                                    + current.color
                                    + "\" "
                                    + "stroke-width=\"3\" "
                                    + "stroke-linejoin=\"round\" "
                                    + "stroke-linecap=\"round\"/>"
                    );

                    writer.newLine();
                }
            }

            /*
             * Legenda.
             */
            int legendX =
                    WIDTH - 165;

            int legendY = 75;

            for (int i = 0;
                 i < series.size();
                 i++) {

                Series current =
                        series.get(i);

                int y =
                        legendY
                                + i * 28;

                /*
                 * Quadrado da legenda.
                 */
                writer.write(
                        "<rect x=\""
                                + legendX
                                + "\" y=\""
                                + (y - 12)
                                + "\" "
                                + "width=\"12\" "
                                + "height=\"12\" "
                                + "fill=\""
                                + current.color
                                + "\"/>"
                );

                writer.newLine();

                /*
                 * Texto da legenda.
                 */
                writer.write(
                        "<text x=\""
                                + (legendX + 20)
                                + "\" y=\""
                                + y
                                + "\" "
                                + "font-size=\"11\" "
                                + "font-family=\"Arial\">"
                                + escape(
                                current.name
                        )
                                + "</text>"
                );

                writer.newLine();
            }

            writer.write("</svg>");
        }
    }

    // ============================================================
    // LEITURA DO CSV
    // ============================================================

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
                    headers.get(i).trim(),
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

            String algorithm =
                    value(
                            values,
                            indexes,
                            "algorithm"
                    );

            String density =
                    value(
                            values,
                            indexes,
                            "density"
                    );

            String verticesText =
                    value(
                            values,
                            indexes,
                            "vertices"
                    );

            if (algorithm.isBlank()
                    || density.isBlank()
                    || verticesText.isBlank()) {

                continue;
            }

            Row row =
                    new Row(
                            algorithm,
                            Integer.parseInt(
                                    verticesText
                            ),
                            density
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

            char c =
                    line.charAt(i);

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

    // ============================================================
    // AUXILIARES
    // ============================================================

    private static String formatNumber(
            double value
    ) {

        return String.format(
                Locale.US,
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
     * Representa uma série do gráfico.
     */
    private static class Series {

        private final String name;
        private final String color;

        private final Map<Integer, Double> points =
                new LinkedHashMap<>();

        private Series(
                String name,
                String color
        ) {

            this.name = name;
            this.color = color;
        }
    }

    /**
     * Representa uma linha do summary.csv.
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

        private double get(
                String column
        ) {

            return values.getOrDefault(
                    column,
                    Double.NaN
            );
        }
    }
}