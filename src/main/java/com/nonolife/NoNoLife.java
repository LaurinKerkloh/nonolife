package com.nonolife;

import net.fabricmc.api.ModInitializer;
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoNoLife implements ModInitializer {
    public static final String MOD_ID = "nonolife";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    int tickCounter;
    NoNoLifeSavedData savedData;
    ServerScoreboard scoreboard;
    Objective remainingPlaytimeObjective;
    Map<ServerPlayer, ServerBossEvent> bossBars = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
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
            if (playtimeTracker.remainingPlaytime() <= 0) {
                player.connection.disconnect(
                        Component.literal("Playtime Limit reached. You can play again tomorrow."));
            }
            playtimeTracker.addPlaytime(1);

            scoreboard.getOrCreatePlayerScore(player, remainingPlaytimeObjective)
                    .set((int) playtimeTracker.remainingPlaytime() / 60);

            // Boss bar
            ServerBossEvent bar = bossBars.get(player);
            if (bar == null) {
                bar = new ServerBossEvent(
                        Component.literal("Remaining Playtime: " + playtimeTracker.remainingPlaytime() / 60 + " min"),
                        BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
                bossBars.put(player, bar);
            }

            bar.addPlayer(player);
            bar.setProgress(Math.min(playtimeTracker.remainingPlaytime() / 3600f, 1));

        }
    }
}
