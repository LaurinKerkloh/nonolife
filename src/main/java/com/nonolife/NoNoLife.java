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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoNoLife implements ModInitializer {
    public static final String MOD_ID = "nonolife";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    int tickCounter;
    Config config;
    NoNoLifeSavedData savedData;
    ServerScoreboard scoreboard;
    Objective totalPlaytimeObjective;
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
        totalPlaytimeObjective = scoreboard.getObjective("total_playtime");
        if (config.showTotalPlaytime && totalPlaytimeObjective == null) {
            totalPlaytimeObjective = scoreboard.addObjective("total_playtime", ObjectiveCriteria.DUMMY,
                    Component.literal("Total Playtime (h)"), ObjectiveCriteria.RenderType.INTEGER, true, null);
            scoreboard.setDisplayObjective(DisplaySlot.LIST, totalPlaytimeObjective);
        }

        if (!config.showTotalPlaytime && totalPlaytimeObjective != null) {
            scoreboard.removeObjective(totalPlaytimeObjective);
        }

    }

    private void onTick(MinecraftServer server) {
        tickCounter++;

        if (tickCounter < 20) {
            return;
        }
        // Run code every 20 ticks (~1 second)
        tickCounter = 0;

        addPlaytime();
        countPlaytimeOnlinePlayers(server);

    }

    private void addPlaytime() {
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        LocalTime addPlaytimeAt = LocalTime.of(config.addPlaytimeAtHour, 0);

        LocalDate addUntil = (now.isAfter(addPlaytimeAt)) ? today : today.minusDays(1);

        int addForXDays = (int) ChronoUnit.DAYS.between(savedData.getLastPlaytimeAddition(), addUntil);

        if (addForXDays <= 0) {
            return;
        }

        savedData.getPlayerMap().forEach((uuid, playtimeTracker) -> {
            playtimeTracker.addPlaytime(config.dailyPlaytime * addForXDays, config.maximumPlaytime);
        });
        savedData.setLastPlaytimeAddition(addUntil);

        savedData.setDirty();
    }

    // Update playtime for every online player
    private void countPlaytimeOnlinePlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PlaytimeTracker playtimeTracker = savedData.getOrCreatePlaytimeTrackerForPlayer(player.getUUID(),
                    config.initialPlaytime);
            int remainingPlaytime = playtimeTracker.getAvailablePlaytime();
            if (remainingPlaytime <= 0) {
                player.connection.disconnect(
                        Component.literal("Playtime Limit reached. You can play again tomorrow."));
                return;
            }

            notifyPlayer(remainingPlaytime, player);

            if (config.showTotalPlaytime) {
                scoreboard.getOrCreatePlayerScore(player, totalPlaytimeObjective)
                        .set((int) playtimeTracker.getTotalPlaytime() / 3600);
            }

            updateBossBarForPlayer(player, remainingPlaytime, playtimeTracker.isShowBossBar());

            playtimeTracker.playedFor(1);

            savedData.setDirty();
        }
    }

    private void notifyPlayer(int remainingPlaytime, ServerPlayer player) {
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

    private void updateBossBarForPlayer(ServerPlayer player, int remainingPlaytime, boolean isShown) {
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
