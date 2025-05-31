package com.evacoffee.beautymod.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public class ModEvents {
    // Dating events
    public static final Event<DateStartCallback> DATE_START = EventFactory.createArrayBacked(
        DateStartCallback.class,
        callbacks -> (player1, player2) -> {
            for (DateStartCallback callback : callbacks) {
                ActionResult result = callback.interact(player1, player2);
                if (result != ActionResult.PASS) return result;
            }
            return ActionResult.PASS;
        }
    );

    public static final Event<SeasonalEventCallback> SEASONAL_EVENT = EventFactory.createArrayBacked(
        SeasonalEventCallback.class,
        callbacks -> (eventType) -> {
            for (SeasonalEventCallback callback : callbacks) {
                callback.onEvent(eventType);
            }
        }
    );

    @FunctionalInterface
    public interface DateStartCallback {
        ActionResult interact(PlayerEntity player1, PlayerEntity player2);
    }

    @FunctionalInterface
    public interface SeasonalEventCallback {
        void onEvent(SeasonalEventType eventType);
    }

    public enum SeasonalEventType {
        VALENTINES_DAY,
        ANNIVERSARY,
        NPC_BIRTHDAY
    }
}