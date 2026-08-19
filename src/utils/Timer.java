package utils;

/**
 * Responsável pela medição de tempo de execução.
 *
 * A medição é realizada em nanossegundos utilizando
 * System.nanoTime(), apropriado para medir intervalos de tempo.
 */
public class Timer {

    private long startTime;
    private long elapsedTimeNanos;
    private boolean running;

    /**
     * Cria um timer parado.
     */
    public Timer() {
        this.startTime = 0L;
        this.elapsedTimeNanos = 0L;
        this.running = false;
    }

    /**
     * Inicia a medição.
     *
     * @throws IllegalStateException se o timer já estiver rodando
     */
    public void start() {
        if (running) {
            throw new IllegalStateException(
                    "O timer já está em execução."
            );
        }

        startTime = System.nanoTime();
        elapsedTimeNanos = 0L;
        running = true;
    }

    /**
     * Finaliza a medição.
     *
     * @throws IllegalStateException se o timer não estiver rodando
     */
    public void stop() {
        if (!running) {
            throw new IllegalStateException(
                    "O timer não está em execução."
            );
        }

        elapsedTimeNanos =
                System.nanoTime() - startTime;

        running = false;
    }

    /**
     * Retorna o tempo decorrido da última medição.
     *
     * @return tempo em nanossegundos
     */
    public long getElapsedTimeNanos() {
        if (running) {
            /*
             * Permite consultar o tempo mesmo antes do stop().
             */
            return System.nanoTime() - startTime;
        }

        return elapsedTimeNanos;
    }

    /**
     * Retorna o tempo em milissegundos.
     *
     * @return tempo em milissegundos
     */
    public double getElapsedTimeMillis() {
        return getElapsedTimeNanos() / 1_000_000.0;
    }

    /**
     * Verifica se o timer está em execução.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Reinicia completamente o timer.
     */
    public void reset() {
        startTime = 0L;
        elapsedTimeNanos = 0L;
        running = false;
    }
}