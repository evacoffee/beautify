package com.evacoffee.beautymod.dating;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class DateOption {
    private final UUID id;
    private final UUID proposer;
    private final UUID recipient;
    private final long proposedTime;
    private final long scheduledTime;
    private final DateLocation location;
    private final Set<UUID> invitedPlayers;
    private final Map<UUID, Boolean> responses;
    private DateStatus status;
    private UUID initiator; // Who actually started the date
    private List<ItemStack> gifts;
    private String customMessage;
    private boolean isPublic;

    public enum DateStatus {
        PENDING("Pending", Formatting.GRAY),
        ACCEPTED("Accepted", Formatting.GREEN),
        DECLINED("Declined", Formatting.RED),
        IN_PROGRESS("In Progress", Formatting.BLUE),
        COMPLETED("Completed", Formatting.GOLD),
        CANCELLED("Cancelled", Formatting.DARK_RED);

        private final String displayName;
        private final Formatting color;

        DateStatus(String displayName, Formatting color) {
            this.displayName = displayName;
            this.color = color;
        }

        public Text getDisplayText() {
            return Text.literal(displayName).formatted(color);
        }
    }

    public DateOption(UUID proposer, UUID recipient, DateLocation location, long scheduledTime) {
        this.id = UUID.randomUUID();
        this.proposer = proposer;
        this.recipient = recipient;
        this.location = location;
        this.scheduledTime = scheduledTime;
        this.proposedTime = System.currentTimeMillis();
        this.status = DateStatus.PENDING;
        this.invitedPlayers = new HashSet<>();
        this.responses = new HashMap<>();
        this.gifts = new ArrayList<>();
        this.isPublic = false;
        
        // Auto-add the main participants
        this.invitedPlayers.add(proposer);
        this.invitedPlayers.add(recipient);
        this.responses.put(proposer, true); // Proposer auto-accepts
    }

    // Core methods
    public boolean respond(UUID playerId, boolean accepted) {
        if (!invitedPlayers.contains(playerId)) return false;
        
        responses.put(playerId, accepted);
        updateStatus();
        return true;
    }

    public boolean startDate(ServerPlayerEntity starter) {
        if (status != DateStatus.ACCEPTED) return false;
        if (!starter.getUuid().equals(proposer) && !starter.getUuid().equals(recipient)) {
            return false;
        }
        
        this.status = DateStatus.IN_PROGRESS;
        this.initiator = starter.getUuid();
        return true;
    }

    public boolean completeDate() {
        if (status != DateStatus.IN_PROGRESS) return false;
        this.status = DateStatus.COMPLETED;
        return true;
    }

    public boolean cancelDate(UUID canceller) {
        if (status == DateStatus.COMPLETED || status == DateStatus.CANCELLED) return false;
        if (!canceller.equals(proposer) && !canceller.equals(recipient)) return false;
        
        this.status = DateStatus.CANCELLED;
        return true;
    }

    public boolean isParticipant(UUID playerId) {
        return playerId.equals(proposer) || playerId.equals(recipient) || invitedPlayers.contains(playerId);
    }

    public boolean hasResponded(UUID playerId) {
        return responses.containsKey(playerId);
    }

    public boolean isAccepted() {
        return status == DateStatus.ACCEPTED;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getProposer() { return proposer; }
    public UUID getRecipient() { return recipient; }
    public long getProposedTime() { return proposedTime; }
    public long getScheduledTime() { return scheduledTime; }
    public DateLocation getLocation() { return location; }
    public DateStatus getStatus() { return status; }
    public Set<UUID> getInvitedPlayers() { return Collections.unmodifiableSet(invitedPlayers); }
    public Map<UUID, Boolean> getResponses() { return Collections.unmodifiableMap(responses); }
    public List<ItemStack> getGifts() { return Collections.unmodifiableList(gifts); }
    public String getCustomMessage() { return customMessage; }
    public boolean isPublic() { return isPublic; }

    // Setters
    public void setCustomMessage(String message) { this.customMessage = message; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public void addGift(ItemStack gift) {
        if (gift != null && !gift.isEmpty()) {
            gifts.add(gift.copy());
        }
    }

    public boolean isAtLocation(BlockPos pos, World world) {
        return location != null && location.isAtLocation(pos, world);
    }

    // Serialization
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("id", id);
        nbt.putUuid("proposer", proposer);
        nbt.putUuid("recipient", recipient);
        nbt.putLong("proposedTime", proposedTime);
        nbt.putLong("scheduledTime", scheduledTime);
        
        if (location != null) {
            nbt.put("location", location.toNbt());
        }
        
        nbt.putString("status", status.name());
        
        NbtList invitedList = new NbtList();
        for (UUID playerId : invitedPlayers) {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putUuid("id", playerId);
            invitedList.add(playerNbt);
        }
        nbt.put("invitedPlayers", invitedList);
        
        NbtCompound responsesNbt = new NbtCompound();
        responses.forEach((playerId, accepted) -> 
            responsesNbt.putBoolean(playerId.toString(), accepted));
        nbt.put("responses", responsesNbt);
        
        if (initiator != null) {
            nbt.putUuid("initiator", initiator);
        }
        
        NbtList giftsList = new NbtList();
        for (ItemStack gift : gifts) {
            giftsList.add(gift.writeNbt(new NbtCompound()));
        }
        nbt.put("gifts", giftsList);
        
        if (customMessage != null) {
            nbt.putString("customMessage", customMessage);
        }
        
        nbt.putBoolean("isPublic", isPublic);
        
        return nbt;
    }

    public static DateOption fromNbt(NbtCompound nbt, World world) {
        UUID proposer = nbt.getUuid("proposer");
        UUID recipient = nbt.getUuid("recipient");
        long scheduledTime = nbt.getLong("scheduledTime");
        
        DateLocation location = null;
        if (nbt.contains("location")) {
            location = DateLocation.fromNbt(nbt.getCompound("location"), (ServerWorld) world);
        }
        
        DateOption dateOption = new DateOption(proposer, recipient, location, scheduledTime);
        dateOption.id = nbt.getUuid("id");
        dateOption.proposedTime = nbt.getLong("proposedTime");
        dateOption.status = DateStatus.valueOf(nbt.getString("status"));
        
        NbtList invitedList = nbt.getList("invitedPlayers", 10);
        for (int i = 0; i < invitedList.size(); i++) {
            NbtCompound playerNbt = invitedList.getCompound(i);
            dateOption.invitedPlayers.add(playerNbt.getUuid("id"));
        }
        
        NbtCompound responsesNbt = nbt.getCompound("responses");
        for (String key : responsesNbt.getKeys()) {
            dateOption.responses.put(UUID.fromString(key), responsesNbt.getBoolean(key));
        }
        
        if (nbt.containsUuid("initiator")) {
            dateOption.initiator = nbt.getUuid("initiator");
        }
        
        NbtList giftsList = nbt.getList("gifts", 10);
        for (int i = 0; i < giftsList.size(); i++) {
            dateOption.gifts.add(ItemStack.fromNbt(giftsList.getCompound(i)));
        }
        
        if (nbt.contains("customMessage")) {
            dateOption.customMessage = nbt.getString("customMessage");
        }
        
        dateOption.isPublic = nbt.getBoolean("isPublic");
        
        return dateOption;
    }

    private void updateStatus() {
        if (status != DateStatus.PENDING) return;
        
        boolean allResponded = invitedPlayers.stream().allMatch(responses::containsKey);
        if (!allResponded) return;
        
        boolean allAccepted = invitedPlayers.stream()
            .allMatch(id -> Boolean.TRUE.equals(responses.get(id)));
        
        this.status = allAccepted ? DateStatus.ACCEPTED : DateStatus.DECLINED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateOption that = (DateOption) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("DateOption{id=%s, proposer=%s, recipient=%s, status=%s}", 
            id, proposer, recipient, status);
    }
}