// Memory.java
public class Memory {
    private final String id;
    private final String title;
    private final Text description;
    private final long timestamp;
    
    public Memory(String id, String title, Text description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and serialization methods
}