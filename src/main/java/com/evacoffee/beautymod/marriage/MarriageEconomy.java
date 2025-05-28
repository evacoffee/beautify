// MarriageEconomy.java
package com.evacoffee.beautymod.marriage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarriageEconomy {
    private final Map<UUID, Integer> sharedBank = new HashMap<>();
    private final Map<UUID, UUID> jointAccounts = new HashMap<>();

    public boolean createJointAccount(ServerPlayerEntity player1, ServerPlayerEntity player2) {
        if (jointAccounts.containsKey(player1.getUuid()) || jointAccounts.containsKey(player2.getUuid())) {
            return false;
        }

        jointAccounts.put(player1.getUuid(), player2.getUuid());
        jointAccounts.put(player2.getUuid(), player1.getUuid());
        sharedBank.put(player1.getUuid(), 0);
        return true;
    }

    public boolean deposit(ServerPlayerEntity player, int amount) {
        UUID account = jointAccounts.get(player.getUuid());
        if (account == null) return false;

        // Deduct from player's money (implement your economy