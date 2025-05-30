package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.dating.DateLocation;
import com.evacoffee.beautymod.dating.DateLocationManager;
import com.evacoffee.beautymod.dating.DateType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DateCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                net.minecraft.server.command.CommandManager.literal("date")
                    .then(net.minecraft.server.command.CommandManager.literal("create")
                        .then(net.minecraft.server.command.CommandManager.argument("name", StringArgumentType.string())
                            .then(net.minecraft.server.command.CommandManager.argument("type", StringArgumentType.string())
                                .executes(DateCommand::createDateLocation)
                            )
                        )
                    )
                    .then(net.minecraft.server.command.CommandManager.literal("list")
                        .executes(DateCommand::listDateLocations)
                    )
                    .then(net.minecraft.server.command.CommandManager.literal("go")
                        .then(net.minecraft.server.command.CommandManager.argument("name", StringArgumentType.string())
                            .executes(DateCommand::goOnDate)
                        )
                    )
            );
        });
    }

    private static int createDateLocation(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(context, "name");
        String typeStr = StringArgumentType.getString(context, "type").toUpperCase();
        
        try {
            DateType type = DateType.valueOf(typeStr);
            boolean success = DateLocationManager.registerLocation(
                player, name, type, player.getBlockPos()
            );
            
            if (success) {
                player.sendMessage(Text.literal("Created new " + type.getDisplayName() + " location: " + name)
                    .formatted(Formatting.GREEN));
            } else {
                player.sendMessage(Text.literal("A location with that name already exists!")
                    .formatted(Formatting.RED));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(Text.literal("Invalid date type! Available types: " + 
                Arrays.toString(Arrays.stream(DateType.values())
                    .map(Enum::name)
                    .toArray()))
                .formatted(Formatting.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int listDateLocations(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        List<DateLocation> locations = DateLocationManager.getPlayerLocations(player);
        if (locations.isEmpty()) {
            player.sendMessage(Text.literal("You haven't created any date locations yet!"));
            return 0;
        }

        player.sendMessage(Text.literal("Your Date Locations:").formatted(Formatting.GOLD));
        for (DateLocation loc : locations) {
            player.sendMessage(Text.literal(String.format("- %s (%s) at %d, %d, %d",
                loc.getName(),
                loc.getType().getDisplayName(),
                loc.getPosition().getX(),
                loc.getPosition().getY(),
                loc.getPosition().getZ()
            )));
        }
        return locations.size();
    }

    private static int goOnDate(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        String name = StringArgumentType.getString(context, "name");
        DateLocation location = DateLocationManager.getLocation(player, name);

        if (location == null) {
            player.sendMessage(Text.literal("Couldn't find a date location with that name!")
                .formatted(Formatting.RED));
            return 0;
        }

        if (!location.isAtLocation(player.getBlockPos(), player.getWorld())) {
            player.sendMessage(Text.literal("You need to be at the date location to start the date!")
                .formatted(Formatting.RED));
            return 0;
        }

        // Start the date!
        switch (location.getType()) {
            case PANCAKE_DATE -> startPancakeDate(player);
            case SPORT_DATE -> startSportDate(player);
            case MOVIE_DATE -> startMovieDate(player);
            case ROMANTIC_DINNER -> startRomanticDinner(player);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void startPancakeDate(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("Enjoy your pancake date! Don't forget the syrup!")
            .formatted(Formatting.GOLD));
        // Add effects or rewards
    }

    private static void startSportDate(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("Let the games begin! Show off your skills!")
            .formatted(Formatting.GOLD));
        // Add effects or rewards
    }

    private static void startMovieDate(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("Shhh... the movie is starting! Enjoy the show!")
            .formatted(Formatting.GOLD));
        // Add effects or rewards
    }

    private static void startRomanticDinner(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("The candlelight dinner is served. Enjoy your romantic evening!")
            .formatted(Formatting.LIGHT_PURPLE));
        // Add effects or rewards
    }
}