package graph;

import java.util.Objects;

/**
 * Representa uma aresta ponderada de um grafo.
 *
 * Uma aresta conecta dois vértices (source e destination)
 * e possui um peso associado.
 */
public class Edge implements Comparable<Edge> {

    private final int source;
    private final int destination;
    private final double weight;

    /**
     * Cria uma nova aresta.
     *
     * @param source vértice de origem
     * @param destination vértice de destino
     * @param weight peso da aresta
     */
    public Edge(int source, int destination, double weight) {
        if (source < 0 || destination < 0) {
            throw new IllegalArgumentException(
                    "Os vértices devem possuir índices não negativos."
            );
        }

        if (weight < 0) {
            throw new IllegalArgumentException(
                    "O peso da aresta não pode ser negativo."
            );
        }

        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    /**
     * Compara arestas pelo peso.
     *
     * Isso será utilizado posteriormente pelo algoritmo de Kruskal.
     */
    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Edge)) {
            return false;
        }

        Edge other = (Edge) obj;

        return source == other.source
                && destination == other.destination
                && Double.compare(weight, other.weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, weight);
    }

    @Override
    public String toString() {
        return String.format(
                "(%d -- %.2f --> %d)",
                source,
                weight,
                destination
        );
    }
}