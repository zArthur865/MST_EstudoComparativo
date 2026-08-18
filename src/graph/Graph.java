package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um grafo ponderado e não direcionado.
 *
 * Os vértices são identificados por inteiros no intervalo:
 *
 * 0 ... vertices - 1
 *
 * O grafo mantém:
 * - uma lista de todas as arestas;
 * - uma lista de adjacência para cada vértice.
 */
public class Graph {

    private final int vertices;

    private final List<Edge> edges;

    private final List<List<Edge>> adjacencyList;

    /**
     * Cria um grafo com a quantidade especificada de vértices.
     *
     * @param vertices quantidade de vértices
     */
    public Graph(int vertices) {
        if (vertices < 0) {
            throw new IllegalArgumentException(
                    "A quantidade de vértices não pode ser negativa."
            );
        }

        this.vertices = vertices;
        this.edges = new ArrayList<>();

        this.adjacencyList = new ArrayList<>(vertices);

        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    /**
     * Adiciona uma aresta não direcionada ao grafo.
     *
     * Uma aresta (u, v) é armazenada nas listas de adjacência
     * de u e de v.
     *
     * @param source vértice de origem
     * @param destination vértice de destino
     * @param weight peso da aresta
     */
    public void addEdge(int source, int destination, double weight) {

        validateVertex(source);
        validateVertex(destination);

        if (source == destination) {
            throw new IllegalArgumentException(
                    "Laços não são permitidos em um grafo de MST."
            );
        }

        Edge edge = new Edge(source, destination, weight);

        edges.add(edge);

        adjacencyList.get(source).add(edge);
        adjacencyList.get(destination).add(edge);
    }

    /**
     * Adiciona uma aresta existente ao grafo.
     *
     * @param edge aresta a ser adicionada
     */
    public void addEdge(Edge edge) {

        if (edge == null) {
            throw new IllegalArgumentException(
                    "A aresta não pode ser nula."
            );
        }

        addEdge(
                edge.getSource(),
                edge.getDestination(),
                edge.getWeight()
        );
    }

    /**
     * Retorna a quantidade de vértices.
     */
    public int getVertices() {
        return vertices;
    }

    /**
     * Retorna a quantidade de arestas.
     */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * Retorna uma visão somente para leitura das arestas.
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Retorna a lista de adjacência de um determinado vértice.
     */
    public List<Edge> getNeighbors(int vertex) {

        validateVertex(vertex);

        return Collections.unmodifiableList(
                adjacencyList.get(vertex)
        );
    }

    /**
     * Retorna a lista de adjacência completa.
     */
    public List<List<Edge>> getAdjacencyList() {

        List<List<Edge>> result = new ArrayList<>();

        for (List<Edge> neighbors : adjacencyList) {
            result.add(
                    Collections.unmodifiableList(neighbors)
            );
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Verifica se uma aresta existe entre dois vértices.
     *
     * Como o grafo é não direcionado, (u, v) e (v, u)
     * representam a mesma conexão.
     */
    public boolean hasEdge(int source, int destination) {

        validateVertex(source);
        validateVertex(destination);

        for (Edge edge : adjacencyList.get(source)) {

            if ((edge.getSource() == source
                    && edge.getDestination() == destination)
                    ||
                    (edge.getSource() == destination
                            && edge.getDestination() == source)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Valida se um vértice pertence ao grafo.
     */
    private void validateVertex(int vertex) {

        if (vertex < 0 || vertex >= vertices) {
            throw new IllegalArgumentException(
                    "Vértice inválido: " + vertex
            );
        }
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        result.append("Grafo com ")
                .append(vertices)
                .append(" vértices e ")
                .append(edges.size())
                .append(" arestas:\n");

        for (int i = 0; i < vertices; i++) {

            result.append(i)
                    .append(": ");

            for (Edge edge : adjacencyList.get(i)) {

                int neighbor =
                        edge.getSource() == i
                                ? edge.getDestination()
                                : edge.getSource();

                result.append("(")
                        .append(neighbor)
                        .append(", ")
                        .append(edge.getWeight())
                        .append(") ");
            }

            result.append("\n");
        }

        return result.toString();
    }
}