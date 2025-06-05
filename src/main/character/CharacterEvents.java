package com.evacoffee.beautymod.character;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.Arrays;
import java.util.List;

public class CharacterEvents {
    private static final Random RANDOM = Random.create();
    

    public enum CharacterType {
        ROMANTIC, SHY, BOLD, PLAYFUL, MYSTERIOUS, GENTLE
    }


    public static Text getRandomGreeting(CharacterType type, int relationshipLevel) {
        List<String> greetings = switch (type) {
            case ROMANTIC -> Arrays.asList(
                "Your presence brightens my day, my love...",
                "Every moment with you feels like a dream...",
                "I've been counting the minutes until I saw you again..."
            );
            case SHY -> Arrays.asList(
                "O-oh! H-hi there...",
                "I was just... um... thinking about you...",
                "*blushes* H-hi..."
            );
            case BOLD -> Arrays.asList(
                "Well, well, look who decided to show up!",
                "Took you long enough to come see me!",
                "I was starting to think you'd forgotten about me!"
            );
            case PLAYFUL -> Arrays.asList(
                "Hey you! Ready for some fun?",
                "Catch me if you can! *giggles*",
                "I've got a secret... but you'll have to get closer to hear it!"
            );
            case MYSTERIOUS -> Arrays.asList(
                "The stars whispered you would come...",
                "I've seen our meeting in my visions...",
                "You're right on time... how curious..."
            );
            case GENTLE -> Arrays.asList(
                "It's so nice to see you again...",
                "You look lovely today, as always...",
                "I was hoping I'd run into you..."
            );
        };
        return Text.of(greetings.get(RANDOM.nextInt(greetings.size())));
    }


    public static void triggerRandomEvent(PlayerEntity player, CharacterType type) {
        int eventType = RANDOM.nextInt(10); // 10 different possible events
        
        switch (eventType) {
            case 0 -> giveGift(player, type);
            case 1 -> startMiniGame(player, type);
            case 2 -> shareSecret(player, type);
            case 3 -> askForHelp(player, type);
            case 4 -> complimentPlayer(player, type);

            default -> {
                // No special event this time
                player.sendMessage(Text.of(getRandomIdleComment(type)), false);
            }
        }
    }

    private static void giveGift(PlayerEntity player, CharacterType type) {
        String gift = switch (type) {
            case ROMANTIC -> "a single red rose";
            case SHY -> "a carefully folded note";
            case BOLD -> "a daring gift";
            case PLAYFUL -> "a small trinket";
            case MYSTERIOUS -> "an enigmatic artifact";
            case GENTLE -> "a handmade gift";
        };
        player.sendMessage(Text.of("I have something for you... " + gift), false);

    }

    private static void startMiniGame(PlayerEntity player, CharacterType type) {
        String game = switch (type) {
            case ROMANTIC -> "a game of truth or dare";
            case SHY -> "a quiet game of cards";
            case BOLD -> "a daring challenge";
            case PLAYFUL -> "a fun little game";
            case MYSTERIOUS -> "a game of riddles";
            case GENTLE -> "a relaxing activity";
        };
        player.sendMessage(Text.of("Would you like to play " + game + " with me?"), false);
    }

    private static void shareSecret(PlayerEntity player, CharacterType type) {
        String secret = switch (type) {
            case ROMANTIC -> "I've never felt this way about anyone before...";
            case SHY -> "I... I think you should know... *whispers* I like you...";
            case BOLD -> "Listen, I don't say this often, but I trust you...";
            case PLAYFUL -> "Psst! Wanna know a secret? *giggles*";
            case MYSTERIOUS -> "The spirits have shown me something about you...";
            case GENTLE -> "I feel like I can tell you anything...";
        };
        player.sendMessage(Text.of(secret), false);
    }

    private static void askForHelp(PlayerEntity player, CharacterType type) {
        String request = switch (type) {
            case ROMANTIC -> "I need your help with something... just the two of us.";
            case SHY -> "C-could you... um... help me with something?";
            case BOLD -> "Hey, I've got a job that needs doing. You in?";
            case PLAYFUL -> "Wanna help me with something fun?";
            case MYSTERIOUS -> "Your skills are required for a matter of great importance...";
            case GENTLE -> "Would you mind helping me with something? I'd really appreciate it.";
        };
        player.sendMessage(Text.of(request), false);
    }

    private static void complimentPlayer(PlayerEntity player, CharacterType type) {
        String compliment = switch (type) {
            case ROMANTIC -> "You take my breath away every time I see you...";
            case SHY -> "Y-you look... really nice today... *blushes*";
            case BOLD -> "Damn, you're looking fine today!";
            case PLAYFUL -> "Has anyone told you you're absolutely adorable when you're focused?";
            case MYSTERIOUS -> "The stars shine brighter when you're near...";
            case GENTLE -> "You have such a kind heart... it's one of the things I love about you.";
        };
        player.sendMessage(Text.of(compliment), false);
    }

    private static String getRandomIdleComment(CharacterType type) {
        return switch (type) {
            case ROMANTIC -> "I was just thinking about you...";
            case SHY -> "U-um... it's nothing...";
            case BOLD -> "So, what's the plan, partner?";
            case PLAYFUL -> "Bored! Entertain me!";
            case MYSTERIOUS -> "The wind carries secrets today...";
            case GENTLE -> "It's such a peaceful day, isn't it?";
        };
    }


    public static CharacterType getRandomCharacterType() {
        return CharacterType.values()[RANDOM.nextInt(CharacterType.values().length)];
    }
}