public class DataValidator {
    private static final Pattern SAFE_STRING = Pattern.compile("^[a-zA-Z0-9 _\\-]{1,64}$");
    private static final int MAX_JSON_DEPTH = 10;
    
    public static boolean isValidPlayerName(String name) {
        return name != null && SAFE_STRING.matcher(name).matches();
    }
    
    public static boolean isValidJson(String json) {
        try {
            validateJsonDepth(new ObjectMapper().readTree(json), 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void validateJsonDepth(JsonNode node, int depth) {
        if (depth > MAX_JSON_DEPTH) {
            throw new ValidationException("JSON depth exceeds maximum allowed");
        }
        // Recursively validate JSON depth
    }
}