public class PerformanceMonitor {
    private static final Map<String, MethodStats> methodStats = new ConcurrentHashMap<>();
    
    public static <T> T monitor(String methodName, Supplier<T> supplier) {
        long startTime = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            long duration = System.nanoTime() - startTime;
            methodStats.computeIfAbsent(methodName, k -> new MethodStats())
                .recordExecution(duration);
        }
    }
    
    public static void logPerformanceMetrics() {
        methodStats.forEach((method, stats) -> {
            ModLogger.info("Method {} - Avg: {}ms, Max: {}ms, Min: {}ms, Calls: {}",
                method,
                stats.getAverageMs(),
                stats.getMaxMs(),
                stats.getMinMs(),
                stats.getCallCount());
        });
    }
}