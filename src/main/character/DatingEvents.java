package com.evacoffee.beautymod.character;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

public class DatingEvents {
    public enum Gender {
        MALE,
        FEMALE,
        NON_BINARY
    }

    public enum EventType {
        ROMANTIC_DATE,
        SPORTING_EVENT,
        CASUAL_HANGOUT,
        ADVENTURE,
        CULTURAL_EVENT,
        GAMING_SESSION
    }

    private static final Random RANDOM = Random.create();

    public static void triggerDatingEvent(PlayerEntity player, Gender playerGender, Gender npcGender) {
        EventType eventType = determineEventType(playerGender, npcGender);
        String eventDescription = generateEventDescription(eventType, playerGender, npcGender);
        
        player.sendMessage(Text.of("§d" + eventDescription), false);
        

        switch (eventType) {
            case ROMANTIC_DATE -> triggerRomanticDate(player);
            case SPORTING_EVENT -> triggerSportingEvent(player);
            case CASUAL_HANGOUT -> triggerCasualHangout(player);
            case ADVENTURE -> triggerAdventure(player);
            case CULTURAL_EVENT -> triggerCulturalEvent(player);
            case GAMING_SESSION -> triggerGamingSession(player);
        }
    }

    private static EventType determineEventType(Gender playerGender, Gender npcGender) {

        if (playerGender == Gender.FEMALE && npcGender == Gender.MALE) {
            return EventType.ROMANTIC_DATE;  // Traditional romantic date
        } else if (playerGender == Gender.MALE && npcGender == Gender.FEMALE) {
            return EventType.SPORTING_EVENT;  // Football/sporting event
        } else if (playerGender == Gender.FEMALE && npcGender == Gender.FEMALE) {
            return EventType.CULTURAL_EVENT;  // Museum or art gallery
        } else if (playerGender == Gender.MALE && npcGender == Gender.MALE) {
            return EventType.GAMING_SESSION;  // Gaming together
        } else {

            return EventType.values()[RANDOM.nextInt(EventType.values().length)];
        }
    }

    private static String generateEventDescription(EventType eventType, Gender playerGender, Gender npcGender) {
        String player = getGenderTerm(playerGender, true);
        String npc = getGenderTerm(npcGender, false);
        
        return switch (eventType) {
            case ROMANTIC_DATE -> String.format("%s takes %s on a romantic candlelit dinner at a fancy restaurant.", 
                npc, player.toLowerCase());
            case SPORTING_EVENT -> String.format("%s invites %s to an exciting football match!", npc, player);
            case CASUAL_HANGOUT -> String.format("%s suggests hanging out at a cozy café with %s.", npc, player);
            case ADVENTURE -> String.format("%s proposes an adventurous hiking trip with %s!", npc, player);
            case CULTURAL_EVENT -> String.format("%s and %s visit an art gallery together.", npc, player);
            case GAMING_SESSION -> String.format("%s challenges %s to a gaming marathon!", npc, player);
        };
    }

    private static String getGenderTerm(Gender gender, boolean isPlayer) {
        return switch (gender) {
            case MALE -> isPlayer ? "He" : "he";
            case FEMALE -> isPlayer ? "She" : "she";
            case NON_BINARY -> isPlayer ? "They" : "they";
        };
    }


    private static void triggerRomanticDate(PlayerEntity player) {
        player.sendMessage(Text.of("§5The atmosphere is filled with romance..."), false);

        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.REGENERATION, 600, 1));
    }

    private static void triggerSportingEvent(PlayerEntity player) {
        player.sendMessage(Text.of("§6The crowd cheers as the game begins!"), false);

        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.SPEED, 1200, 0));
    }

    private static void triggerCasualHangout(PlayerEntity player) {
        player.sendMessage(Text.of("§eYou enjoy some quality time together."), false);
        player.getHungerManager().add(6, 0.6f);
    }

    private static void triggerAdventure(PlayerEntity player) {
        player.sendMessage(Text.of("§2The great outdoors await your exploration!"), false);
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.SPEED, 1800, 0));
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.NIGHT_VISION, 3600, 0));
    }

    private static void triggerCulturalEvent(PlayerEntity player) {
        player.sendMessage(Text.of("§9You both appreciate the fine arts together."), false);
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.HERO_OF_THE_VILLAGE, 2400, 0));
    }

    private static void triggerGamingSession(PlayerEntity player) {
        player.sendMessage(Text.of("§3Game on! The competition is intense!"), false);
        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.HASTE, 1800, 0));
    }


    public static Gender getRandomGender() {
        return Gender.values()[RANDOM.nextInt(Gender.values().length)];
    }
}