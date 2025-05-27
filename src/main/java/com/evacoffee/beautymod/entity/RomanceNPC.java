private String npcName;
private String personality;
private String[] dialogueLines;
private Identifier texture;

public RomanceNPC(EntityType<? extends RomanceNPC> entityType, World world) {
    super(entityType, world);
}

// === Attributes for movement and health ===
public static DefaultAttributeContainer.Builder createMobAttributes() {
    return PathAwareEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
}

// === NPC Configuration ===
public void setNPCInfo(String name, String personality, String[] lines, Identifier texture) {
    this.npcName = name;
    this.personality = personality;
    this.dialogueLines = lines;
    this.texture = texture;
}

public String getNPCName() {
    return npcName;
}

public String getPersonality() {
    return personality;
}

public String[] getDialogueLines() {
    return dialogueLines;
}

public Identifier getTexture() {
    return texture;
}

public Text getDialogueFor(PlayerEntity player) {
    int affection = AffectionComponentInitializer.AFFECTION.get(player).getAffection(npcName);
    if (affection >= 80) {
        return Text.of("💖 " + npcName + ": " + dialogueLines[0]);
    } else if (affection >= 40) {
        return Text.of("😊 " + npcName + ": " + dialogueLines[1]);
    } else {
        return Text.of("😐 " + npcName + ": " + dialogueLines[2]);
    }
}

public void interact(PlayerEntity player) {
    player.sendMessage(getDialogueFor(player), false);
    AffectionComponentInitializer.AFFECTION.get(player).addAffection(npcName, 2);
}

// === Static predefined NPC data ===
public static final Map<String, NPCInfo> NPC_DATA = new HashMap<>();

static {
    NPC_DATA.put("luna", new NPCInfo("Luna", "Shy", new String[]{
            "Oh! I didn’t expect to see you here...",
            "Do you... like flowers too?",
            "Let’s take a walk... if you want."
    }, "beautymod:textures/entity/luna.png"));

    NPC_DATA.put("zara", new NPCInfo("Zara", "Flirty", new String[]{
            "Hey cutie~ Looking good today.",
            "Wanna rob some villagers together?",
            "You know you’re my favorite, right?"
    }, "beautymod:textures/entity/zara.png"));

    NPC_DATA.put("mira", new NPCInfo("Mira", "Artistic", new String[]{
            "I painted this for you... hope you like it!",
            "Beauty is everywhere—especially in you.",
            "Want to help me design a new mural?"
    }, "beautymod:textures/entity/mira.png"));

    NPC_DATA.put("celia", new NPCInfo("Celia", "Cool", new String[]{
            "Hey. Need anything?",
            "I’ve got your back. Always.",
            "Let’s watch under the stars tonight."
    }, "beautymod:textures/entity/celia.png"));

    NPC_DATA.put("kai", new NPCInfo("Kai", "Charming", new String[]{
            "Looking radiant as always.",
            "A rose for a rose.",
            "How about a dance?"
    }, "beautymod:textures/entity/kai.png"));

    NPC_DATA.put("dante", new NPCInfo("Dante", "Mysterious", new String[]{
            "There’s more to me than meets the eye.",
            "Want to see a secret place?",
            "You ask a lot of interesting questions..."
    }, "beautymod:textures/entity/dante.png"));

    NPC_DATA.put("theo", new NPCInfo("Theo", "Sweet", new String[]{
            "I baked these cookies for you!",
            "Let’s watch the sunset together.",
            "You make everything better."
    }, "beautymod:textures/entity/theo.png"));

    NPC_DATA.put("rico", new NPCInfo("Rico", "Bold", new String[]{
            "Let’s build something big—together!",
            "No challenge is too big if you’re with me!",
            "Hey! You light up my world!"
    }, "beautymod:textures/entity/rico.png"));
}

// === Helper class for defining NPCs ===
public static class NPCInfo {
    public final String name;
    public final String personality;
    public final String[] dialogue;
    public final String texturePath;

    public NPCInfo(String name, String personality, String[] dialogue, String texturePath) {
        this.name = name;
        this.personality = personality;
        this.dialogue = dialogue;
        this.texturePath = texturePath;
    }
}
