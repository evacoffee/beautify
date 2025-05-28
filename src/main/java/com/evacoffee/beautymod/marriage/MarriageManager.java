package com.evacoffee.beautymod.marriage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MarriageManager {
    private final Map<UUID, MarriageProposal> proposals = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    
    private static final long PROPOSAL_EXPIRE_TIME = 5 * 60 * 1000; // 5 minutes
    private static final long MARRIAGE_COOLDOWN = 24 * 60 * 60 * 1000; // 24 hours
    
    public boolean createProposal(ServerPlayerEntity proposer, ServerPlayerEntity target) {
        // Check cooldown
        if (cooldowns.getOrDefault(proposer.getUuid(), 0L) > System.currentTimeMillis()) {
            long remaining = (cooldowns.get(proposer.getUuid()) - System.currentTimeMillis()) / 1000;
            long minutes = remaining / 60;
            long seconds = remaining % 60;
            proposer.sendMessage(Text.literal(String.format("You must wait %d minutes and %d seconds before proposing again!", 
                minutes, seconds)).formatted(Formatting.RED), false);
            return false;
        }
        
        // Create and store proposal
        MarriageProposal proposal = new MarriageProposal(proposer, target, System.currentTimeMillis());
        proposals.put(proposer.getUuid(), proposal);
        
        // Schedule cleanup
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (proposals.containsKey(proposer.getUuid())) {
                    proposals.remove(proposer.getUuid());
                    proposer.sendMessage(Text.literal("Your proposal to " + target.getName().getString() + " has expired.")
                        .formatted(Formatting.GRAY), false);
                }
            }
        }, PROPOSAL_EXPIRE_TIME);
        
        return true;
    }
    
    public boolean hasPendingProposal(ServerPlayerEntity proposer, ServerPlayerEntity target) {
        MarriageProposal proposal = proposals.get(proposer.getUuid());
        return proposal != null && 
               proposal.getTarget().getUuid().equals(target.getUuid()) &&
               !proposal.isExpired();
    }
    
    public void clearProposal(ServerPlayerEntity proposer) {
        proposals.remove(proposer.getUuid());
    }
    
    public void setCooldown(ServerPlayerEntity player) {
        cooldowns.put(player.getUuid(), System.currentTimeMillis() + MARRIAGE_COOLDOWN);
    }
    
    public void clearCooldown(ServerPlayerEntity player) {
        cooldowns.remove(player.getUuid());
    }
    
    public boolean isOnCooldown(ServerPlayerEntity player) {
        return cooldowns.getOrDefault(player.getUuid(), 0L) > System.currentTimeMillis();
    }
    
    public long getRemainingCooldown(ServerPlayerEntity player) {
        return Math.max(0, cooldowns.getOrDefault(player.getUuid(), 0L) - System.currentTimeMillis());
    }
    
    private static class MarriageProposal {
        private final ServerPlayerEntity proposer;
        private final ServerPlayerEntity target;
        private final long timestamp;
        
        public MarriageProposal(ServerPlayerEntity proposer, ServerPlayerEntity target, long timestamp) {
            this.proposer = proposer;
            this.target = target;
            this.timestamp = timestamp;
        }
        
        public ServerPlayerEntity getProposer() {
            return proposer;
        }
        
        public ServerPlayerEntity getTarget() {
            return target;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > PROPOSAL_EXPIRE_TIME;
        }
    }
}
