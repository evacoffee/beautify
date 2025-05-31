public class RateLimiter {
    private final Map<UUID, RequestData> requestMap = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long timeWindowMs;
    
    public RateLimiter(int maxRequests, Duration timeWindow) {
        this.maxRequests = maxRequests;
        this.timeWindowMs = timeWindow.toMillis();
    }
    
    public synchronized boolean allowRequest(UUID playerId) {
        long currentTime = System.currentTimeMillis();
        RequestData data = requestMap.computeIfAbsent(playerId, k -> new RequestData());
        
        // Remove old timestamps
        data.timestamps.removeIf(timestamp -> currentTime - timestamp > timeWindowMs);
        
        if (data.timestamps.size() >= maxRequests) {
            return false;
        }
        
        data.timestamps.add(currentTime);
        return true;
    }
    
    private static class RequestData {
        final Queue<Long> timestamps = new ConcurrentLinkedQueue<>();
    }
}