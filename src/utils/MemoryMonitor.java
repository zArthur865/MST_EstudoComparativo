package utils;

/**
 * Utilitário para estimar o consumo de memória durante a execução.
 */
public class MemoryMonitor {
    private long memoryBefore;
    private long memoryAfter;

    public void start() {
        Runtime.getRuntime().gc();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        this.memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public void stop() {
        this.memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /**
     * Retorna a quantidade aproximada de memória gasta em bytes.
     */
    public long getUsedMemoryBytes() {
        long used = memoryAfter - memoryBefore;
        return Math.max(0, used);
    }
}