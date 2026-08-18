package utils;

/**
 * Utilitário para medir o tempo de execução dos algoritmos.
 */
public class Timer {
    private long startTime;
    private long endTime;
    private boolean isRunning;

    public void start() {
        this.startTime = System.nanoTime();
        this.isRunning = true;
    }

    public void stop() {
        this.endTime = System.nanoTime();
        this.isRunning = false;
    }

    /**
     * Retorna o tempo decorrido em nanossegundos.
     */
    public long getElapsedTimeNanos() {
        if (isRunning) {
            return System.nanoTime() - startTime;
        }
        return endTime - startTime;
    }

    /**
     * Retorna o tempo decorrido em milissegundos.
     */
    public double getElapsedTimeMillis() {
        return getElapsedTimeNanos() / 1_000_000.0;
    }
}