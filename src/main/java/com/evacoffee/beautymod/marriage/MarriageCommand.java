package com.evacoffee.beautymod.marriage;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MarriageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("marriage")
            .then(literal("propose")
                .then(argument("player", EntityArgumentType.player())
                    .executes(MarriageCommand::propose)))
            .then(literal("accept")
                .then(argument("player", EntityArgumentType.player())
                    .executes(MarriageCommand::accept)))
            .then(literal("divorce")
                .executes(MarriageCommand::divorce))
            .then(literal("sethome")
                .executes(MarriageCommand::sethome))
            .then(literal("home")
                .executes(MarriageCommand::home))
            .then(literal("status")
                .executes(MarriageCommand::status))
            .then(literal("perks")
                .executes(ctx -> listPerks(ctx.getSource()))
                .then(argument("perk", StringArgumentType.word())
                    .executes(ctx -> unlockPerk(ctx, StringArgumentType.getString(ctx, "perk")))))
        );
    }

    private static int propose(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        
        if (player.getUuid().equals(target.getUuid())) {
            context.getSource().sendError(Text.literal("You cannot propose to yourself!").formatted(Formatting.RED));
            return 0;
        }

        if (MarriageManager.propose(player, target)) {
            player.sendMessage(Text.literal("Marriage proposal sent to " + target.getEntityName() + "!").formatted(Formatting.GREEN), false);
            target.sendMessage(Text.literal(player.getEntityName() + " has proposed to you! Use /marriage accept " + player.getEntityName() + " to accept.")
                .formatted(Formatting.GOLD), false);
            return 1;
        } else {
            context.getSource().sendError(Text.literal("Could not send proposal. You or the target might already be married."));
            return 0;
        }
    }

    private static int accept(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity proposer = EntityArgumentType.getPlayer(context, "player");
        
        if (MarriageManager.acceptProposal(player, proposer)) {
            player.sendMessage(Text.literal("You are now married to " + proposer.getEntityName() + "!").formatted(Formatting.GOLD), false);
            proposer.sendMessage(Text.literal(player.getEntityName() + " has accepted your proposal!").formatted(Formatting.GOLD), false);
            return 1;
        } else {
            context.getSource().sendError(Text.literal("No marriage proposal found from that player or you are already married."));
            return 0;
        }
    }

    private static int divorce(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        MarriageComponent marriage = MarriageComponentInitializer.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.literal("You are not married!").formatted(Formatting.RED));
            return 0;
        }

        String spouseName = marriage.getSpouseName();
        marriage.divorce();
        
        // Notify both players
        player.sendMessage(Text.literal("You are now divorced from " + spouseName).formatted(Formatting.RED), false);
        
        // Notify spouse if online
        UUID spouseUuid = marriage.getSpouseUuid();
        if (spouseUuid != null) {
            ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(spouseUuid);
            if (spouse != null) {
                spouse.sendMessage(Text.literal("You are now divorced from " + player.getEntityName()).formatted(Formatting.RED), false);
                MarriageComponentInitializer.get(spouse).divorce();
            }
        }
        
        return 1;
    }

    private static int sethome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        MarriageComponent marriage = MarriageComponentInitializer.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.literal("You must be married to set a home!").formatted(Formatting.RED));
            return 0;
        }

        marriage.setHome(player.getBlockPos(), player.getWorld().getRegistryKey().getValue().toString());
        player.sendMessage(Text.literal("Marriage home set to your current location!").formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int home(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        MarriageComponent marriage = MarriageComponentInitializer.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.literal("You must be married to use this command!").formatted(Formatting.RED));
            return 0;
        }

        BlockPos homePos = marriage.getHomePos();
        String homeWorld = marriage.getHomeWorld();
        
        if (homePos == null || homeWorld == null) {
            context.getSource().sendError(Text.literal("No home has been set! Use /marriage sethome first.").formatted(Formatting.RED));
            return 0;
        }

        // Teleport the player to their home
        // Note: Implement teleportation logic here
        player.teleport(
            player.getServer().getWorld(player.getWorld().getRegistryKey()), // Target world
            homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5, // Position
            player.getYaw(), player.getPitch() // Rotation
        );
        
        player.sendMessage(Text.literal("Teleported to your marriage home!").formatted(Formatting.GREEN), false);
        return 1;
    }

    private static int status(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        MarriageComponent marriage = MarriageComponentInitializer.get(player);
        
        if (!marriage.isMarried()) {
            player.sendMessage(Text.literal("You are not currently married.").formatted(Formatting.GRAY), false);
            return 0;
        }

        long daysMarried = (System.currentTimeMillis() - marriage.getWeddingDay()) / (1000 * 60 * 60 * 24);
        
        player.sendMessage(Text.literal("=== Marriage Status ===").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Spouse: " + marriage.getSpouseName()).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal("Married for: " + daysMarried + " days").formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal("Home: " + (marriage.getHomePos() != null ? "Set" : "Not set")).formatted(Formatting.WHITE), false);
        
        return 1;
    }

    private static int listPerks(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        MarriageComponent marriage = MarriageComponentInitializer.get(player);
        
        if (!marriage.isMarried()) {
            source.sendError(Text.literal("You must be married to view perks!").formatted(Formatting.RED));
            return 0;
        }

        int marriageLevel = 1; // Implement level calculation
        
        player.sendMessage(Text.literal("=== Marriage Perks ===").formatted(Formatting.GOLD), false);
        player.sendMessage(Text.literal("Your level: " + marriageLevel).formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal(""), false);
        
        for (MarriagePerk perk : MarriagePerk.values()) {
            boolean hasPerk = marriage.hasPerk(perk);
            boolean canAfford = marriageLevel >= perk.getRequiredLevel();
            String status = hasPerk ? "§aUNLOCKED" : 
                         canAfford ? "§eCLICK TO UNLOCK" : 
                         "§cRequires Level " + perk.getRequiredLevel();
            
            player.sendMessage(Text.literal(perk.getDisplayName() + ": " + status).formatted(Formatting.WHITE), false);
            player.sendMessage(Text.literal("  " + perk.getDescription()).formatted(Formatting.GRAY), false);
        }
        
        return 1;
    }

    private static int unlockPerk(CommandContext<ServerCommandSource> context, String perkName) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        MarriageComponent marriage = MarriageComponentInitializer.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.literal("You must be married to unlock perks!").formatted(Formatting.RED));
            return 0;
        }

        MarriagePerk perk;
        try {
            perk = MarriagePerk.valueOf(perkName.toUpperCase());
        } catch (IllegalArgumentException e) {
            context.getSource().sendError(Text.literal("Invalid perk name!").formatted(Formatting.RED));
            return 0;
        }

        int marriageLevel = 1; // Implement level calculation
        
        if (marriage.unlockPerk(perk, marriageLevel)) {
            player.sendMessage(Text.literal("Unlocked perk: " + perk.getDisplayName()).formatted(Formatting.GREEN), false);
            // Apply perk effects immediately
            MarriagePerkManager.applyPerkEffects(player, perk);
        } else {
            context.getSource().sendError(Text.literal("You don't meet the requirements for this perk!").formatted(Formatting.RED));
        }
        
        return 1;
    }
}