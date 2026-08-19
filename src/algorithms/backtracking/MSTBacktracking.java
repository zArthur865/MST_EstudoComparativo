package algorithms.backtracking;

import algorithms.common.MSTAlgorithm;
import algorithms.common.MSTResult;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementação de MST utilizando Backtracking.
 *
 * O algoritmo explora diferentes combinações de arestas
 * buscando uma árvore geradora de peso mínimo.
 *
 * Para reduzir o espaço de busca:
 * - as arestas são processadas em ordem crescente de peso;
 * - combinações que formariam ciclos são podadas;
 * - soluções cujo peso já não pode superar a melhor solução
 *   conhecida também são podadas.
 */
public class MSTBacktracking implements MSTAlgorithm {

    private BacktrackingMetrics metrics;

    private List<Edge> bestSolution;
    private double bestWeight;

    private List<Edge> candidateEdges;

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
         * Inicializa as métricas para uma nova execução.
         */
        metrics = new BacktrackingMetrics();

        bestSolution = new ArrayList<>();
        bestWeight = Double.POSITIVE_INFINITY;

        /*
         * Cria uma cópia das arestas para não alterar
         * a estrutura original do Graph.
         */
        candidateEdges = new ArrayList<>(
                graph.getEdges()
        );

        /*
         * Processar as arestas em ordem crescente
         * melhora a qualidade das primeiras soluções
         * encontradas e, consequentemente, permite
         * mais podas.
         */
        candidateEdges.sort(
                Comparator.comparingDouble(
                        Edge::getWeight
                )
        );

        /*
         * Caso especial: um grafo com um único vértice.
         */
        if (vertices == 1) {
            return new MSTResult(
                    new ArrayList<>(),
                    0.0
            );
        }

        /*
         * Inicia a busca.
         */
        List<Edge> currentSolution =
                new ArrayList<>();

        boolean[] selected =
                new boolean[candidateEdges.size()];

        backtrack(
                0,
                currentSolution,
                0.0,
                selected,
                vertices
        );

        /*
         * Se nenhuma solução foi encontrada,
         * o grafo não é conexo.
         */
        if (bestSolution.size()
                != vertices - 1) {

            throw new IllegalArgumentException(
                    "Não foi possível construir uma MST. "
                            + "O grafo provavelmente não é conexo."
            );
        }

        return new MSTResult(
                bestSolution,
                bestWeight
        );
    }

    /**
     * Função recursiva principal do Backtracking.
     */
    private void backtrack(
            int index,
            List<Edge> currentSolution,
            double currentWeight,
            boolean[] selected,
            int vertices
    ) {

        metrics.incrementRecursiveCalls();
        metrics.incrementStatesExplored();

        int depth = currentSolution.size();

        metrics.updateMaxDepth(depth);

        /*
         * Se já temos V - 1 arestas, podemos verificar
         * se construímos uma árvore geradora.
         */
        if (currentSolution.size()
                == vertices - 1) {

            if (isConnected(
                    currentSolution,
                    vertices
            )) {

                if (currentWeight < bestWeight) {

                    bestWeight = currentWeight;

                    bestSolution =
                            new ArrayList<>(
                                    currentSolution
                            );
                }
            }

            return;
        }

        /*
         * Não há mais arestas disponíveis.
         */
        if (index >= candidateEdges.size()) {
            return;
        }

        /*
         * Poda por custo:
         *
         * se o peso atual já é maior ou igual
         * à melhor solução encontrada, não vale
         * a pena continuar explorando.
         */
        if (currentWeight >= bestWeight) {

            metrics.incrementPrunings();

            return;
        }

        /*
         * Poda estrutural:
         *
         * Mesmo escolhendo todas as próximas arestas,
         * não conseguiremos alcançar V - 1 arestas.
         */
        int remainingEdges =
                candidateEdges.size() - index;

        int neededEdges =
                (vertices - 1)
                        - currentSolution.size();

        if (remainingEdges < neededEdges) {

            metrics.incrementPrunings();

            return;
        }

        Edge currentEdge =
                candidateEdges.get(index);

        /*
         * ============================================
         * RAMO 1 — INCLUIR A ARESTA
         * ============================================
         */

        if (!createsCycle(
                currentSolution,
                currentEdge
        )) {

            currentSolution.add(
                    currentEdge
            );

            selected[index] = true;

            backtrack(
                    index + 1,
                    currentSolution,
                    currentWeight
                            + currentEdge.getWeight(),
                    selected,
                    vertices
            );

            selected[index] = false;

            currentSolution.remove(
                    currentSolution.size() - 1
            );

        } else {

            /*
             * A inclusão criaria um ciclo.
             */
            metrics.incrementPrunings();
        }

        /*
         * ============================================
         * RAMO 2 — EXCLUIR A ARESTA
         * ============================================
         */

        backtrack(
                index + 1,
                currentSolution,
                currentWeight,
                selected,
                vertices
        );
    }

    /**
     * Verifica se a inclusão de uma aresta criaria
     * um ciclo na solução parcial.
     */
    private boolean createsCycle(
            List<Edge> currentSolution,
            Edge candidate
    ) {

        int maxVertex = 0;

        for (Edge edge : candidateEdges) {
            maxVertex = Math.max(
                    maxVertex,
                    Math.max(
                            edge.getSource(),
                            edge.getDestination()
                    )
            );
        }

        maxVertex = Math.max(
                maxVertex,
                Math.max(
                        candidate.getSource(),
                        candidate.getDestination()
                )
        );

        boolean[] visited =
                new boolean[maxVertex + 1];

        /*
         * DFS para verificar se já existe caminho
         * entre as extremidades da nova aresta.
         */
        return pathExists(
                candidate.getSource(),
                candidate.getDestination(),
                currentSolution,
                visited
        );
    }

    /**
     * Verifica se existe um caminho entre source e destination
     * usando somente as arestas da solução parcial.
     */
    private boolean pathExists(
            int current,
            int target,
            List<Edge> solution,
            boolean[] visited
    ) {

        if (current == target) {
            return true;
        }

        visited[current] = true;

        for (Edge edge : solution) {

            int neighbor = -1;

            if (edge.getSource() == current) {

                neighbor =
                        edge.getDestination();

            } else if (
                    edge.getDestination()
                            == current
            ) {

                neighbor =
                        edge.getSource();
            }

            if (neighbor != -1
                    && !visited[neighbor]) {

                if (pathExists(
                        neighbor,
                        target,
                        solution,
                        visited
                )) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verifica se a solução parcial conecta
     * todos os vértices.
     */
    private boolean isConnected(
            List<Edge> solution,
            int vertices
    ) {

        if (vertices == 1) {
            return true;
        }

        boolean[] visited =
                new boolean[vertices];

        List<List<Integer>> adjacency =
                new ArrayList<>();

        for (int i = 0; i < vertices; i++) {
            adjacency.add(
                    new ArrayList<>()
            );
        }

        for (Edge edge : solution) {

            adjacency
                    .get(edge.getSource())
                    .add(edge.getDestination());

            adjacency
                    .get(edge.getDestination())
                    .add(edge.getSource());
        }

        dfs(
                0,
                adjacency,
                visited
        );

        for (boolean vertexVisited : visited) {

            if (!vertexVisited) {
                return false;
            }
        }

        return true;
    }

    /**
     * DFS utilizada para verificar conectividade.
     */
    private void dfs(
            int vertex,
            List<List<Integer>> adjacency,
            boolean[] visited
    ) {

        visited[vertex] = true;

        for (int neighbor :
                adjacency.get(vertex)) {

            if (!visited[neighbor]) {

                dfs(
                        neighbor,
                        adjacency,
                        visited
                );
            }
        }
    }

    /**
     * Retorna as métricas da última execução.
     */
    public BacktrackingMetrics getMetrics() {

        if (metrics == null) {

            throw new IllegalStateException(
                    "O algoritmo ainda não foi executado."
            );
        }

        return metrics;
    }

    @Override
    public String getName() {
        return "Backtracking - MST";
    }
}