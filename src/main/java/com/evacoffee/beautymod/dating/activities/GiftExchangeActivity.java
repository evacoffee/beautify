package com.evacoffee.beautymod.dating.activities;

import com.evacoffee.beautymod.dating.DateActivity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.*;

public class GiftExchangeActivity extends DateActivity {
    private final Map<UUID, UUID> exchangePairs = new HashMap<>();
    private final Map<UUID, ItemStack> gifts = new HashMap<>();
    private boolean giftsExchanged = false;

    public GiftExchangeActivity(UUID dateId, DateLocation location, List<UUID> participants) {
        super(dateId, location, participants, 600); //30 sec gift exchanges
        //pair them up
        for (int i = 0; i < participants.size() - 1; i += 2) {
            exchangePairs.put(participants.get(i), participants.get(i + 1));
            if(i + 1 < participants.size() - 1) {
                exchangePairs.put(participants.get(i + 1), participants.get(i));
            }
        }
    }

    @Override
    public boolean canStart() {
        return allParticipantsPresent() && exchangePairs.size() >= 2;
    }

    @Override
    public void onStart() {
        participants.forEach(playerId-> {
            PlayerEntity player = location.getWorld().getPlayerByUuid(playerId);
            if(player != null) {
                UUID recipientId = exchangePairs.get(playerId);
                PlayerEntity recipient = location.getWorld().getPlayerByUuid(recipientId);
                player.sendMessage(Text.of("Exchange gifts with " +
                    (recipient != null ? recipient.getName().getString() : "your partner")), false);
            }
        });
    }

    public void submitGift(UUID giverId, ItemStack gift) {
        gifts.put(giverId, gift.copy());
        checkGiftExchangeComplete();
    }

    private void checkGiftExchangeComplete() {
        if(gifts.size() == exchangePairs.size()) {
            giftsExchanged = true;
            distributeGifts();
        }
    }

    private void distributeGifts() {
        gifts.forEach((giverId, gift) -> {
            UUID receiverId = exchangePairs.get(giverId);
            PlayerEntity receiver = location.getWorld().getPlayerByUuid(receiverId);
            if (receiver != null && !gift.isEmpty()) {
                // Add gift to receiver's inventory
                if (!receiver.getInventory().insertStack(gift)) {
                    // Drop if no space
                    receiver.dropItem(gift, false);
                }
                // Calculate relationship impact based on gift
                // This would integrate with your relationship system
            }
        });
    }

    @Override
    public void onComplete() {
        if (!giftsExchanged) {
            cancel();
        }
    }

    @Override
    public void onCancel() {
        // Return gifts if exchange wasn't completed
        if (!giftsExchanged) {
            gifts.forEach((giverId, gift) -> {
                if (!gift.isEmpty()) {
                    PlayerEntity giver = location.getWorld().getPlayerByUuid(giverId);
                    if (giver != null) {
                        if (!giver.getInventory().insertStack(gift)) {
                            giver.dropItem(gift, false);
                        }
                    }
                }
            });
        }
    }
}