package graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Responsável por salvar e carregar instâncias de grafos.
 *
 * Formato do arquivo:
 *
 * V E
 * source destination weight
 * source destination weight
 * ...
 */
public class GraphLoader {

    /**
     * Salva um grafo em um arquivo.
     *
     * @param graph grafo a ser salvo
     * @param path caminho do arquivo
     * @throws IOException caso ocorra erro de escrita
     */
    public static void save(
            Graph graph,
            Path path
    ) throws IOException {

        if (graph == null) {
            throw new IllegalArgumentException(
                    "O grafo não pode ser nulo."
            );
        }

        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(path)) {

            writer.write(
                    graph.getVertices()
                            + " "
                            + graph.getEdgeCount()
            );

            writer.newLine();

            for (Edge edge : graph.getEdges()) {

                writer.write(
                        String.format(
                                Locale.US,
                                "%d %d %.6f",
                                edge.getSource(),
                                edge.getDestination(),
                                edge.getWeight()
                        )
                );

                writer.newLine();
            }
        }
    }

    /**
     * Carrega um grafo a partir de um arquivo.
     *
     * @param path caminho do arquivo
     * @return grafo carregado
     * @throws IOException caso ocorra erro de leitura
     */
    public static Graph load(
            Path path
    ) throws IOException {

        try (BufferedReader reader =
                     Files.newBufferedReader(path)) {

            String firstLine = reader.readLine();

            if (firstLine == null) {
                throw new IOException(
                        "Arquivo vazio."
                );
            }

            StringTokenizer header =
                    new StringTokenizer(firstLine);

            if (header.countTokens() != 2) {
                throw new IOException(
                        "Cabeçalho inválido. Esperado: V E"
                );
            }

            int vertices =
                    Integer.parseInt(header.nextToken());

            int expectedEdges =
                    Integer.parseInt(header.nextToken());

            Graph graph = new Graph(vertices);

            int loadedEdges = 0;

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                /*
                 * Ignora linhas vazias.
                 */
                if (line.isEmpty()) {
                    continue;
                }

                StringTokenizer tokenizer =
                        new StringTokenizer(line);

                if (tokenizer.countTokens() != 3) {
                    throw new IOException(
                            "Linha de aresta inválida: "
                                    + line
                    );
                }

                int source =
                        Integer.parseInt(
                                tokenizer.nextToken()
                        );

                int destination =
                        Integer.parseInt(
                                tokenizer.nextToken()
                        );

                double weight =
                        Double.parseDouble(
                                tokenizer.nextToken()
                        );

                graph.addEdge(
                        source,
                        destination,
                        weight
                );

                loadedEdges++;
            }

            if (loadedEdges != expectedEdges) {
                throw new IOException(
                        "Quantidade de arestas incompatível. "
                                + "Esperado: "
                                + expectedEdges
                                + ", encontrado: "
                                + loadedEdges
                );
            }

            return graph;
        }
    }
}