package algorithms.backtracking;

import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do algoritmo de Backtracking para Árvore Geradora Mínima.
 *
 * Estratégia:
 * 1. Ordena as arestas por peso crescente.
 * 2. Para cada aresta, decide recursivamente incluí-la ou não na solução.
 * 3. Podas aplicadas:
 *    - Não incluir arestas que formem ciclo (verificação com Union-Find simplificado);
 *    - Se o número de arestas restantes for insuficiente para completar V - 1;
 *    - Se o limite inferior otimista (peso atual + menores arestas restantes)
 *      já não puder superar a melhor solução encontrada.
 * 4. Retorna {@link BacktrackingResult} com métricas de exploração
 *    da busca para análise comparativa com os algoritmos gulosos.
 */
public class MSTBacktracking implements MSTAlgorithm {

    @Override
    public MSTResult execute(Graph graph) {

        if (graph == null) {
            throw new IllegalArgumentException(
                    "O grafo não pode ser nulo."
            );
        }

        int vertices = graph.getVertices();

        if (vertices == 0) {
            throw new IllegalArgumentException(
                    "O grafo deve possuir pelo menos um vértice."
            );
        }

        /*
         * Grafo com 1 vértice:
         * a MST é vazia e possui peso 0.
         */
        if (vertices == 1) {

            return new BacktrackingResult(
                    new ArrayList<>(),
                    0.0,
                    1,
                    0,
                    0
            );
        }

        /*
         * Copiamos as arestas e ordenamos por peso crescente.
         *
         * A ordenação permite aplicar a poda por limite inferior.
         */
        List<Edge> sortedEdges =
                new ArrayList<>(graph.getEdges());

        sortedEdges.sort(null);

        int edgeCount = sortedEdges.size();

        /*
         * minRemaining[i] = soma dos pesos das arestas
         * na posição i até o final da lista ordenada.
         *
         * Isso representa o limite inferior otimista:
         * assumimos que conseguimos adicionar as menores arestas
         * restantes sem formar ciclos.
         */
        double[] minRemaining = new double[edgeCount + 1];
        minRemaining[edgeCount] = 0.0;

        for (int i = edgeCount - 1; i >= 0; i--) {
            minRemaining[i] =
                    minRemaining[i + 1] + sortedEdges.get(i).getWeight();
        }

        /*
         * Estado inicial da busca.
         *
         * Utilizamos um Union-Find simples (sem compressão de caminho)
         * para permitir o rollback durante o backtracking.
         */
        int[] parent = new int[vertices];

        for (int i = 0; i < vertices; i++) {
            parent[i] = i;
        }

        List<Edge> currentEdges = new ArrayList<>();

        double[] bestWeight = {Double.POSITIVE_INFINITY};

        List<Edge> bestEdges = new ArrayList<>();

        long[] exploredStates = {0};
        long[] prunedStates = {0};
        int[] maxRecursionDepth = {0};

        backtrack(
                sortedEdges,
                minRemaining,
                0,
                vertices,
                parent,
                currentEdges,
                0.0,
                bestWeight,
                bestEdges,
                exploredStates,
                prunedStates,
                maxRecursionDepth,
                0
        );

        /*
         * Se não encontramos V - 1 arestas,
         * o grafo não é conexo.
         */
        if (bestEdges.size() != vertices - 1) {

            throw new IllegalArgumentException(
                    "Não foi possível construir uma MST. "
                            + "O grafo provavelmente não é conexo."
            );
        }

        return new BacktrackingResult(
                bestEdges,
                bestWeight[0],
                exploredStates[0],
                prunedStates[0],
                maxRecursionDepth[0]
        );
    }

    /**
     * Busca recursiva por decisão binária sobre cada aresta.
     *
     * Em cada nível decidimos incluir ou não a aresta atual.
     * As podas reduzem drasticamente o espaço de busca.
     *
     * @param edges            arestas ordenadas por peso
     * @param minRemaining     soma acumulada dos menores pesos restantes
     * @param index            posição atual na lista de arestas
     * @param vertices         quantidade de vértices do grafo
     * @param parent           vetor do Union-Find (para detectar ciclos)
     * @param currentEdges     arestas selecionadas na solução parcial
     * @param currentWeight    peso atual da solução parcial
     * @param bestWeight       melhor peso encontrado até o momento
     * @param bestEdges        arestas da melhor solução encontrada
     * @param exploredStates   contador de estados explorados
     * @param prunedStates     contador de estados podados
     * @param maxRecursionDepth profundidade máxima atingida
     * @param depth            profundidade atual da recursão
     */
    private void backtrack(
            List<Edge> edges,
            double[] minRemaining,
            int index,
            int vertices,
            int[] parent,
            List<Edge> currentEdges,
            double currentWeight,
            double[] bestWeight,
            List<Edge> bestEdges,
            long[] exploredStates,
            long[] prunedStates,
            int[] maxRecursionDepth,
            int depth
    ) {

        exploredStates[0]++;

        maxRecursionDepth[0] =
                Math.max(maxRecursionDepth[0], depth);

        /*
         * Solução completa:
         * V - 1 arestas sem ciclos formam uma árvore geradora.
         *
         * Como todos os pesos são não negativos, qualquer solução
         * completa encontrada é automaticamente melhor ou igual
         * às que ainda seriam geradas a partir dela.
         */
        if (currentEdges.size() == vertices - 1) {

            bestWeight[0] = currentWeight;

            bestEdges.clear();
            bestEdges.addAll(currentEdges);

            return;
        }

        /*
         * Poda: não restam arestas para tentar.
         */
        if (index >= edges.size()) {
            prunedStates[0]++;
            return;
        }

        /*
         * Poda: quantidade insuficiente de arestas restantes
         * para completar V - 1.
         */
        int needed = (vertices - 1) - currentEdges.size();

        if (needed > edges.size() - index) {
            prunedStates[0]++;
            return;
        }

        /*
         * Poda por limite inferior otimista:
         *
         * mesmo que as próximas 'needed' menores arestas
         * pudessem ser todas adicionadas sem formar ciclo,
         * o peso final não superaria a melhor solução atual.
         */
        double optimisticLowerBound =
                currentWeight
                        + (minRemaining[index] - minRemaining[index + needed]);

        if (optimisticLowerBound >= bestWeight[0]) {
            prunedStates[0]++;
            return;
        }

        Edge edge = edges.get(index);

        int source = edge.getSource();
        int destination = edge.getDestination();

        /*
         * Opção 1: INCLUIR a aresta atual,
         * somente se não formar ciclo na floresta parcial.
         *
         * Tentamos incluir primeiro para encontrar uma solução
         * completa rapidamente, o que fortalece a poda por
         * limite inferior nos demais ramos.
         */
        int rootSource = find(parent, source);
        int rootDestination = find(parent, destination);

        if (rootSource != rootDestination) {

            /*
             * Union (sem path compression para permitir rollback).
             */
            parent[rootSource] = rootDestination;

            currentEdges.add(edge);

            backtrack(
                    edges,
                    minRemaining,
                    index + 1,
                    vertices,
                    parent,
                    currentEdges,
                    currentWeight + edge.getWeight(),
                    bestWeight,
                    bestEdges,
                    exploredStates,
                    prunedStates,
                    maxRecursionDepth,
                    depth + 1
            );

            /*
             * Rollback:
             * remove a aresta e desfaz o union.
             */
            currentEdges.remove(currentEdges.size() - 1);
            parent[rootSource] = rootSource;
        }

        /*
         * Opção 2: NÃO incluir a aresta atual.
         */
        backtrack(
                edges,
                minRemaining,
                index + 1,
                vertices,
                parent,
                currentEdges,
                currentWeight,
                bestWeight,
                bestEdges,
                exploredStates,
                prunedStates,
                maxRecursionDepth,
                depth + 1
        );
    }

    /**
     * Encontra a raiz do conjunto de um vértice.
     *
     * Versão sem compressão de caminho para permitir
     * desfazer (rollback) a operação de union no backtracking.
     */
    private int find(int[] parent, int vertex) {

        while (parent[vertex] != vertex) {
            vertex = parent[vertex];
        }

        return vertex;
    }

    @Override
    public String getName() {
        return "Backtracking";
    }
}