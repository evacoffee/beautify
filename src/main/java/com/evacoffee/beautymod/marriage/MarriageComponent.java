package com.evacoffee.beautymod.marriage;

import com.evacoffee.beautymod.BeautyMod;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class MarriageComponent implements AutoSyncedComponent {
    // NBT Keys
    private static final String MARRIED_TO_KEY = "MarriedTo";
    private static final String IS_MARRIED_KEY = "IsMarried";
    private static final String WEDDING_DAY_KEY = "WeddingDay";
    private static final String HOME_POS_KEY = "HomePos";
    private static final String SHARED_INVENTORY_KEY = "SharedInventory";
    private static final String MARRIAGE_LEVEL_KEY = "MarriageLevel";
    private static final String MARRIAGE_XP_KEY = "MarriageXp";
    private static final String UNLOCKED_PERKS_KEY = "UnlockedPerks";
    private static final String DAYS_MARRIED_KEY = "DaysMarried";
    private static final String TIMES_TELEPORTED_TO_SPOUSE_KEY = "TimesTeleportedToSpouse";
    private static final String LAST_ANNIVERSARY_DAY_KEY = "LastAnniversaryDay";
    private static final String IS_PUBLIC_KEY = "IsPublic";
    private static final String PET_NAMES_KEY = "PetNames";

    private final PlayerEntity player;
    private boolean isMarried = false;
    private UUID spouseUuid;
    private String spouseName;
    private long weddingDay;
    private UUID marriageProposal;
    private GlobalPos homePosition;
    private final List<ItemStack> sharedInventory = new ArrayList<>(27); // 3 rows of 9
    private int marriageLevel = 1;
    private int marriageXP = 0;
    private final Set<MarriagePerk> unlockedPerks = EnumSet.noneOf(MarriagePerk.class);
    private int timesTeleportedToSpouse = 0;
    private long lastAnniversaryDay = -1;
    private boolean isPublic = true;
    private final Map<UUID, String> petNames = new HashMap<>();

    public MarriageComponent(PlayerEntity player) {
        this.player = player;
    }

    // ===== Core Marriage Methods =====

    public boolean marry(ServerPlayerEntity spouse, long weddingDay) {
        if (isMarried) return false;
        
        this.isMarried = true;
        this.spouseUuid = spouse.getUuid();
        this.spouseName = spouse.getEntityName();
        this.weddingDay = weddingDay;
        
        // Unlock basic perks
        unlockPerk(MarriagePerk.SHARED_INVENTORY);
        
        // Sync the component
        sync();
        return true;
    }

    public void divorce() {
        if (!isMarried) return;
        
        // Notify spouse if online
        if (player.world != null && !player.world.isClient) {
            ServerPlayerEntity spousePlayer = ((ServerWorld) player.world).getServer().getPlayerManager().getPlayer(spouseUuid);
            if (spousePlayer != null) {
                spousePlayer.sendMessage(Text.of("§c" + player.getEntityName() + " has divorced you."), false);
            }
        }
        
        // Reset all marriage data
        isMarried = false;
        spouseUuid = null;
        spouseName = null;
        weddingDay = 0;
        homePosition = null;
        sharedInventory.clear();
        marriageLevel = 1;
        marriageXP = 0;
        unlockedPerks.clear();
        timesTeleportedToSpouse = 0;
        lastAnniversaryDay = -1;
        isPublic = true;
        
        sync();
    }

    // ===== Home Management =====

    public boolean setHomePosition(BlockPos pos, RegistryKey<World> dimension) {
        if (!isMarried) return false;
        
        this.homePosition = GlobalPos.create(dimension, pos);
        sync();
        return true;
    }

    public boolean teleportToHome(ServerPlayerEntity player) {
        if (!isMarried || homePosition == null) return false;
        if (!hasPerk(MarriagePerk.HOME_TELEPORT)) return false;
        
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        
        ServerWorld targetWorld = server.getWorld(homePosition.getDimension());
        if (targetWorld == null) return false;
        
        player.teleport(
            targetWorld,
            homePosition.getPos().getX() + 0.5,
            homePosition.getPos().getY(),
            homePosition.getPos().getZ() + 0.5,
            player.getYaw(),
            player.getPitch()
        );
        return true;
    }

    // ===== Perk System =====

    public boolean hasPerk(MarriagePerk perk) {
        return unlockedPerks.contains(perk);
    }

    public boolean unlockPerk(MarriagePerk perk) {
        if (!isMarried || marriageLevel < perk.getLevelRequired() || hasPerk(perk)) {
            return false;
        }
        
        unlockedPerks.add(perk);
        applyPerkEffects(perk);
        sync();
        return true;
    }

    private void applyPerkEffects(MarriagePerk perk) {
        if (player == null || player.world.isClient) return;
        
        switch (perk) {
            case SHARED_XP:
                if (player instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) player).addExperience(100);
                }
                break;
            case ETERNAL_BOND:
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.LUCK, Integer.MAX_VALUE, 1, false, false
                ));
                break;
            case SOUL_BOND:
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.ABSORPTION, 1200, 1, false, false
                ));
                break;
        }
        
        if (player instanceof ServerPlayerEntity) {
            player.sendMessage(Text.of("§aUnlocked perk: §e" + perk.getDisplayName()), false);
        }
    }

    // ===== Experience & Leveling =====

    public void addMarriageXP(int amount) {
        if (!isMarried) return;
        
        marriageXP += amount;
        int xpForNextLevel = getXPForNextLevel();
        
        while (marriageXP >= xpForNextLevel) {
            marriageXP -= xpForNextLevel;
            marriageLevel++;
            xpForNextLevel = getXPForNextLevel();
            
            // Notify player of level up
            if (player instanceof ServerPlayerEntity) {
                player.sendMessage(Text.of("§a§lMarriage Level Up! §r§fNow level " + marriageLevel), false);
                
                // Check for new perks unlocked at this level
                for (MarriagePerk perk : MarriagePerk.values()) {
                    if (perk.getLevelRequired() == marriageLevel) {
                        unlockPerk(perk);
                    }
                }
            }
        }
        
        sync();
    }

    private int getXPForNextLevel() {
        // Simple exponential curve for level progression
        return 1000 + (marriageLevel * 250);
    }

    // ===== Time-based Methods =====

    public int getDaysMarried(long currentTime) {
        if (weddingDay == 0) return 0;
        long ticksPassed = currentTime - weddingDay;
        return (int)(ticksPassed / 24000L); // Convert ticks to days
    }

    public int getYearsMarried(long currentTime) {
        return getDaysMarried(currentTime) / 365;
    }

    public boolean checkAnniversary(long currentTime) {
        if (weddingDay == 0) return false;
        
        long currentDay = currentTime / 24000L;
        long lastAnniversary = lastAnniversaryDay;
        long daysMarried = currentDay - (weddingDay / 24000L);
        
        // Check if it's a new anniversary day and we haven't celebrated it yet
        if (daysMarried > 0 && daysMarried % 365 == 0 && currentDay != lastAnniversary) {
            lastAnniversaryDay = currentDay;
            sync();
            return true;
        }
        return false;
    }

    // ===== Shared Inventory =====

    public boolean addToSharedInventory(ItemStack stack) {
        if (!isMarried || !hasPerk(MarriagePerk.SHARED_INVENTORY)) return false;
        
        for (int i = 0; i < sharedInventory.size(); i++) {
            ItemStack slot = sharedInventory.get(i);
            if (slot.isEmpty()) {
                sharedInventory.set(i, stack.copy());
                sync();
                return true;
            } else if (ItemStack.canCombine(slot, stack) && slot.getCount() < slot.getMaxCount()) {
                int transferAmount = Math.min(stack.getCount(), slot.getMaxCount() - slot.getCount());
                slot.increment(transferAmount);
                stack.decrement(transferAmount);
                if (stack.isEmpty()) {
                    sync();
                    return true;
                }
            }
        }
        return false;
    }

    public ItemStack takeFromSharedInventory(int slot) {
        if (!isMarried || !hasPerk(MarriagePerk.SHARED_INVENTORY) || slot < 0 || slot >= sharedInventory.size()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack stack = sharedInventory.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack result = stack.split(stack.getCount());
        if (stack.isEmpty()) {
            sharedInventory.set(slot, ItemStack.EMPTY);
        }
        
        sync();
        return result;
    }

    // ===== Utility Methods =====

    public boolean teleportToSpouse(ServerPlayerEntity player) {
        if (!isMarried || !hasPerk(MarriagePerk.SPOUSE_TELEPORT)) return false;
        
        ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(spouseUuid);
        if (spouse == null) return false;
        
        // Check cooldown or other conditions
        if (timesTeleportedToSpouse >= 3) {
            player.sendMessage(Text.of("§cYou've reached your daily teleport limit to your spouse."), false);
            return false;
        }
        
        player.teleport(
            (ServerWorld) spouse.world,
            spouse.getX(),
            spouse.getY(),
            spouse.getZ(),
            spouse.getYaw(),
            spouse.getPitch()
        );
        
        timesTeleportedToSpouse++;
        sync();
        return true;
    }

    public void onPlayerTick() {
        if (!isMarried || player.world.isClient) return;
        
        long currentTime = player.world.getTime();
        
        // Check for anniversaries daily
        if (currentTime % 24000 == 0) {
            checkAnniversary(currentTime);
        }
        
        // Apply buffs when near spouse
        if (hasPerk(MarriagePerk.SOUL_BOND) && player.world instanceof ServerWorld) {
            ServerPlayerEntity spouse = ((ServerWorld) player.world).getServer().getPlayerManager().getPlayer(spouseUuid);
            if (spouse != null && spouse.world == player.world && 
                player.distanceTo(spouse) < 16.0) {
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.REGENERATION, 100, 0, false, false
                ));
            }
        }
    }

    // ===== Getters =====

    public boolean isMarried() { return isMarried; }
    public UUID getSpouseUuid() { return spouseUuid; }
    public String getSpouseName() { return spouseName; }
    public long getWeddingDay() { return weddingDay; }
    public int getMarriageLevel() { return marriageLevel; }
    public int getMarriageXP() { return marriageXP; }
    public Set<MarriagePerk> getUnlockedPerks() { return Collections.unmodifiableSet(unlockedPerks); }
    public List<ItemStack> getSharedInventory() { return Collections.unmodifiableList(sharedInventory); }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { 
        this.isPublic = isPublic; 
        sync();
    }
    public UUID getMarriageProposal() { return marriageProposal; }
    public void setMarriageProposal(UUID proposerUuid) { 
        this.marriageProposal = proposerUuid; 
        sync();
    }

    // ===== NBT Serialization =====

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.isMarried = tag.getBoolean(IS_MARRIED_KEY);
        
        if (tag.containsUuid(MARRIED_TO_KEY)) {
            this.spouseUuid = tag.getUuid(MARRIED_TO_KEY);
            this.spouseName = tag.getString("SpouseName");
        }
        
        this.weddingDay = tag.getLong(WEDDING_DAY_KEY);
        
        if (tag.contains(HOME_POS_KEY, NbtElement.COMPOUND_TYPE)) {
            NbtCompound homeTag = tag.getCompound(HOME_POS_KEY);
            this.homePosition = GlobalPos.CODEC.decode(NbtOps.INSTANCE, homeTag)
                .result()
                .map(pair -> pair.getFirst())
                .orElse(null);
        }
        
        // Read shared inventory
        this.sharedInventory.clear();
        NbtList inventoryTag = tag.getList(SHARED_INVENTORY_KEY, NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < inventoryTag.size(); i++) {
            this.sharedInventory.add(ItemStack.fromNbt(inventoryTag.getCompound(i)));
        }
        
        this.marriageLevel = tag.getInt(MARRIAGE_LEVEL_KEY);
        this.marriageXP = tag.getInt(MARRIAGE_XP_KEY);
        
        // Read unlocked perks
        this.unlockedPerks.clear();
        NbtList perksTag = tag.getList(UNLOCKED_PERKS_KEY, NbtElement.STRING_TYPE);
        for (int i = 0; i < perksTag.size(); i++) {
            try {
                this.unlockedPerks.add(MarriagePerk.valueOf(perksTag.getString(i)));
            } catch (IllegalArgumentException e) {
                BeautyMod.LOGGER.warn("Unknown perk: " + perksTag.getString(i));
            }
        }
        
        this.timesTeleportedToSpouse = tag.getInt(TIMES_TELEPORTED_TO_SPOUSE_KEY);
        this.lastAnniversaryDay = tag.getLong(LAST_ANNIVERSARY_DAY_KEY);
        this.isPublic = tag.getBoolean(IS_PUBLIC_KEY);
        
        // Read pet names
        NbtCompound petsTag = tag.getCompound(PET_NAMES_KEY);
        this.petNames.clear();
        for (String key : petsTag.getKeys()) {
            this.petNames.put(UUID.fromString(key), petsTag.getString(key));
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean(IS_MARRIED_KEY, isMarried);
        
        if (spouseUuid != null) {
            tag.putUuid(MARRIED_TO_KEY, spouseUuid);
            tag.putString("SpouseName", spouseName != null ? spouseName : "");
        }
        
        tag.putLong(WEDDING_DAY_KEY, weddingDay);
        
        if (homePosition != null) {
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, homePosition)
                .result()
                .ifPresent(pos -> tag.put(HOME_POS_KEY, pos));
        }
        
        // Write shared inventory
        NbtList inventoryTag = new NbtList();
        for (ItemStack stack : sharedInventory) {
            inventoryTag.add(stack.writeNbt(new NbtCompound()));
        }
        tag.put(SHARED_INVENTORY_KEY, inventoryTag);
        
        tag.putInt(MARRIAGE_LEVEL_KEY, marriageLevel);
        tag.putInt(MARRIAGE_XP_KEY, marriageXP);
        
        // Write unlocked perks
        NbtList perksTag = new NbtList();
        for (MarriagePerk perk : unlockedPerks) {
            perksTag.add(NbtString.of(perk.name()));
        }
        tag.put(UNLOCKED_PERKS_KEY, perksTag);
        
        tag.putInt(TIMES_TELEPORTED_TO_SPOUSE_KEY, timesTeleportedToSpouse);
        tag.putLong(LAST_ANNIVERSARY_DAY_KEY, lastAnniversaryDay);
        tag.putBoolean(IS_PUBLIC_KEY, isPublic);
        
        // Write pet names
        NbtCompound petsTag = new NbtCompound();
        petNames.forEach((id, name) -> petsTag.putString(id.toString(), name));
        tag.put(PET_NAMES_KEY, petsTag);
    }

    private void sync() {
        if (player != null && !player.world.isClient) {
            BeautyMod.MARRIAGE_COMPONENT.sync(player);
        }
    }
}