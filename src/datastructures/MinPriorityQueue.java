package datastructures;

import graph.Edge;

import java.util.PriorityQueue;

/**
 * Fila de prioridade mínima utilizada pelo algoritmo de Prim.
 *
 * As arestas são removidas em ordem crescente de peso.
 */
public class MinPriorityQueue {

    private final PriorityQueue<Edge> queue;

    public MinPriorityQueue() {
        this.queue = new PriorityQueue<>();
    }

    /**
     * Insere uma aresta na fila.
     */
    public void add(Edge edge) {
        queue.add(edge);
    }

    /**
     * Remove e retorna a aresta de menor peso.
     *
     * @return aresta de menor peso ou null caso a fila esteja vazia
     */
    public Edge extractMin() {
        return queue.poll();
    }

    /**
     * Verifica se a fila está vazia.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Retorna o tamanho atual da fila.
     */
    public int size() {
        return queue.size();
    }

    /**
     * Remove todos os elementos.
     */
    public void clear() {
        queue.clear();
    }
}