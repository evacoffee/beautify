// GiftHandler.java
public class GiftHandler {
    private static final Map<Item, Integer> GIFT_VALUES = new HashMap<>();
    
    static {
        // Initialize gift values
        GIFT_VALUES.put(Items.ROSE, 10);
        GIFT_VALUES.put(Items.DIAMOND, 50);
        // Add more items as needed
    }
    
    public static int getGiftValue(ItemStack stack) {
        return GIFT_VALUES.getOrDefault(stack.getItem(), 0);
    }
}