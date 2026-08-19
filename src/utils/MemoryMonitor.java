package utils;

/**
 * Responsável por estimar o consumo de memória de uma execução.
 *
 * A estimativa é baseada na diferença entre a memória utilizada
 * antes e depois da execução.
 *
 * Observação:
 * em Java, o Garbage Collector pode interferir na medição,
 * portanto o valor deve ser interpretado como uma estimativa.
 */
public class MemoryMonitor {

    private long initialUsedMemory;
    private long finalUsedMemory;
    private long usedMemoryBytes;
    private boolean running;

    public MemoryMonitor() {
        this.initialUsedMemory = 0L;
        this.finalUsedMemory = 0L;
        this.usedMemoryBytes = 0L;
        this.running = false;
    }

    /**
     * Inicia a medição.
     */
    public void start() {
        if (running) {
            throw new IllegalStateException(
                    "O monitor de memória já está em execução."
            );
        }

        /*
         * Solicita uma coleta antes da medição.
         *
         * Não há garantia de que o GC ocorrerá imediatamente,
         * por isso essa operação serve apenas para reduzir
         * o ruído da medição.
         */
        System.gc();

        Runtime runtime =
                Runtime.getRuntime();

        initialUsedMemory =
                runtime.totalMemory()
                        - runtime.freeMemory();

        finalUsedMemory = 0L;
        usedMemoryBytes = 0L;

        running = true;
    }

    /**
     * Finaliza a medição.
     */
    public void stop() {
        if (!running) {
            throw new IllegalStateException(
                    "O monitor de memória não está em execução."
            );
        }

        Runtime runtime =
                Runtime.getRuntime();

        finalUsedMemory =
                runtime.totalMemory()
                        - runtime.freeMemory();

        /*
         * O consumo estimado não pode ser negativo.
         *
         * Uma redução pode ocorrer caso o GC tenha sido executado
         * durante o algoritmo.
         */
        usedMemoryBytes =
                Math.max(
                        0L,
                        finalUsedMemory
                                - initialUsedMemory
                );

        running = false;
    }

    /**
     * Retorna a memória estimada utilizada.
     *
     * @return memória em bytes
     */
    public long getUsedMemoryBytes() {
        if (running) {
            Runtime runtime =
                    Runtime.getRuntime();

            long currentUsedMemory =
                    runtime.totalMemory()
                            - runtime.freeMemory();

            return Math.max(
                    0L,
                    currentUsedMemory
                            - initialUsedMemory
            );
        }

        return usedMemoryBytes;
    }

    /**
     * Retorna a memória em KB.
     */
    public double getUsedMemoryKilobytes() {
        return getUsedMemoryBytes() / 1024.0;
    }

    /**
     * Retorna a memória em MB.
     */
    public double getUsedMemoryMegabytes() {
        return getUsedMemoryBytes()
                / (1024.0 * 1024.0);
    }

    /**
     * Verifica se a medição está em andamento.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Reinicia o monitor.
     */
    public void reset() {
        initialUsedMemory = 0L;
        finalUsedMemory = 0L;
        usedMemoryBytes = 0L;
        running = false;
    }
}