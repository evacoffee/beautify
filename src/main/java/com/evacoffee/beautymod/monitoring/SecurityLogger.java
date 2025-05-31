public class SecurityLogger {
    private static final Path LOG_DIR = Paths.get("logs/beautymod/security");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public static void logSecurityEvent(SecurityEvent event) {
        try {
            Path logFile = LOG_DIR.resolve("security-" + LocalDate.now().format(DATE_FORMAT) + ".log");
            Files.createDirectories(logFile.getParent());
            
            String logEntry = String.format("[%s] [%s] %s: %s%n",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                event.getLevel(),
                event.getSource(),
                event.getMessage());
            
            Files.writeString(logFile, logEntry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            ModLogger.error("Failed to log security event", e);
        }
    }
}