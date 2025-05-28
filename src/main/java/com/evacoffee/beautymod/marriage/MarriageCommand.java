package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.marriage.MarriageComponent;
import com.evacoffee.beautymod.marriage.MarriagePerk;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MarriageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("marriage")
            .then(literal("propose")
                .then(argument("player", EntityArgumentType.player())
                    .executes(MarriageCommand::proposeMarriage)))
            .then(literal("accept")
                .executes(MarriageCommand::acceptProposal))
            .then(literal("divorce")
                .executes(MarriageCommand::divorce))
            .then(literal("sethome")
                .executes(MarriageCommand::setHome))
            .then(literal("home")
                .executes(MarriageCommand::goHome))
            .then(literal("status")
                .executes(MarriageCommand::marriageStatus))
            .then(literal("share")
                .then(argument("item", StringArgumentType.greedyString())
                    .executes(MarriageCommand::shareItem))));
    }

    private static int proposeMarriage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        
        if (player == target) {
            context.getSource().sendError(Text.of("You can't propose to yourself!"));
            return 0;
        }

        MarriageComponent playerMarriage = MarriageComponent.get(player);
        MarriageComponent targetMarriage = MarriageComponent.get(target);

        if (playerMarriage.isMarried()) {
            context.getSource().sendError(Text.of("You're already married!"));
            return 0;
        }

        if (targetMarriage.isMarried()) {
            context.getSource().sendError(Text.of(target.getEntityName() + " is already married!"));
            return 0;
        }

        playerMarriage.setMarriageProposal(target.getUuid());
        player.sendMessage(Text.of("Marriage proposal sent to " + target.getEntityName()), false);
        target.sendMessage(Text.of(player.getEntityName() + " has proposed to you! Type /marriage accept to accept."), false);
        return 1;
    }

    private static int acceptProposal(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        MarriageComponent marriage = MarriageComponent.get(player);
        UUID proposerUuid = marriage.getMarriageProposal();

        if (proposerUuid == null) {
            context.getSource().sendError(Text.of("You don't have any pending marriage proposals!"));
            return 0;
        }

        ServerPlayerEntity proposer = player.getServer().getPlayerManager().getPlayer(proposerUuid);
        if (proposer == null) {
            context.getSource().sendError(Text.of("The player who proposed is no longer online."));
            return 0;
        }

        MarriageComponent proposerMarriage = MarriageComponent.get(proposer);
        if (!proposerMarriage.isMarried() && !marriage.isMarried()) {
            long weddingDay = player.getWorld().getTime();
            proposerMarriage.marry(player, weddingDay);
            marriage.marry(proposer, weddingDay);
            
            player.getServer().getPlayerManager().broadcast(
                Text.of("§d" + player.getEntityName() + " and " + proposer.getEntityName() + " are now married!"),
                false
            );
        }
        return 1;
    }

    private static int divorce(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        MarriageComponent marriage = MarriageComponent.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.of("You're not married!"));
            return 0;
        }

        UUID spouseUuid = marriage.getSpouseUuid();
        ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(spouseUuid);
        
        marriage.divorce();
        if (spouse != null) {
            MarriageComponent.get(spouse).divorce();
            spouse.sendMessage(Text.of("§c" + player.getEntityName() + " has divorced you."), false);
        }

        player.sendMessage(Text.of("§cYou are now divorced."), false);
        return 1;
    }

    private static int setHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        MarriageComponent marriage = MarriageComponent.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.of("You need to be married to set a home!"));
            return 0;
        }

        marriage.setHomePosition(player.getBlockPos(), player.getWorld().getRegistryKey());
        player.sendMessage(Text.of("§aMarriage home set!"), false);
        return 1;
    }

    private static int goHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        MarriageComponent marriage = MarriageComponent.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.of("You need to be married to use this command!"));
            return 0;
        }

        if (marriage.teleportToHome(player)) {
            player.sendMessage(Text.of("§aWelcome home!"), false);
        } else {
            context.getSource().sendError(Text.of("No home set! Use /marriage sethome first."));
        }
        return 1;
    }

    private static int marriageStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        MarriageComponent marriage = MarriageComponent.get(player);
        
        if (!marriage.isMarried()) {
            context.getSource().sendError(Text.of("You're not married!"));
            return 0;
        }

        ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(marriage.getSpouseUuid());
        String spouseName = spouse != null ? spouse.getEntityName() : "Offline";
        
        player.sendMessage(Text.of("§6--- Marriage Status ---"), false);
        player.sendMessage(Text.of("§fSpouse: §e" + spouseName), false);
        player.sendMessage(Text.of("§fMarriage Level: §e" + marriage.getMarriageLevel()), false);
        player.sendMessage(Text.of("§fDays Married: §e" + marriage.getDaysMarried(player.getWorld().getTime())), false);
        player.sendMessage(Text.of("§fUnlocked Perks: §e" + marriage.getUnlockedPerks().size()), false);
        
        return 1;
    }

    private static int shareItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String itemName = StringArgumentType.getString(context, "item");
        player.sendMessage(Text.of("§aItem shared with spouse: " + itemName), false);
        return 1;
    }
}