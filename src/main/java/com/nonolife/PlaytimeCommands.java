package com.nonolife;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

class PlaytimeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext regestryAccess,
            Commands.CommandSelection environment) {
        dispatcher.register(
                Commands.literal("playtime")
                        .then(Commands.literal("show").executes(PlaytimeCommands::showBossbarCommand)));
        dispatcher.register(
                Commands.literal("playtime")
                        .then(Commands.literal("hide").executes(PlaytimeCommands::hideBossbarCommand)));
    }

    private static int showBossbarCommand(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("Command can only be called by a player"));
            return 0;
        }
        NoNoLifeSavedData.get(context.getSource().getServer())
                .playtimeTrackerForPlayer(player.getUUID()).showBossBar = true;
        return Command.SINGLE_SUCCESS;
    }

    private static int hideBossbarCommand(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("Command can only be called by a player"));
            return 0;
        }

        NoNoLifeSavedData.get(context.getSource().getServer())
                .playtimeTrackerForPlayer(player.getUUID()).showBossBar = false;
        return Command.SINGLE_SUCCESS;
    }
}
