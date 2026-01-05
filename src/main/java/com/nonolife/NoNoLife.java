package com.nonolife;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class NoNoLife implements ModInitializer {
    public static final String MOD_ID = "nonolife";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    int tickCounter;
    Config config;
    NoNoLifeSavedData savedData;
    ServerScoreboard scoreboard;
    Objective remainingPlaytimeObjective;
    Map<ServerPlayer, ServerBossEvent> bossBars = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            config = Config.load(server);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
        CommandRegistrationCallback.EVENT.register(PlaytimeCommands::register);
    }

    private void onServerStart(MinecraftServer server) {
        // load mod data
        if (savedData == null) {
            savedData = NoNoLifeSavedData.get(server);
        }

        // setup scoreboard
        scoreboard = server.getScoreboard();
        remainingPlaytimeObjective = scoreboard.getObjective("remaining_playtime");
        if (remainingPlaytimeObjective == null) {
            // Create a new one if it doesn't exist
            remainingPlaytimeObjective = scoreboard.addObjective("remaining_playtime", ObjectiveCriteria.DUMMY,
                    Component.literal("Remaining Playtime (min)"), ObjectiveCriteria.RenderType.INTEGER, true,
                    null);
        }

        scoreboard.setDisplayObjective(DisplaySlot.LIST, remainingPlaytimeObjective);
    }

    private void onTick(MinecraftServer server) {
        tickCounter++;

        if (tickCounter < 20) {
            return;
        }
        // Run code every 20 ticks (~1 second)
        tickCounter = 0;

        // Update playtime for every online player
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlaytimeTracker playtimeTracker = savedData.playtimeTrackerForPlayer(player.getUUID());
            long remainingPlaytime = playtimeTracker.remainingPlaytime(config.dailyPlaytime);
            if (remainingPlaytime <= 0) {
                player.connection.disconnect(
                        Component.literal("Playtime Limit reached. You can play again tomorrow."));
                return;
            }

            notifyPlayer(remainingPlaytime, player);

            scoreboard.getOrCreatePlayerScore(player, remainingPlaytimeObjective)
                    .set((int) playtimeTracker.getPlaytime() / 3600);

            updateBossBarForPlayer(player, remainingPlaytime, playtimeTracker.isShowBossBar());

            playtimeTracker.addPlaytime(1);
        }
    }

    private void notifyPlayer(long remainingPlaytime, ServerPlayer player) {
        if (remainingPlaytime == 300) {
            player.sendSystemMessage(Component.literal("You have 5 minutes of playtime left."));
        }

        if (remainingPlaytime == 60) {
            player.sendSystemMessage(Component.literal("You have 1 minute of playtime left."));
        }

        if (remainingPlaytime == 30) {
            player.sendSystemMessage(Component.literal("You have 30 seconds of playtime left."));
        }

        if (remainingPlaytime <= 10 && remainingPlaytime > 1) {
            player.sendSystemMessage(
                    Component.literal("You have " + remainingPlaytime + " seconds of playtime left."));
        }

        if (remainingPlaytime == 1) {
            player.sendSystemMessage(Component.literal("You have 1 second of playtime left."));
        }
    }

    private void updateBossBarForPlayer(ServerPlayer player, long remainingPlaytime, boolean isShown) {
        ServerBossEvent bar = bossBars.get(player);

        if (!isShown) {
            if (bar != null) {
                bar.removeAllPlayers();
                bossBars.remove(player);
            }
        } else {
            if (bar == null) {
                bar = new ServerBossEvent(
                        Component.literal(
                                "Remaining Playtime: " + remainingPlaytime / 60 + " min"),
                        BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
                bossBars.put(player, bar);
                bar.addPlayer(player);
            }

            bar.setName(Component.literal("Remaining Playtime: " + remainingPlaytime / 60 + " min"));
            bar.setProgress(Math.min(remainingPlaytime / (float) config.dailyPlaytime, 1));

        }
    }
}
