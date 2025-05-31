public class SecureDataStorage {
    private final EncryptionService encryptionService;
    private final Path storagePath;
    
    public SecureDataStorage(Path basePath) {
        this.storagePath = basePath.resolve("secure_data");
        this.encryptionService = new EncryptionService();
        ensureDirectoryExists();
    }
    
    public void saveData(UUID playerId, String key, String data) {
        try {
            String encrypted = encryptionService.encrypt(data);
            Path filePath = getPlayerFile(playerId, key);
            Files.writeString(filePath, encrypted, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            ModLogger.error("Failed to save secure data", e);
        }
    }
    
    // ... other methods
}